//package com.dksys.biz.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.provider.ClientDetailsService;
//import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
//import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
//
//@Configuration
//public class OAuth2RequestFactoryConfig {
//
//    @Bean
//    public OAuth2RequestFactory oAuth2RequestFactory(ClientDetailsService clientDetailsService) {
//        return new DefaultOAuth2RequestFactory(clientDetailsService);
//    }
//}