package com.dksys.biz.user.sm.sm61.service;

import java.util.List;
import java.util.Map;

public interface SM61Svc {
	
	List<Map<String, String>> selectVendInitEstimateList(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int updateVendInitEstimate(Map<String, String> param);

	int deleteVendInitEstimate(Map<String, String> paramMap) throws Exception;
	
}