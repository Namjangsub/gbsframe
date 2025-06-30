package com.dksys.biz.main.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.dksys.biz.main.mapper.LoginMapper;
import com.dksys.biz.main.service.LoginService;
import com.dksys.biz.main.vo.User;

@Service
@SuppressWarnings("all")
public class LoginServiceImpl implements LoginService {

	@Autowired
	LoginMapper loginMapper;

	@Autowired
	private Environment env;

	@Value("${kakaoSend:false}") // KAKAO-SEND 키가 없으면 false 사용
	private String kakaoSend;

	@Override
	public User selectUserInfo(Map<String, String> param) {

		// DB에서 사용자 정보 조회
		User user = loginMapper.selectUserInfo(param);
		if (user != null) {

			String[] profiles = env.getActiveProfiles();
			String profile = (profiles != null && profiles.length > 0) ? profiles[0] : "default";

			// 조회된 VO 객체에 서버 정보 추가로 세팅
			user.setServerType(profile);
			user.setKakaoSend(kakaoSend); // 예: 시스템 환경에서 설정한 값
		}
		return user;
	}

	@Override
	public int insertUserHistory(User user) {
		return loginMapper.insertUserHistory(user);
	}

	@Override
	public int updateLastLoginTime(String username, String refreshToken, String userAgent, String ipAddress) {
		return loginMapper.updateLastLoginTime(username, refreshToken, userAgent, ipAddress);
	}

	@Override
	public String isLoginWithin24Hours(String username, String userAgent, String ipAddress) {
		return loginMapper.isLoginWithin24Hours(username, userAgent, ipAddress);
	}

	@Override
	public String getRefreshToken(String username) {
		return loginMapper.getRefreshToken(username);
	}
}