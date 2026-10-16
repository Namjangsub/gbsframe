package com.dksys.biz.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dksys.biz.admin.cm.cm06.mapper.CM06Mapper;

@Component
public class SenderNameResolver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CM06Mapper cm06Mapper;

	/**
	 * 알림 발신자 표시명 결정: userNm → creatNm → TB_CM06M01 사용자명 조회 → userId / "신청자"
	 * 값이 비었거나 ID와 같으면 이름이 아닌 것으로 보고 다음 후보로 넘어간다.
	 */
	public String resolveSenderNm(String userNm, String creatNm, String userId) {
		String senderNm = userNm;
		if (isNotName(senderNm, userId)) {
			senderNm = creatNm;
		}
		if (isNotName(senderNm, userId) && userId != null) {
			try {
				Map<String, String> userQry = new HashMap<>();
				userQry.put("userId", userId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(userQry);
				if (uInfo != null && uInfo.get("name") != null && !uInfo.get("name").isEmpty()) {
					senderNm = uInfo.get("name");
				}
			} catch (Exception e) {
				logger.warn("발신자 사용자명 조회 실패: userId={}", userId, e);
			}
		}
		if (senderNm == null || senderNm.isEmpty()) {
			return userId != null ? userId : "신청자";
		}
		return senderNm;
	}

	private boolean isNotName(String candidate, String userId) {
		return candidate == null || candidate.isEmpty() || candidate.equals(userId);
	}
}
