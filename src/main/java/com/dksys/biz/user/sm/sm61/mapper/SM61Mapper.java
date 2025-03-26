package com.dksys.biz.user.sm.sm61.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM61Mapper {
	
	List<Map<String, String>> selectVendInitEstimateList(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int updateVendInitEstimate(Map<String, String> dtl);

	int deleteVendInitEstimate(Map<String, String> paramMap);

}




