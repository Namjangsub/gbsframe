package com.dksys.biz.config;


import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccessTokenValidationFilter accessTokenValidationFilter;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler; 
    
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JsonLoginAuthenticationFilter jsonLoginFilter = new JsonLoginAuthenticationFilter(authenticationManager());
        jsonLoginFilter.setAuthenticationSuccessHandler(customLoginSuccessHandler);
//        jsonLoginFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
        jsonLoginFilter.setAuthenticationFailureHandler(customLoginFailureHandler);

        http
            .headers()
            	.frameOptions().sameOrigin()   // pdf.js, iframe 같은 오리진은 허용
            .and()
        		.cors()
        	.and()
            	.csrf().disable()
            	.formLogin().disable()
            	.logout().disable()
            	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            	// 로그인(JSON 로그인) 처리 필터 등록
	            .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
	            // JWT(Access Token)로 SecurityContext 채우는 필터 등록
	            .addFilterBefore(accessTokenValidationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
	            // 로그인 API는 누구나 접근
	            // 1) 화이트리스트를 **먼저** 선언
	            .antMatchers("/", "/oauth/**", "/login", "/customLogout", "/error", "/static/**", "/download/**", "/s/**", "/favicon.ico", "/index.html").permitAll()
	            // 2) 그 뒤에 개별 규칙
	            .antMatchers("/static/html/cmn/modal/**").authenticated()
	            // PDF 변환 API 는 인증 필요
	            .antMatchers(HttpMethod.GET,
	                    "/api/convert/pdf",
	                    "/api/convert/pdf/**"
	            ).authenticated()
            .anyRequest().authenticated();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
