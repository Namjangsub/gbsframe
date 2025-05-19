package com.dksys.biz.user.pm.pm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM10Mapper {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap);

	int updateMn(Object paramMap);

	int deleteMn(Map<String, String> paramMap);

}