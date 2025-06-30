package com.dksys.biz.config;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.main.mapper.LoginMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

	public String getStoredRefreshToken(String username) {
		return loginMapper.getRefreshToken(username);
	}

	// 토큰 비교 시 (해시 저장 및 비교)
//	public boolean isRefreshTokenValid(String username, String incomingRefreshToken) {
////		String hashedIncoming = TokenHashUtils.sha256(incomingRefreshToken);
//		String storedHash = loginMapper.getRefreshToken(username);
//		return incomingRefreshToken.equals(storedHash);
//	}


	public boolean isRefreshTokenValid(String username, String incomingRefreshToken) {
		try {
			// 🔐 JWT의 jti 필드 추출 (refresh_token의 UUID 원본)
			Claims claims = Jwts.parser()
				.setSigningKey("biz2020".getBytes()) // JwtAccessTokenConverter의 서명 키와 동일해야 함
				.parseClaimsJws(incomingRefreshToken)
				.getBody();

			String jti = claims.getId(); // JWT의 jti → 원래 UUID 값

			String storedHash = loginMapper.getRefreshToken(username); // DB에 저장된 UUID
			System.out.println("✅ JWT jti(UUID): " + jti);
			System.out.println("✅ DB stored: " + storedHash);

			return jti.equals(storedHash); // ✅ 최종 비교
		} catch (Exception e) {
			System.err.println("❌ refresh_token JWT 파싱 실패: " + e.getMessage());
			return false;
		}
	}
}