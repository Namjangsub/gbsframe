package com.dksys.biz.main.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.dksys.biz.main.vo.User;

@Mapper
public interface LoginMapper {

	User selectUserInfo(Map<String, String> param);

	int insertUserHistory(User user);

	int updateLastLoginTime(String username, String refreshToken, String userAgent, String ipAddress);

	int updateLogoutTime(String username, String userAgent, String ipAddress);

	String isLoginWithin24Hours(String username, String userAgent, String ipAddress);

	String getRefreshToken(String username, String userAgent, String ipAddress);

}
