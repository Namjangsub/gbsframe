package com.dksys.biz.user.qm.qm02.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface QM02Svc {

    // 그리드 카운트
	int select_grid_Count(Map<String, String> paramMap);
	
	// 그리드 카운트
	int select_gochal_count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);
//    
//    // 창고 코드 검색
//    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

    // 그리드 검색
	List<Map<String, String>> select_stock_modal(Map<String, String> paramMap);
	
	// 그리드 검색
	List<Map<String, String>> select_soojung_modal(Map<String, String> paramMap);
	 
	// 그리드  검색
	List<Map<String, String>> select_cobtp_modal(Map<String, String> paramMap);
		
	 // 그리드  검색
	List<Map<String, String>> select_all_modal(Map<String, String> paramMap);

	// 그리드 검색
	List<Map<String, Object>> select_zupiter_modal(Map<String, String> paramMap);
		
    // 수정화면 정보
	Map<String, String> select_qm02_Info(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_qm02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_qm02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_qm02(Map<String, String> paramMap) throws Exception;

	//DATA INSERT
	int insert_qm02_p02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_qm02_p02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
}
