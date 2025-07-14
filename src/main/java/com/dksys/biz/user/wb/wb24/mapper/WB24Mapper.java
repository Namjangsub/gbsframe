package com.dksys.biz.user.wb.wb24.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB24Mapper {
	
	int selectWbsIssueListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsIssueList(Map<String, String> paramMap);
	
	Map<String, String> selectMaxWbsIssueNo(Map<String, String> paramMap);
	
	int selectWbsIssueSeqNext(Map<String, String> paramMap);
	
	int wbsIssueInsert(Map<String, String> paramMap);
	
	int wbsActInsert(Map<String, String> paramMap);
	
	int wbsIssueUpdate(Map<String, String> paramMap);
	
	int updateIssueComment(Map<String, String> paramMap);

	int wbsActUpdate(Map<String, String> paramMap);
	
	int wbsIssCloseYnConfirm(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap);
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);

	List<Map<String, String>> actChk(Map<String, String> paramMap);

	List<Map<String, String>> issueResultChk(Map<String, String> paramMap);

	List<Map<String, String>> selectMemberTelNo(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsIssueListDashboard(Map<String, String> paramMap);

	List<Map<String, String>> select_wb24_shareUserchk(Map<String, String> paramMap);
	
	int wbsIssueDelete(Map<String, String> paramMap);
	
	int wbsIssueResultDelete(Map<String, String> paramMap);

	Map<String, String> selectTeamManagerInfo(Map<String, String> paramMap);

    Map<String, String> selectDept2TeamManagerInfo(Map<String, String> paramMap);

	Map<String, String> selectTeamManagerSpecialInfo(Map<String, String> paramMap);

	int updateWbsIssueResultEvaluate(Map<String, String> paramMap);

	int updateWbsIssueStChk(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_planInfo(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_rsltInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectVendProblemList(Map<String, String> paramMap);

	int updateVendCd(Map<String, String> paramMap);  // 발생공급업체 Update

	Map<String, String> selectIssueInfo(Map<String, String> paramMap);		// 문제정보를 가지고 발주요청서 등록

	int issueReqExistChk(Map<String, String> paramMap);
}