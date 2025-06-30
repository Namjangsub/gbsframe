package com.dksys.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.WebClientUtil;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Bean
	public MappingJackson2JsonView jsonView() {
		return new MappingJackson2JsonView();
	}

	@Bean
	public MessageUtils messageUtils() {
		return new MessageUtils();
	}

	@Bean
	public WebClientUtil webClientUtil() {
		return new WebClientUtil();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new DeviceResolverHandlerInterceptor());
    }

	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
				.allowedOrigins("https://ai.gunyangitt.co.kr") // 실운영 도메인만 허용
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*").allowCredentials(true).maxAge(3600);
	}

	// CORS Filter Bean 추가 (글로벌 설정)
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.addAllowedOrigin("https://ai.gunyangitt.co.kr");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.addExposedHeader("Authorization");

		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}