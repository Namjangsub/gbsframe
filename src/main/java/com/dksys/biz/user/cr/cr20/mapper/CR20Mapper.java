package com.dksys.biz.user.cr.cr20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR20Mapper {

	int select_cr20m01_List_Count(Map<String, String> paramMap);
	
	List<Map<String, String>> select_cr20m01_List(Map<String, String> paramMap);
	
}
