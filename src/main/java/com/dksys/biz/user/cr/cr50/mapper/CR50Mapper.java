package com.dksys.biz.user.cr.cr50.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR50Mapper {
	
	List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap);

	Map<String, String> selectPfuInfo(Map<String, String> paramMap);

//	Map<String, String> selectPfuClobInfo(Map<String, String> paramMap);

	int insertPfu(Map<String, String> paramMap);
	
}










