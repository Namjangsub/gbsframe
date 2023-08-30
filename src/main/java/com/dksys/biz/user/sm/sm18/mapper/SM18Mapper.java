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
}

