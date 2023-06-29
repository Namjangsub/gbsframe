package com.dksys.biz.user.cr.cr08.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR08Mapper {
	 
	 int selectSalesStmtConCount(Map<String, String> paramMap);
	 
	 int selectSalesStmtCalCount(Map<String, String> paramMap);
	 
	 int selectSalesStmtCount(Map<String, String> paramMap);

	  List<Map<String, String>> selectSalesStmtList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtConList(Map<String, String> paramMap);	 
	  
	  Map<String, String> selectSalesStmtInfo(Map<String, String> paramMap);	  
	  
	  List<Map<String, String>> selectSalesStmtExcelList(Map<String, String> paramMap);
	  
	  List<Map<String, String>> selectSalesStmtCalExcelList(Map<String, String> paramMap);
	  
	  int selectConfirmCount(Map<String, String> paramMap);
	  
	  int selectSalesStmtSeqNext(Map<String, String> paramMap);
	  
	  String selectSalesStmtCalNext(Map<String, String> paramMap);
	  
	  int insertSalesStmt(Map<String, String> paramMap);
	  
	  int updateSalesStmt(Map<String, String> paramMap);
	  
	  int updateSalesStmt2(Map<String, String> paramMap);
	  
	  int deleteSalesStmt(Map<String, String> paramMap);
	  
	  int deleteSalesStmt2(Map<String, String> paramMap);

}
