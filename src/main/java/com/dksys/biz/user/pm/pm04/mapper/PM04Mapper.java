package com.dksys.biz.user.pm.pm04.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM04Mapper {

	  int selectDailyWorkCount(Map<String, String> paramMap);

	  List<Map<String, String>> selectDailyWorkMainList(HashMap<String, Object> map);
	  
	  List<Map<String, String>> select_all_name(Map<String, String> paramMap);
}
