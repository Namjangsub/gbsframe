package com.dksys.biz.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.dksys.biz.admin.cm.cm06.service.CM06Svc;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper om = new ObjectMapper();

    
    @Autowired
    CM06Svc cm06Svc;

    @Override
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
        res.setContentType("application/json;charset=UTF-8");

        

        Map<String,Object> body = new LinkedHashMap<String,Object>();
        body.put("error", "INVALID_CREDENTIALS");
        body.put("message", ex.getMessage());

        if (ex instanceof LoginFailureException) {
            com.dksys.biz.main.vo.User user =
                ((LoginFailureException) ex).getUser();

            if ("N".equals(user.getUseYn())) {
                body.put("msg", "비활성화된 계정입니다 ...");
            } else {
                body.put("msg", "비밀번호를 확인해주세요.");
                Map<String,String> param = new HashMap<String,String>();
                param.put("isPwErr","Y"); 
                param.put("userId", user.getId());
                body.put("usrInfo", cm06Svc.updatePwErrCnt(param));
            }
        } else {
            body.put("msg","인증 실패");
        }
    	
        
        res.getWriter().write(om.writeValueAsString(body));
    }
}