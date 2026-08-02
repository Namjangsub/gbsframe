package com.dksys.biz.user.pm.pm60.service;

import java.util.List;
import java.util.Map;

public interface PM60Svc {

	List<Map<String, String>> selectTeamMemberList(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvCalendar(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvActualCalendar(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationCalendar(Map<String, String> paramMap);

	Map<String, String> selectRsvDtl(Map<String, String> paramMap);

	int insertRsv(Map<String, String> paramMap) throws Exception;

	int updateRsv(Map<String, String> paramMap) throws Exception;

	int deleteRsv(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> selectRsvDateOverlapList(Map<String, String> paramMap);

	List<Map<String, String>> selectVndrList(Map<String, String> paramMap);

	int insertVndr(Map<String, String> paramMap) throws Exception;

	int updateVndr(Map<String, String> paramMap) throws Exception;

	int deleteVndr(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> selectRsvStatDaily(Map<String, String> paramMap);

}
