package com.dksys.biz.user.sm.sm21.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM21Mapper {
	List<Map<String, String>> sm21_main_grid1_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm21_main_grid2_selectList(Map<String, String> paramMap);

	int update_sm21_payYn(Map<String, String> param);
	
	int grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 팝업 입력대상 검색
	List<Map<String, String>> select_sm21_insert_target_modal(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_sm21(Map<String, String> paramMap);
	
	int update_sm21_Del(Map<String, String> paramMap);

	String select_sm21_SeqNext(Map<String, String> paramMap);

	int insert_sm21_payChk(Map<String, String> paramMap);

	int update_sm21_payChk(Map<String, String> paramMap);

	int update_sm21_payChk_billNo(Map<String, String> paramMap);

	int delete_sm21_payChk(Map<String, String> paramMap);
}




