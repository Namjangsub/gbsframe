package com.dksys.biz.user.pm.pm30.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.pm.pm30.mapper.PM30Mapper;
import com.dksys.biz.user.pm.pm30.service.PM30Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM30SvcImpl implements PM30Svc {

	@Autowired
	PM30Mapper pm30Mapper;

	@Override
	public List<Map<String, String>> selectAttendanceList(Map<String, String> paramMap) {
		return pm30Mapper.selectAttendanceList(paramMap);
	}

	@Override
	public Map<String, Object> saveAttendanceList(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> result = new HashMap<>();

		String coCd = (String) paramMap.get("coCd");
		String loginId = (String) paramMap.get("loginId");
		if (loginId == null || loginId.isEmpty()) {
			loginId = (String) paramMap.get("userId");
		}
		if (loginId == null || loginId.isEmpty()) {
			loginId = "SYSTEM";
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) paramMap.get("list");

		int resultCount = 0;
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> row : list) {
				row.put("coCd", coCd);
				row.put("loginId", loginId);
				resultCount += pm30Mapper.mergeAttendance(row);
			}
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rawList = (List<Map<String, Object>>) paramMap.get("rawList");

		int rawResultCount = 0;
		if (rawList != null && !rawList.isEmpty()) {
			for (Map<String, Object> row : rawList) {
				row.put("coCd", coCd);
				row.put("loginId", loginId);
				rawResultCount += pm30Mapper.mergeAttendanceRaw(row);
			}
		}

		result.put("resultCode", "0000");
		result.put("resultMessage", "저장되었습니다.");
		result.put("resultCount", resultCount);
		result.put("rawResultCount", rawResultCount);

		return result;
	}

	@Override
	public Map<String, Object> selectRelatedApplications(Map<String, Object> paramMap) {
		Map<String, Object> result = new HashMap<>();

		@SuppressWarnings("unchecked")
		List<String> empNoList = (List<String>) paramMap.get("empNoList");

		List<Map<String, String>> tripList = new ArrayList<>();
		List<Map<String, String>> vacationList = new ArrayList<>();
		List<Map<String, String>> reportTripList = new ArrayList<>();
		List<Map<String, String>> empSalesAreaList = new ArrayList<>();

		if (empNoList != null && !empNoList.isEmpty()) {
			Map<String, Object> queryMap = new HashMap<>(paramMap);
			queryMap.put("empNoList", empNoList);

			tripList = pm30Mapper.selectTripList(queryMap);
			if (tripList == null) {
				tripList = new ArrayList<>();
			}

			vacationList = pm30Mapper.selectVacationList(queryMap);
			if (vacationList == null) {
				vacationList = new ArrayList<>();
			}

			reportTripList = pm30Mapper.selectReportTripList(queryMap);
			if (reportTripList == null) {
				reportTripList = new ArrayList<>();
			}

			empSalesAreaList = pm30Mapper.selectEmpSalesArea(queryMap);
			if (empSalesAreaList == null) {
				empSalesAreaList = new ArrayList<>();
			}

			List<Map<String, String>> savedWorkList = pm30Mapper.selectSavedWorkList(queryMap);
			if (savedWorkList == null) {
				savedWorkList = new ArrayList<>();
			}
			result.put("savedWorkList", savedWorkList);
		}

		result.put("resultCode", "0000");
		result.put("resultMessage", "성공");
		result.put("tripList", tripList);
		result.put("vacationList", vacationList);
		result.put("reportTripList", reportTripList);
		result.put("empSalesAreaList", empSalesAreaList);

		return result;
	}

	@Override
	public List<Map<String, String>> selectAttendanceDailyMonthly(Map<String, String> paramMap) {
		return pm30Mapper.selectAttendanceDailyMonthly(paramMap);
	}

	@Override
	public List<Map<String, String>> selectAttendanceEmployeeMonthly(Map<String, String> paramMap) {
		return pm30Mapper.selectAttendanceEmployeeMonthly(paramMap);
	}

	@Override
	public List<Map<String, String>> selectHourlyWorkerMonthly(Map<String, String> paramMap) {
		return pm30Mapper.selectHourlyWorkerMonthly(paramMap);
	}

}
