# ============================================================================
#  Collect-OpenFiles.ps1
#  SMB 열린 파일 수집기 (요청/임대 기반)
#  - 대상: Windows Server 2012 R2 파일서버 (Windows PowerShell 4.0 이상 호환)
#  - 동작: request.txt 가 최근(임대 유효)일 때만 Get-SmbOpenFile 로 열린 파일을
#          수집하여 openfiles.json 으로 원자적 저장
#  - 주의: 반드시 UTF-8 BOM 으로 저장할 것 (PS5.1 이하에서 BOM 없는 한글 깨짐)
# ============================================================================

# ---------------------------------------------------------------------------
# 상수 / 카운터
# ---------------------------------------------------------------------------
$baseDir         = "D:\FileOpenMonitor"
$outputFile      = Join-Path $baseDir "openfiles.json"
$tempFile        = Join-Path $baseDir "openfiles.tmp"
$logFile         = Join-Path $baseDir "collector.log"
$requestFile     = Join-Path $baseDir "request.txt"
$intervalSeconds = 5
$leaseSeconds    = 30

# --- 점유해제(Close) 명령 처리 상수 (Spring 과 합의된 계약) ---
#   request.txt 와 동일한 신호 방식: $baseDir 안의 고정 파일 2개를 주고받는다.
#   - close_request.json : Spring 이 기존 파일에 제자리 덮어쓰기(요청)
#   - close_result.json  : 수집기가 Save-AtomicFile 로 원자적 작성(결과, Spring 은 읽기만)
$closeRequestFile         = Join-Path $baseDir "close_request.json"
$closeResultFile          = Join-Path $baseDir "close_result.json"
$commandExpirySeconds     = 30    # RequestedAtUtc 가 현재보다 이만큼 초과 과거면 EXPIRED
$commandFutureSkewSeconds = 300   # RequestedAtUtc 가 미래로 이만큼 초과면 EXPIRED (시계 skew 허용 상한)
$closeParseGraceSeconds   = 5     # 같은 mtime 으로 이 시간 넘게 파싱 불가면 INVALID 확정 (부분 쓰기 대응)

# 정규식 (.NET 에서 $ 는 끝 개행 앞에서도 매치되므로 반드시 \A..\z 로 앵커,
#         \d 는 비ASCII 숫자까지 매치하므로 [0-9] 사용)
$script:guidRegex         = '\A[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\z'
$script:numericIdRegex    = '\A[0-9]{1,20}\z'

# 결과 코드별 한글 메시지
$script:closeMessages = @{
    'CLOSED'    = '파일 핸들을 닫았습니다.'
    'NOT_FOUND' = '대상 핸들이 이미 존재하지 않습니다.'
    'MISMATCH'  = '세션/상대경로/사용자 정보가 일치하지 않아 닫지 않았습니다.'
    'DIRECTORY' = '파일만 점유해제 가능합니다. (폴더는 해제할 수 없습니다)'
    'EXPIRED'   = '요청이 만료되어 처리하지 않았습니다.'
    'INVALID'   = '명령 형식이 올바르지 않습니다.'
    'ERROR'     = '핸들 닫기 중 오류가 발생했습니다.'
}

# 점유해제 요청 처리 상태 (중복/재실행 방지 + 부분 쓰기 재시도)
$script:lastCloseReqMtime     = [DateTime]::MinValue  # 마지막으로 처리 완료한 요청 파일 mtime(UTC)
$script:lastCloseReqId        = ''                    # 마지막으로 처리한 RequestId (같으면 건너뜀)
$script:closePendingMtime     = [DateTime]::MinValue  # 현재 파싱 대기 중인 mtime
$script:closePendingFirstSeen = [DateTime]::MinValue  # 그 mtime 을 처음 본 시각(UTC)

$script:successCount  = 0
$script:failureCount  = 0
$script:lastSuccessAt = $null
$script:lastFileCount = 0

# BOM 없는 UTF-8 (출력 파일 본문용)
$utf8 = New-Object System.Text.UTF8Encoding($false)

# ---------------------------------------------------------------------------
# JSON 직렬화기 (ConvertTo-Json 은 PS4.0/5.1 에서 깨지므로 사용 금지)
# ---------------------------------------------------------------------------
Add-Type -AssemblyName System.Web.Extensions
$serializer = New-Object System.Web.Script.Serialization.JavaScriptSerializer
$serializer.MaxJsonLength  = [int]::MaxValue
$serializer.RecursionLimit = 20

# ---------------------------------------------------------------------------
# Win32 Native API (kernel32) P/Invoke
#   - MoveFileEx: 원자적 파일 교체
#   - FindFirstFileW / FindClose: 잠긴 파일/폴더도 파일시스템 레벨에서 100% 파일/디렉토리 판정
# ---------------------------------------------------------------------------
if (-not ('FileOpenMonitor.NativeMethods' -as [type])) {
    Add-Type -Namespace 'FileOpenMonitor' -Name 'NativeMethods' -MemberDefinition @'
using System;
using System.IO;
using System.Runtime.InteropServices;

[StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
public struct WIN32_FIND_DATA {
    public uint dwFileAttributes;
    public System.Runtime.InteropServices.ComTypes.FILETIME ftCreationTime;
    public System.Runtime.InteropServices.ComTypes.FILETIME ftLastAccessTime;
    public System.Runtime.InteropServices.ComTypes.FILETIME ftLastWriteTime;
    public uint nFileSizeHigh;
    public uint nFileSizeLow;
    public uint dwReserved0;
    public uint dwReserved1;
    [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 260)]
    public string cFileName;
    [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 14)]
    public string cAlternateFileName;
}

