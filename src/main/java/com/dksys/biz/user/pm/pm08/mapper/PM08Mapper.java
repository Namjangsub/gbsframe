package com.dksys.biz.user.pm.pm08.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM08Mapper {

	int selectSubstituteWorkCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSubstituteWorkList(Map<String, String> paramMap);

	Map<String, String> selectSubstituteWorkDtl(Map<String, String> paramMap);

	int insertSubstituteWork(Map<String, String> paramMap);

	int updateSubstituteWork(Map<String, String> paramMap);

	int updateSubstituteWorkResult(Map<String, String> paramMap);

	int deleteSubstituteWork(Map<String, String> paramMap);

	int deleteSubstituteWorkResult(Map<String, String> paramMap);

	String selectSubstituteWorkReqNoNext(Map<String, String> paramMap);

	int selectSubstituteWorkDuplicateCheck(Map<String, String> paramMap);

	int updateSubstituteWorkReqStatus(Map<String, String> paramMap);

	int updateSubstituteWorkResultStatus(Map<String, String> paramMap);

	int selectApprovalCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSubstituteWorkProjectList(Map<String, String> paramMap);

	int insertSubstituteWorkProjectList(Map<String, String> paramMap);

	int deleteSubstituteWorkProjectList(Map<String, String> paramMap);

	int updateSubstituteWorkReqMngOpn(Map<String, String> paramMap);

	int updateSubstituteWorkResultMngOpn(Map<String, String> paramMap);

	int selectApprovedCountExceptApplicant(Map<String, String> paramMap);

	List<Map<String, String>> selectSubstituteVacationStatusList(Map<String, String> paramMap);

}
