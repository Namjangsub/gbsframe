package com.dksys.biz.user.cr.cr08.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR08Svc {	  
	  
	  int  selectSalesStmtConCount(Map<String, String> paramMap);
	  
	  int selectSalesStmtCount(Map<String, String> paramMap);
	  
	  int selectSalesStmtCalCount(Map<String, String> paramMap);

	  List<Map<String, String>> selectSalesStmtList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtConList(Map<String, String> paramMap);
	  
	  Map<String, String> selectSalesStmtInfo(Map<String, String> paramMap);	  
	  
	  List<Map<String, String>> selectSalesStmtExcelList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalExcelList(Map<String, String> paramMap);

	  int selectConfirmCount(Map<String, String> paramMap);
	  
	  int insertSalesStmt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
	  
	  int updateSalesStmt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	  
	  int deleteSalesStmt(Map<String, String> paramMap) throws Exception;
}
