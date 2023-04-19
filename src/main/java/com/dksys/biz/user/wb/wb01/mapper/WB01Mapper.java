package com.dksys.biz.user.wb.wb01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB01Mapper {
	int selectWbsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsList01(Map<String, String> paramMap);

}