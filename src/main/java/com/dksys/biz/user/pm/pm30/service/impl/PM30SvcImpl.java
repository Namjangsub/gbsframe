package com.dksys.biz.user.pm.pm30.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rawList = (List<Map<String, Object>>) paramMap.get("rawList");

		// 마감 검증: list와 rawList의 모든 workDt를 수집해서 한 번에 검증 (N+1 쿼리 방지)
		List<String> allWorkDates = new ArrayList<>();
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> row : list) {
				Object workDtObj = row.get("workDt");
				if (workDtObj != null) {
					allWorkDates.add(String.valueOf(workDtObj));
				}
			}
		}
		if (rawList != null && !rawList.isEmpty()) {
			for (Map<String, Object> row : rawList) {
				Object workDtObj = row.get("workDt");
				if (workDtObj != null) {
					allWorkDates.add(String.valueOf(workDtObj));
				}
			}
		}
		if (!allWorkDates.isEmpty()) {
			assertNotClosed(allWorkDates.toArray(new String[0]));
		}

		int resultCount = 0;
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> row : list) {
				row.put("coCd", coCd);
				row.put("loginId", loginId);
				sanitizeAttendanceTime(row, "clsNormal");
				sanitizeAttendanceTime(row, "clsOt");
				sanitizeAttendanceTime(row, "clsNight");
				sanitizeAttendanceTime(row, "clsTotal");
				resultCount += pm30Mapper.mergeAttendance(row);
			}
		}

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

		// 마감 검증: workYm (YYYYMM) 기준
		String workYm = (String) paramMap.get("workYm");
		if (workYm != null && !workYm.isEmpty()) {
			assertNotClosed(workYm);
		}

		// 저장 단계에서 변동분 정상시간(normTm) 및 연장시간(otTm)을 사전 계산 및 검증하여 DB에 확정 포맷으로 적재
		calculateAndValidateChangeHours(paramMap);

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

		// 마감 검증: list의 모든 workDt를 수집해서 한 번에 검증
		List<String> allWorkDates = new ArrayList<>();
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> row : list) {
				Object workDtObj = row.get("workDt");
				if (workDtObj != null) {
					allWorkDates.add(String.valueOf(workDtObj));
				}
			}
		}
		if (!allWorkDates.isEmpty()) {
			assertNotClosed(allWorkDates.toArray(new String[0]));
		}

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

	private static final java.util.regex.Pattern NUMERIC_PATTERN = java.util.regex.Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

	/**
	 * 변동분내역(contentTxt1, contentTxt2) 파싱, 컬럼별 정제(Sanitize) 및 정상시간(normTm)/연장시간(otTm) 사전 산출.
	 * 비수치 문자(한글/특수문자 오타 등)나 파이프 오염을 정제하여 규격 포맷으로 contentTxt1, contentTxt2를 재구성하여 저장.
	 */
	private void calculateAndValidateChangeHours(Map<String, Object> paramMap) {
		String contentTxt1 = (String) paramMap.getOrDefault("contentTxt1", "");
		String contentTxt2 = (String) paramMap.getOrDefault("contentTxt2", "");
		String raw = (contentTxt1 != null ? contentTxt1 : "") + (contentTxt2 != null ? contentTxt2 : "");

		double normSum = 0.0;
		double otSum = 0.0;

		if (raw.contains("|")) {
			String[] parts = raw.split("\\|", -1);
			String[] sanitized = new String[15];
			for (int i = 0; i < 15; i++) {
				String val = (i < parts.length && parts[i] != null) ? parts[i].trim() : "";
				if (i == 0 || i == 1 || i == 13 || i == 14) {
					// 텍스트 필드 (적요, 요일구분, 근무지, 비고) - 파이프 문자 제거
					sanitized[i] = val.replace("|", "").trim();
				} else if (i >= 2 && i <= 11) {
					// 시간 수치 필드 (col 2~11)
					double num = extractFirstNumber(val);
					num = Math.round(num * 10.0) / 10.0;
					if (num == 0.0) {
						sanitized[i] = "";
					} else if (num == Math.floor(num)) {
						sanitized[i] = String.valueOf((long) num);
					} else {
						sanitized[i] = String.format(Locale.US, "%.1f", num);
					}

					// 정상시간 합계 (NORM_TM): col 3 + col 6 + col 9 (평일 정상, 토요 정상, 휴일 정상)
					if (i == 2 || i == 5 || i == 8) {
						normSum += num;
					}
					// 연장시간 합계 (OT_TM): col 4 + col 5 + col 7 + col 8 + col 10 + col 11 (평일 연장/야간, 토요 연장/야간, 휴일 연장/야간)
					if (i == 3 || i == 4 || i == 6 || i == 7 || i == 9 || i == 10) {
						otSum += num;
					}
				} else if (i == 12) {
					// 수당 필드 (col 12)
					double num = extractFirstNumber(val);
					if (num == 0.0) {
						sanitized[i] = "";
					} else if (num == Math.floor(num)) {
						sanitized[i] = String.valueOf((long) num);
					} else {
						sanitized[i] = String.format(Locale.US, "%.1f", num);
					}
				} else {
					sanitized[i] = val.replace("|", "").trim();
				}
			}

			// 정제된 15개 항목으로 파이프 문자열 재구성
			String sanitizedFull = String.join("|", sanitized);
			String cleanTxt1 = sanitizedFull.length() > 200 ? sanitizedFull.substring(0, 200) : sanitizedFull;
			String cleanTxt2 = sanitizedFull.length() > 200 ? sanitizedFull.substring(200, Math.min(400, sanitizedFull.length())) : "";

			paramMap.put("contentTxt1", cleanTxt1);
			paramMap.put("contentTxt2", cleanTxt2.isEmpty() ? null : cleanTxt2);
		} else {
			// 구버전 단일 텍스트 호환
			normSum = extractFirstNumber(contentTxt1);
			otSum = extractFirstNumber(contentTxt2);
		}

		normSum = Math.round(normSum * 10.0) / 10.0;
		otSum = Math.round(otSum * 10.0) / 10.0;

		paramMap.put("normTm", normSum);
		paramMap.put("otTm", otSum);
	}

	/**
	 * 수치 문자열에서 첫 번째 유효 숫자(정수/소수) 안전 추출
	 * 예: "5ㅇㅀ" -> 5.0, "4ㄴㅇㄹ" -> 4.0, "ㄴㅇㄹ" -> 0.0, "8:30" -> 8.5
	 */
	private double extractFirstNumber(String str) {
		if (str == null || str.trim().isEmpty()) {
			return 0.0;
		}
		String s = str.trim().replace(":", ".");
		java.util.regex.Matcher m = NUMERIC_PATTERN.matcher(s);
		if (m.find()) {
			try {
				return Double.parseDouble(m.group());
			} catch (NumberFormatException ignored) {
				return 0.0;
			}
		}
		return 0.0;
	}

	/**
	 * 일별 근태 시간 필드(clsNormal, clsOt, clsNight 등) 저장 전 정규화
	 */
	private void sanitizeAttendanceTime(Map<String, Object> row, String key) {
		Object val = row.get(key);
		if (val == null) {
			row.put(key, "0");
			return;
		}
		String s = String.valueOf(val).trim();
		if (s.isEmpty() || "0".equals(s) || "0.0".equals(s)) {
			row.put(key, "0");
			return;
		}
		try {
			double d = Double.parseDouble(s.replace(":", "."));
			row.put(key, String.format(Locale.US, "%.1f", d));
		} catch (Exception e) {
			row.put(key, "0");
		}
	}

	@Override
	public String selectAttendanceCloseYm() {
		return pm30Mapper.selectAttendanceCloseYm();
	}

	@Override
	public void saveAttendanceCloseYm(String closeYm, String loginId, String pgmId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("closeYm", closeYm);
		paramMap.put("loginId", loginId != null ? loginId : "SYSTEM");
		paramMap.put("updtPgm", pgmId);
		pm30Mapper.updateAttendanceCloseYm(paramMap);
	}

	@Override
	public void assertNotClosed(String... dateStrs) {
		String closeYm = pm30Mapper.selectAttendanceCloseYm();
		if (closeYm == null || closeYm.isEmpty()) {
			return; // 마감 없음 상태
		}

		// 입력된 각 날짜에서 숫자만 추출하여 앞 6자리(YYYYMM) 추출 후 비교
		for (String dateStr : dateStrs) {
			if (dateStr == null || dateStr.isEmpty()) {
				continue; // null/empty는 건너뜀
			}

			// 숫자만 추출 (YYYYMMDD, YYYYMMDDHHMMSS, YYYY-MM-DD 등 다양한 형식 지원)
			String digitsOnly = dateStr.replaceAll("[^0-9]", "");
			if (digitsOnly.length() < 6) {
				continue; // 6자리 미만이면 건너뜀 (유효하지 않은 날짜)
			}

			String dateYm = digitsOnly.substring(0, 6);

			// dateYm <= closeYm 이면 마감된 기간
			if (dateYm.compareTo(closeYm) <= 0) {
				throw new IllegalStateException(
					"마감된 기간(" + closeYm + " 이하)의 근태 자료는 등록·수정·삭제할 수 없습니다."
				);
			}
		}
	}

}
