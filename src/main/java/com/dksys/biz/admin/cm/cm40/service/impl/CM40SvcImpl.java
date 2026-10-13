package com.dksys.biz.admin.cm.cm40.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dksys.biz.admin.cm.cm40.model.FileOpenFile;
import com.dksys.biz.admin.cm.cm40.model.FileOpenSnapshot;
import com.dksys.biz.admin.cm.cm40.service.CM40Svc;
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

    @Value("${file-open.snapshot-path:}")
    private String snapshotPath;

    @Value("${file-open.max-age-seconds:30}")
    private long maxAgeSeconds;

    // 리스 유지용 요청 파일 경로. 비어 있으면 요청 파일 갱신 기능을 끈다(오류 아님)
    @Value("${file-open.request-path:}")
    private String requestPath;

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
