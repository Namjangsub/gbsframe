package com.dksys.biz.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {

    @Value("${security.jwt.signing-key}")
    private String signingKey;

    public String reissueAccessToken(Claims refreshClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", refreshClaims.get("userId"));
        claims.put("userNm", refreshClaims.get("userNm"));
        claims.put("user_name", refreshClaims.get("user_name"));
        claims.put("roles", refreshClaims.get("roles"));
        claims.put("empNo", refreshClaims.get("empNo"));
        claims.put("coCd", refreshClaims.get("coCd"));
        claims.put("originCoCd", refreshClaims.get("originCoCd"));
        claims.put("mngCoCd", refreshClaims.get("mngCoCd"));
        claims.put("deptId", refreshClaims.get("deptId"));
        claims.put("levelCd", refreshClaims.get("levelCd"));
        claims.put("email", refreshClaims.get("email"));
        claims.put("enterDt", refreshClaims.get("enterDt"));
        claims.put("authInfo", refreshClaims.get("authInfo"));
        claims.put("serverType", refreshClaims.get("serverType"));
        claims.put("kakaoSend", refreshClaims.get("kakaoSend"));
        claims.put("userGrade", refreshClaims.get("userGrade"));
        claims.put("clntCd", refreshClaims.get("clntCd"));
        claims.put("teamManager", refreshClaims.get("teamManager"));
        claims.put("mngId", refreshClaims.get("mngId"));
        claims.put("jti", UUID.randomUUID().toString());
        return createAccessToken(claims);
    }
    

    public String createAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, signingKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public String createRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, signingKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}
