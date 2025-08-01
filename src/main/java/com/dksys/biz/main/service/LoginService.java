package com.dksys.biz.main.service;

import java.util.Map;

import com.dksys.biz.main.vo.User;

public interface LoginService {

	User selectUserInfo(Map<String, String> param);

	int insertUserHistory(User user);

	int updateLastLoginTime(String username, String refreshToken, String userAgent, String ipAddress, String deviceType);

	int updateLogoutTime(String username, String userAgent, String ipAddress, String deviceType);

	String isLoginWithin24Hours(String username, String userAgent, String ipAddress, String deviceType);

	String getRefreshToken(String username, String userAgent, String ipAddress, String deviceType);

}
