package com.dksys.biz.user.sm.sm17.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM17Svc {

	// 발주 리스트 카운트	
	int sm17_grid1_selectCount(Map<String, String> paramMap);
		
	List<Map<String, String>> sm17_grid1_selectList(Map<String, String> paramMap);
	
	// 창고 코드 검색
    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);
}