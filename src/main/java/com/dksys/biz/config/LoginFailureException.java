package com.dksys.biz.config;

import org.springframework.security.authentication.BadCredentialsException;

public class LoginFailureException extends BadCredentialsException {

    private final com.dksys.biz.main.vo.User user; // 필요 정보

    public LoginFailureException(String msg, com.dksys.biz.main.vo.User user) {
        super(msg);
        this.user = user;
    }
    public com.dksys.biz.main.vo.User getUser() { return user; }
}
