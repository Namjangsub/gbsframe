package com.dksys.biz.user.wb.wb20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB20Mapper {
	
	int selectToDoCount(Map<String, String> paramMap);

	List<Map<String, String>> selectToDoList(Map<String, String> paramMap);
	
	int toDoCfDtUpdate(Map<String, String> paramMap);
	
	int updateRsltsApproval(Map<String, String> paramMap);
	
	List<Map<String, String>> selectToDoExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTodoDivList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalYnList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalMaxTodoKeyChk(Map<String, String> paramMap);
	
	int updateRsltsQmApproval(Map<String, String> paramMap);
	
	int updateQmQeqst(Map<String, String> paramMap);
		
	int updateQmMobileApproval(Map<String, String> paramMap);
	
	List<Map<String, String>> selectGetDeptList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);	
	
	int updateApprovalLine(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);
	  
	List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);	  
	
	String selectmaxTodoKey(Map<String, String> paramMap);
	
	int insertTodoMaster(Map<String, String> paramMap);  
	
}