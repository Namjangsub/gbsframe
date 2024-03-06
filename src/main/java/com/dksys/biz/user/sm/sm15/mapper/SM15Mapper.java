package com.dksys.biz.user.sm.sm15.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SM15Mapper {
	
	List<Map<String, String>> sm15selectPurchaseListNew(Map<String, String> paramMap);

	List<Map<String, String>> selectSM15MainList(Map<String, String> paramMap);
}




