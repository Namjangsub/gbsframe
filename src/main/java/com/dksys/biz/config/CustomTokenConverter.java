package com.dksys.biz.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import com.dksys.biz.main.vo.User;
import com.dksys.biz.util.UserTokenInfoUtil;

@Component
public class CustomTokenConverter extends JwtAccessTokenConverter {

	@Value("${security.jwt.signing-key}")
	private String signingKey;

	@Override
	public Map<String, Object> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		Map<String, Object> claims = new HashMap<>(super.convertAccessToken(token, authentication));

		Object principal = authentication.getPrincipal();
		if (principal instanceof User) {
			User user = (User) principal;
			claims.putAll(UserTokenInfoUtil.buildUserInfo(user)); // 사용자 정보 삽입
		}

		return claims;
	}
}