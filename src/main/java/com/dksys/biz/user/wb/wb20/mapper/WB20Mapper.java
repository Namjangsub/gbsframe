package com.dksys.biz.user.wb.wb20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB20Mapper {

	int selectToDoCount(Map<String, String> paramMap);

	int selectToDoCountNewSql(Map<String, String> paramMap);

	List<Map<String, String>> selectToDoList(Map<String, String> paramMap);

	List<Map<String, String>> selectToDoListNewSql(Map<String, String> paramMap);

	int toDoCfDtUpdate(Map<String, String> paramMap);

	int updateRsltsApproval(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap);

	List<Map<String, String>> selectTodoDivList(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalYnList(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalMaxTodoKeyChk(Map<String, String> paramMap);

	int updateQmMobileApproval(Map<String, String> paramMap);

	List<Map<String, String>> selectGetDeptList(Map<String, String> paramMap);

	List<Map<String, String>> selectGetApprovalList(Map<String, String> paramMap);

	int updateApprovalLine(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap);

	List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);

	String selectmaxTodoKey(Map<String, String> paramMap);

	String selectSystemCreateDttm(Map<String, String> paramMap);

	int insertTodoMaster(Map<String, String> paramMap);

	int deleteTodoMaster(Map<String, String> param);

	int deleteAllTodoMaster(Map<String, String> param);

	//결재 todo 삭제시 순번
	int updateTodoMasterSanctnSn(Map<String, String> paramMap);


	List<Map<String, String>> selectMobileTodoSelect(Map<String, String> paramMap);

	Map<String, String> selectTodoFinalYn(Map<String, String> paramMap);

	int updateApprovalCancle(Map<String, String> paramMap);

	int M08selectToDoCount(Map<String, String> paramMap);

	List<Map<String, String>> M08selectToDoList(Map<String, String> paramMap);


	//화면에 결재자정보주 해당유저가 있으면 결재/공유 버튼 활성화 자료 검색
	List<Map<String, String>> selectCurrentUserApprovalDataList(Map<String, String> paramMap);

    List<Map<String, String>> selectCurrentUserApprovalDataListFromTodoKey(Map<String, String> paramMap);

}