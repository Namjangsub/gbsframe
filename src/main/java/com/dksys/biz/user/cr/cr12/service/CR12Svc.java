package com.dksys.biz.user.cr.cr12.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR12Svc {

    // 그리드 카운트
	int select_cr12_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_cr12_List(Map<String, String> paramMap);
}