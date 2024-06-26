package com.dksys.biz.user.bm.bm11.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface BM11Svc {

    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
    
    // 수정화면 정보
	Map<String, String> select_bm11_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_bm11_Info_Dtl(Map<String, String> paramMap);

	// 중복 체크 조회
	Map<String, String> DupChk_BM11M01(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_bm11(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_bm11(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_bm11(Map<String, String> paramMap) throws Exception;
}