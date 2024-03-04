package com.dksys.biz.user.sm.sm18.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM18Svc {
	//카운트	
	int sm18_gridView_selectCount(Map<String, String> paramMap);
		
	//리스트
	List<Map<String, String>> sm18_gridView_selectList(Map<String, String> paramMap);

	//카운트	
	int sm18_gridView_selectCountNew(Map<String, String> paramMap);
		
	//리스트
	List<Map<String, String>> sm18_gridView_selectListNew(Map<String, String> paramMap);

	//리스트 - 2024.03.04 쿼리 수정
	List<Map<String, String>> sm18_gridView_selectListNewNam(Map<String, String> paramMap);

}