package com.dksys.biz.user.cr.cr19.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR19Svc {

    // 그리드 카운트
	int select_cr19_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr19_List(Map<String, String> paramMap);

	int select_cr19m02_Count(Map<String, String> paramMap);
	
	List<Map<String, String>> select_cr19m02_List(Map<String, String> paramMap);

	// 수주버전 조회
	List<Map<String, Object>> select_ordrsHistNo_List(Map<String, String> paramMap);

	void save_cr19(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	//void save_cr19_create_cr10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int save_cr19_create_cr10(Map<String, String> paramMap);
}