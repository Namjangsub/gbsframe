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

	String selectMaxWbsPlanNo(Map<String, String> paramMap);

	String selectWbsSeqNext(Map<String, String> paramMap);

	int selectMaxWbsCode(Map<String, String> paramMap);

	int wbsLevel1Insert(Map<String, String> paramMap);

	int wbsLevel1Update(Map<String, String> paramMap);

	List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap);

	int wbsLevel2Insert(Map<String, String> paramMap);

	int wbsLevel2Update(Map<String, String> paramMap);

	List<Map<String, String>> wbsPlanListChk(Map<String, String> paramMap);

	int deleteWbsPlanlist(Map<String, String> paramMap);

    int wbsLevel2MngIdUpdate(Map<String, String> paramMap);

	int wbsLevel2Delete(Map<String, String> paramMap);

	int wbsVerUpInsert(Map<String, String> paramMap);

	int wbsVerUpUpdate(Map<String, String> paramMap);

	List<Map<String, String>> selectVerNoNext(Map<String, String> paramMap);

	int wbsLevel1confirm(Map<String, String> paramMap);
//확정시  동일한 salesCd에 해당하는 1레벨은 일괄 적용 건별 수정에서는 추가된 코드나 삭제된 코드는 적용되지 않음(코드 추가 삭제로 인한 처리 추가)
	int wbsLevel1confirmAll(Map<String, String> sharngMap);

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

	// 유저 템플릿 mapper
	// ------------------------------------------------------------------------------------

	int selectWbsUserTaskTempletCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsUserTaskTempletList(Map<String, String> paramMap);

	String selectNewWbsUserTaskTempletCd(Map<String, String> paramMap);

	int wbsUserTaskTempletInsert(Map<String, String> sharngMap);

	int wbsUserTaskTempletUpdate(Map<String, String> sharngMap);

	int wbsUserTaskTempletDelete(Map<String, String> sharngMap);

	// ------------------------------------------------------------------------------------

	List<Map<String, String>> selectHistWBS1Level(Map<String, String> paramMap);

	// 일괄복사부분
	int ModalwbsPlanconfirmListCount(Map<String, String> paramMap);

	List<Map<String, String>> ModalwbsPlanconfirmList(Map<String, String> paramMap);

	List<Map<String, String>> selectWbcPlan(Map<String, String> paramMap);

	int updateWbcPlan(Map<String, String> sharngMap);

	List<Map<String, String>> selectWbcPlanTodoList(Map<String, String> paramMap);

	int wbcPlanTodoInsert(Map<String, String> paramMap);

	// 일괄확정부분
	int Modalwb22noconfirmListCount(Map<String, String> paramMap);

	List<Map<String, String>> Modalwb22noconfirmList(Map<String, String> paramMap);

	int confirm_wb22(Map<String, String> paramMap);
	// 일괄확정부분 끝

	List<Map<String, String>> selectWbcPlanUpdteTodoList(Map<String, String> paramMap);

	Map<String, String> wbsResultLastVerNoSearch(Map<String, String> paramMap);

	// wbs계획 관리 변경사항 저장
	int updateWbsChanges(Map<String, String> paramMap);

	// Ver.up Check
	List<Map<String, String>> wbsVerUpInsertChk(Map<String, String> paramMap);

	int wbsLevel1GantUpdate(Map<String, String> paramMap);

	int wbsRsltsGantDelete(Map<String, String> paramMap);
}