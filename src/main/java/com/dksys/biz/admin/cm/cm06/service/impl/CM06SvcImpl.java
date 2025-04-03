package com.dksys.biz.admin.cm.cm06.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm04.mapper.CM04Mapper;
import com.dksys.biz.admin.cm.cm06.mapper.CM06Mapper;
import com.dksys.biz.admin.cm.cm06.service.CM06Svc;

@Service
@Transactional
public class CM06SvcImpl implements CM06Svc {

    @Autowired
    CM06Mapper cm06Mapper;
    
    @Autowired
    CM04Mapper cm04Mapper;

	@Autowired
	CM06Svc cm06Svc;

    @Override
	public int selectUserCount(Map<String, String> paramMap) {
		return cm06Mapper.selectUserCount(paramMap);
	}
    
	@Override
	public List<Map<String, String>> selectUserList(Map<String, String> paramMap) {
		return cm06Mapper.selectUserList(paramMap);
	}
	
	@Override
	public Map<String, String> selectUserInfo(Map<String, String> paramMap) {
		return cm06Mapper.selectUserInfo(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectUserTree(Map<String, String> paramMap) {
		List<Map<String, String>> deptTree = cm04Mapper.selectDeptTree(paramMap);
		List<Map<String, String>> useTree = cm06Mapper.selectUserTree(paramMap);
		deptTree.addAll(useTree);
		return deptTree;
	}
	
	@Override
	public List<Map<String, String>> selectSignUserTree(Map<String, String> paramMap) {
		List<Map<String, String>> deptTree = cm04Mapper.selectDeptSignTree(paramMap);
		List<Map<String, String>> useSignTree = cm06Mapper.selectSignUserTree(paramMap);
		deptTree.addAll(useSignTree);
		return deptTree;
	}

	@Override
	public void insertUser(Map<String, String> paramMap) throws Exception{
		cm06Mapper.insertUser(paramMap);
		cm06Mapper.insertUserOauth(paramMap);

		// 이미지 업로드를 위한 로직 (타입 변경 및 필요 매개변수 삽입)
		// 이미지 Insert 및 Update에 관한 모듈로 분리
		if (paramMap.containsKey("userImg")) {
			Map<String, Object> imgParam = new HashMap<>();
			imgParam.put("userId", paramMap.get("userId"));
			imgParam.put("userImg", paramMap.get("userImg"));
			
			cm06Svc.updateUserImg(imgParam);
		}
	}
	
	@Override
	public void updateUser(Map<String, String> paramMap) throws Exception {
		cm06Mapper.updateUser(paramMap);

		// 이미지 업로드를 위한 로직 (타입 변경 및 필요 매개변수 삽입)
		// 이미지 Insert 및 Update에 관한 모듈로 분리
		if (paramMap.containsKey("userImg")) {
			Map<String, Object> imgParam = new HashMap<>();
			imgParam.put("userId", paramMap.get("userId"));
			imgParam.put("userImg", paramMap.get("userImg"));
			
			cm06Svc.updateUserImg(imgParam);
		}
	}

	@Override
	public int insertPgmHistory(Map<String, String> paramMap) {
		return cm06Mapper.insertPgmHistory(paramMap);
	}

	@Override
	public int updatePw(Map<String, String> paramMap) {
		int result = 0;
		result += cm06Mapper.updatePw(paramMap);
		result += cm06Mapper.updateTokenPw(paramMap);
		return result;
	}
    
	@Override
	public List<Map<String, String>> selectRuleCheckList(Map<String, String> paramMap) {
		return cm06Mapper.selectRuleCheckList(paramMap); 
	}

	@Override
	public Map<String, String> updatePwErrCnt(Map<String, String> paramMap) {
		cm06Mapper.updatePwErrCnt(paramMap);
		Map<String, String> usrInfo = cm06Mapper.selectUserInfo(paramMap);
		if(Integer.parseInt(usrInfo.get("passErrCnt")) >= 10) {
			cm06Mapper.updateUserN(paramMap);
		}
		return usrInfo;
	}
    
	@Override
	public List<Map<String, String>> selectUserStatusList(Map<String, String> paramMap) {
		return cm06Mapper.selectUserStatusList(paramMap); 
	}

	@Override
	public int updateUserStatus(Map<String, String> paramMap) {
		int result = 0;
		result += cm06Mapper.updateUserStatus(paramMap);
		return result;
	}
    
	@Override
	public List<Map<String, String>> selectEmployeeStatusList(Map<String, String> paramMap) {
		return cm06Mapper.selectEmployeeStatusList(paramMap); 
	}


	@Override
	public Map<String, String> checkUserIdImage(Map<String, String> paramMap) {
		return cm06Mapper.checkUserIdImage(paramMap);
	}

	// 이미지 업로드
	@Override
	public void updateUserImg(Map<String, Object> paramMap) throws Exception {
		if (paramMap.containsKey("userImg")) {
			Object userImgObj = paramMap.get("userImg");
			if (userImgObj instanceof String) {
				String userImgStr = (String) userImgObj;
				// 원하는 형식인 경우에만 바이트 배열로 변환하여 저장합니다.
				if (userImgStr != null && userImgStr.startsWith("data:application/octet-stream;base64,")) {
					// 단순히 문자열을 UTF-8 바이트 배열로 변환합니다.
					byte[] userImgBytes = userImgStr.getBytes(StandardCharsets.UTF_8);
					paramMap.put("userImg", userImgBytes);
				} else {
					paramMap.put("userImg", null);
				}
			}
		}
		cm06Mapper.updateUserImg(paramMap);
	}

	@Override
	public List<Map<String, String>> checkUserIdImageList(Map<String, String> paramMap) {
		return cm06Mapper.checkUserIdImageList(paramMap);
	}
}