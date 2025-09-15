package com.dksys.biz.config;


import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

        http.cors().and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/audit-ingest").permitAll()
            // 1) 화이트리스트를 **먼저** 선언
            .antMatchers("/", "/oauth/**", "/login", "/customLogout", "/error", "/static/**", "/download/**", "/s/**", "/favicon.ico", "/index.html").permitAll()
            // 2) 그 뒤에 개별 규칙
            .antMatchers("/static/html/cmn/modal/**").authenticated()
            .anyRequest().authenticated()
            .and()
            .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(accessTokenValidationFilter, UsernamePasswordAuthenticationFilter.class);
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
