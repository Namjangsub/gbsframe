package com.dksys.biz.user.wb.wb24.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB24Svc {
	
	int selectWbsIssueListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWbsIssueList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap);
	
	int wbsIssueInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsIssueUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsIssCloseYnConfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMemberTelNo(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsIssueListDashboard(Map<String, String> paramMap);

}