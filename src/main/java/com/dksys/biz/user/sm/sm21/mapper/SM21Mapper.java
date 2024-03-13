package com.dksys.biz.user.sm.sm21.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
}