public class NativeMethods {
    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    public static extern bool MoveFileEx(string lpExistingFileName, string lpNewFileName, uint dwFlags);

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    public static extern IntPtr FindFirstFileW(string lpFileName, out WIN32_FIND_DATA lpFindFileData);

    [DllImport("kernel32.dll", SetLastError = true)]
    public static extern bool FindClose(IntPtr hFindFile);

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    public static extern uint GetFileAttributesW(string lpFileName);

    public const uint FILE_ATTRIBUTE_DIRECTORY = 0x00000010;
    public const uint INVALID_FILE_ATTRIBUTES = 0xFFFFFFFF;
    public static readonly IntPtr INVALID_HANDLE_VALUE = new IntPtr(-1);
}
'@
}

# ---------------------------------------------------------------------------
# 원자적 파일 저장 (임시파일 작성 -> MoveFileEx 로 교체)
#   MOVEFILE_REPLACE_EXISTING(0x1) | MOVEFILE_WRITE_THROUGH(0x8)
# ---------------------------------------------------------------------------
function Save-AtomicFile {
    param(
        [string]$TargetPath,
        [string]$TempPath,
        [string]$Content
    )
    [System.IO.File]::WriteAllText($TempPath, $Content, $utf8)

    $MOVEFILE_REPLACE_EXISTING = [uint32]0x1
    $MOVEFILE_WRITE_THROUGH    = [uint32]0x8
    $flags = $MOVEFILE_REPLACE_EXISTING -bor $MOVEFILE_WRITE_THROUGH

    $ok = [FileOpenMonitor.NativeMethods]::MoveFileEx($TempPath, $TargetPath, $flags)
    if (-not $ok) {
        $errCode = [System.Runtime.InteropServices.Marshal]::GetLastWin32Error()
        $win32   = New-Object System.ComponentModel.Win32Exception($errCode)
        throw ("원자적 파일 교체 실패(MoveFileEx): " + $win32.Message)
    }
}

# ---------------------------------------------------------------------------
# 로그 기록 (타임스탬프 + 메시지, UTF8 Add-Content)
# ---------------------------------------------------------------------------
function Write-CollectorLog {
    param([string]$Message)
    $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    Add-Content -Path $logFile -Value ("[" + $ts + "] " + $Message) -Encoding UTF8
}

