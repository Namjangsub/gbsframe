package com.dksys.biz.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class Oauth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

	@Value("${security.jwt.signing-key}")
	private String signingKey;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserLoginLogService userLoginLogService;

	@Autowired
	@Qualifier("tiberoDataSource")
	private DataSource dataSource;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CustomTokenEnhancer customTokenEnhancer;

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Autowired
	private OAuth2RequestFactory requestFactory;

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		DefaultAccessTokenConverter defaultConverter = new DefaultAccessTokenConverter();
		defaultConverter.setIncludeGrantType(true);

		JwtAccessTokenConverter converter = new CustomTokenConverter();
		converter.setSigningKey(signingKey);
		converter.setAccessTokenConverter(defaultConverter);
		return converter;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public TokenEnhancerChain tokenEnhancerChain() {
		TokenEnhancerChain chain = new TokenEnhancerChain();
		chain.setTokenEnhancers(Arrays.asList(customTokenEnhancer, accessTokenConverter()));
		return chain;
	}

	@Bean
	@Primary
	public AuthorizationServerTokenServices tokenServices() {
		DefaultTokenServices services = new DefaultTokenServices();
		services.setTokenStore(tokenStore());
		services.setTokenEnhancer(tokenEnhancerChain());
		services.setSupportRefreshToken(true);
		services.setReuseRefreshToken(true);
		services.setAccessTokenValiditySeconds(120); // 테스트용 2분
		services.setRefreshTokenValiditySeconds(86400); // 24시간
		return services;
	}

	@Bean
	public CustomRefreshTokenGranter customRefreshTokenGranter() {
		return new CustomRefreshTokenGranter(tokenServices(), clientDetailsService, requestFactory, userLoginLogService, accessTokenConverter(),
				signingKey);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		List<TokenGranter> granters = new ArrayList<>(getDefaultTokenGranters(endpoints));
		granters.add(customRefreshTokenGranter());

		endpoints.authenticationManager(authenticationManager).tokenStore(tokenStore()).tokenEnhancer(tokenEnhancerChain())
				.accessTokenConverter(accessTokenConverter()).tokenServices(tokenServices()).tokenGranter(new CompositeTokenGranter(granters));
	}

	private List<TokenGranter> getDefaultTokenGranters(AuthorizationServerEndpointsConfigurer endpoints) {
		AuthorizationServerTokenServices tokenServices = tokenServices();
		AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();

		List<TokenGranter> tokenGranters = new ArrayList<>();
		tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetailsService, requestFactory));
		tokenGranters.add(new ImplicitTokenGranter(tokenServices, clientDetailsService, requestFactory));
		tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices, clientDetailsService, requestFactory));
		return tokenGranters;
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()").allowFormAuthenticationForClients()
				.authenticationEntryPoint((req, res, ex) -> {
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.setContentType("application/json;charset=UTF-8");
					res.getWriter().write("{\"error\":\"unauthorized\"}");
				});
	}
}
