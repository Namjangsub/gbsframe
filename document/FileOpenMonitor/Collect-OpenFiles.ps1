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
# MoveFileEx (kernel32) P/Invoke - 원자적 파일 교체용
# ---------------------------------------------------------------------------
if (-not ('FileOpenMonitor.NativeMethods' -as [type])) {
    Add-Type -Namespace 'FileOpenMonitor' -Name 'NativeMethods' -MemberDefinition @'
[System.Runtime.InteropServices.DllImport("kernel32.dll", SetLastError = true, CharSet = System.Runtime.InteropServices.CharSet.Unicode)]
public static extern bool MoveFileEx(string lpExistingFileName, string lpNewFileName, uint dwFlags);
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
# 경로 유형 판정 (File / Directory / Unknown / NotFoundOrLocked)
#   - 드라이브 경로(두번째 문자 ':')이고 '\\?\' 접두 없으면 '\\?\' 부착 (260자 초과 대응)
# ---------------------------------------------------------------------------
function Get-SmbPathType {
    param([string]$Path)

    if ([string]::IsNullOrEmpty($Path)) { return "Unknown" }

    $probe = $Path
    if ($Path.Length -ge 2 -and $Path.Substring(1, 1) -eq ':' -and -not $Path.StartsWith('\\?\')) {
        $probe = '\\?\' + $Path
    }

    try {
        $attr = [System.IO.File]::GetAttributes($probe)
        if (($attr -band [System.IO.FileAttributes]::Directory) -eq [System.IO.FileAttributes]::Directory) {
            return "Directory"
        } else {
            return "File"
        }
    } catch {
        return "NotFoundOrLocked"
    }
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
Write-Host " 종료: Ctrl+C"
Write-Host "==============================================================="

Write-Host "[대기] 요청 없음 - 화면 접속 시 수집 시작" -ForegroundColor Gray
Write-CollectorLog "수집기 시작 - 대기 상태 (요청 없음)"

# ===========================================================================
#  메인 루프 (1초마다 request.txt 확인)
# ===========================================================================
$active           = $false              # 현재 수집(활성) 상태 여부
$lastCollectAt    = [DateTime]::MinValue # 마지막 수집 시각(UTC)
$lastSuccessLogAt = [DateTime]::MinValue # 마지막 성공 로그 시각(UTC)

while ($true) {
    $now = [DateTime]::UtcNow

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
