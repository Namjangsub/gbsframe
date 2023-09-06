package com.dksys.biz.user.wb.wb21.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB21Svc {


	List<Map<String, String>> selectMaxSjNo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectCodeList(Map<String, String> paramMap);

    int selectSjListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSjList(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteSjListChk(Map<String, String> paramMap);
	   
	int deleteSjList(Map<String, String> paramMap);
	
	int sjInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int sjUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
	
	List<Map<String, String>> selectSjDtlList(Map<String, String> paramMap);
	
	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	int sjConfirmY(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int sjConfirmN(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	
}