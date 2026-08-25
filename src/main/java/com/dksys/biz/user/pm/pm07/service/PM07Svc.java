package com.dksys.biz.user.pm.pm07.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM07Svc {

	int selectVacationCount(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationList(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationCalendarList(Map<String, String> paramMap);

	Map<String, String> selectVacationDtl(Map<String, String> paramMap);

	Map<String, String> insertVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	Map<String, String> updateVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	Map<String, String> deleteVacation(Map<String, String> paramMap) throws Exception;

	Map<String, String> selectAnnualBalance(Map<String, String> paramMap);

	Map<String, String> saveAnnualGrant(Map<String, String> paramMap);

	List<Map<String, String>> selectAnnualGrantList(Map<String, String> paramMap);

	Map<String, String> saveAnnualGrantList(Map<String, Object> paramMap);

	int calcAnnualGrantDays(String enterDt, String yy);

	List<Map<String, String>> selectAutoCalcAnnualGrantList(Map<String, String> paramMap);

	int applyVacationApproved(Map<String, String> paramMap) throws Exception;

	int ensureDailyWorkReport(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationOverlapCheck(Map<String, String> paramMap);

	List<Map<String, String>> selectAnnualUseStatusList(Map<String, String> paramMap);

}
