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

	void callCopyWbsPlan(Map<String, String> paramMap);

	int selectWbsTaskTempletCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsTaskTempletList(Map<String, String> paramMap);

	int saveWbsTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	// 유저 템플릿 service
	//---------------------------------------------------------------------------------------------------

	int selectWbsUserTaskTempletCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsUserTaskTempletList(Map<String, String> paramMap);

	int saveWbsUserTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	//---------------------------------------------------------------------------------------------------

	List<Map<String, String>> selectHistWBS1Level(Map<String, String> paramMap);
	
	// 계획일괄복사부분
	int ModalwbsPlanconfirmListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> ModalwbsPlanconfirmList(Map<String, String> paramMap);
	
	int confirm_copy(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbcPlanTodoList(Map<String, String> paramMap);
	
	int wbcPlanTodoInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	// 일괄확정부분
	int Modalwb22noconfirmListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> Modalwb22noconfirmList(Map<String, String> paramMap);

	int confirm_wb22(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	// 일괄확정부분 끝
	
	
	List<Map<String, String>> selectWbcPlanUpdteTodoList(Map<String, String> paramMap);

	Map<String, String> wbsResultLastVerNoSearch(Map<String, String> paramMap);
	
	// wbs계획 관리 변경사항 저장
	int updateWbsChanges(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
}