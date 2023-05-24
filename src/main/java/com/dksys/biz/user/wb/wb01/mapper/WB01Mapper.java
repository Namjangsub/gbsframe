package com.dksys.biz.user.wb.wb01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB01Mapper {
	int selectWbsPlanCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanLvl1List(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanLvlCboList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanSubLvlCboList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
	
}