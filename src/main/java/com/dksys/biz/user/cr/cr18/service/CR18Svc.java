package com.dksys.biz.user.cr.cr18.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR18Svc {

    // 그리드 카운트
	int select_cr18_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr18_List(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr18_Excel(Map<String, String> paramMap);

	// // 수금유형 조회
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}