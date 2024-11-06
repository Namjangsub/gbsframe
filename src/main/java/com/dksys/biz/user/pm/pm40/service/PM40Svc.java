package com.dksys.biz.user.pm.pm40.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM40Svc {

	
		// 그리드 카운트
		int selectMainGridListCount(Map<String, String> paramMap);
		
		// 그리드 리스트
		List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);
		
		//DATA INSERT
		int insert_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
		//DATA UPDATE
		int update_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
		//DATA DELETE
		int delete_pm40(Map<String, String> paramMap) throws Exception;
	
		List<Map<String, String>> selectYearWorkMainList(Map<String, String> paramMap);
		
		String select_pm40_Next_MNGM_NO(Map<String, String> paramMap);

		int select_gochal_count(Map<String, String> paramMap);
		
		Map<String, String> select_pm40_Info(Map<String, String> paramMap);
		
}
