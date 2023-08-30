package com.dksys.biz.user.sm.sm18.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM18Svc {
	//카운트	
	int sm18_gridView_selectCount(Map<String, String> paramMap);
		
	//리스트
	List<Map<String, String>> sm18_gridView_selectList(Map<String, String> paramMap);
}