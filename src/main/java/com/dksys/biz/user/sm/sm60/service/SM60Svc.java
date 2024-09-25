package com.dksys.biz.user.sm.sm60.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM60Svc {
	
	List<Map<String, String>> selectVendEstimateList(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int updateVendEstimate(Map<String, String> param);

	int deleteVendEstimate(Map<String, String> paramMap) throws Exception;
	
}