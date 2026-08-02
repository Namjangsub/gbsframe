package com.dksys.biz.user.pm.pm60.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.pm.pm60.mapper.PM60Mapper;
import com.dksys.biz.user.pm.pm60.service.PM60Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM60SvcImpl implements PM60Svc {

	@Autowired
	PM60Mapper pm60Mapper;

	@Override
	public List<Map<String, String>> selectTeamMemberList(Map<String, String> paramMap) {
		return pm60Mapper.selectTeamMemberList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectRsvCalendar(Map<String, String> paramMap) {
		return pm60Mapper.selectRsvCalendar(paramMap);
	}

	@Override
	public List<Map<String, String>> selectRsvActualCalendar(Map<String, String> paramMap) {
		return pm60Mapper.selectRsvActualCalendar(paramMap);
	}

	@Override
	public List<Map<String, String>> selectVacationCalendar(Map<String, String> paramMap) {
		return pm60Mapper.selectVacationCalendar(paramMap);
	}

	@Override
	public Map<String, String> selectRsvDtl(Map<String, String> paramMap) {
		return pm60Mapper.selectRsvDtl(paramMap);
	}

	@Override
	public int insertRsv(Map<String, String> paramMap) throws Exception {
		normalizeRsvParam(paramMap);
		validateRsvParam(paramMap);
		validateRsvDateOverlap(paramMap);
		return pm60Mapper.insertRsv(paramMap);
	}

	@Override
	public int updateRsv(Map<String, String> paramMap) throws Exception {
		normalizeRsvParam(paramMap);
		validateRsvParam(paramMap);
		validateRsvDateOverlap(paramMap);
		return pm60Mapper.updateRsv(paramMap);
	}

	@Override
	public int deleteRsv(Map<String, String> paramMap) throws Exception {
		return pm60Mapper.deleteRsv(paramMap);
	}

	@Override
	public List<Map<String, String>> selectRsvDateOverlapList(Map<String, String> paramMap) {
		return pm60Mapper.selectRsvDateOverlapList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectVndrList(Map<String, String> paramMap) {
		return pm60Mapper.selectVndrList(paramMap);
	}

	@Override
	public int insertVndr(Map<String, String> paramMap) throws Exception {
		validateVndrParam(paramMap);
		return pm60Mapper.insertVndr(paramMap);
	}

	@Override
	public int updateVndr(Map<String, String> paramMap) throws Exception {
		validateVndrParam(paramMap);
		return pm60Mapper.updateVndr(paramMap);
	}

	@Override
	public int deleteVndr(Map<String, String> paramMap) throws Exception {
		return pm60Mapper.deleteVndr(paramMap);
	}

	@Override
	public List<Map<String, String>> selectRsvStatDaily(Map<String, String> paramMap) {
		return pm60Mapper.selectRsvStatDaily(paramMap);
	}

	private void normalizeRsvParam(Map<String, String> paramMap) {
		if (paramMap.get("tripStDtm") != null) {
			paramMap.put("tripStDtm", paramMap.get("tripStDtm").replace("-", "").replace(" ", "").replace(":", ""));
		}
		if (paramMap.get("tripEdDtm") != null) {
			paramMap.put("tripEdDtm", paramMap.get("tripEdDtm").replace("-", "").replace(" ", "").replace(":", ""));
		}
	}

	private void validateRsvParam(Map<String, String> paramMap) {
		if (!hasText(paramMap.get("userId"))) {
			throw new RuntimeException("출장자를 선택해주세요.");
		}
		if (!hasText(paramMap.get("rsvStsCd"))) {
			throw new RuntimeException("예약상태를 선택해주세요.");
		}
		if (!hasText(paramMap.get("tripStDtm")) || paramMap.get("tripStDtm").length() < 8) {
			throw new RuntimeException("출장 시작일시를 입력해주세요.");
		}
		if (!hasText(paramMap.get("tripEdDtm")) || paramMap.get("tripEdDtm").length() < 8) {
			throw new RuntimeException("출장 종료일시를 입력해주세요.");
		}
	}

	private void validateRsvDateOverlap(Map<String, String> paramMap) {
		if ("Y".equals(paramMap.get("forceOverlap"))) {
			return;
		}

		String userId = paramMap.get("userId");
		String stDtm = paramMap.get("tripStDtm");
		String edDtm = paramMap.get("tripEdDtm");

		if (!hasText(userId) || !hasText(stDtm) || !hasText(edDtm) || stDtm.length() < 8 || edDtm.length() < 8) {
			return;
		}

		Map<String, String> checkParam = new HashMap<>();
		checkParam.put("userId", userId);
		checkParam.put("tripStDate", stDtm.substring(0, 8));
		checkParam.put("tripEdDate", edDtm.substring(0, 8));
		checkParam.put("rsvNo", paramMap.get("rsvNo"));

		List<Map<String, String>> overlapList = pm60Mapper.selectRsvDateOverlapList(checkParam);
		if (overlapList != null && !overlapList.isEmpty()) {
			Map<String, String> overlap = overlapList.get(0);
			throw new RuntimeException(userId + "님의 출장기간이 기등록된 " + overlap.get("docType") + "(" + overlap.get("docNo") + ")와 중복됩니다.");
		}
	}

	private void validateVndrParam(Map<String, String> paramMap) {
		if (!hasText(paramMap.get("vndrNm"))) {
			throw new RuntimeException("업체명을 입력해주세요.");
		}
		if (!hasText(paramMap.get("workDt")) || paramMap.get("workDt").length() < 8) {
			throw new RuntimeException("근무일자를 입력해주세요.");
		}
	}

	private boolean hasText(String text) {
		return text != null && !text.trim().isEmpty();
	}

}
