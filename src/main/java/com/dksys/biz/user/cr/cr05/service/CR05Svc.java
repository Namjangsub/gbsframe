package com.dksys.biz.user.cr.cr05.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR05Svc {

    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 삭제 전 체크
    List<Map<String, Object>> delete_Chk(Map<String, String> paramMap);

    // 팝업 입력대상 검색
	List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap);

    // 수정화면 정보
	Map<String, String> select_cr05_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_cr05_Info_Dtl(Map<String, String> paramMap);

	// 수금유형 조회
	List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
	
	// 계좌번호 조회
	List<Map<String, Object>> selectBkacCd(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_cr05(Map<String, String> paramMap) throws Exception;
}