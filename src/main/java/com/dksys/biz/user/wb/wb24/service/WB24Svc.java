package com.dksys.biz.user.wb.wb24.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB24Svc {
	
	int selectWbsIssueListCount(Map paramMap);
		
	List<Map<String, String>> selectWbsIssueList(Map paramMap);	

	List<Map<String, String>> selectWbsIssueCountBySalesCds(Map paramMap);
	
	Map<String, String> selectMaxWbsIssueNo(Map<String, String> paramMap);
	
	int wbsIssueInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int wbsIssueUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int updateIssueComment(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int wbsIssCloseYnConfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMemberTelNo(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsIssueListDashboard(Map<String, String> paramMap);

	int wbsIssueDelete(Map<String, String> paramMap) throws Exception;

	Map<String, String> selectTeamManagerInfo(Map<String, String> paramMap);

    Map<String, String> selectDept2TeamManagerInfo(Map<String, String> paramMap);

	Map<String, String> selectTeamManagerSpecialInfo(Map<String, String> paramMap);

	int updateWbsIssueResultEvaluate(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_planInfo(Map<String, String> paramMap);

	Map<String, String> select_wb2401p01_rsltInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectVendProblemList(Map<String, String> paramMap);

	int updateVendCd(Map<String, String> param);   // 발생공급업체 Update

	Map<String, String> selectIssueInfo(Map<String, String> paramMap);		// 문제정보를 가지고 발주요청서 등록
}
