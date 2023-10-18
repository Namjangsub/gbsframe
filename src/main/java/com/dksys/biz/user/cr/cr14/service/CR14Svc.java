package com.dksys.biz.user.cr.cr14.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR14Svc {

    // 그리드 카운트
	int select_cr14_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr14_List(Map<String, String> paramMap);

	// POPUP 그리드 카운트
	int select_modal_List1_Count(Map<String, String> paramMap);
	int select_modal_List2_Count(Map<String, String> paramMap);
	int select_modal_List3_Count(Map<String, String> paramMap);
	int select_modal_List4_Count(Map<String, String> paramMap);
	int select_modal_List5_Count(Map<String, String> paramMap);
	int select_modal_List6_Count(Map<String, String> paramMap);

	// POPUP 그리드 리스트
	List<Map<String, String>> select_modal_List1(Map<String, String> paramMap);
	List<Map<String, String>> select_modal_List2(Map<String, String> paramMap);
	List<Map<String, String>> select_modal_List3(Map<String, String> paramMap);
	List<Map<String, String>> select_modal_List4(Map<String, String> paramMap);
	List<Map<String, String>> select_modal_List5(Map<String, String> paramMap);
	List<Map<String, String>> select_modal_List6(Map<String, String> paramMap);

	// 수주버전 조회
	List<Map<String, Object>> select_ordrsHistNo_List(Map<String, String> paramMap);
}