package com.dksys.biz.user.dw.dw02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DW02Mapper {

	int searchAuditsCount(Map<String, String> paramMap);

	List<Map<String, String>> searchAuditsList(Map<String, String> paramMap);
	
}