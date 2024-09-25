package com.dksys.biz.user.sm.sm60.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM60Mapper {
	
	List<Map<String, String>> selectVendEstimateList(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int updateVendEstimate(Map<String, String> dtl);

	int deleteVendEstimate(Map<String, String> paramMap);

}




