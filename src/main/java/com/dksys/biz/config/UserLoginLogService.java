package com.dksys.biz.config;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.main.mapper.LoginMapper;
@Service
public class UserLoginLogService {
    
	@Autowired
	LoginMapper loginMapper;

	public void updateLastLoginTime(String username, String refreshToken, String userAgent, String ipAddress) {
//		String hashedToken = TokenHashUtils.sha256(refreshToken);
		loginMapper.updateLastLoginTime(username, refreshToken, userAgent, ipAddress);
	}

	public boolean isLoginWithin24Hours(String username, String userAgent, String ipAddress) {
		try {
			String lastLogin = loginMapper.isLoginWithin24Hours(username, userAgent, ipAddress);

//			Timestamp lastLogin = Timestamp.valueOf("2025-06-20 08:00:00");
			Timestamp lastLoginTimestamp = Timestamp.valueOf(lastLogin);

			long hours = Duration.between(lastLoginTimestamp.toInstant(), Instant.now()).toHours();

			if (hours < 24) {
				System.out.println("24시간 이내 로그인");
				return true;
			} else {
				System.out.println("24시간 초과됨 → 재로그인 필요");
				return false;
			}

		} catch (Exception e) {
			System.err.println("❌ 로그인 시간 조회 실패: " + e.getMessage());
			return false;
		}
	}

	public String getStoredRefreshToken(String username, String userAgent, String ipAddress) {
		return loginMapper.getRefreshToken(username, userAgent, ipAddress);
	}

	// 토큰 비교 시 (해시 저장 및 비교)
	public boolean isRefreshTokenValid(String username, String incomingRefreshToken, String userAgent, String ipAddress) {
		String storedHash = loginMapper.getRefreshToken(username, userAgent, ipAddress);
		return incomingRefreshToken.equals(storedHash);
	}

}