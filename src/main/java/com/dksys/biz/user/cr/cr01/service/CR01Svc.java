package com.dksys.biz.user.cr.cr01.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR01Svc {
	
	String selectMaxEstNo();
	
	public int selectEstCount(Map<String, String> param);
	
	public List<Map<String, Object>> selectEstList(Map<String, String> param);
	
	Map<String, Object> selectEstInfo(Map<String, String> paramMap);
	
	List<Map<String, Object>> selectEstDetail(Map<String, String> paramMap);
	
	
	void insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);


	
	int updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int deleteEst(Map<String, String> paramMap);
}