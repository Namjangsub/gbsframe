package com.dksys.biz.user.wb.wb22.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB22Mapper {
	
	int selectWbsSjListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsSjList(Map<String, String> paramMap);
	
	Map<String, String> selectSjInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap);
	
	int selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	int selectWbsSeqNext(Map<String, String> paramMap);
	
	int selectMaxWbsCode(Map<String, String> paramMap);
	
	int wbsLevel1Insert(Map<String, String> paramMap);
	
	int wbsLevel1Update(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap);
	
	int wbsLevel2Insert(Map<String, String> paramMap);
	
	int wbsLevel2Update(Map<String, String> paramMap);
	
	List<Map<String, String>> wbsPlanListChk(Map<String, String> paramMap);
	
	int deleteWbsPlanlist(Map<String, String> paramMap);
	
	int wbsLevel2Delete(Map<String, String> paramMap);
	
	int wbsVerUpInsert(Map<String, String> paramMap);
	
	int wbsVerUpUpdate(Map<String, String> paramMap);
	
	List<Map<String, String>> selectVerNoNext(Map<String, String> paramMap);
	
	int wbsLevel1confirm(Map<String, String> paramMap);
	
	int wbsLevel2confirm(Map<String, String> paramMap);
	
	List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap);

	List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap);
	   
	int selectWbsRstlsSeqNext(Map<String, String> paramMap);
	
	int wbsRsltsInsert(Map<String, String> paramMap);
	
	int wbsRsltsUpdate(Map<String, String> paramMap);
	
	int wbsRsltsDelete(Map<String, String> paramMap);
	
	int wbsRsltsconfirm(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap);
	
	List<Map<String, String>> selectIncompleteJob(Map<String, String> paramMap);

	void callCopyWbsPlan(Map<String, String> paramMap);

	int selectWbsTaskTempletCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsTaskTempletList(Map<String, String> paramMap);

	String selectNewWbsTaskTempletCd(Map<String, String> paramMap);

	int wbsTaskTempletInsert(Map<String, String> sharngMap);

	int wbsTaskTempletUpdate(Map<String, String> sharngMap);

	int wbsTaskTempletDelete(Map<String, String> sharngMap);

	List<Map<String, String>> selectHistWBS1Level(Map<String, String> paramMap);	
	
	// 일괄복사부분
	int ModalwbsPlanconfirmListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> ModalwbsPlanconfirmList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbcPlan(Map<String, String> paramMap);
	
	int updateWbcPlan(Map<String, String> sharngMap);
}