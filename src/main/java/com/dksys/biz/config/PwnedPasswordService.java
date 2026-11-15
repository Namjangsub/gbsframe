package com.dksys.biz.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * HIBP Pwned Passwords Range API (k-anonymity) 기반 유출 비밀번호 검사.
 * 권고(advisory) 용도이며, 어떤 예외가 발생해도 0(미검출)을 반환하여 로그인을 막지 않는다.
 */
@Component
public class PwnedPasswordService {

    private static final Logger log = LoggerFactory.getLogger(PwnedPasswordService.class);

    private static final String RANGE_URL = "https://api.pwnedpasswords.com/range/";
    private static final int TIMEOUT_MS = 2000;

    public int pwnedCount(String plain) {
        if (plain == null || plain.isEmpty()) {
            return 0;
        }
        HttpURLConnection conn = null;
        try {
            String hash = sha1Hex(plain);
            String prefix = hash.substring(0, 5);
            String suffix = hash.substring(5);

            conn = (HttpURLConnection) new URL(RANGE_URL + prefix).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Add-Padding", "true");
            conn.setRequestProperty("User-Agent", "pwd-check");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.warn("HIBP check skipped: http status {}", conn.getResponseCode());
                return 0;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.US_ASCII))) {
                String maskedTarget = maskSuffix(suffix);
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    int idx = line.indexOf(':');
                    if (idx < 0) {
                        continue;
                    }
                    lineCount++;
                    String respSuffix = line.substring(0, idx).trim();
                    String respCountStr = line.substring(idx + 1).trim();
                    boolean matched = respSuffix.equalsIgnoreCase(suffix);
                    if (matched) {
                      log.info("HIBP compare [{}]: received=[{}:{}], targetSuffix=[{}], matched={}",
                      lineCount, respSuffix, respCountStr, maskedTarget, matched);
                        int count = Integer.parseInt(respCountStr);
                        return count > 0 ? count : 0;
                    }
                }
                log.info("HIBP check completed: totalLines={}, matched=false", lineCount);
            }
        } catch (Exception e) {
            log.warn("HIBP check skipped: {}", e.getClass().getSimpleName());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return 0;
    }

    public boolean isPwned(String plain) {
        return pwnedCount(plain) > 0;
    }

    private static String maskSuffix(String suffix) {
        if (suffix == null || suffix.length() <= 10) {
            return "****";
        }
        return suffix.substring(0, 6) + "****" + suffix.substring(suffix.length() - 4);
    }

    private static String sha1Hex(String plain) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
