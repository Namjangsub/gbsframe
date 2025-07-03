package com.dksys.biz.config;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class Oauth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

	@Value("${security.jwt.signing-key}")
	private String signingKey;

//	@Autowired
//	private AuthenticationManager authenticationManager;
//
//	@Autowired
//	private UserLoginLogService userLoginLogService;

	@Autowired
	@Qualifier("tiberoDataSource")
	private DataSource dataSource;

	@Autowired
	private PasswordEncoder passwordEncoder;

//	@Autowired
//	private CustomTokenEnhancer customTokenEnhancer;

//	@Autowired
//	private ClientDetailsService clientDetailsService;
//
//	@Autowired
//	private OAuth2RequestFactory requestFactory;

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

//	@Bean
//	public TokenEnhancerChain tokenEnhancerChain() {
//		TokenEnhancerChain chain = new TokenEnhancerChain();
//		chain.setTokenEnhancers(Arrays.asList(customTokenEnhancer, accessTokenConverter()));
//		return chain;
//	}

//	@Bean
//	@Primary
//	/***********************************************
//	 * JWT 토큰 자체의 만료 시간으로 서버가 발급한 토큰이 언제까지 유효한지 설정 서버에서 토큰을 수용할 수 있는 시간으로 초단위임 클라이언트가 쿠키를 보관 중이어도 서버에서 만료된 토큰이면 401 Unauthorized 발생 Spring
//	 * DefaultTokenServices Bean
//	 * 
//	 * @return
//	 */
//	public AuthorizationServerTokenServices tokenServices() {
//		DefaultTokenServices services = new DefaultTokenServices();
//		services.setTokenStore(tokenStore());
//		services.setTokenEnhancer(tokenEnhancerChain());
//		services.setSupportRefreshToken(true);
//		services.setReuseRefreshToken(true);
//		services.setAccessTokenValiditySeconds(120); // 테스트용 2분
//		services.setRefreshTokenValiditySeconds(300); // 24시간
//		return services;
//	}

//	@Bean
//	public CustomRefreshTokenGranter customRefreshTokenGranter() {
//		return new CustomRefreshTokenGranter(tokenServices(), clientDetailsService, requestFactory, userLoginLogService, accessTokenConverter(),
//				signingKey);
//	}

//	@Override
//	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
//		List<TokenGranter> granters = new ArrayList<>(getDefaultTokenGranters(endpoints));
//		granters.add(customRefreshTokenGranter());
//
//		endpoints.authenticationManager(authenticationManager).tokenStore(tokenStore()).tokenEnhancer(tokenEnhancerChain())
//				.accessTokenConverter(accessTokenConverter()).tokenServices(tokenServices()).tokenGranter(new CompositeTokenGranter(granters));
//	}

//	private List<TokenGranter> getDefaultTokenGranters(AuthorizationServerEndpointsConfigurer endpoints) {
//		AuthorizationServerTokenServices tokenServices = tokenServices();
//		AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();
//
//		List<TokenGranter> tokenGranters = new ArrayList<>();
//		tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetailsService, requestFactory));
//		tokenGranters.add(new ImplicitTokenGranter(tokenServices, clientDetailsService, requestFactory));
//		tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices, clientDetailsService, requestFactory));
//		return tokenGranters;
//	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) {
		security.tokenKeyAccess("permitAll()")			// 공개 키 (tokenKey) 접근 허용
				.checkTokenAccess("isAuthenticated()")	// /check_token 엔드포인트 인증 필요
				.allowFormAuthenticationForClients()	// form 기반 client 인증 허용
				.authenticationEntryPoint((req, res, ex) -> {
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.setContentType("application/json;charset=UTF-8");
					res.getWriter().write("{\"error\":\"unauthorized\"}");
				});
	}
}
