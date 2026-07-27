package com.dksys.biz.user.pm.pm51.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM51Mapper {

	int selectTripReqListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqList(Map<String, String> paramMap);

	Map<String, String> selectTripReqM01(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTripRptExpenseSummaryByReqNo(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqD01(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqD02(Map<String, String> paramMap);

	Map<String, String> selectTripReqChgM01(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqChgD01(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqChgD02(Map<String, String> paramMap);

	int selectTripReqChgOpenCount(Map<String, String> paramMap);

	int insertTripReqChgM01(Map<String, String> paramMap);

	int insertTripReqChgD01(Map<String, String> paramMap);

	int insertTripReqChgD02(Map<String, String> paramMap);

	int updateTripReqChgM01(Map<String, String> paramMap);

	int deleteTripReqChgD01(Map<String, String> paramMap);

	int deleteTripReqChgD02(Map<String, String> paramMap);

	// 변경신청 재상신 시 이전 저장분의 미결(아직 결재처리 안 된) 결재선만 삭제 - 이미 승인된 이력은 보존
	int deleteTripReqChgPendingApprovalLine(Map<String, String> paramMap);

	int insertTripReqHistM01(Map<String, String> paramMap);

	int insertTripReqHistD01(Map<String, String> paramMap);

	int insertTripReqHistD02(Map<String, String> paramMap);

	int insertTripReqHistAppliedM01(Map<String, String> paramMap);

	int insertTripReqHistAppliedD01(Map<String, String> paramMap);

	int insertTripReqHistAppliedD02(Map<String, String> paramMap);

	int selectTripReqHistCount(Map<String, String> paramMap);

	int applyTripReqChgM01(Map<String, String> paramMap);

	int deleteTripReqD01ForChg(Map<String, String> paramMap);

	int applyTripReqChgD01(Map<String, String> paramMap);

	int submitTripReqChg(Map<String, String> paramMap);

	int completeTripReqChg(Map<String, String> paramMap);

	int insertTripReqM01(Map<String, String> paramMap);

	int insertTripReqD01(Map<String, String> paramMap);

	int insertTripReqD02(Map<String, String> paramMap);

	int updateTripReqM01(Map<String, String> paramMap);

	int updateTripReqPayAmounts(Map<String, String> paramMap);

	int updateTripReqAprvStsCd(Map<String, String> paramMap);

	int updateTripReqPayDone(Map<String, String> paramMap);

	int updateTripReqPayCancel(Map<String, String> paramMap);

	int updateTripReqPayCancelMngApproval(Map<String, String> paramMap);

	int updateTripReqSalesInfo(Map<String, String> paramMap);

	int deleteTripReqD01(Map<String, String> paramMap);

	int deleteTripReqD02(Map<String, String> paramMap);

	int deleteTripReqM01(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalMngChk(Map<String, String> paramMap);

	int deleteTripReqApprovalLines(Map<String, String> paramMap);

	int deleteTripReqMngApprovalLines(Map<String, String> paramMap);

	List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap);

	String selectTripRptSalesCd(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptPayExpenseSum(Map<String, String> paramMap);

	int selectTripRptEatCntSum(Map<String, String> paramMap);

	Map<String, String> selectTripRptTravelerExpenseSum(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptTravelerApprovalLines(Map<String, String> paramMap);

	int selectTripRptListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptList(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqForRpt(Map<String, String> paramMap);

	Map<String, String> selectTripRptM02(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptD03(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptD02(Map<String, String> paramMap);

	int insertTripRptM02(Map<String, String> paramMap);

	int insertTripRptD02(Map<String, String> paramMap);

	int insertTripRptD03(Map<String, String> paramMap);

	int updateTripRptM02(Map<String, String> paramMap);

	int updateTripRptMngEval(Map<String, String> paramMap);

	int deleteTripRptD02(Map<String, String> paramMap);

	int deleteTripRptD03(Map<String, String> paramMap);

	int deleteTripRptM02(Map<String, String> paramMap);

	int selectTripRptExists(Map<String, String> paramMap);

	List<Map<String, Object>> selectTripExpenseStatusList(Map<String, Object> paramMap);

	int updateTripExpenseStatus(Map<String, Object> paramMap);

	int updateTripRptAcctSettle(Map<String, Object> paramMap);

	int updateTripRptPayDone(Map<String, String> paramMap);

	List<Map<String, String>> selectTripDateOverlapList(Map<String, String> paramMap);

	List<Map<String, String>> selectTripScheduleByMonth(Map<String, String> paramMap);

}
