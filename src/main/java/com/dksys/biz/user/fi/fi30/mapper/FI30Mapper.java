package com.dksys.biz.user.fi.fi30.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FI30Mapper {
	// 그리드 카운트
	int select_fi30_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_fi30_List(Map<String, String> paramMap);

	Map<String, String> select_fi30_detail_info(Map<String, String> paramMap);
}
