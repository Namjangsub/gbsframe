package com.dksys.biz.user.cr.cr11.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR11Svc {
	
	// 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
		
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
  

}