package com.dksys.biz.user.sm.sm21.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM21Svc {
	List<Map<String, String>> sm21_main_grid1_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm21_main_grid2_selectList(Map<String, String> paramMap);

	int update_sm21_payYn(Map<String, String> paramMap) throws Exception;

	int grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 팝업 입력대상 검색
	List<Map<String, String>> select_sm21_insert_target_modal(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_sm21(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_sm21(Map<String, String> paramMap) throws Exception;
}