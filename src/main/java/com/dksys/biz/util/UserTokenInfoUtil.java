package com.dksys.biz.util;

import java.util.HashMap;
import java.util.Map;

import com.dksys.biz.main.vo.User;

public class UserTokenInfoUtil {

	public static Map<String, Object> buildUserInfo(User user) {
		Map<String, Object> info = new HashMap<>();

		info.put("userId", user.getId());
		info.put("userNm", user.getName());
		info.put("empNo", user.getEmpNo());
		info.put("coCd", user.getCoCd());
		info.put("originCoCd", user.getOriginCoCd());
		info.put("mngCoCd", user.getMngCoCd());
		info.put("deptId", user.getDeptId());
		info.put("levelCd", user.getLevelCd());
		info.put("email", user.getEmail());
		info.put("enterDt", user.getEnterDt());
		info.put("authInfo", user.getAuthInfo());
		info.put("userGrade", user.getUserGrade());
		info.put("clntCd", user.getClntCd());
		info.put("teamManager", user.getTeamManager());
		info.put("mngId", user.getMngId());
		info.put("kakaoSend", user.getKakaoSend());
		info.put("serverType", user.getServerType());

//		String[] profiles = env.getActiveProfiles();
//		String profile = (profiles != null && profiles.length > 0) ? profiles[0] : "default";
//		info.put("serverType", profile);

		return info;
	}
}