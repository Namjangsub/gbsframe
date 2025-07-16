package com.dksys.biz.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {
	private RequestUtils() {
		// 생성자 막기 (유틸리티 클래스이므로)
	}

	public static void clearCookie(String name, HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
            .path("/")
            .httpOnly(true)
            .secure(true)
            .maxAge(0)
            .sameSite("None")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
	
	public static String getUserAgent() {
		HttpServletRequest request = getCurrentHttpRequest();
		if (request != null) {
			String ua = request.getHeader("User-Agent");
			return (ua != null && ua.length() > 255) ? ua.substring(0, 255) : ua;
		}
		return "UNKNOWN";
	}


	private static final Pattern MOBILE_INDICATOR = Pattern.compile(
		    "(?i)" +               // 대소문자 무시
		    "android|webos|iphone|ipad|ipod|blackberry|iemobile|blackberry|opera mini|mobile"
		);
    public static String detectDeviceType(String ua) {
        if (ua == null) {
            return "DESKTOP";          // UA 없으면 데스크톱으로 간주
        }
		return MOBILE_INDICATOR.matcher(ua).find()
		           ? "MOBILE"
		           : "DESKTOP";
    }
    	
	public static String getClientIpOld() {
		HttpServletRequest request = getCurrentHttpRequest();
		if (request != null) {
			return "UNKNOWN";
		}
		// 프록시를 통한 실제 IP 확인 (우선순위 순)
		// 게이트웨이를 거치는 경우, 클라이언트의 실제 IP는 보통 X-Forwarded-For 헤더에
		// X-Forwarded-For: client1, proxy1, proxy2
		String[] headers = { "X-Forwarded-For", "X-Real-IP", "X-Forwarded", "X-Cluster-Client-IP", "CF-Connecting-IP" // Cloudflare
		};

		for (String header : headers) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				// X-Forwarded-For 헤더의 경우, 가장 첫 번째 IP가 실제 클라이언트 IP
                if ("X-Forwarded-For".equalsIgnoreCase(header)) {
                    return ip.split(",")[0].trim();
                }
                return ip.trim();
			}
		}
		return request.getRemoteAddr();
	}

    public static String getClientIp() {
		HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return "UNKNOWN";
        }

        // 1. X-Forwarded-For (XFF) 헤더: 가장 표준적이며 널리 사용됨
        // X-Forwarded-For: <client>, <proxy1>, <proxy2>
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader != null && !xffHeader.isEmpty() && !"unknown".equalsIgnoreCase(xffHeader)) {
            // 헤더 값에 여러 IP가 쉼표로 구분되어 있는 경우, 가장 첫 번째 IP가 실제 클라이언트 IP
            return xffHeader.split(",")[0].trim();
        }

        // 2. 다른 일반적인 프록시 관련 헤더들 (신뢰도 순)
        String[] otherHeaders = {
            "Proxy-Client-IP",      // Apache 웹 서버 모듈 (mod_proxy)
            "WL-Proxy-Client-IP",   // WebLogic 서버
            "Forwarded",			// 표준화 RFC 7239
            "X-Real-IP",            // Nginx 프록시
            "HTTP_CLIENT_IP",       // 일부 프록시
            "X-Cluster-Client-IP",	// 서버 클러스터 환경
            "HTTP_X_FORWARDED_FOR", // 일부 오래된 프레임워크나 설정
            "HTTP_X_FORWARDED",		//일부 구형 환경/변형
            "HTTP_X_CLUSTER_CLIENT_IP", //클러스터 환경
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"           // 마지막 대체 수단
        };

        for (String header : otherHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.trim();
            }
        }

        // 3. 모든 헤더에 IP가 없는 경우, 최종적으로 getRemoteAddr() 사용
        // 이것은 직접 연결되었거나, 마지막 프록시의 IP일 수 있음
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        
        return "UNKNOWN";
    }

	// 브라우저/디바이스 정보 ex) https://ai.gunyangitt.co.kr/static/index.html
	public static String getReferer() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader("Referer") : "UNKNOWN";
	}

	// 요청 메소드 ex) POST
	public static String getMethod() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getMethod() : "UNKNOWN";
	}

	// 전체 요청 URL ex) https://ai.gunyangitt.co.kr/oauth/token
	public static String getRequestUrl() {
		HttpServletRequest request = getCurrentHttpRequest();
		if (request != null) {
			StringBuffer url = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null) {
				url.append("?").append(queryString);
			}
			return url.toString();
		}
		return "UNKNOWN";
	}

	// 요청 프로토콜 및 버전 ex) HTTP/1.1
	public static String getProtocol() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getProtocol() : "UNKNOWN";
	}

	// 클라이언트 포트
	public static int getClientPort() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getRemotePort() : -1;
	}

	// 서버 정보 ex) ai.gunyangitt.co.kr:443
	public static String getServerInfo() {
		HttpServletRequest request = getCurrentHttpRequest();
		if (request != null) {
			return request.getServerName() + ":" + request.getServerPort();
		}
		return "UNKNOWN";
	}

	// Accept 헤더 (클라이언트가 수용할 수 있는 콘텐츠 타입) == */*
	public static String getAcceptHeader() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader("Accept") : "UNKNOWN";
	}

	// Accept-Language (클라이언트 언어 설정) == ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
	public static String getAcceptLanguage() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader("Accept-Language") : "UNKNOWN";
	}

	// Accept-Encoding (클라이언트가 지원하는 인코딩) == gzip, deflate, br, zstd
	public static String getAcceptEncoding() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader("Accept-Encoding") : "UNKNOWN";
	}

	// 세션 ID
	public static String getSessionId() {
		HttpServletRequest request = getCurrentHttpRequest();
		if (request != null && request.getSession(false) != null) {
			return request.getSession().getId();
		}
		return "NO_SESSION";
	}

	// 요청 스키마 (HTTP/HTTPS)
	public static String getScheme() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getScheme() : "UNKNOWN";
	}

	// HTTPS 여부 ( true )
	public static boolean isSecure() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null && request.isSecure();
	}

	// 컨텐츠 타입 ( application/x-www-form-urlencoded; charset=UTF-8 )
	public static String getContentType() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getContentType() : "UNKNOWN";
	}

	// 컨텐츠 길이
	public static long getContentLength() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getContentLengthLong() : -1;
	}

	// 호스트 헤더
	public static String getHostHeader() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader("Host") : "UNKNOWN";
	}

	// 커스텀 헤더 값 가져오기
	public static String getHeader(String headerName) {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getHeader(headerName) : null;
	}

	// 모든 헤더 정보 맵으로 반환
	public static Map<String, String> getAllHeaders() {
		HttpServletRequest request = getCurrentHttpRequest();
		Map<String, String> headers = new HashMap<>();

		if (request != null) {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				headers.put(headerName, request.getHeader(headerName));
			}
		}
		return headers;
	}

	// 디바이스 타입 추정 (간단한 로직) (DESKTOP)
	public static String getDeviceType() {
		String userAgent = getUserAgent().toLowerCase();
		if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
			return "MOBILE";
		} else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
			return "TABLET";
		}
		return "DESKTOP";
	}

	// 요청 시간 (밀리초)
	public static long getRequestTime() {
		return System.currentTimeMillis();
	}

	// 요청한 경로 정보
	public static String getRequestPath() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getRequestURI() : "UNKNOWN";
	}

	// 쿼리 스트링
	public static String getQueryString() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getQueryString() : null;
	}

	// 인증 타입
	public static String getAuthType() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getAuthType() : "NONE";
	}

	// 로그인 사용자 정보
	public static String getRemoteUser() {
		HttpServletRequest request = getCurrentHttpRequest();
		return request != null ? request.getRemoteUser() : null;
	}

	private static HttpServletRequest getCurrentHttpRequest() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest();
		}
		return null;
	}
}