# ---------------------------------------------------------------------------
# 콘솔 상태 출력 (State 별 컬러)
# ---------------------------------------------------------------------------
function Write-CollectorStatus {
    param(
        [string]$State,
        [string]$Message = "",
        [string]$Elapsed = ""
    )
    switch ($State) {
        "수집 중" { $color = "Cyan"  }
        "성공"    { $color = "Green" }
        "실패"    { $color = "Red"   }
        default   { $color = "Gray"  }
    }

    if ($script:lastSuccessAt) {
        $lastStr = $script:lastSuccessAt.ToString("HH:mm:ss")
    } else {
        $lastStr = "없음"
    }

    $line = "[" + $State + "]"
    if ($Message) { $line += " " + $Message }
    if ($Elapsed) { $line += " (" + $Elapsed + ")" }
    $line += " | 성공 " + $script:successCount + " 실패 " + $script:failureCount `
           + " | 열린 파일 " + $script:lastFileCount + "건 | 마지막 성공 " + $lastStr

    Write-Host $line -ForegroundColor $color
}

# ---------------------------------------------------------------------------
# 공유 Path -> 공유 Name 매핑
# ---------------------------------------------------------------------------
function Get-ShareNameByPath {
    param(
        [string]$LocalPath,
        $Shares
    )
    foreach ($s in $Shares) {
        $sp = $s.Path
        if ([string]::Equals($LocalPath, $sp, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $s.Name
        }
        if ($LocalPath.StartsWith($sp + '\', [System.StringComparison]::OrdinalIgnoreCase)) {
            return $s.Name
        }
    }
    return ''
}

# ---------------------------------------------------------------------------
# 경로 유형 판정 (File / Directory / Unknown)
#   - 실제 저장된 파일시스템 MFT/속성 정보 기반으로 판정:
#     1) Win32 GetFileAttributesW API (kernel32.dll):
#        잠긴 파일/디렉토리도 즉시 속성 비트(0x10 = FILE_ATTRIBUTE_DIRECTORY) 획득
#     2) Win32 FindFirstFileW API:
#        핸들 기반 MFT 디렉토리 엔트리 속성 조회
#     3) .NET Directory::Exists / File::Exists
#     4) .NET File::GetAttributes
# ---------------------------------------------------------------------------
function Get-SmbPathType {
    param([string]$Path)

    if ([string]::IsNullOrEmpty($Path)) { return "Unknown" }

    $cleanPath = $Path.TrimEnd('\')
    $probe = $cleanPath
    if ($probe.Length -ge 2 -and $probe.Substring(1, 1) -eq ':' -and -not $probe.StartsWith('\\?\')) {
        $probe = '\\?\' + $probe
    }

    # 1. Win32 GetFileAttributesW (가장 빠르고 잠긴 핸들 영향 없이 정확함)
    try {
        $rawAttr = [FileOpenMonitor.NativeMethods]::GetFileAttributesW($probe)
        if ($rawAttr -eq [FileOpenMonitor.NativeMethods]::INVALID_FILE_ATTRIBUTES -and $probe -ne $cleanPath) {
            $rawAttr = [FileOpenMonitor.NativeMethods]::GetFileAttributesW($cleanPath)
        }
        if ($rawAttr -ne [FileOpenMonitor.NativeMethods]::INVALID_FILE_ATTRIBUTES) {
            if (($rawAttr -band [FileOpenMonitor.NativeMethods]::FILE_ATTRIBUTE_DIRECTORY) -eq [FileOpenMonitor.NativeMethods]::FILE_ATTRIBUTE_DIRECTORY) {
                return "Directory"
            } else {
                return "File"
            }
        }
    } catch { }

    # 2. Win32 FindFirstFileW (MFT 엔트리 검색)
    try {
        $findData = New-Object FileOpenMonitor.WIN32_FIND_DATA
        $hFind = [FileOpenMonitor.NativeMethods]::FindFirstFileW($probe, [ref]$findData)
        if ($hFind -eq [FileOpenMonitor.NativeMethods]::INVALID_HANDLE_VALUE -and $probe -ne $cleanPath) {
            $hFind = [FileOpenMonitor.NativeMethods]::FindFirstFileW($cleanPath, [ref]$findData)
        }
        if ($hFind -ne [FileOpenMonitor.NativeMethods]::INVALID_HANDLE_VALUE) {
            [void][FileOpenMonitor.NativeMethods]::FindClose($hFind)
            if (($findData.dwFileAttributes -band [FileOpenMonitor.NativeMethods]::FILE_ATTRIBUTE_DIRECTORY) -eq [FileOpenMonitor.NativeMethods]::FILE_ATTRIBUTE_DIRECTORY) {
                return "Directory"
            } else {
                return "File"
            }
        }
    } catch { }

    # 3. .NET 표준 Exists (드라이브 루트 등)
    try {
        if ([System.IO.Directory]::Exists($cleanPath)) { return "Directory" }
        if ([System.IO.File]::Exists($cleanPath)) { return "File" }
    } catch { }

    # 4. .NET File::GetAttributes
    try {
        $attr = [System.IO.File]::GetAttributes($cleanPath)
        if (($attr -band [System.IO.FileAttributes]::Directory) -eq [System.IO.FileAttributes]::Directory) {
            return "Directory"
        } else {
            return "File"
        }
    } catch { }

    return "Unknown"
}

# ---------------------------------------------------------------------------
# 수집 1회 실행 (성공 시 열린 파일 건수 반환)
# ---------------------------------------------------------------------------
function Invoke-CollectionOnce {
    $startedAt = [DateTime]::UtcNow

    # 공유 목록 (수집 주기마다 갱신) - 특수공유 제외 + Path 있는 것만, 긴 Path 우선
    $shares = Get-SmbShare -ErrorAction Stop |
        Where-Object { -not $_.Special -and $_.Path } |
        ForEach-Object { @{ Name = $_.Name; Path = $_.Path.TrimEnd('\') } } |
        Sort-Object { $_.Path.Length } -Descending

    $openFiles = Get-SmbOpenFile -ErrorAction Stop

    # 경로 유형 캐시 (주기마다 초기화) - 같은 경로는 한 번만 판정
    $typeCache = @{}

    $items = New-Object System.Collections.ArrayList
    foreach ($f in $openFiles) {
        $localPath = [string]$f.Path

        # 캐시 조회 후 없을 때만 Get-SmbPathType 호출
        if ($typeCache.ContainsKey($localPath)) {
            $entryType = $typeCache[$localPath]
        } else {
            $entryType = Get-SmbPathType $localPath
            $typeCache[$localPath] = $entryType
        }
        $isDir = ($entryType -eq "Directory")

        # ShareName 은 MSFT_SmbOpenFile 에 없는 속성 -> 공유 목록에서 Path 로 역매핑
        $shareName = Get-ShareNameByPath $localPath $shares
        # ShareRelativePath 는 MSFT_SmbOpenFile 기본 제공 속성을 그대로 사용 (임의 계산 금지)
        $shareRel  = [string]$f.ShareRelativePath

        $item = @{
            FileId             = [string]$f.FileId
            SessionId          = [string]$f.SessionId
            Path               = [string]$localPath
            EntryType          = [string]$entryType
            IsDirectory        = [bool]$isDir
            ShareName          = [string]$shareName
            ShareRelativePath  = [string]$shareRel
            ClientComputerName = [string]$f.ClientComputerName
            ClientUserName     = [string]$f.ClientUserName
        }
        [void]$items.Add($item)
    }

    # ArrayList 를 그대로 직렬화하면 순환 참조 오류 -> 반드시 object[] 로 변환
    $filesArr = [object[]]$items.ToArray()

    $snapshot = @{
        Success        = $true
        CollectedAtUtc = $startedAt.ToString("o")
        Count          = $filesArr.Count
        Files          = $filesArr
        Error          = $null
    }

    $json = $serializer.Serialize($snapshot)
    Save-AtomicFile -TargetPath $outputFile -TempPath $tempFile -Content $json

    return $filesArr.Count
}

# ===========================================================================
#  점유해제(Close) 명령 처리
#   - Spring 이 close_request.json 에 제자리 덮어쓰기로 요청을 쓰면, 수집기가
#     메인 루프에서 mtime 변화를 감지해 검증 후 해당 SMB 핸들을 닫고
#     close_result.json 으로 결과를 돌려준다(Spring 은 결과를 읽기만).
#   - 안전: 명령 값은 절대 코드/명령 문자열로 실행하지 않는다. 정규식 검증 후
#     타입 변환(uint64)해 cmdlet 파라미터로만 전달하고, Close 는 검증 통과한
#     CIM 객체를 그대로 파이프한다(문자열 경로 매칭 금지).
# ===========================================================================

# 로그/결과에 넣을 값의 개행 제거 (감사 로그 한 줄 보장)
function Remove-CollectorNewline {
    param($Value)
    if ($null -eq $Value) { return '' }
    $s = [string]$Value
    $s = $s -replace "[\r\n]+", ' '
    return $s.Trim()
}

# JSON 딕셔너리에서 키 안전 조회 (없으면 $null)
#   JavaScriptSerializer 는 Dictionary<string,object> 를 돌려주는데, PowerShell 은
#   .Contains(key) 를 제네릭 ICollection<KeyValuePair>.Contains 로 바인딩하려다 실패한다.
#   비제네릭 [System.Collections.IDictionary] 로 캐스팅해 Contains(object) 를 명시 호출.
function Get-JsonDictValue {
    param($Dict, [string]$Key)
    # 주의: 반드시 인라인 캐스트로 호출해야 한다. $d=[IDictionary]$Dict; $d.Contains()
    #   형태는 PowerShell 이 런타임 타입(제네릭 Dictionary)으로 다시 바인딩해 실패한다.
    if ($null -ne $Dict -and ($Dict -is [System.Collections.IDictionary])) {
        if (([System.Collections.IDictionary]$Dict).Contains($Key)) {
            return ([System.Collections.IDictionary]$Dict)[$Key]
        }
    }
    return $null
}

# 결과 해시에 코드/성공/메시지 채워 반환
function Set-CloseResult {
    param($Result, [string]$Code)
    $Result.Code    = $Code
    $Result.Success = ($Code -eq 'CLOSED')
    $Result.Message = $script:closeMessages[$Code]
    return $Result
}

# 결과 파일 작성 (close_result.json, BOM 없는 UTF-8, Save-AtomicFile 로 원자적 교체)
function Write-CommandResult {
    param(
        [string]$RequestId,
        [bool]$Success,
        [string]$Code,
        [string]$Message
    )
    $payload = @{
        RequestId      = $RequestId
        Success        = $Success
        Code           = $Code
        Message        = $Message
        ProcessedAtUtc = [DateTime]::UtcNow.ToString("o")
    }
    $json = $serializer.Serialize($payload)
    $temp = $closeResultFile + ".tmp"
    Save-AtomicFile -TargetPath $closeResultFile -TempPath $temp -Content $json
}

# ---------------------------------------------------------------------------
# 요청 1건 판정/실행 (순수 코어, 파일 I/O 없음 — 테스트에서 그대로 로딩 가능)
#   입력: 원본 JSON 문자열, 현재 UTC
#   출력: @{ RequestId; Code; Success; Message; Parseable; Log* } 해시
#     - Parseable=$false : JSON 파싱 불가(비객체 포함). 호출부가 "아직 쓰는 중"인지
#       판단해 재시도/만료 확정에 사용한다.
#   처리 순서: JSON 파싱 -> 형식/만료 검증 -> 핸들 조회(세션필터)
#              -> 폴더 거부 -> 상대경로/사용자 대조 -> 검증된 객체 Close
# ---------------------------------------------------------------------------
function Resolve-CloseCommand {
    param(
        [string]$RawJson,
        [DateTime]$NowUtc
    )

    $result = @{
        RequestId      = ''
        Code           = 'INVALID'
        Success        = $false
        Message        = $script:closeMessages['INVALID']
        Parseable      = $false
        LogRequestedBy = ''
        LogFileId      = ''
        LogShareRel    = ''
        LogUser        = ''
        LogReason      = ''
    }

    # 1. JSON 파싱 (실패/비객체 -> Parseable=$false, INVALID)
    $cmd = $null
    try { $cmd = $serializer.DeserializeObject($RawJson) } catch { $cmd = $null }
    if ($null -eq $cmd -or -not ($cmd -is [System.Collections.IDictionary])) {
        return (Set-CloseResult $result 'INVALID')
    }
    $result.Parseable = $true

    # RequestId 는 JSON 에서만 취득 (GUID 형식일 때만)
    $requestId = ''
    $pRequestId = [string](Get-JsonDictValue $cmd 'RequestId')
    if ($pRequestId -match $script:guidRegex) {
        $requestId = $pRequestId
        $result.RequestId = $requestId
    }

    # 필드 추출 (숫자로 올 수 있으므로 [string] 변환)
    $fileId    = [string](Get-JsonDictValue $cmd 'FileId')
    $sessionId = [string](Get-JsonDictValue $cmd 'SessionId')
    $expRel    = [string](Get-JsonDictValue $cmd 'ExpectedRelativePath')
    $expUser   = [string](Get-JsonDictValue $cmd 'ExpectedUser')
    $reqBy     = [string](Get-JsonDictValue $cmd 'RequestedBy')
    $reason    = [string](Get-JsonDictValue $cmd 'Reason')
    $reqAtRaw  = Get-JsonDictValue $cmd 'RequestedAtUtc'

    # 감사 로그용 값 채움 (개행 제거, 서버 로컬 Path 는 포함하지 않음)
    $result.LogRequestedBy = Remove-CollectorNewline $reqBy
    $result.LogFileId      = Remove-CollectorNewline $fileId
    $result.LogShareRel    = Remove-CollectorNewline $expRel
    $result.LogUser        = Remove-CollectorNewline $expUser
    $result.LogReason      = Remove-CollectorNewline $reason

    # 2. 형식 검증
    if ($requestId -notmatch $script:guidRegex)      { return (Set-CloseResult $result 'INVALID') }
    if ($fileId    -notmatch $script:numericIdRegex) { return (Set-CloseResult $result 'INVALID') }
    if ($sessionId -notmatch $script:numericIdRegex) { return (Set-CloseResult $result 'INVALID') }
    if ([string]::IsNullOrEmpty($expRel) -or [string]::IsNullOrEmpty($expUser) -or `
        [string]::IsNullOrEmpty($reqBy)  -or [string]::IsNullOrEmpty($reason)) {
        return (Set-CloseResult $result 'INVALID')
    }

    # 만료 검증 (RequestedAtUtc 를 반드시 UTC 로 해석)
    $reqAtUtc = $null
    if ($reqAtRaw -is [DateTime]) {
        # JavaScriptSerializer 가 \/Date()\/ 를 DateTime 으로 돌려준 경우
        $reqAtUtc = ([DateTime]$reqAtRaw).ToUniversalTime()
    } else {
        $reqAtStr = [string]$reqAtRaw
        if ([string]::IsNullOrEmpty($reqAtStr)) { return (Set-CloseResult $result 'INVALID') }
        try {
            $reqAtUtc = [DateTime]::Parse(
                $reqAtStr,
                [System.Globalization.CultureInfo]::InvariantCulture,
                ([System.Globalization.DateTimeStyles]::AdjustToUniversal -bor [System.Globalization.DateTimeStyles]::AssumeUniversal))
        } catch {
            return (Set-CloseResult $result 'INVALID')
        }
    }
    $ageSeconds = ($NowUtc - $reqAtUtc).TotalSeconds
    if ($ageSeconds -gt $commandExpirySeconds -or $ageSeconds -lt (-1 * $commandFutureSkewSeconds)) {
        return (Set-CloseResult $result 'EXPIRED')
    }

    # 3. 핸들 조회 (FileId) - 검증된 값만 uint64 변환해 파라미터로 전달
    try { $fid = [uint64]$fileId } catch { return (Set-CloseResult $result 'INVALID') }

    $open = $null
    try {
        $open = Get-SmbOpenFile -FileId $fid -ErrorAction Stop
    } catch {
        # 핸들 없음만 NOT_FOUND, 권한/WMI 등 기타 실패는 ERROR
        if ($_.CategoryInfo.Category -eq 'ObjectNotFound') {
            return (Set-CloseResult $result 'NOT_FOUND')
        }
        return (Set-CloseResult $result 'ERROR')
    }

    # 세션 일치 후보만 (숫자 비교 - 선행 0 등 방지). 정확히 1건일 때만 진행
    $sid = [uint64]$sessionId
    $cands = @($open | Where-Object { $null -ne $_.SessionId } | Where-Object {
        $ok = $false
        try { $ok = ([uint64]$_.SessionId -eq $sid) } catch { $ok = $false }
        $ok
    })
    if ($cands.Count -ne 1) {
        return (Set-CloseResult $result 'NOT_FOUND')
    }
    $candidate = $cands[0]

    # 4. 폴더 핸들 거부
    if ((Get-SmbPathType ([string]$candidate.Path)) -eq 'Directory') {
        return (Set-CloseResult $result 'DIRECTORY')
    }

    # 5. 대조 (ShareRelativePath / ClientUserName, OrdinalIgnoreCase)
    $candRel  = [string]$candidate.ShareRelativePath
    $candUser = [string]$candidate.ClientUserName
    if (-not [string]::Equals($candRel, $expRel, [System.StringComparison]::OrdinalIgnoreCase) -or `
        -not [string]::Equals($candUser, $expUser, [System.StringComparison]::OrdinalIgnoreCase)) {
        return (Set-CloseResult $result 'MISMATCH')
    }

    # 6. 검증된 CIM 객체를 그대로 닫는다 (문자열 경로 매칭 금지)
    try {
        $candidate | Close-SmbOpenFile -Force -ErrorAction Stop
    } catch {
        return (Set-CloseResult $result 'ERROR')
    }

    return (Set-CloseResult $result 'CLOSED')
}

# 감사 로그 한 줄 (항상 기록, 서버 로컬 Path 미포함)
function Write-CloseAudit {
    param($d)
    Write-CollectorLog (
        "점유해제 요청ID=" + $d.RequestId +
        " 요청자=" + $d.LogRequestedBy +
        " FileId=" + $d.LogFileId +
        " 상대경로=" + $d.LogShareRel +
        " 대상사용자=" + $d.LogUser +
        " 사유=" + $d.LogReason +
        " 결과=" + $d.Code)
}

# 콘솔 한 줄 (성공 Green / 실패 Yellow) - 기존 Write-CollectorStatus 와 별개
function Write-CloseConsole {
    param($d)
    if ($d.Success) { $cc = "Green" } else { $cc = "Yellow" }
    Write-Host ("[점유해제] " + $d.Code + " 요청ID=" + $d.RequestId +
                " FileId=" + $d.LogFileId + " : " + $d.Message) -ForegroundColor $cc
}

# 공유 잠금 충돌 없이 UTF-8 로 파일 전체 읽기
#   - PS 5.1/4.0 은 기본 ANSI 로 읽어 한글 경로가 깨지므로 반드시 UTF-8 로 연다.
#   - FileShare.ReadWrite 로 열어 Spring 의 동시 쓰기와 잠금 충돌이 나지 않게 한다.
#   - BOM 자동 감지(두번째 인자 $true). 실패는 호출부가 try/catch 로 받아 재시도.
function Read-SharedUtf8Text {
    param([string]$Path)
    $fs = $null; $sr = $null
    try {
        $fs = New-Object System.IO.FileStream(
            $Path, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Read, [System.IO.FileShare]::ReadWrite)
        $sr = New-Object System.IO.StreamReader($fs, [System.Text.Encoding]::UTF8, $true)
        return $sr.ReadToEnd()
    } finally {
        if ($null -ne $sr) { $sr.Dispose() }       # StreamReader.Dispose 가 내부 스트림도 닫음
        elseif ($null -ne $fs) { $fs.Dispose() }
    }
}

# 기존 close_result.json 의 RequestId 조회 (없거나 실패하면 $null)
#   수집기 재시작 후에도 같은 RequestId 를 재처리/덮어쓰지 않기 위한 내구적 중복 검사용.
function Get-ResultRequestId {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return $null }
    try {
        $txt = Read-SharedUtf8Text $Path
        $obj = $serializer.DeserializeObject($txt)
        if ($obj -is [System.Collections.IDictionary]) {
            return [string](Get-JsonDictValue $obj 'RequestId')
        }
    } catch { }
    return $null
}

# ---------------------------------------------------------------------------
# 점유해제 요청 확인/처리 (메인 루프에서 매 반복 호출, 임대 상태와 무관)
#   - close_request.json 의 mtime 이 마지막 처리 mtime 보다 새로우면 읽어서 처리.
#   - 읽기/파싱 실패는 "아직 쓰는 중"으로 보고 이번 반복은 건너뜀(= 재시도).
#     같은 mtime 으로 유예시간 초과 파싱 불가면 INVALID(빈 RequestId) 결과 기록 후
#     그 mtime 을 처리 완료로 표시.
#   - 같은 RequestId 재요청은 건너뜀(중복/재실행 방지). close_request.json 은 처리
#     후에도 남으므로, 수집기 재시작 후에도 기존 close_result.json 의 RequestId 와
#     대조해 재처리/결과 덮어쓰기를 막는다(CLOSED 를 NOT_FOUND 로 오탐 404 방지).
#     한 번도 처리된 적 없는 과거 요청만 RequestedAtUtc 만료(30초)로 차단된다.
# ---------------------------------------------------------------------------
function Invoke-CloseRequestCheck {
    param([DateTime]$NowUtc)

    # 요청 파일 mtime 확인 (없음/조회 실패 -> 아무 것도 하지 않음)
    $mtime = $null
    try {
        $mtime = (Get-Item -Path $closeRequestFile -ErrorAction Stop).LastWriteTimeUtc
    } catch {
        return
    }

    if ($mtime -le $script:lastCloseReqMtime) { return }   # 새 요청 아님

    # 읽기 (UTF-8 + FileShare.ReadWrite). 잠김/부분 쓰기 등 실패 -> 아직 쓰는 중일 수 있음
    $raw = $null
    try {
        $raw = Read-SharedUtf8Text $closeRequestFile
    } catch {
        $raw = $null
    }

    # 파싱 (중복 가드를 Close 이전에 수행하기 위해 여기서 먼저 파싱한다)
    $parsed = $null
    if ($null -ne $raw) {
        try { $parsed = $serializer.DeserializeObject($raw) } catch { $parsed = $null }
    }

    # 파싱 불가 (읽기 실패/부분 쓰기 포함) -> 재시도/만료 확정 처리
    if ($null -eq $parsed -or -not ($parsed -is [System.Collections.IDictionary])) {
        if ($script:closePendingMtime -ne $mtime) {
            # 이 mtime 을 처음 봄 -> 다음 반복 재시도
            $script:closePendingMtime     = $mtime
            $script:closePendingFirstSeen = $NowUtc
            return
        }
        if (($NowUtc - $script:closePendingFirstSeen).TotalSeconds -le $closeParseGraceSeconds) {
            return   # 아직 유예시간 내 -> 계속 재시도
        }

        # 유예시간 초과 파싱 불가 -> INVALID 확정 (RequestId 빈 값), mtime 처리 완료 기록
        $inv = @{
            RequestId = ''; Code = ''; Success = $false; Message = '';
            LogRequestedBy = ''; LogFileId = ''; LogShareRel = ''; LogUser = ''; LogReason = ''
        }
        $inv = Set-CloseResult $inv 'INVALID'
        Write-CloseAudit $inv
        Write-CloseConsole $inv
        try {
            Write-CommandResult -RequestId '' -Success $false -Code 'INVALID' -Message $script:closeMessages['INVALID']
        } catch {
            Write-CollectorLog ("결과 파일 작성 실패: " + (Remove-CollectorNewline $_.Exception.Message))
        }
        $script:lastCloseReqMtime = $mtime
        $script:closePendingMtime = [DateTime]::MinValue
        return
    }

    # 파싱 성공 -> 대기 상태 해제
    $script:closePendingMtime = [DateTime]::MinValue

    # RequestId 사전 추출 (GUID 형식일 때만)
    $reqId = [string](Get-JsonDictValue $parsed 'RequestId')
    if ($reqId -notmatch $script:guidRegex) { $reqId = '' }

    # 같은 RequestId 재요청은 Close 이전에 건너뜀 (결과 덮어쓰기/재처리 금지).
    #   메모리(lastCloseReqId) + 기존 결과파일 RequestId 와 대조 -> 수집기 재시작 후에도
    #   이미 CLOSED 된 요청을 다시 처리해 NOT_FOUND 로 덮어쓰는 오탐 404 를 막는다.
    $priorResultId = Get-ResultRequestId $closeResultFile
    if ($reqId -ne '' -and ($reqId -eq $script:lastCloseReqId -or $reqId -eq $priorResultId)) {
        $script:lastCloseReqId    = $reqId
        $script:lastCloseReqMtime = $mtime
        return
    }

    # 검증 + (모두 통과 시) 실제 Close 수행 -> 중복이 아닌 신규 요청만 여기 도달
    $decision = Resolve-CloseCommand -RawJson $raw -NowUtc $NowUtc

    # 처리 완료 상태를 먼저 기록한다. 이후 감사 로그/결과 쓰기가 실패해도(로그 파일
    # 잠김 등) 재처리로 이미 CLOSED 된 핸들을 NOT_FOUND 로 덮어쓰는 오탐 404 를 막는다.
    if ($decision.RequestId -ne '') { $script:lastCloseReqId = $decision.RequestId }
    $script:lastCloseReqMtime = $mtime

    try { Write-CloseAudit $decision }   catch { }   # 로그 실패가 결과 쓰기를 건너뛰지 않게
    try { Write-CloseConsole $decision } catch { }
    try {
        Write-CommandResult -RequestId $decision.RequestId -Success $decision.Success `
                            -Code $decision.Code -Message $decision.Message
    } catch {
        try { Write-CollectorLog ("결과 파일 작성 실패 요청ID=" + $decision.RequestId + ": " + (Remove-CollectorNewline $_.Exception.Message)) } catch { }
    }
}

