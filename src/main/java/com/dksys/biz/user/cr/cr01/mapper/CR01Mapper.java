package com.dksys.biz.user.cr.cr01.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CR01Mapper {
	
	
	String selectMaxEstNo(Map<String, String> paramMap);
	
	String selectEstNo();
	
	int selectEstCount(Map<String, String> param);
	
	List<Map<String, Object>> selectEstList(Map<String, String> param);
	
	Map<String, Object> selectEstInfo(Map<String, String> paramMap);
	
	   
    List<Map<String, Object>> selectEstDetail(Map<String, String> paramMap);
    
	int insertEst(Map<String, String> paramMap);
	
	int insertEstDetail(Map<String, String> detailMap);
	
	int updateEst(Map<String, String> paramMap);

	int deleteEstDetail(Map<String, String> paramMap);
	
	int deleteEst(Map<String, String> paramMap);
}
