package com.dksys.biz.user.pm.pm50.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM50Mapper {
	int select_pm50_ListCount(Map<String, String> paramMap);

    List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap);

	Map<String, String> select_pm50_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectFileList(Map<String, String> paramMap);

	int delete_pm50(Map<String, String> paramMap);
	
}