# ===========================================================================
#  초기화
# ===========================================================================
if (-not (Test-Path $baseDir)) {
    New-Item -ItemType Directory -Path $baseDir -Force | Out-Null
}

# request.txt 가 없으면 빈 파일 생성 + 오래된 시각으로 설정
#   (Spring 계정이 새 파일 생성 권한 없이 "기존 파일 쓰기"만 해도 되도록)
if (-not (Test-Path $requestFile)) {
    New-Item -ItemType File -Path $requestFile | Out-Null
    (Get-Item $requestFile).LastWriteTimeUtc = `
        [DateTime]::ParseExact("2000-01-01", "yyyy-MM-dd", [System.Globalization.CultureInfo]::InvariantCulture)
}

# close_request.json 이 없으면 빈 파일 생성 + 오래된 시각으로 설정 (request.txt 와 동일 방식).
#   우리가 방금 만든 빈 파일(파싱 불가)을 요청으로 오인하지 않도록 처리 완료 mtime 을
#   그 시각으로 미리 세팅한다. Spring 이 실제로 덮어쓰면 mtime 이 갱신되어 감지된다.
#   (반대로 재시작 전부터 파일이 이미 있었다면 pre-seed 하지 않아, 그 요청을 읽되
#    RequestedAtUtc 만료 검사로 과거 요청의 재실행이 차단된다.)
if (-not (Test-Path $closeRequestFile)) {
    New-Item -ItemType File -Path $closeRequestFile | Out-Null
    $seedTime = [DateTime]::ParseExact("2000-01-01", "yyyy-MM-dd", [System.Globalization.CultureInfo]::InvariantCulture)
    (Get-Item $closeRequestFile).LastWriteTimeUtc = $seedTime
    $script:lastCloseReqMtime = (Get-Item $closeRequestFile).LastWriteTimeUtc
}

# ===========================================================================
#  시작 배너
# ===========================================================================
Write-Host "==============================================================="
Write-Host " SMB 열린 파일 수집기 (요청/임대 기반)"
Write-Host "---------------------------------------------------------------"
Write-Host (" 실행 경로   : " + $baseDir)
Write-Host (" 수집 주기   : " + $intervalSeconds + "초")
Write-Host (" 임대 시간   : " + $leaseSeconds + "초")
Write-Host (" 요청 파일   : " + (Split-Path $requestFile -Leaf))
Write-Host (" 출력 파일   : " + (Split-Path $outputFile  -Leaf))
Write-Host (" 점유해제 요청 파일: " + (Split-Path $closeRequestFile -Leaf))
Write-Host " 점유해제 명령 처리: 사용"
Write-Host " 종료: Ctrl+C"
Write-Host "==============================================================="

Write-Host "[대기] 요청 없음 - 화면 접속 시 수집 시작" -ForegroundColor Gray
Write-CollectorLog "수집기 시작 - 대기 상태 (요청 없음)"

# ===========================================================================
#  메인 루프 (1초마다 request.txt 확인)
# ===========================================================================
$active               = $false              # 현재 수집(활성) 상태 여부
$lastCollectAt        = [DateTime]::MinValue # 마지막 수집 시각(UTC)
$lastSuccessLogAt     = [DateTime]::MinValue # 마지막 성공 로그 시각(UTC)

while ($true) {
    $now = [DateTime]::UtcNow

    # --- 점유해제 요청 처리 (임대 request.txt 상태와 무관하게 매 반복 수행) ---
    #     어떤 예외도 메인 수집 루프를 죽이지 않도록 전체를 try/catch 로 감싼다.
    try {
        Invoke-CloseRequestCheck -NowUtc $now
    } catch {
        Write-CollectorLog ("점유해제 처리 예외: " + (Remove-CollectorNewline $_.Exception.Message))
    }

    # request.txt 의 LastWriteTimeUtc 확인 (없음/실패는 요청 없음 처리)
    $reqTime = $null
    try {
        $reqItem = Get-Item -Path $requestFile -ErrorAction Stop
        $reqTime = $reqItem.LastWriteTimeUtc
    } catch {
        $reqTime = $null
    }

    # 임대 유효 여부: 요청 시각이 현재(UTC) 기준 leaseSeconds 이내
    $leaseActive = $false
    if ($null -ne $reqTime) {
        $ageSeconds = ($now - $reqTime).TotalSeconds
        if ($ageSeconds -le $leaseSeconds) { $leaseActive = $true }
    }

    if ($leaseActive) {
        # 대기 -> 수집 전환 (상태 전환 시에만 출력/로그)
        if (-not $active) {
            $active = $true
            $lastSuccessLogAt = [DateTime]::MinValue   # 재개 시 첫 성공은 반드시 로그
            Write-Host "[수집 시작] 화면 요청 감지" -ForegroundColor Cyan
            Write-CollectorLog "상태 전환: 수집 시작 (화면 요청 감지)"
        }

        # 간격 판정: 마지막 수집 후 intervalSeconds 이상 경과했을 때만 수집
        $sinceCollect = ($now - $lastCollectAt).TotalSeconds
        if ($sinceCollect -ge $intervalSeconds) {
            $sw = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                Write-CollectorStatus -State "수집 중"

                $cnt = Invoke-CollectionOnce
                $sw.Stop()
                $elapsedStr = "{0:N2}초" -f $sw.Elapsed.TotalSeconds

                $script:successCount++
                $script:lastSuccessAt = Get-Date
                $script:lastFileCount = $cnt
                $lastCollectAt = $now

                # 성공 로그는 상태 전환 또는 60초에 한 번으로 제한 (로그 비대화 방지)
                if (($now - $lastSuccessLogAt).TotalSeconds -ge 60) {
                    Write-CollectorLog ("수집 성공: 열린 파일 " + $cnt + "건, 소요 " + $elapsedStr)
                    $lastSuccessLogAt = $now
                }

                Write-CollectorStatus -State "성공" -Message ("열린 파일 " + $cnt + "건") -Elapsed $elapsedStr
            }
            catch {
                $sw.Stop()
                $elapsedStr = "{0:N2}초" -f $sw.Elapsed.TotalSeconds
                $script:failureCount++
                $lastCollectAt = $now
                # 실패 로그는 항상 기록
                Write-CollectorLog ("수집 실패: " + $_.Exception.Message)
                Write-CollectorStatus -State "실패" -Message $_.Exception.Message -Elapsed $elapsedStr
            }
        }
    }
    else {
        # 수집 -> 대기 전환 (상태 전환 시에만 출력/로그)
        if ($active) {
            $active = $false
            if ($script:lastSuccessAt) {
                $lastStr = $script:lastSuccessAt.ToString("HH:mm:ss")
            } else {
                $lastStr = "없음"
            }
            Write-Host ("[대기 전환] 요청 없음 " + $leaseSeconds + "초 경과 - 수집 중지") -ForegroundColor Gray
            Write-Host ("대기 중 | 마지막 수집 " + $lastStr + " | 열린 파일 " + $script:lastFileCount + "건") -ForegroundColor Gray
            Write-CollectorLog ("상태 전환: 대기 (요청 없음 " + $leaseSeconds + "초 경과)")
        }
    }

    Start-Sleep -Seconds 1
}
