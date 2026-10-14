package com.dksys.biz.admin.cm.cm40.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dksys.biz.admin.cm.cm05.service.CM05Svc;
import com.dksys.biz.admin.cm.cm40.model.FileOpenFile;
import com.dksys.biz.admin.cm.cm40.model.FileOpenSnapshot;
import com.dksys.biz.admin.cm.cm40.service.CM40Svc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CM40SvcImpl implements CM40Svc {

    private static final Logger log = LoggerFactory.getLogger(CM40SvcImpl.class);

    // 캐시 TTL: 성공은 짧게(3초), 실패는 조금 길게(5초) — 실패 경로를 과하게 재시도하지 않음
    private static final long CACHE_TTL_SUCCESS_MS = 3000L;
    private static final long CACHE_TTL_FAILURE_MS = 5000L;
    private static final int MAX_READ_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 100L;
    private static final long CLOCK_SKEW_TOLERANCE_SEC = -5L;
    // 리스 유지용 요청 파일은 최소 2초 간격으로만 갱신
    private static final long TOUCH_MIN_INTERVAL_MS = 2000L;
    // 같은 원인의 반복 로그는 60초에 한 번만
    private static final long LOG_THROTTLE_MS = 60000L;

    // 점유해제: 결과 파일 폴링 간격/최대 대기
    private static final long CLOSE_POLL_INTERVAL_MS = 500L;
    private static final long CLOSE_TIMEOUT_MS = 10000L;
    // 단일 신호 파일 방식이라 동시 1건만. 선행 요청 처리 중이면 최대 12초 대기
    private static final long CLOSE_ACQUIRE_TIMEOUT_MS = 12000L;
    // 권한 판정(허용자 목록) 캐시 TTL. 코드 변경이 곧 반영되도록 짧게 유지
    private static final long CLOSE_AUTH_CACHE_TTL_MS = 30000L;
    // 사유 최대 길이 / 경로·사용자 필드 최대 길이
    private static final int CLOSE_REASON_MAX = 200;
    private static final int CLOSE_FIELD_MAX = 1024;
    // 로그 1줄에 기록하는 각 값의 최대 길이(로그 비대화 방지)
    private static final int LOG_FIELD_MAX = 256;
    // 스냅샷과 같은 폴더에 두는 고정 신호 파일명(요청: Spring 쓰기 / 결과: 수집기 쓰기)
    private static final String CLOSE_REQUEST_FILE = "close_request.json";
    private static final String CLOSE_RESULT_FILE = "close_result.json";
    // fileId/sessionId 형식: 1~20자리 숫자만(UInt64 범위라 long 파싱은 하지 않음)
    private static final Pattern DIGITS_1_20 = Pattern.compile("^\\d{1,20}$");

    @Value("${file-open.snapshot-path:}")
    private String snapshotPath;

    @Value("${file-open.max-age-seconds:30}")
    private long maxAgeSeconds;

    // 리스 유지용 요청 파일 경로. 비어 있으면 요청 파일 갱신 기능을 끈다(오류 아님)
    @Value("${file-open.request-path:}")
    private String requestPath;

    // 점유해제 명령 파일을 쓰는 공유 폴더(수집기가 폴링). 비어 있으면 점유해제 기능 OFF
    @Value("${file-open.command-dir:}")
    private String commandDir;

    // 점유해제 실행 허용자 목록이 담긴 공통코드 ID(기본 SPECRTS20). 비어 있으면 모든 사용자 거부
    @Value("${file-open.close-auth-code-id:SPECRTS20}")
    private String closeAuthCodeId;

    // 허용자 목록 공통코드 조회에 기존 CM05 서비스를 그대로 재사용(신규 SQL/매퍼 없음)
    @Autowired
    private CM05Svc cm05Svc;

    // 단일 신호 파일 방식이라 점유해제는 1건씩 직렬화
    private final Semaphore closeSemaphore = new Semaphore(1);

    // 허용자 집합 캐시(소문자/trim 적용). fail-closed: 조회 실패 시 빈 집합
    private volatile Set<String> closeAuthCache;
    private volatile long closeAuthCacheAtMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 읽기(파싱)와 touch는 서로 다른 단일 스레드 데몬 executor 로 분리.
    // UNC 공유 I/O 가 수 초간 멈출 수 있어, 느린 읽기가 touch(리스 갱신)를 막지 못하도록 함
    private final ExecutorService readerExecutor = newDaemonExecutor("cm40-fileopen-reader");
    private final ExecutorService toucherExecutor = newDaemonExecutor("cm40-fileopen-toucher");

    // 마지막 스냅샷 결과를 담는 불변 홀더(스냅샷+로드시각 또는 실패메시지). 단일 volatile 로 원자적 교체
    private volatile SnapshotHolder cache;

    // single-flight: 동시에 하나의 갱신/하나의 touch 만 수행
    private final AtomicBoolean refreshing = new AtomicBoolean(false);
    private final AtomicBoolean touching = new AtomicBoolean(false);

    // 마지막 touch 시각(요청 스레드가 읽고 touch 스레드가 씀)
    private volatile long lastTouchAtMs;

    // 로그 throttle: 같은 원인(key)은 60초에 한 번만.
    // reader/toucher 는 각각 단일 스레드이므로 원인별 상태를 단순 필드로 유지(원인이 바뀌면 즉시 1회 허용)
    private volatile String lastReadLogKey;
    private volatile long lastReadLogAtMs;
    private volatile String lastTouchLogKey;
    private volatile long lastTouchLogAtMs;
    // 수집기 오류 로그는 요청 스레드(다중)에서 남기므로 CAS 로 보호(단일 원인)
    private final AtomicLong lastCollectorErrorLogMs = new AtomicLong(0L);

    @Override
    public Map<String, Object> getFileOpenStatus() {
        // 화면이 열려 있는 동안 리스를 유지하기 위한 요청 파일 갱신. 응답에는 영향 없음(항상 비차단)
        try {
            touchRequestFile();
        } catch (RuntimeException ignore) {
            // touch 는 조회 응답에 절대 영향을 주지 않음(by construction)
        }

        List<Map<String, Object>> noFiles = Collections.emptyList();

        if (snapshotPath == null || snapshotPath.trim().isEmpty()) {
            return buildResult("ERROR", "스냅샷 경로가 설정되지 않았습니다", null, 0L, noFiles);
        }

        try {
            SnapshotHolder holder = cache;
            long now = System.currentTimeMillis();

            // 캐시가 한 번도 채워지지 않은 첫 호출: 비동기 갱신만 예약하고 즉시 반환(다음 폴링에서 정상)
            if (holder == null) {
                scheduleRefresh();
                return buildResult("ERROR", "스냅샷을 불러오는 중입니다", null, 0L, noFiles);
            }

            // TTL 경과 시 비동기 갱신 예약하되, 응답은 기존 캐시로 즉시 반환
            long ttl = holder.isSuccess() ? CACHE_TTL_SUCCESS_MS : CACHE_TTL_FAILURE_MS;
            if (now - holder.loadedAtMs >= ttl) {
                scheduleRefresh();
            }

            return buildStatusFromHolder(holder);
        } catch (RuntimeException e) {
            log.error("file-open status build failed: {}", e.getClass().getName());
            return buildResult("ERROR", "조회 중 오류가 발생했습니다", null, 0L, noFiles);
        }
    }

    // 응답 시점의 현재 시각 기준으로 age/status 를 계산(기존 판정 로직 재사용)
    private Map<String, Object> buildStatusFromHolder(SnapshotHolder holder) {
        List<Map<String, Object>> noFiles = Collections.emptyList();

        if (!holder.isSuccess()) {
            return buildResult("ERROR", holder.failureMessage, null, 0L, noFiles);
        }

        FileOpenSnapshot snapshot = holder.snapshot;
        String collectedAtUtc = snapshot.getCollectedAtUtc();

        if (Boolean.FALSE.equals(snapshot.getSuccess())) {
            if (shouldLog(lastCollectorErrorLogMs)) {
                log.warn("file-open snapshot reports collector error: {}", maskPath(snapshot.getError()));
            }
            return buildResult("ERROR", "수집기 오류", collectedAtUtc, 0L, noFiles);
        }

        Instant collected = parseCollectedAt(collectedAtUtc);
        if (collected == null) {
            return buildResult("ERROR", "시각 파싱 오류", collectedAtUtc, 0L, noFiles);
        }

        long age = Duration.between(collected, Instant.now()).getSeconds();
        if (age < CLOCK_SKEW_TOLERANCE_SEC) {
            return buildResult("ERROR", "시각 불일치", collectedAtUtc, age, noFiles);
        }

        List<Map<String, Object>> files = toFiles(snapshot.getFiles());
        if (age > maxAgeSeconds) {
            return buildResult("STALE", "마지막 수집 후 " + age + "초 경과", collectedAtUtc, age, files);
        }
        return buildResult("OK", "정상", collectedAtUtc, age, files);
    }

    // 캐시 갱신 예약: CAS 로 single-flight 보장 후 executor 제출.
    // 종료 등으로 제출이 거부되면 플래그를 되돌려 영구 true 로 굳는 것을 방지
    private void scheduleRefresh() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        try {
            readerExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        cache = loadSnapshot();
                    } catch (RuntimeException e) {
                        // 예기치 못한 실패에도 엔트리를 반드시 남겨 첫 호출 메시지가 영구 고착되지 않게 함
                        if (shouldLogRead(e.getClass().getName())) {
                            log.warn("file-open snapshot load failed: {} {}",
                                    e.getClass().getName(), maskPath(e.getMessage()));
                        }
                        cache = SnapshotHolder.failure("스냅샷을 읽을 수 없습니다");
                    } finally {
                        refreshing.set(false);
                    }
                }
            });
        } catch (RejectedExecutionException ree) {
            refreshing.set(false);
        }
    }

    // executor 스레드 안에서만 호출됨(요청 스레드는 블로킹하지 않음)
    private SnapshotHolder loadSnapshot() {
        Path path;
        try {
            path = Paths.get(snapshotPath);
        } catch (InvalidPathException e) {
            if (shouldLogRead(e.getClass().getName())) {
                log.warn("file-open snapshot path invalid: {} {}",
                        e.getClass().getName(), maskPath(e.getMessage()));
            }
            return SnapshotHolder.failure("스냅샷을 읽을 수 없습니다");
        }

        // 폴더면 영구 오류(재시도 없음)
        if (Files.isDirectory(path)) {
            if (shouldLogRead("isDirectory")) {
                log.warn("file-open snapshot path is a directory: <snapshot-path>");
            }
            return SnapshotHolder.failure("스냅샷 경로가 폴더입니다. 파일명까지 지정하십시오");
        }

        for (int attempt = 1; attempt <= MAX_READ_ATTEMPTS; attempt++) {
            try {
                byte[] bytes = Files.readAllBytes(path);
                FileOpenSnapshot snapshot = objectMapper.readValue(bytes, FileOpenSnapshot.class);
                if (snapshot != null) {
                    return SnapshotHolder.success(snapshot);
                }
                // 빈 JSON(null) 은 일시 오류로 보고 재시도
                if (shouldLogRead("empty")) {
                    log.warn("file-open snapshot is empty");
                }
            } catch (NoSuchFileException e) {
                // 영구 오류 — 재시도 안 함
                if (shouldLogRead(e.getClass().getName())) {
                    log.warn("file-open snapshot read failed: {} {}",
                            e.getClass().getName(), maskPath(e.getMessage()));
                }
                return SnapshotHolder.failure("스냅샷 파일을 찾을 수 없습니다");
            } catch (AccessDeniedException e) {
                // 영구 오류 — 재시도 안 함
                if (shouldLogRead(e.getClass().getName())) {
                    log.warn("file-open snapshot read failed: {} {}",
                            e.getClass().getName(), maskPath(e.getMessage()));
                }
                return SnapshotHolder.failure("스냅샷 읽기 권한이 없습니다");
            } catch (IOException | RuntimeException e) {
                // 그 밖의 IOException(공유 위반 등) 및 JSON 파싱 실패는 일시 오류 → 재시도(기존 동작 유지)
                if (shouldLogRead(e.getClass().getName())) {
                    log.warn("file-open snapshot read failed: {} {}",
                            e.getClass().getName(), maskPath(e.getMessage()));
                }
            }
            if (attempt < MAX_READ_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return SnapshotHolder.failure("스냅샷을 읽을 수 없습니다");
                }
            }
        }
        return SnapshotHolder.failure("스냅샷을 읽을 수 없습니다");
    }

    // 리스 유지용 요청 파일 touch(비동기, single-flight). 실패해도 예외를 밖으로 던지지 않음
    private void touchRequestFile() {
        if (requestPath == null || requestPath.trim().isEmpty()) {
            return; // 비어 있으면 기능 OFF
        }
        long now = System.currentTimeMillis();
        if (now - lastTouchAtMs < TOUCH_MIN_INTERVAL_MS) {
            return;
        }
        if (!touching.compareAndSet(false, true)) {
            return; // 이전 touch 진행 중이면 건너뜀
        }
        // 성공/실패와 무관하게 2초 간격 cadence 를 유지(느린/실패 경로 폭주 방지)
        lastTouchAtMs = now;
        try {
            toucherExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doTouch();
                    } finally {
                        touching.set(false);
                    }
                }
            });
        } catch (RejectedExecutionException ree) {
            touching.set(false);
        }
    }

    private void doTouch() {
        try {
            Path p = Paths.get(requestPath);
            // 현재 UTC 시각 문자열로 덮어써 최종수정시각을 갱신(기본 옵션: 없으면 생성, 있으면 truncate)
            Files.write(p, Instant.now().toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            // IOException / InvalidPathException 등 — 요청 처리에 영향을 주지 않도록 삼킴
            if (shouldLogTouch(e.getClass().getName())) {
                log.warn("file-open request touch failed: {} {}",
                        e.getClass().getName(), maskRequestPath(e.getMessage()));
            }
        }
    }

    private Instant parseCollectedAt(String collectedAtUtc) {
        if (collectedAtUtc == null) {
            return null;
        }
        try {
            return Instant.parse(collectedAtUtc);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private List<Map<String, Object>> toFiles(List<FileOpenFile> files) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (FileOpenFile f : files) {
            if (f == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fileId", f.getFileId());
            item.put("sessionId", f.getSessionId());
            item.put("shareName", f.getShareName() == null ? "" : f.getShareName());
            item.put("shareRelativePath", f.getShareRelativePath());
            item.put("fileName", extractFileName(f.getShareRelativePath()));
            item.put("clientComputerName", f.getClientComputerName());
            item.put("clientUserName", f.getClientUserName());
            String entryType = f.getEntryType();
            item.put("entryType", entryType == null ? "" : entryType);
            Boolean isDirectory = f.getIsDirectory();
            if (isDirectory == null) {
                isDirectory = "Directory".equals(entryType);
            }
            item.put("isDirectory", isDirectory);
            result.add(item);
        }
        return result;
    }

    private String extractFileName(String shareRelativePath) {
        if (shareRelativePath == null || shareRelativePath.isEmpty()) {
            return "";
        }
        int idx = Math.max(shareRelativePath.lastIndexOf('\\'), shareRelativePath.lastIndexOf('/'));
        return shareRelativePath.substring(idx + 1);
    }

    private String maskPath(String message) {
        return maskWith(message, snapshotPath, "<snapshot-path>");
    }

    private String maskRequestPath(String message) {
        return maskWith(message, requestPath, "<request-path>");
    }

    // 로그에 내부 경로가 새지 않도록 마스킹. 설정 원본과 정규화(Paths.get().toString()) 형태를 모두 치환
    private String maskWith(String message, String cfg, String placeholder) {
        if (message == null) {
            return "";
        }
        if (cfg == null || cfg.isEmpty()) {
            return message;
        }
        String masked = message.replace(cfg, placeholder);
        try {
            String normalized = Paths.get(cfg).toString();
            if (!normalized.equals(cfg)) {
                masked = masked.replace(normalized, placeholder);
            }
        } catch (RuntimeException e) {
            // 정규화 실패 시 원본 치환만 적용
        }
        return masked;
    }

    // 같은 원인의 반복 로그를 60초에 한 번으로 제한(여러 스레드 대비 CAS). 수집기 오류 전용(단일 원인)
    private boolean shouldLog(AtomicLong lastMs) {
        long now = System.currentTimeMillis();
        long prev = lastMs.get();
        if (now - prev >= LOG_THROTTLE_MS) {
            return lastMs.compareAndSet(prev, now);
        }
        return false;
    }

    // reader 스레드 전용(단일 스레드): 원인(key)이 바뀌었거나 60초 경과 시에만 로그 허용
    private boolean shouldLogRead(String key) {
        long now = System.currentTimeMillis();
        if (!key.equals(lastReadLogKey) || now - lastReadLogAtMs >= LOG_THROTTLE_MS) {
            lastReadLogKey = key;
            lastReadLogAtMs = now;
            return true;
        }
        return false;
    }

    // toucher 스레드 전용(단일 스레드)
    private boolean shouldLogTouch(String key) {
        long now = System.currentTimeMillis();
        if (!key.equals(lastTouchLogKey) || now - lastTouchLogAtMs >= LOG_THROTTLE_MS) {
            lastTouchLogKey = key;
            lastTouchLogAtMs = now;
            return true;
        }
        return false;
    }

    private ExecutorService newDaemonExecutor(final String name) {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            }
        });
    }

    // 스프링 빈 종료 시 데몬 executor 정리
    @PreDestroy
    public void shutdown() {
        readerExecutor.shutdownNow();
        toucherExecutor.shutdownNow();
    }

    private Map<String, Object> buildResult(String status, String message, String collectedAtUtc,
                                            long ageSeconds, List<Map<String, Object>> files) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("message", message);
        result.put("collectedAtUtc", collectedAtUtc);
        result.put("ageSeconds", ageSeconds);
        result.put("count", files.size());
        result.put("files", files);
        return result;
    }

    // ================== 점유해제(열린 핸들 강제 닫기) ==================

    // 현재 로그인 사용자가 점유해제 허용 목록에 있는지(조회 응답용). 실패해도 예외를 밖으로 던지지 않음
    @Override
    public boolean isCloseAuthorizedForCurrentUser() {
        try {
            return isCloseAuthorized(getLoginUserId());
        } catch (RuntimeException e) {
            // 권한 조회 실패는 조회 응답을 깨지 않도록 삼키고 거부(fail-closed)
            return false;
        }
    }

    @Override
    public Map<String, Object> closeOpenFile(Map<String, String> param) {
        String requestId = null;
        String userId = null;
        String fileId = "";
        String sessionId = "";
        String expectedPath = "";
        String expectedUser = "";
        String reason = "";
        int resultCode = 500;
        String resultMessage = "내부 오류가 발생했습니다";
        try {
            // 1) 사용자 ID 는 서버 보안 컨텍스트에서만 획득(클라이언트 값 불신)
            userId = getLoginUserId();
            if (!isCloseAuthorized(userId)) {
                resultCode = 403;
                resultMessage = "점유해제 권한이 없습니다";
                return buildClose(resultCode, resultMessage, null);
            }

            // 2) 입력 검증
            if (param == null) {
                resultCode = 400;
                resultMessage = "요청 파라미터가 없습니다";
                return buildClose(resultCode, resultMessage, null);
            }
            fileId = trimToEmpty(param.get("fileId"));
            sessionId = trimToEmpty(param.get("sessionId"));
            expectedPath = trimToEmpty(param.get("expectedPath"));
            expectedUser = trimToEmpty(param.get("expectedUser"));
            reason = sanitizeReason(param.get("reason"));

            if (!DIGITS_1_20.matcher(fileId).matches() || !DIGITS_1_20.matcher(sessionId).matches()) {
                resultCode = 400;
                resultMessage = "대상 식별자 형식이 올바르지 않습니다";
                return buildClose(resultCode, resultMessage, null);
            }
            if (expectedPath.isEmpty() || expectedPath.length() > CLOSE_FIELD_MAX
                    || expectedUser.isEmpty() || expectedUser.length() > CLOSE_FIELD_MAX) {
                resultCode = 400;
                resultMessage = "대상 정보가 올바르지 않습니다";
                return buildClose(resultCode, resultMessage, null);
            }
            if (reason.isEmpty() || reason.length() > CLOSE_REASON_MAX) {
                resultCode = 400;
                resultMessage = "사유를 1~200자로 입력하십시오";
                return buildClose(resultCode, resultMessage, null);
            }

            // 3) 최근 캐시 스냅샷과 대조(클라이언트가 임의 대상을 보낼 수 없게 함)
            FileOpenFile target = findInSnapshot(fileId, sessionId);
            if (target == null) {
                resultCode = 404;
                resultMessage = "대상이 최근 목록에 없거나 이미 닫혔습니다";
                return buildClose(resultCode, resultMessage, null);
            }
            if (isDirectoryEntry(target)) {
                resultCode = 400;
                resultMessage = "파일만 점유해제 가능합니다. (폴더는 해제할 수 없습니다)";
                return buildClose(resultCode, resultMessage, null);
            }
            String snapPath = trimToEmpty(target.getShareRelativePath());
            String snapUser = trimToEmpty(target.getClientUserName());
            if (!snapPath.equals(expectedPath) || !snapUser.equals(expectedUser)) {
                resultCode = 409;
                resultMessage = "대상이 변경되었습니다. 새로고침 후 다시 시도하십시오";
                return buildClose(resultCode, resultMessage, null);
            }

            // 4) 신호 파일 폴더 결정: 설정이 비면 스냅샷과 같은 폴더(부모)를 사용
            Path dir = resolveCommandDir();
            if (dir == null) {
                resultCode = 500;
                resultMessage = "점유해제 설정이 되어 있지 않습니다";
                return buildClose(resultCode, resultMessage, null);
            }

            // 5) 단일 신호 파일이므로 1건씩 직렬화(선행 처리 중이면 최대 12초 대기)
            boolean acquired = false;
            try {
                acquired = closeSemaphore.tryAcquire(CLOSE_ACQUIRE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            if (!acquired) {
                resultCode = 503;
                resultMessage = "다른 점유해제 요청을 처리 중입니다. 잠시 후 다시 시도하십시오";
                return buildClose(resultCode, resultMessage, null);
            }
            try {
                requestId = UUID.randomUUID().toString();
                // 요청 파일에는 스냅샷에서 검증된 값(상대경로/클라이언트 사용자)을 기록
                boolean written = writeCommandFile(dir, requestId, fileId, sessionId, snapPath, snapUser, userId, reason);
                if (!written) {
                    resultCode = 500;
                    resultMessage = "점유해제 요청을 전달하지 못했습니다";
                    return buildClose(resultCode, resultMessage, null);
                }
                Map<String, Object> awaited = awaitResult(dir, requestId);
                resultCode = ((Integer) awaited.get("resultCode")).intValue();
                resultMessage = (String) awaited.get("resultMessage");
                return buildClose(resultCode, resultMessage, requestId);
            } finally {
                closeSemaphore.release();
            }
        } catch (RuntimeException e) {
            // 내부 경로가 섞일 수 있는 e.getMessage() 는 로깅/응답에 쓰지 않음
            log.error("file-open close failed: {}", e.getClass().getName());
            resultCode = 500;
            resultMessage = "내부 오류가 발생했습니다";
            return buildClose(resultCode, resultMessage, requestId);
        } finally {
            // 성공/거부/오류 모두 1줄 감사 로그(개행·제어문자 제거, 내부 경로 미기록)
            writeAuditLog(requestId, userId, fileId, sessionId, expectedPath, expectedUser, reason, resultCode, resultMessage);
        }
    }

    // 로그인 사용자 ID(서버 보안 컨텍스트 기준). 휴가(PM07) 근태담당자 판정과 동일 방식
    private String getLoginUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    // 허용 목록 포함 여부(대소문자 무시, trim). fail-closed
    private boolean isCloseAuthorized(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        Set<String> allowed = getCloseAuthSet();
        return allowed.contains(userId.trim().toLowerCase(Locale.ROOT));
    }

    // 허용자 집합을 30초 캐시. 설정 공통코드 ID 가 비면 빈 집합(모두 거부)
    private Set<String> getCloseAuthSet() {
        if (closeAuthCodeId == null || closeAuthCodeId.trim().isEmpty()) {
            return Collections.emptySet();
        }
        long now = System.currentTimeMillis();
        Set<String> cached = closeAuthCache;
        if (cached != null && now - closeAuthCacheAtMs < CLOSE_AUTH_CACHE_TTL_MS) {
            return cached;
        }
        Set<String> loaded = loadCloseAuthSet();
        closeAuthCache = loaded;
        closeAuthCacheAtMs = now;
        return loaded;
    }

    // 기존 CM05 하위코드 조회를 재사용해 childCodeList[0].codeEtc 의 쉼표 구분 사용자ID 를 집합으로 로드
    private Set<String> loadCloseAuthSet() {
        Set<String> result = new HashSet<>();
        try {
            Map<String, String> codeParam = new HashMap<>();
            codeParam.put("codeId", closeAuthCodeId.trim());
            List<Map<String, String>> childCodeList = cm05Svc.selectChildCodeList(codeParam);
            if (childCodeList != null && !childCodeList.isEmpty()) {
                // 공유 쿼리는 CODE_ID 접두 일치(LIKE 'SPECRTS20%')이므로, 지정한 ID 와 정확히 같은 행만 사용
                String wantedCodeId = closeAuthCodeId.trim();
                Map<String, String> first = null;
                for (int r = 0; r < childCodeList.size(); r++) {
                    Map<String, String> row = childCodeList.get(r);
                    if (row != null && row.get("codeId") != null
                            && wantedCodeId.equalsIgnoreCase(row.get("codeId").trim())) {
                        first = row;
                        break;
                    }
                }
                String codeEtc = (first == null) ? null : first.get("codeEtc");
                if (codeEtc != null) {
                    String[] ids = codeEtc.split(",");
                    for (int i = 0; i < ids.length; i++) {
                        String id = (ids[i] == null) ? "" : ids[i].trim().toLowerCase(Locale.ROOT);
                        if (!id.isEmpty()) {
                            result.add(id);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            // 조회 실패 시 빈 집합(fail-closed). 원인은 클래스명만 로깅
            log.warn("file-open close-auth load failed: {}", e.getClass().getName());
            return Collections.emptySet();
        }
        return result;
    }

    // 마지막 성공 캐시 스냅샷에서 fileId+sessionId 로 항목을 찾음(없으면 null)
    private FileOpenFile findInSnapshot(String fileId, String sessionId) {
        SnapshotHolder holder = cache;
        if (holder == null || !holder.isSuccess()) {
            return null;
        }
        FileOpenSnapshot snapshot = holder.snapshot;
        if (snapshot == null || Boolean.FALSE.equals(snapshot.getSuccess())) {
            return null;
        }
        List<FileOpenFile> files = snapshot.getFiles();
        if (files == null) {
            return null;
        }
        for (FileOpenFile f : files) {
            if (f == null) {
                continue;
            }
            if (fileId.equals(f.getFileId()) && sessionId.equals(f.getSessionId())) {
                return f;
            }
        }
        return null;
    }

    // 폴더 핸들 판정(toFiles 와 동일 규칙): isDirectory 가 null 이면 entryType 으로 보강
    private boolean isDirectoryEntry(FileOpenFile f) {
        Boolean isDirectory = f.getIsDirectory();
        if (isDirectory == null) {
            isDirectory = Boolean.valueOf("Directory".equals(f.getEntryType()));
        }
        return Boolean.TRUE.equals(isDirectory);
    }

    // 신호 파일을 둘 폴더. command-dir 가 명시돼 있으면 그 경로(예외용),
    // 비어 있으면 request-path 와 같은 폴더(수집기 작업 폴더), 그것도 없으면 스냅샷 파일의 부모 폴더
    private Path resolveCommandDir() {
        try {
            if (commandDir != null && !commandDir.trim().isEmpty()) {
                return Paths.get(commandDir.trim());
            }
            if (requestPath != null && !requestPath.trim().isEmpty()) {
                Path requestParent = Paths.get(requestPath.trim()).getParent();
                if (requestParent != null) {
                    return requestParent;
                }
            }
            if (snapshotPath != null && !snapshotPath.trim().isEmpty()) {
                return Paths.get(snapshotPath.trim()).getParent();
            }
        } catch (RuntimeException e) {
            // 경로 파싱 실패 → 설정 미비로 취급
            return null;
        }
        return null;
    }

    // close_request.json 에 UTF-8(BOM 없음) JSON 을 직접 덮어쓰기(임시파일/rename 없음).
    // 파일이 있으면 TRUNCATE, 없으면 CREATE. 실패 시 false(내부 경로는 로깅하지 않음)
    private boolean writeCommandFile(Path dir, String requestId, String fileId, String sessionId,
                                     String relativePath, String clientUser, String requestedBy, String reason) {
        try {
            Path requestFile = dir.resolve(CLOSE_REQUEST_FILE);

            // 키는 PascalCase 고정. fileId/sessionId 는 문자열로 기록(수집기에서 UInt64 로 캐스팅)
            Map<String, Object> command = new LinkedHashMap<>();
            command.put("RequestId", requestId);
            command.put("FileId", fileId);
            command.put("SessionId", sessionId);
            command.put("ExpectedRelativePath", relativePath);
            command.put("ExpectedUser", clientUser);
            command.put("RequestedBy", requestedBy);
            command.put("Reason", reason);
            command.put("RequestedAtUtc", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());

            byte[] bytes = objectMapper.writeValueAsBytes(command);
            Files.write(requestFile, bytes,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException | RuntimeException e) {
            log.warn("file-open close request write failed: {}", e.getClass().getName());
            return false;
        }
    }

    // close_result.json 을 500ms 간격 최대 10초 폴링. 내가 보낸 RequestId 와 같은 결과만 채택(이전 결과 무시).
    // 결과 파일은 수집기 소유이므로 삭제하지 않는다. 타임아웃 504
    private Map<String, Object> awaitResult(Path dir, String requestId) {
        Path result = dir.resolve(CLOSE_RESULT_FILE);
        long deadline = System.currentTimeMillis() + CLOSE_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (Files.exists(result)) {
                    byte[] bytes = Files.readAllBytes(result);
                    JsonNode node = objectMapper.readTree(bytes);
                    if (node != null && requestId.equals(textOf(node, "RequestId"))) {
                        String code = textOf(node, "Code");
                        String message = textOf(node, "Message");
                        int mapped = mapResultCode(code);
                        String msg = (message == null || message.trim().isEmpty())
                                ? defaultMessageForCode(mapped) : message.trim();
                        return buildAwait(mapped, msg);
                    }
                    // RequestId 불일치(이전 결과)거나 아직 작성 중 → 계속 폴링
                }
            } catch (IOException | RuntimeException e) {
                // 일시적 읽기 오류(공유 위반 등)는 계속 폴링
            }
            try {
                Thread.sleep(CLOSE_POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return buildAwait(504, "파일서버 수집기 응답이 없습니다. 잠시 후 다시 확인하십시오");
    }

    private Map<String, Object> buildAwait(int resultCode, String resultMessage) {
        Map<String, Object> m = new HashMap<>();
        m.put("resultCode", Integer.valueOf(resultCode));
        m.put("resultMessage", resultMessage);
        return m;
    }

    private String textOf(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    // 수집기 결과 Code → HTTP 유사 응답코드
    private int mapResultCode(String code) {
        if ("CLOSED".equals(code)) {
            return 200;
        }
        if ("NOT_FOUND".equals(code)) {
            return 404;
        }
        if ("MISMATCH".equals(code)) {
            return 409;
        }
        if ("DIRECTORY".equals(code)) {
            return 400;
        }
        if ("EXPIRED".equals(code)) {
            return 504;
        }
        if ("INVALID".equals(code)) {
            return 400;
        }
        // ERROR 및 알 수 없는 코드
        return 500;
    }

    private String defaultMessageForCode(int code) {
        switch (code) {
            case 200:
                return "점유를 해제했습니다";
            case 400:
                return "요청이 올바르지 않습니다";
            case 404:
                return "대상을 찾을 수 없거나 이미 닫혔습니다";
            case 409:
                return "대상이 변경되었습니다";
            case 504:
                return "수집기 응답이 없습니다";
            default:
                return "점유해제 처리 중 오류가 발생했습니다";
        }
    }

    private String trimToEmpty(String s) {
        return (s == null) ? "" : s.trim();
    }

    // 사유: 제어문자(개행 포함) 제거 후 trim
    private String sanitizeReason(String reason) {
        if (reason == null) {
            return "";
        }
        return reason.replaceAll("[\\p{Cntrl}]", "").trim();
    }

    private Map<String, Object> buildClose(int resultCode, String resultMessage, String requestId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("resultCode", Integer.valueOf(resultCode));
        m.put("resultMessage", resultMessage);
        if (requestId != null) {
            m.put("requestId", requestId);
        }
        return m;
    }

    // 감사 로그 1줄. 모든 필드의 개행·제어문자 제거, 내부 Path·UNC 경로는 기록하지 않음
    private void writeAuditLog(String requestId, String userId, String fileId, String sessionId,
                               String expectedPath, String expectedUser, String reason,
                               int resultCode, String resultMessage) {
        try {
            log.info("file-open close audit: reqId={}, by={}, fileId={}, sessionId={}, path={}, user={}, reason={}, result={}/{}",
                    logSafe(requestId), logSafe(userId), logSafe(fileId), logSafe(sessionId),
                    logSafe(expectedPath), logSafe(expectedUser), logSafe(reason),
                    resultCode, logSafe(resultMessage));
        } catch (RuntimeException ignore) {
            // 감사 로깅 실패가 요청 처리에 영향을 주지 않도록 함
        }
    }

    // 로그 인젝션 방지: 개행 등 제어문자를 공백으로 치환하고 길이를 제한(로그 비대화 방지)
    private String logSafe(String s) {
        if (s == null) {
            return "";
        }
        String cleaned = s.replaceAll("[\\p{Cntrl}\\u0085\\u2028\\u2029]", " ");
        return (cleaned.length() > LOG_FIELD_MAX) ? cleaned.substring(0, LOG_FIELD_MAX) : cleaned;
    }

    // 마지막 스냅샷 결과를 담는 불변 홀더. 성공이면 snapshot, 실패면 failureMessage 만 채워짐
    private static final class SnapshotHolder {
        private final FileOpenSnapshot snapshot;
        private final String failureMessage;
        private final long loadedAtMs;

        private SnapshotHolder(FileOpenSnapshot snapshot, String failureMessage, long loadedAtMs) {
            this.snapshot = snapshot;
            this.failureMessage = failureMessage;
            this.loadedAtMs = loadedAtMs;
        }

        static SnapshotHolder success(FileOpenSnapshot snapshot) {
            return new SnapshotHolder(snapshot, null, System.currentTimeMillis());
        }

        static SnapshotHolder failure(String message) {
            return new SnapshotHolder(null, message, System.currentTimeMillis());
        }

        boolean isSuccess() {
            return failureMessage == null;
        }
    }
}
