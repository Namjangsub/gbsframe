package com.dksys.biz.user.wb.wb25.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB25Svc {
	
	int selectWbsTaskEvlCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWbsTaskEvlList(Map<String, String> paramMap);	
	
	int wbsTaskEvlInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsTaskEvlUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsTaskEvlCloseYnConfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectWbsTaskEvlResultList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectWbsTaskEvlIssList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectEvlCloseChk(Map<String, String> paramMap);
	
}