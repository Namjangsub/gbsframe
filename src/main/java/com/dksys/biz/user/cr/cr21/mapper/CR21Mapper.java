package com.dksys.biz.user.cr.cr21.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR21Mapper {
	
	List<Map<String, String>> selectPrjctVSResultChart(Map<String, String> paramMap);
	
}










