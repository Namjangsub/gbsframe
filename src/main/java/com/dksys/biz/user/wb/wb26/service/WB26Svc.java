package com.dksys.biz.user.wb.wb26.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB26Svc {

    // 그리드 카운트
	int select_wb26_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_wb26_List(Map<String, String> paramMap);

	int wb26save(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int update_wb26_confirmYn(Map<String, String> paramMap) throws Exception;

	int select_wb2602_List_Count(Map<String, String> paramMap);
	List<Map<String, String>> select_wb2602_List(Map<String, String> paramMap);
	List<Map<String, String>> select_wb06_List(Map<String, String> paramMap);

	int select_wb2603_List_Count(Map<String, String> paramMap);
	List<Map<String, String>> select_wb2603_List(Map<String, String> paramMap);

	List<Map<String, String>> select_wb2604_List(Map<String, String> paramMap);

	List<Map<String, String>> select_wb2605_List(Map<String, String> paramMap);

	List<Map<String, String>> select_wb2605_metaList(Map<String, String> paramMap);

	int update_wb26(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsTaskTempletGantList(Map<String, String> paramMap);

	int updateWbsRemarks(Map<String, String> paramMap);
	int updateWbsSchedule(Map<String, String> paramMap) throws Exception;
	// // 수금유형 조회
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}