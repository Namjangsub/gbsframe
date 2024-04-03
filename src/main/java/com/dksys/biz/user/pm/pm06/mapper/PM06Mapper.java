package com.dksys.biz.user.pm.pm06.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM06Mapper {

  int selectTripRptListCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTripRptList(Map<String, String> paramMap);
	  
}