package com.dksys.biz.user.sm.sm20.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM20Svc {
	List<Map<String, String>> sm20_main_grid1_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm20_main_grid2_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm20_main_grid3_selectList(Map<String, String> paramMap);

	int update_sm20_payYn(Map<String, String> paramMap) throws Exception;

	int grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 팝업 입력대상 검색
	List<Map<String, String>> select_sm20_insert_target_modal(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_sm20_Info(Map<String, String> paramMap);

	// 수정화면 상세정보
	List<Map<String, String>> select_sm20_Info_Dtl(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_sm20(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_sm20(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_sm20(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);
}