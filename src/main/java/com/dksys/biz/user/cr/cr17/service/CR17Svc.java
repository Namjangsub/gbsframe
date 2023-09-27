package com.dksys.biz.user.cr.cr17.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR17Svc {

    // 그리드 카운트
	int select_cr17_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr17_List(Map<String, String> paramMap);

	// // 수금유형 조회
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}