//package com.dksys.biz.config;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//
//public class TokenHashUtils {
//	public static String sha256(String value) {
//		try {
//			MessageDigest digest = MessageDigest.getInstance("SHA-256");
//			byte[] hashed = digest.digest(value.getBytes());
//			return Base64.getEncoder().encodeToString(hashed);
//		} catch (NoSuchAlgorithmException e) {
//			throw new RuntimeException("SHA-256 지원 안됨", e);
//		}
//	}
//}