package com.dksys.biz.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행되도록 설정
public class RefreshTokenInjectFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

//		if (isRefreshTokenRequestWithoutParam(request)) {
//			String refreshToken = extractRefreshTokenFromCookie(request);// UUID
//
//			if (refreshToken != null) {
//				HttpServletRequest wrappedRequest = wrapRequestWithRefreshToken(request, refreshToken);
//				filterChain.doFilter(wrappedRequest, response);
//				return;
//			}
//		}
		if ("/oauth/token".equals(request.getRequestURI()) && "refresh_token".equals(request.getParameter("grant_type"))
				&& request.getParameter("refresh_token") == null) {

			// 1) 쿠키에서 리프레시 토큰 값 꺼내기
			String candidate = extractRefreshTokenFromCookie(request.getCookies());

			// 2) JWT 형식인지 간단 검증 (dot이 2개 있어야 header.payload.signature)
			if (candidate != null && candidate.chars().filter(ch -> ch == '.').count() == 2) {
				// 3) 파라미터로 주입하기 위해 래핑
				HttpServletRequest wrappedRequest = new OAuth2RefreshTokenRequestWrapper(request, candidate);

				// 래핑된 요청으로 체인 계속 실행 후 리턴
				filterChain.doFilter(wrappedRequest, response);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String extractRefreshTokenFromCookie(Cookie[] cookies) {
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if ("refresh_token".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	// 간단한 HttpServletRequestWrapper 구현 (파라미터 주입용)
	public class OAuth2RefreshTokenRequestWrapper extends HttpServletRequestWrapper {
		private final String refreshToken;

		public OAuth2RefreshTokenRequestWrapper(HttpServletRequest request, String refreshToken) {
			super(request);
			this.refreshToken = refreshToken;
		}

		@Override
		public String getParameter(String name) {
			if ("refresh_token".equals(name)) {
				return refreshToken;
			}
			return super.getParameter(name);
		}

		@Override
		public String[] getParameterValues(String name) {
			if ("refresh_token".equals(name)) {
				return new String[] { refreshToken };
			}
			return super.getParameterValues(name);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			Map<String, String[]> map = new HashMap<>(super.getParameterMap());
			map.put("refresh_token", new String[] { refreshToken });
			return Collections.unmodifiableMap(map);
		}
	}

	private boolean isRefreshTokenRequestWithoutParam(HttpServletRequest request) {
		return "POST".equalsIgnoreCase(request.getMethod()) && "/oauth/token".equals(request.getRequestURI())
				&& Objects.equals("refresh_token", request.getParameter("grant_type")) && request.getParameter("refresh_token") == null;
	}

	private String extractRefreshTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			System.out.println("🔴 No cookies present in request");
			return null;
		}

		for (Cookie cookie : cookies) {
			System.out.println("🔎 Cookie: " + cookie.getName() + "=" + cookie.getValue());
		}

		return Arrays.stream(cookies).filter(c -> "refresh_token".equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
	}

	private HttpServletRequest wrapRequestWithRefreshToken(HttpServletRequest request, String refreshToken) {
		return new HttpServletRequestWrapper(request) {

			@Override
			public String getParameter(String name) {
				if ("refresh_token".equals(name))
					return refreshToken;
				return super.getParameter(name);
			}

			@Override
			public Map<String, String[]> getParameterMap() {
				Map<String, String[]> map = new HashMap<>(super.getParameterMap());
				map.put("refresh_token", new String[] { refreshToken });
				return map;
			}

			@Override
			public Enumeration<String> getParameterNames() {
				List<String> names = Collections.list(super.getParameterNames());
				if (!names.contains("refresh_token"))
					names.add("refresh_token");
				return Collections.enumeration(names);
			}

			@Override
			public String[] getParameterValues(String name) {
				if ("refresh_token".equals(name))
					return new String[] { refreshToken };
				return super.getParameterValues(name);
			}
		};
	}
}
