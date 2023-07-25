package com.dksys.biz.user.cr.cr08.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR08Svc {

    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

    // 팝업 입력대상 검색
	List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap);

    // 수정화면 정보
	Map<String, String> select_cr08_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_cr08_Info_Dtl(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_cr08(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_cr08(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_cr08(Map<String, String> paramMap) throws Exception;
}