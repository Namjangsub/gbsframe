package com.dksys.biz.user.sm.sm18.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM18Mapper {	
	
	//카운트
	int sm18_gridView_selectCount(Map<String, String> paramMap);
	
	//리스트
	List<Map<String, String>> sm18_gridView_selectList(Map<String, String> paramMap);	

	//카운트
	int sm18_gridView_selectCountNew(Map<String, String> paramMap);
	
	//리스트
	List<Map<String, String>> sm18_gridView_selectListNew(Map<String, String> paramMap);	
	
	//리스트 - 2024.03.04 쿼리 수정
	List<Map<String, String>> sm18_gridView_selectListNewNam(Map<String, String> paramMap);	

}

