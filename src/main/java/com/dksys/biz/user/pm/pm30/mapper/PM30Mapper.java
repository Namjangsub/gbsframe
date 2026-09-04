package com.dksys.biz.user.pm.pm30.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM30Mapper {

	List<Map<String, String>> selectAttendanceList(Map<String, String> paramMap);

	int mergeAttendance(Map<String, Object> paramMap);

	int mergeAttendanceRaw(Map<String, Object> paramMap);

	List<Map<String, String>> selectDailyApplicationList(Map<String, Object> paramMap);

	List<Map<String, String>> selectEmpSalesArea(Map<String, Object> paramMap);

	List<Map<String, String>> selectSavedWorkList(Map<String, Object> paramMap);

	List<Map<String, String>> selectAttendanceDailyMonthly(Map<String, String> paramMap);

	List<Map<String, String>> selectAttendanceEmployeeMonthly(Map<String, String> paramMap);

	List<Map<String, String>> selectHourlyWorkerMonthly(Map<String, String> paramMap);

	List<Map<String, String>> selectAttendanceChangeList(Map<String, String> paramMap);

	int mergeAttendanceChange(Map<String, Object> paramMap);

	int deleteAttendance(Map<String, Object> paramMap);

}
