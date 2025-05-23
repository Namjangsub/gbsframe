package com.dksys.biz.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import com.dksys.biz.main.vo.User;

public class CustomTokenConverter extends JwtAccessTokenConverter {
    
    @Autowired
    private Environment env;
    
	@Value("${kakaoSend:false}") // KAKAO-SEND 키가 없으면 false 사용
	private boolean kakaoSend;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (authentication.getOAuth2Request().getGrantType().equalsIgnoreCase("password")) {
            User user = (User) authentication.getPrincipal();
            final Map<String, Object> additionalInfo = new HashMap<String, Object>();
            //토큰에 넣을 값 세팅
            additionalInfo.put("userId", user.getId());
            additionalInfo.put("userNm", user.getName());
            additionalInfo.put("empNo", user.getEmpNo());
            additionalInfo.put("coCd", user.getCoCd());
            additionalInfo.put("mngCoCd", user.getMngCoCd());
            additionalInfo.put("deptId", user.getDeptId());
            additionalInfo.put("levelCd", user.getLevelCd());
            additionalInfo.put("email", user.getEmail());
            additionalInfo.put("enterDt", user.getEnterDt());
            additionalInfo.put("authInfo", user.getAuthInfo());
            additionalInfo.put("serverType", env.getActiveProfiles()[0]);
			additionalInfo.put("kakaoSend", kakaoSend);
            additionalInfo.put("userGrade", user.getUserGrade());
            additionalInfo.put("clntCd", user.getClntCd());
            
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        }
        accessToken = super.enhance(accessToken, authentication);
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(new HashMap<>());
        return accessToken;
    }
    
}