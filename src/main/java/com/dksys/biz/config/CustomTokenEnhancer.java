//package com.dksys.biz.config;
//
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.token.TokenEnhancer;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import com.dksys.biz.main.service.LoginService;
//import com.dksys.biz.main.vo.User;
//
//@Component
//public class CustomTokenEnhancer implements TokenEnhancer {
//
//	@Autowired
//	private HttpServletResponse response;
//
//	@Value("${security.jwt.signing-key}")
//	private String signingKey;
//
//	@Autowired
//	private UserLoginLogService userLoginLogService;
//
//	@Autowired
//	LoginService loginService;
//
//	@Override
//	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
//		if (!(accessToken instanceof DefaultOAuth2AccessToken)) {
//			return accessToken;
//		}
//
//		DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
//		Map<String, Object> additionalInfo = new HashMap<>();
//
//		// 안전한 principal 추출
//		Object principal = authentication.getUserAuthentication() != null ? authentication.getUserAuthentication().getPrincipal() : null;
//
//		if (principal instanceof User) {
//			User user = (User) principal;
//			fillUserInfo(additionalInfo, user);
//
//		} else if (principal instanceof String) {
//			String userId = (String) principal;
//			Map<String, String> param = new HashMap<>();
//			param.put("id", userId);
//			User user = loginService.selectUserInfo(param);
//			if (user != null) {
//				fillUserInfo(additionalInfo, user);
//			} else {
//				additionalInfo.put("userId", principal.toString());
//			}
//		} else {;
//			throw new IllegalStateException("인증 주체가 User 객체도, String도 아닙니다. principal=" + principal);
//		}
//
//		// 부가 정보 설정
//		token.setAdditionalInformation(additionalInfo);
//
//		// refresh_token 쿠키 설정
//		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		HttpServletResponse response = attr != null ? attr.getResponse() : null;
//
//		if (response != null && token.getRefreshToken() != null) {
//			String refreshTokenValue = token.getRefreshToken().getValue();
////			System.out.println("[Enhancer] 쿠키 저장할 refresh_token = " + refreshTokenValue);
////			addRefreshTokenCookie(response, refreshTokenValue);
//
//			// 로그인 기록 업데이트
//			String grantType = authentication.getOAuth2Request().getGrantType();
//			if ("password".equals(grantType) && principal instanceof User) {
//				User user = (User) principal;
//				String userAgent = RequestUtils.getUserAgent();
//				String clientIp = RequestUtils.getClientIp();
//				userLoginLogService.updateLastLoginTime(user.getId(), refreshTokenValue, userAgent, clientIp);
//			}
//		}
//
//		return token;
//	}
//
//	void fillUserInfo(Map<String, Object> info, User user) {
//		info.put("userId", user.getId());
//		info.put("userNm", user.getName());
//		info.put("empNo", user.getEmpNo());
//		info.put("coCd", user.getCoCd());
//		info.put("mngCoCd", user.getMngCoCd());
//		info.put("deptId", user.getDeptId());
//		info.put("levelCd", user.getLevelCd());
//		info.put("email", user.getEmail());
//		info.put("enterDt", user.getEnterDt());
//		info.put("authInfo", user.getAuthInfo());
//		info.put("serverType", user.getServerType());
//		info.put("kakaoSend", user.getKakaoSend());
//		info.put("userGrade", user.getUserGrade());
//		info.put("clntCd", user.getClntCd());
//	}
//
//}