package com.dksys.biz.config;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

public class CustomTokenStoreChain implements TokenStore {

	private final List<TokenStore> stores;

	public CustomTokenStoreChain(List<TokenStore> stores) {
		this.stores = stores;
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		for (TokenStore store : stores) {
			OAuth2AccessToken token = store.getAccessToken(authentication);
			if (token != null)
				return token;
		}
		return null;
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		for (TokenStore store : stores) {
			OAuth2Authentication auth = store.readAuthentication(token);
			if (auth != null)
				return auth;
		}
		return null;
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		for (TokenStore store : stores) {
			OAuth2Authentication auth = store.readAuthentication(token);
			if (auth != null)
				return auth;
		}
		return null;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		for (TokenStore store : stores) {
			store.storeAccessToken(token, authentication);
		}
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		for (TokenStore store : stores) {
			OAuth2AccessToken token = store.readAccessToken(tokenValue);
			if (token != null)
				return token;
		}
		return null;
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		for (TokenStore store : stores) {
			store.removeAccessToken(token);
		}
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		for (TokenStore store : stores) {
			store.storeRefreshToken(refreshToken, authentication);
		}
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		for (TokenStore store : stores) {
			OAuth2RefreshToken rt = store.readRefreshToken(tokenValue);
			if (rt != null)
				return rt;
		}
		return null;
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		for (TokenStore store : stores) {
			OAuth2Authentication auth = store.readAuthenticationForRefreshToken(token);
			if (auth != null)
				return auth;
		}
		return null;
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		for (TokenStore store : stores) {
			store.removeRefreshToken(token);
		}
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		for (TokenStore store : stores) {
			store.removeAccessTokenUsingRefreshToken(refreshToken);
		}
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		List<OAuth2AccessToken> result = new ArrayList<>();
		for (TokenStore store : stores) {
			Collection<OAuth2AccessToken> tokens = store.findTokensByClientId(clientId);
			if (tokens != null)
				result.addAll(tokens);
		}
		return result;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		List<OAuth2AccessToken> result = new ArrayList<>();
		for (TokenStore store : stores) {
			Collection<OAuth2AccessToken> tokens = store.findTokensByClientIdAndUserName(clientId, userName);
			if (tokens != null)
				result.addAll(tokens);
		}
		return result;
	}
}