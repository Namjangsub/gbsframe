package com.dksys.biz.user.cr.cr01.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CR01Mapper {
	
	
	String selectMaxEstNo(Map<String, String> paramMap);

	String selectEstNo();
	
	int selectEstCount(Map<String, String> param);
	int selectEstDetailCount(Map<String, String> param);
	List<Map<String, Object>> selectEstList(Map<String, String> param);
	List<Map<String, Object>> selectEstListNotOrdrs(Map<String, String> param);



	Map<String, Object> selectEstInfo(Map<String, String> paramMap);
	
	   
    List<Map<String, Object>> selectEstDetail(Map<String, String> paramMap);
    
	int insertEst(Map<String, String> paramMap);
	
	int insertEstDetail(Map<String, Object> detailMap);
	int updateEst(Map<String, String> paramMap);
	int updateEstConfirm(Map<String, String> paramMap);
	int deleteEstDetail(Map<String, String> paramMap);

	int deleteAllEstDetails(Map<String, String> paramMap);

	int deleteEst(Map<String, String> paramMap);

	int updateEstDetail(Map<String, Object> paramMap);

	String selectMaxEstDeg(Map<String, String> paramMap);

	String selectMaxFileTrgtKey();



}
