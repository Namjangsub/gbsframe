package com.dksys.biz.user.cr.cr01.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR01Svc {
	
	String selectMaxEstNo(Map<String, String> paramMap);
	
	public int selectEstCount(Map<String, String> param);
	
	public List<Map<String, Object>> selectEstList(Map<String, String> param);
	
	Map<String, Object> selectEstInfo(Map<String, String> paramMap);
	

	
	String insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	Map<String, String> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	
	int updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int deleteEst(Map<String, String> paramMap);
}