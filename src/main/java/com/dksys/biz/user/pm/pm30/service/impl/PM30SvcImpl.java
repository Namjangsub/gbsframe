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

		List<Map<String, String>> dailyApplicationList = new ArrayList<>();
		List<Map<String, String>> empSalesAreaList = new ArrayList<>();

		if (empNoList != null && !empNoList.isEmpty()) {
			Map<String, Object> queryMap = new HashMap<>(paramMap);
			queryMap.put("empNoList", empNoList);

			dailyApplicationList = pm30Mapper.selectDailyApplicationList(queryMap);
			if (dailyApplicationList == null) {
				dailyApplicationList = new ArrayList<>();
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
		result.put("dailyApplicationList", dailyApplicationList);
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

	@Override
	public List<Map<String, String>> selectAttendanceChangeList(Map<String, String> paramMap) {
		return pm30Mapper.selectAttendanceChangeList(paramMap);
	}

	@Override
	public Map<String, Object> saveAttendanceChange(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> result = new HashMap<>();

		String coCd = (String) paramMap.get("coCd");
		String loginId = (String) paramMap.get("loginId");
		if (loginId == null || loginId.isEmpty()) {
			loginId = (String) paramMap.get("userId");
		}
		if (loginId == null || loginId.isEmpty()) {
			loginId = "SYSTEM";
		}

		paramMap.put("coCd", coCd);
		paramMap.put("loginId", loginId);

		int resultCount = pm30Mapper.mergeAttendanceChange(paramMap);

		result.put("resultCode", "0000");
		result.put("resultMessage", "저장되었습니다.");
		result.put("resultCount", resultCount);

		return result;
	}

	@Override
	public Map<String, Object> deleteAttendanceList(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> result = new HashMap<>();

		String coCd = (String) paramMap.get("coCd");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) paramMap.get("list");

		int resultCount = 0;
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> row : list) {
				row.put("coCd", coCd);
				resultCount += pm30Mapper.deleteAttendance(row);
			}
		}

		result.put("resultCode", "0000");
		result.put("resultMessage", "삭제되었습니다.");
		result.put("resultCount", resultCount);

		return result;
	}

}
