package com.dksys.biz.user.wb.wb22.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB22Svc {
	List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);	

	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap);
	
	int wbsLevel1Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap);
	
	int wbsLevel2Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	
}