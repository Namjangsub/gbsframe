package com.dksys.biz.user.pm.pm07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM07Mapper {

	int selectVacationCount(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationList(Map<String, String> paramMap);

	Map<String, String> selectVacationDtl(Map<String, String> paramMap);

	int insertVacation(Map<String, String> paramMap);

	int updateVacation(Map<String, String> paramMap);

	int deleteVacation(Map<String, String> paramMap);

	int selectAnnualBalance(Map<String, String> paramMap);

	Map<String, String> selectAnnualBalanceInfo(Map<String, String> paramMap);

	int insertAnnualGrant(Map<String, String> paramMap);

	int updateAnnualGrant(Map<String, String> paramMap);

	int selectAnnualGrantCount(Map<String, String> paramMap);

	Map<String, String> selectAnnualGrant(Map<String, String> paramMap);

	List<Map<String, String>> selectAnnualGrantList(Map<String, String> paramMap);

	int deleteAnnualGrant(Map<String, String> paramMap);

	List<Map<String, String>> selectUnregisteredUserList(Map<String, String> paramMap);

	String selectVacationReqNoNext(Map<String, String> paramMap);

	Map<String, String> selectWorkRptCodeByDept(Map<String, String> paramMap);

	String selectFileTrgtKeyNext();

	int insertDailyWorkReport(Map<String, String> paramMap);

	String selectUserEnterDt(Map<String, String> paramMap);

	int selectVacationApprovalChk(Map<String, String> paramMap);

	int selectApprovalCount(Map<String, String> paramMap);

	int updateVacationApprovalStatus(Map<String, String> paramMap);

	String selectDailyWorkReportSeqNext(Map<String, String> paramMap);

	Map<String, String> selectAnnualGrantByUser(Map<String, String> paramMap);

	int insertVacationDates(Map<String, String> paramMap);

	int deleteVacationDates(Map<String, String> paramMap);

	int deleteDailyWorkReportByVacation(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationDateList(Map<String, String> paramMap);

	int updateVacationDateWorkRptNo(Map<String, String> paramMap);

}
