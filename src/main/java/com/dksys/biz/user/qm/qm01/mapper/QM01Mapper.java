package com.dksys.biz.user.qm.qm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM01Mapper {

	int selectQualityReqCount(Map<String, String> paramMap);

	int selectPurchaseListPopCount(Map<String, String> paramMap);

	int selectShareUserCount(Map<String, String> paramMap);

	int selectSignUserCount(Map<String, String> paramMap);

	int selectShareUserResCount(Map<String, String> paramMap);

	int selectSignResCount(Map<String, String> paramMap);

	List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);

	List<Map<String, String>> selectPurchaseListPop(Map<String, String> paramMap);

	List<Map<String, String>> selectShareUserlst(Map<String, String> paramMap);

	List<Map<String, String>> selectShareResUserlst(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);

	// List<Map<String, String>> selectSignUserInfo(Map<String, String> paramMap);

	Map<String, String> selectQtyReqInfo(Map<String, String> paramMap);

	Map<String, String> selectQtyReqRespInfo(Map<String, String> paramMap);

	int selectConfirmCount(Map<String, String> paramMap);

	int selectQualityReqSeqNext(Map<String, String> paramMap);

	String selectQualityReqCalNext(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap);

	int insertQualityReq(Map<String, String> paramMap);

	int insertQualityResp(Map<String, String> paramMap);

	int updateReqRsltChg(Map<String, String> paramMap);

	int updateQualityReq(Map<String, String> paramMap);

	int updateQualityResp(Map<String, String> paramMap);

	int updateQualityResultComment(Map<String, String> paramMap);

	int deleteQualityReq(Map<String, String> paramMap);

	int deleteQualityResp(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);

	List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);

	List<Map<String, String>> deleteWbsApprovalListChk(Map<String, String> paramMap);

	int deleteWbsSharngList(Map<String, String> paramMap);

	int deleteWbsApprovalList(Map<String, String> paramMap);

	int insertWbsSharngList(Map<String, String> paramMap);

	int insertWbsApprovalList(Map<String, String> paramMap);

	int selectCodeMaxCount(Map<String, String> paramMap);

	List<Map<String, String>> selectMainCodeList(Map<String, String> param);

	List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);

	String selectBaljooSttusChk(Map<String, String> paramMap);

	// List<Map<String, String>> selectApprovalAll(Map<String, String> paramMap);
	Map<String, String> selectApprovalAll(Map<String, String> paramMap);

	int updateReqSt(Map<String, String> paramMap);

	int updateReqStRslt(Map<String, String> paramMap);

	int updateReqActMnRslt(Map<String, String> paramMap);	// 투입공수 업데이트

	int updateReqActMdCancle(Map<String, String> paramMap);	// 투입공수 업데이트

	int updateReqStChk(Map<String, String> paramMap);

	int updateCheckDept(Map<String, String> paramMap);

	List<Map<String, String>> deleteWbsSharngListChk1(Map<String, String> paramMap);

	List<Map<String, String>> deleteWbsApprovalListChk1(Map<String, String> paramMap);

	int deleteWbsSharngList1(Map<String, String> paramMap);

	int deleteWbsApprovalList1(Map<String, String> paramMap);

	int deleteWbsSharngList2(Map<String, String> paramMap);

	int deleteWbsApprovalList2(Map<String, String> paramMap);

	int deleteApprovalList(Map<String, String> paramMap);

	int deleteApprovalListTodoNo(Map<String, String> paramMap);

	// 과제확정 공유자 삭제
	int deleteSJApprovalList1(Map<String, String> paramMap);

	List<Map<String, String>> nonOrderStatusList(Map<String, String> paramMap);

	// void insertWb24SharngList(Map<String, String> param);
	int insertWb24SharngList(Map<String, String> paramMap);

	int updateWb24SharngList1(Map<String, String> paramMap);

	int deleteWb24SharngList1(Map<String, String> paramMap);

	int updateWb24ApprovalList1(Map<String, String> paramMap);

	int deleteWb24ApprovalList1(Map<String, String> paramMap);

	int updateWb24SharngList2(Map<String, String> paramMap);

	int deleteWb24SharngList2(Map<String, String> paramMap);

	int updateWb24ApprovalList2(Map<String, String> paramMap);

	int deleteWb24ApprovalList2(Map<String, String> paramMap);

	int updateWbsIssueReqNo(Map<String, String> paramMap);

	int clearWbsIssueReqNo(Map<String, String> paramMap);

	String selectReqStCurrentStatus(Map<String, String> paramMap);

    void deleteWorkReportSharngList(Map<String, String> paramMap);

}