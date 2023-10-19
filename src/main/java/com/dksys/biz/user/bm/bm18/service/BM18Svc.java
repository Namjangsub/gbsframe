package com.dksys.biz.user.bm.bm18.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM18Svc {

	//알림톡수신 카운트
	int selectReceptionMessageListCount(Map<String, String> paramMap);
	
	//알림톡수신 조회
	List<Map<String, String>> selectReceptionMessageList(Map<String, String> paramMap);
	
	String selectMaxRcvNo(Map<String, String> paramMap);
	
}