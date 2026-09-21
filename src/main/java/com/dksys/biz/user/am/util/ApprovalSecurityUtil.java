package com.dksys.biz.user.am.util;

import java.util.regex.Pattern;

/**
 * 전자결재 보안 및 민감정보 마스킹 유틸리티
 */
public class ApprovalSecurityUtil {

    // 주민등록번호 패턴 (6자리-7자리)
    private static final Pattern RRN_PATTERN = Pattern.compile("\\b(\\d{6})[- ]?([1-4]\\d{6})\\b");
    // 휴대폰 번호 패턴
    private static final Pattern MOBILE_PATTERN = Pattern.compile("\\b(01[016789])[- ]?(\\d{3,4})[- ]?(\\d{4})\\b");
    // 이메일 패턴
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9_.+-]{1,2})[a-zA-Z0-9_.+-]*(@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
    // JSON 내부 비밀번호 / 토큰 / 시크릿 패턴
    private static final Pattern JSON_SECRET_PATTERN = Pattern.compile("(\"(?:password|token|secret|accessToken|refreshToken|apiKey)\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE);

    private ApprovalSecurityUtil() {
        // 인스턴스화 방지
    }

    /**
     * 텍스트 내의 개인정보 및 민감정보를 정규식 기반으로 마스킹
     *
     * @param text 원본 문자열
     * @return 마스킹된 문자열
     */
    public static String maskSensitiveData(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String masked = text;
        // 1. 주민등록번호 마스킹 (앞 6자리만 남기고 뒷자리 마스킹)
        masked = RRN_PATTERN.matcher(masked).replaceAll("$1-*******");
        // 2. 휴대폰 번호 마스킹 (가운데 자리 마스킹)
        masked = MOBILE_PATTERN.matcher(masked).replaceAll("$1-****-$3");
        // 3. 이메일 마스킹 (앞 2글자 제외 마스킹)
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("$1***$2");
        // 4. JSON 비밀정보 마스킹
        masked = JSON_SECRET_PATTERN.matcher(masked).replaceAll("$1***$2");

        return masked;
    }

    /**
     * IP 주소 마스킹 (IPv4 기준 마지막 옥텟 마스킹: 192.168.1.100 -> 192.168.1.***)
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "UNKNOWN".equalsIgnoreCase(ip)) {
            return "UNKNOWN";
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        return ip;
    }
}
