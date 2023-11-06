package com.dksys.biz.user.pm.pm05.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface PM05Mapper {
  int pm05_grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> pm05_grid1_selectList(Map<String, String> paramMap);
  
  List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);
}