package com.dksys.biz.user.sd.sd07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SD07Mapper {

	List<Map<String, String>> selectCloseYmList(Map<String, String> paramMap);
	
	int saveClose(Map<String, String> paramMap);

	List<Map<String, String>> selectCloseLastYm(Map<String, String> paramMap);

}