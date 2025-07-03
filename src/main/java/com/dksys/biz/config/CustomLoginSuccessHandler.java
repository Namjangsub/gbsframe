package com.dksys.biz.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.dksys.biz.main.vo.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;

	@Autowired
	private UserLoginLogService userLoginLogService;
	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        Object principal = authentication.getPrincipal();
        String username = authentication.getName();

        // accessToken 및 refreshToken 생성
        Map<String, Object> claims = new HashMap<>();

        if (principal instanceof User) {
            User user = (User) principal;
    		fillUserInfo(claims, user);
            claims.put("user_name", user.getUsername()); // 추가 키
            claims.put("roles", user.getAuthInfo());
        } else {
            claims.put("user_name", username);
        }
        
        claims.put("jti", UUID.randomUUID().toString());

        String accessToken = tokenService.createAccessToken(claims);
        String refreshToken = tokenService.createRefreshToken(claims);

        // grant_type 확인
        String grantType = null;
        Object details = authentication.getDetails();
        if (details instanceof String) {
            grantType = (String) details;
        }

        if ("password".equalsIgnoreCase(grantType) && principal instanceof User) {
            User user = (User) principal;
            String userAgent = RequestUtils.getUserAgent();
            String clientIp = RequestUtils.getClientIp();
            userLoginLogService.updateLastLoginTime(user.getId(), refreshToken, userAgent, clientIp);
        }
        
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(60 * 60 * 24) // 24시간
            .sameSite("None")
            .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.setHeader("Authorization", "Bearer " + accessToken);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"msg\":\"success\"}");
    }
    


	void fillUserInfo(Map<String, Object> info, User user) {
		info.put("userId", user.getId());
		info.put("userNm", user.getName());
		info.put("empNo", user.getEmpNo());
		info.put("coCd", user.getCoCd());
		info.put("mngCoCd", user.getMngCoCd());
		info.put("deptId", user.getDeptId());
		info.put("levelCd", user.getLevelCd());
		info.put("email", user.getEmail());
		info.put("enterDt", user.getEnterDt());
		info.put("authInfo", user.getAuthInfo());
		info.put("serverType", user.getServerType());
		info.put("kakaoSend", user.getKakaoSend());
		info.put("userGrade", user.getUserGrade());
		info.put("clntCd", user.getClntCd());
		
	}

}
