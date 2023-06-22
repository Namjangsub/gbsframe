package com.dksys.biz.user.cr.cr08.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR08Svc {	  
	
	  int selectPchsCostCount(Map<String, String> paramMap);
	  
	  int selectSalesStmtCalCount(Map<String, String> paramMap);

	  List<Map<String, String>> selectSalesStmtList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalList(Map<String, String> paramMap);

	  Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtExcelList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalExcelList(Map<String, String> paramMap);

	  int selectConfirmCount(Map<String, String> paramMap);

	  int insertPchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	  int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	  int deletePchsCost(Map<String, String> paramMap) throws Exception;
}
