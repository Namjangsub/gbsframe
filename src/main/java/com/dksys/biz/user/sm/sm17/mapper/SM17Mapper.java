package com.dksys.biz.user.sm.sm17.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM17Mapper {	

	int sm17_grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> sm17_grid1_selectList(Map<String, String> paramMap);	
		
	// 창고 코드 검색
    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);
}




