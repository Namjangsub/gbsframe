package com.dksys.biz.user.sm.sm19.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface SM19Svc {

    // 그리드 카운트
	int select_sm19_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_sm19_List(Map<String, String> paramMap);

	// 창고 조회
	List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

	// 수불유형 조회
	List<Map<String, Object>> selectinoutDiv(Map<String, String> paramMap);
}