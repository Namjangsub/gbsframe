package com.dksys.biz.user.sm.sm06.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface SM06Svc {

	 // 그리드 카운트
		int grid1_selectCount(Map<String, String> paramMap);
		
		// 그리드 리스트
		List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
//	    
//	    // 창고 코드 검색
//	    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

	    // 팝업 재고 검색
		List<Map<String, String>> select_whin_modal(Map<String, String> paramMap);

	    // 수정화면 정보
		Map<String, String> select_sm06_Info(Map<String, String> paramMap);
		
		// 수정화면 상세정보
		List<Map<String, String>> select_sm06_Info_Dtl(Map<String, String> paramMap);
		
		//DATA INSERT
		int insert_sm06(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
		//DATA UPDATE
		int update_sm06(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
		//DATA DELETE
		int delete_sm06(Map<String, String> paramMap) throws Exception;
		
}
