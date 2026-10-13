package com.dksys.biz.admin.cm.cm40.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final long CACHE_TTL_MS = 3000L;
    private static final int MAX_READ_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 100L;
    private static final long CLOCK_SKEW_TOLERANCE_SEC = -5L;

    @Value("${file-open.snapshot-path:}")
    private String snapshotPath;

    @Value("${file-open.max-age-seconds:30}")
    private long maxAgeSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FileOpenSnapshot cachedSnapshot;
    private long cachedAtMs;

    @Override
    public synchronized Map<String, Object> getFileOpenStatus() {
        try {
            return buildStatus();
        } catch (RuntimeException e) {
            log.error("file-open status build failed: {}", e.getClass().getName());
            return buildResult("ERROR", "조회 중 오류가 발생했습니다", null, 0L, Collections.<Map<String, Object>>emptyList());
        }
    }

    private Map<String, Object> buildStatus() {
        List<Map<String, Object>> noFiles = Collections.emptyList();

        if (snapshotPath == null || snapshotPath.trim().isEmpty()) {
            return buildResult("ERROR", "스냅샷 경로가 설정되지 않았습니다", null, 0L, noFiles);
        }

        long now = System.currentTimeMillis();
        if (cachedAtMs == 0L || now - cachedAtMs >= CACHE_TTL_MS) {
            cachedSnapshot = readSnapshot();
            cachedAtMs = System.currentTimeMillis();
        }

        FileOpenSnapshot snapshot = cachedSnapshot;
        if (snapshot == null) {
            return buildResult("ERROR", "스냅샷을 읽을 수 없습니다", null, 0L, noFiles);
        }

        String collectedAtUtc = snapshot.getCollectedAtUtc();

        if (Boolean.FALSE.equals(snapshot.getSuccess())) {
            log.warn("file-open snapshot reports collector error: {}", maskPath(snapshot.getError()));
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

    private FileOpenSnapshot readSnapshot() {
        for (int attempt = 1; attempt <= MAX_READ_ATTEMPTS; attempt++) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(snapshotPath));
                FileOpenSnapshot snapshot = objectMapper.readValue(bytes, FileOpenSnapshot.class);
                if (snapshot != null) {
                    return snapshot;
                }
                log.warn("file-open snapshot is empty (attempt {}/{})", attempt, MAX_READ_ATTEMPTS);
            } catch (IOException | RuntimeException e) {
                log.warn("file-open snapshot read failed (attempt {}/{}): {} {}",
                        attempt, MAX_READ_ATTEMPTS, e.getClass().getName(), maskPath(e.getMessage()));
            }
            if (attempt < MAX_READ_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
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
        if (message == null) {
            return "";
        }
        if (snapshotPath == null || snapshotPath.isEmpty()) {
            return message;
        }
        return message.replace(snapshotPath, "<snapshot-path>");
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
}
