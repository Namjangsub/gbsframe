package com.dksys.biz.admin.cm.cm12.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM12Mapper {

    List<Map<String, String>> selectSolarLunarEventHolidaysList(Map<String, String> paramMap);
	
}