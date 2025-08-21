package com.dksys.biz.user.wb.wb26.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB26Mapper {
    // 그리드 카운트
	int select_wb26_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_wb26_List(Map<String, String> paramMap);

	int wb26save(Map<String, String> paramMap);

	int insert_wb26(Map<String, String> paramMap);

	int update_wb26(Map<String, String> paramMap);

	int update_wb26_confirmYn(Map<String, String> param);

	int select_wb2602_List_Count(Map<String, String> paramMap);
	List<Map<String, String>> select_wb2602_List(Map<String, String> paramMap);

	int select_wb2603_List_Count(Map<String, String> paramMap);
	List<Map<String, String>> select_wb2603_List(Map<String, String> paramMap);

	List<Map<String, String>> select_wb2604_List(Map<String, String> paramMap);
	
	List<Map<String, String>> select_wb2605_List(Map<String, String> paramMap);

	List<Map<String, String>> select_wb2605_metaList(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsTaskTempletGantList(Map<String, String> paramMap);

    // // 수금유형 검색
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}
