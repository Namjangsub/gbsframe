package com.dksys.biz.user.sm.sm50.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM50Mapper {

	List<Map<String, String>> selectBomCostTreeList(Map<String, String> paramMap);

	List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap);
	
	
	List<Map<String, String>> selectBomAllCostList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomAllLevelTempList(Map<String, String> paramMap);
	
	
	void callBomTempUpd(Map<String, String> param);

	List<Map<String, String>> selectBomAllEnterList(Map<String, String> paramMap);

}
