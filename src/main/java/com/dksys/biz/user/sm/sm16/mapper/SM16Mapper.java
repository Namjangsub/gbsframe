package com.dksys.biz.user.sm.sm16.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SM16Mapper {
	
	List<Map<String, String>> sm16selectPurchaseListNew(Map<String, String> paramMap);

	List<Map<String, String>> selectSM16MainList(Map<String, String> paramMap);

}




