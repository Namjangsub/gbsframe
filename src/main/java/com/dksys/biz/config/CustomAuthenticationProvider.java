package com.dksys.biz.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dksys.biz.main.service.LoginService;
import com.dksys.biz.main.vo.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final LoginService loginService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        
        Map<String, String> map = new HashMap<>();
        map.put("id", username);

        User user = loginService.selectUserInfo(map);

//        System.out.println(">>> 입력 비밀번호: [" + password + "]");
//        System.out.println(">>> DB 비밀번호: [" + user.getPassword() + "]");
//        System.out.println(">>> matches 결과: " + passwordEncoder.matches(password, user.getPassword()));
        // 암호가 다르거나, 사용않는 아이디인경우 접근 오류 처리
        if ((!passwordEncoder.matches(password, user.getPassword())) || ("N".equals(user.getUseYn()))) {
//            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            throw new LoginFailureException("INVALID_CREDENTIALS", user);
            
        }

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}