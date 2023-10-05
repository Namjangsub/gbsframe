package com.dksys.biz.user.wb.wb22.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB22Svc {
	
	int selectWbsSjListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWbsSjList(Map<String, String> paramMap);	

	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap);
	
	int wbsLevel1Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap);
	
	int wbsLevel2Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectVerNoNext(Map<String, String> paramMap);
	
	int wbsLevel1confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsLevel2confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
	
	List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap);

	List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap);
	   
	int wbsRsltsInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsRsltsUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsRsltsconfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap);		
	
	List<Map<String, String>> selectIncompleteJob(Map<String, String> paramMap);
	
}