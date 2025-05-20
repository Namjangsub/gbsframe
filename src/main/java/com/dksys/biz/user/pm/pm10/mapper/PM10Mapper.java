package com.dksys.biz.user.pm.pm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM10Mapper {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap);

	int pm10_main_insert(Map<String, String> param);

	int pm10_d01_insert(Map<String, String> param);

	int pm10_d01_update(Map<String, String> param);

	int pm10_d03_insert(Map<String, String> param);

	int pm10_d03_update(Map<String, String> param);

	int insertMn(Map<String, String> param);

	int updateMn(Object paramMap);

	int deleteMn(Map<String, String> paramMap);

}