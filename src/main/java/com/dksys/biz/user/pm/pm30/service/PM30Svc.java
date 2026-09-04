package com.dksys.biz.user.pm.pm30.service;

import java.util.List;
import java.util.Map;

public interface PM30Svc {

	List<Map<String, String>> selectAttendanceList(Map<String, String> paramMap);

	Map<String, Object> saveAttendanceList(Map<String, Object> paramMap) throws Exception;

	Map<String, Object> selectRelatedApplications(Map<String, Object> paramMap);

	List<Map<String, String>> selectAttendanceDailyMonthly(Map<String, String> paramMap);
	List<Map<String, String>> selectAttendanceEmployeeMonthly(Map<String, String> paramMap);
	List<Map<String, String>> selectHourlyWorkerMonthly(Map<String, String> paramMap);

	List<Map<String, String>> selectAttendanceChangeList(Map<String, String> paramMap);

	Map<String, Object> saveAttendanceChange(Map<String, Object> paramMap) throws Exception;

	Map<String, Object> deleteAttendanceList(Map<String, Object> paramMap) throws Exception;

}
