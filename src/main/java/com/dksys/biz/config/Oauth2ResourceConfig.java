package com.dksys.biz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableResourceServer
public class Oauth2ResourceConfig extends ResourceServerConfigurerAdapter {

	@Override
    public void configure(ResourceServerSecurityConfigurer resources) {
      
    }

	@Autowired
	private LoginTimeValidationFilter loginTimeValidationFilter;

	// SecurityConfig → 인증 처리 관련 (폼 로그인, 토큰 발급, 로그아웃 등)
	// Oauth2ResourceConfig → 자원 보호 관련 (access_token 인증 후 리소스 접근)
    @Override
    public void configure(HttpSecurity http) throws Exception {
    	http.cors().and().authorizeRequests()
				.antMatchers("/", "/oauth/**", "/login", "/logout", "/customLogout", "/static/**", "/download/**", "/ws/**", "/admin/cm/cm08/**",
						"/s/**")
				.permitAll()
				.anyRequest().authenticated()
				.and().addFilterBefore(loginTimeValidationFilter, UsernamePasswordAuthenticationFilter.class);
    }
	
}