package com.dksys.biz.user.wb.wb20.service;

import java.util.List;
import java.util.Map;

public interface WB20Svc {


	int selectToDoCount(Map<String, String> paramMap);

	List<Map<String, String>> selectToDoList(Map<String, String> paramMap);

	int toDoCfDtUpdate(Map<String, String> paramMap);

	int updateRsltsApproval(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap);

	List<Map<String, String>> selectTodoDivList(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalYnList(Map<String, String> paramMap);

	int updateQmMobileApproval(Map<String, String> paramMap);

	List<Map<String, String>> selectGetDeptList(Map<String, String> paramMap);

	List<Map<String, String>> selectGetApprovalList(Map<String, String> paramMap);

	Map<String, String> insertApprovalLine(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap);

	List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);

	String selectmaxTodoKey(Map<String, String> paramMap);

	int insertTodoMaster(Map<String, String> paramMap) throws Exception;

	int deleteTodoMaster(Map<String, String> param);

	List<Map<String, String>> selectMobileTodoSelect(Map<String, String> paramMap);

	Map<String, String> selectTodoFinalYn(Map<String, String> paramMap);

	int updateApprovalCancle(Map<String, String> paramMap);

	int M08selectToDoCount(Map<String, String> paramMap);

	List<Map<String, String>> M08selectToDoList(Map<String, String> paramMap);

	List<Map<String, String>> selectCurrentUserApprovalDataList(Map<String, String> paramMap);

    List<Map<String, String>> selectCurrentUserApprovalDataListFromTodoKey(Map<String, String> paramMap);

}