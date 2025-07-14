package com.dksys.biz.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dksys.biz.main.service.LoginService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@Component
public class AccessTokenValidationFilter extends OncePerRequestFilter {

    @Value("${security.jwt.signing-key}")
    private String signingKey;

	@Autowired
	private UserLoginLogService userLoginLogService;

    @Autowired
    LoginService loginService;
    
	@Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
        	// 쿠키에서 refresh_token 추출
            String refreshToken = extractRefreshTokenFromCookie(request);

            if (refreshToken != null) {
                try {
                    Claims refreshClaims = parseJwt(refreshToken);
                    String username = refreshClaims.get("user_name", String.class);
                    String userAgent = RequestUtils.getUserAgent();
                    String clientIp = RequestUtils.getClientIp();

                    if (!userLoginLogService.isLoginWithin24Hours(username, userAgent, clientIp)) {
                        loginService.updateLogoutTime(username, userAgent, clientIp);
                        RequestUtils.clearCookie("access_token", response);
                        RequestUtils.clearCookie("refresh_token", response);
                        SecurityContextHolder.clearContext();
                        reject(response, "로그인 24시간 초과");
                        return;
                    }

                    // access_token 재발급
                    String newAccessToken = tokenService.reissueAccessToken(refreshClaims);

                    // SecurityContext 인증 객체 주입
                    Authentication authentication = buildAuthentication(refreshClaims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 응답 헤더에 새 토큰 전달
                    response.setHeader("X-New-Access-Token", newAccessToken);

                    chain.doFilter(request, response);
                    return;
                } catch (JwtException e) {
                    SecurityContextHolder.clearContext();
                    reject(response, "refresh_token 유효성 실패");
                    return;
                }
            } else {
                reject(response, "Authorization 헤더와 Refresh Token 모두 없음");
                return;
            }
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = parseJwt(token);
            // 권한 설정
            String username = (String) claims.get("userId");
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // access token 만료 → refresh 로 재발급
            String refreshToken = extractRefreshToken(request);
            if (refreshToken != null) {
                try {
                    Claims refreshClaims = parseJwt(refreshToken);
                    String username = refreshClaims.get("user_name", String.class);
					String userAgent = RequestUtils.getUserAgent();
					String clientIp = RequestUtils.getClientIp();
					if (!userLoginLogService.isLoginWithin24Hours(username, userAgent, clientIp)) {
						loginService.updateLogoutTime(username, userAgent, clientIp); // 로그아웃 처리

						RequestUtils.clearCookie("access_token", response);
						RequestUtils.clearCookie("refresh_token", response);
						// 4. SecurityContext 강제 초기화 (auth == null 일 수 있어도 상관 없음)
						SecurityContextHolder.clearContext();
						
						reject(response, "로그인 24시간 초과");
						return;
					}

				    // access_token 재발급
                    String newAccessToken = tokenService.reissueAccessToken(refreshClaims);

                    // SecurityContext 인증 객체 주입
                    Authentication authentication = buildAuthentication(refreshClaims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 응답 헤더에 새 토큰 전달
                    response.setHeader("X-New-Access-Token", newAccessToken);
//                    System.out.println("인증 완료 후 context: {}"+ SecurityContextHolder.getContext().getAuthentication());
                    // 반드시 체인 흐름 재개
                    chain.doFilter(request, response);
                } catch (JwtException e) {
                	SecurityContextHolder.clearContext();  // 안전하게 context 비우기
                    reject(response, "refresh_token 유효성 실패");
                }
            } else {
                reject(response, "access_token 만료 + refresh_token 없음");
            }
        } catch (JwtException e) {
            // 기타 오류 (손상된 토큰 등)
            SecurityContextHolder.clearContext();
            reject(response, "access_token 유효성 실패");
        }

    }
    
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    private void reject(HttpServletResponse response, String message) throws IOException {
    	response.reset();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private Authentication buildAuthentication(Claims claims) {
        String username = claims.get("user_name", String.class);
        UserDetails userDetails = new User(username, "", AuthorityUtils.createAuthorityList("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    

	private Claims parseJwt(String token) {
        return Jwts.parser()
            .setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(token)
            .getBody();
	}

}
