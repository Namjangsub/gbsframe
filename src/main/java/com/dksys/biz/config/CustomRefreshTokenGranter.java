//package com.dksys.biz.config;
//
//import java.nio.charset.StandardCharsets;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
//import org.springframework.security.oauth2.provider.ClientDetails;
//import org.springframework.security.oauth2.provider.ClientDetailsService;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
//import org.springframework.security.oauth2.provider.TokenRequest;
//import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
//import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
//import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//
//public class CustomRefreshTokenGranter extends AbstractTokenGranter {
//
//	private static final String GRANT_TYPE = "refresh_token";
//
//	private final UserLoginLogService userLoginLogService;
//	private final JwtAccessTokenConverter accessTokenConverter;
//	private final String signingKey;
//
//	public CustomRefreshTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService,
//			OAuth2RequestFactory requestFactory, UserLoginLogService userLoginLogService, JwtAccessTokenConverter accessTokenConverter,
//			@Value("${security.jwt.signing-key}") String signingKey) {
//		super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
//		this.userLoginLogService = userLoginLogService;
//		this.accessTokenConverter = accessTokenConverter;
//		this.signingKey = signingKey;
//	}
//
//	@Override
//	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest request) {
//		String jwt = request.getRequestParameters().get(GRANT_TYPE);
//		if (jwt == null) {
//			jwt = extractRefreshTokenFromCookie();
//		}
//		System.out.println("jwt =  " + jwt);
//		if (jwt == null || jwt.chars().filter(ch -> ch == '.').count() != 2) {
//			throw new InvalidGrantException("Invalid refresh token");
//		}
//
//		Claims claims;
//		try {
//			claims = Jwts.parser().setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(jwt).getBody();
//		} catch (Exception e) {
//			throw new InvalidGrantException("Invalid JWT signature");
//		}
//
//		String username = claims.get("user_name", String.class);
//		String userAgent = RequestUtils.getUserAgent();
//		String clientIp = RequestUtils.getClientIp();
//		if (!userLoginLogService.isLoginWithin24Hours(username, userAgent, clientIp)) {
//			throw new InvalidGrantException("Refresh token expired or login timeout");
//		}
//
//		return accessTokenConverter.extractAuthentication(claims);
//	}
//
//	private String extractRefreshTokenFromCookie() {
//		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		if (attrs == null)
//			return null;
//
//		HttpServletRequest req = attrs.getRequest();
//		if (req.getCookies() == null)
//			return null;
//
//		for (Cookie c : req.getCookies()) {
//			if (GRANT_TYPE.equals(c.getName())) {
//				return c.getValue();
//			}
//		}
//		return null;
//	}
//}
