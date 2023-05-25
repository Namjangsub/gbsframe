package com.dksys.biz.user.wb.wb03.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB03Mapper {
	int selectWbsPlanCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanLvl1List(Map<String, String> paramMap);
	
	
}