package com.dksys.biz.user.pm.pm07.service.impl;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.pm.pm07.mapper.PM07Mapper;
import com.dksys.biz.user.pm.pm07.service.PM07Svc;
import com.dksys.biz.user.pm.pm30.service.PM30Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM07SvcImpl implements PM07Svc {

	@Autowired
	PM07Mapper pm07Mapper;

	@Autowired
	WB20Svc wb20Svc;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	PM30Svc pm30Svc;

	@Override
	public int selectVacationCount(Map<String, String> paramMap) {
		return pm07Mapper.selectVacationCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectVacationList(Map<String, String> paramMap) {
		return pm07Mapper.selectVacationList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectVacationCalendarList(Map<String, String> paramMap) {
		return pm07Mapper.selectVacationCalendarList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectAttendanceCalendarList(Map<String, String> paramMap) {
		return pm07Mapper.selectAttendanceCalendarList(paramMap);
	}

	@Override
	public Map<String, String> selectVacationDtl(Map<String, String> paramMap) {
		Map<String, String> result = pm07Mapper.selectVacationDtl(paramMap);
		if (result != null) {
			List<Map<String, String>> vacationDateList = pm07Mapper.selectVacationDateList(paramMap);
			result.put("vacationDateList", new GsonBuilder().disableHtmlEscaping().create().toJson(vacationDateList));
		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectVacationOverlapCheck(Map<String, String> paramMap) {
		return pm07Mapper.selectVacationOverlapCheck(paramMap);
	}

	// 저장(등록/수정) 시점 서버측 최종 겹침 검증. 화면의 실시간 조회를 신뢰하지 않고 DB 기준으로 재확인한다.
	// (동일 신청자 REQ_ID 기준, SANCTN_STS 무관 전 건 대상 - PM0701P01.html checkVacationOverlap() 과 동일 규칙)
	private List<Map<String, String>> findVacationOverlap(Map<String, String> paramMap, String excludeReqNo) {
		String stTm = paramMap.get("stTm");
		String edTm = paramMap.get("edTm");
		Map<String, String> overlapQuery = new HashMap<String, String>();
		overlapQuery.put("reqId", paramMap.get("reqId"));
		overlapQuery.put("stDt", paramMap.get("stDt"));
		overlapQuery.put("edDt", paramMap.get("edDt"));
		overlapQuery.put("stTm", (stTm == null || stTm.isEmpty()) ? "0000" : stTm);
		overlapQuery.put("edTm", (edTm == null || edTm.isEmpty()) ? "2359" : edTm);
		if (excludeReqNo != null && !excludeReqNo.isEmpty()) {
			overlapQuery.put("reqNo", excludeReqNo);
		}
		return pm07Mapper.selectVacationOverlapCheck(overlapQuery);
	}

	/**
	 * DB 저장(등록/수정) 직전 백엔드에서 휴가일수(vacDays) 및 차감일수(deductDays)를 최종 재산정/평가한다.
	 * 프론트엔드의 전달값에만 의존하지 않고 휴가구분(vacTypeCd) 및 일자 데이터(vacDtArr 또는 stDt~edDt)를 기준으로 백엔드 최종 검증을 수행한다.
	 */
	private void evaluateVacationAndDeductDays(Map<String, String> paramMap) {
		String stDtStr = paramMap.get("stDt");
		String edDtStr = paramMap.get("edDt");
		String vacTypeCd = paramMap.get("vacTypeCd");
		String vacDtArr = paramMap.get("vacDtArr"); // 프론트엔드에서 주말/공휴일/음력휴일이 제외된 휴가일자별 상세 목록 JSON (vacDt, workHour)

		// 1. vacDays (휴가일수/영업일수) 산정
		double vacDays = 0.0;
		if (vacDtArr != null && !vacDtArr.trim().isEmpty() && !"[]".equals(vacDtArr.trim())) {
			try {
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Type vacDtType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> vacDtList = gson.fromJson(vacDtArr, vacDtType);
				if (vacDtList != null && !vacDtList.isEmpty()) {
					vacDays = vacDtList.size();
				}
			} catch (Exception e) {
				// parsing fallback
			}
		}

		if (vacDays == 0.0 && stDtStr != null && edDtStr != null) {
			String rawStDt = stDtStr.replaceAll("[^0-9]", "");
			String rawEdDt = edDtStr.replaceAll("[^0-9]", "");
			if (rawStDt.length() == 8 && rawEdDt.length() == 8) {
				try {
					LocalDate stDt = LocalDate.parse(rawStDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
					LocalDate edDt = LocalDate.parse(rawEdDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
					vacDays = ChronoUnit.DAYS.between(stDt, edDt) + 1.0;
				} catch (Exception e) {
					vacDays = 1.0;
				}
			}
		}

		if (vacDays <= 0.0) {
			vacDays = 1.0;
		}

		// 2. deductDays (차감일수) 백엔드 최종 평가
		// - 반차류: 반차(PM07TYPE02), 포상휴가반차(PM07TYPE08), 대체휴가반차(PM07TYPE12) -> 0.5일
		// - 비차감류: 교육/훈련(PM07TYPE05), 병가(PM07TYPE13), 조퇴(PM07TYPE03), 외출(PM07TYPE04), 재택근무(PM07TYPE10), 지각(PM07TYPE14) -> 0.0일
		// - 그 외 모든 휴가(연차, 대체휴가, 경조휴가, 포상휴가, 하계휴가 등): 영업일수(vacDays)만큼 차감
		double deductDays = 0.0;

		String vacTypeNm = paramMap.get("vacTypeNm") == null ? "" : paramMap.get("vacTypeNm").trim();

		// 반차류 여부
		boolean isHalfDay = "PM07TYPE02".equals(vacTypeCd) || "PM07TYPE08".equals(vacTypeCd) || "PM07TYPE12".equals(vacTypeCd) || vacTypeNm.contains("반차");
		
		// 비차감류 여부 (교육/훈련, 병가, 조퇴, 외출, 재택, 지각)
		boolean isNonDeduct = "PM07TYPE03".equals(vacTypeCd) || "PM07TYPE04".equals(vacTypeCd) || "PM07TYPE05".equals(vacTypeCd)
				|| "PM07TYPE10".equals(vacTypeCd) || "PM07TYPE13".equals(vacTypeCd) || "PM07TYPE14".equals(vacTypeCd)
				|| vacTypeNm.contains("교육") || vacTypeNm.contains("훈련") || vacTypeNm.contains("병가")
				|| vacTypeNm.contains("조퇴") || vacTypeNm.contains("외출") || vacTypeNm.contains("재택") || vacTypeNm.contains("지각");

		if (isHalfDay) {
			deductDays = 0.5;
		} else if (isNonDeduct) {
			deductDays = 0.0;
		} else {
			// 그 외 모든 휴가(연차, 대체휴가, 경조휴가, 포상휴가, 하계휴가 등)는 영업일수(vacDays)만큼 차감
			deductDays = vacDays;
		}

		paramMap.put("vacDays", String.format("%.1f", vacDays));
		paramMap.put("deductDays", String.format("%.1f", deductDays));
	}

	/**
	 * 일자 문자열의 주말(토/일) 여부 판정
	 */
	private boolean isWeekend(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return false;
		}
		String digits = dateStr.replaceAll("[^0-9]", "");
		if (digits.length() < 8) {
			return false;
		}
		try {
			LocalDate dt = LocalDate.parse(digits.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
			java.time.DayOfWeek dow = dt.getDayOfWeek();
			return (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 포상휴가(PM07TYPE07, PM07TYPE08) 신청 시 잔여 검증
	 * @param paramMap 휴가신청 파라미터 (vacTypeCd, reqId, stDt, deductDays 포함)
	 * @return 검증 통과 시 null, 실패 시 오류 메시지
	 */
	private String validateAwardVacationBalance(Map<String, String> paramMap) {
		String vacTypeCd = paramMap.get("vacTypeCd");

		// 포상휴가가 아니면 검증 통과
		if (!("PM07TYPE07".equals(vacTypeCd) || "PM07TYPE08".equals(vacTypeCd))) {
			return null;
		}

		String reqId = paramMap.get("reqId");
		String stDtStr = paramMap.get("stDt");
		String deductDaysStr = paramMap.get("deductDays");
		String reqNo = paramMap.get("reqNo"); // 수정 시에만 값이 있음

		if (reqId == null || reqId.isEmpty() || stDtStr == null || stDtStr.isEmpty()) {
			return "포상휴가 신청자 정보가 부족합니다.";
		}

		// 시작일자를 YYYYMMDD 형식으로 정규화
		String normStDt = stDtStr.replaceAll("[^0-9]", "");
		if (normStDt.length() != 8) {
			return "신청 시작일자 형식이 올바르지 않습니다.";
		}

		// 차감일수 파싱
		double deductDays = 0.0;
		if (deductDaysStr != null && !deductDaysStr.isEmpty()) {
			try {
				deductDays = Double.parseDouble(deductDaysStr);
			} catch (NumberFormatException e) {
				return "차감일수 계산 오류: " + deductDaysStr;
			}
		}

		// 잔여 검증 쿼리 실행
		Map<String, String> balanceQuery = new HashMap<>();
		balanceQuery.put("userId", reqId);
		balanceQuery.put("reqStDt", normStDt);
		if (reqNo != null && !reqNo.isEmpty()) {
			balanceQuery.put("excludeReqNo", reqNo);
		}

		try {
			Map<String, Object> balanceInfo = pm07Mapper.selectAwardVacationBalanceCheck(balanceQuery);
			if (balanceInfo == null || balanceInfo.isEmpty()) {
				return "유효한 포상휴가 지급 내역이 없거나 신청일자가 지급 유효기간을 벗어났습니다.";
			}

			// selectAwardVacationBalanceCheck는 resultType="CamelMap"이라 SQL 별칭(TOTAL_GRANTED 등)이
			// CamelMap.put()에서 camelCase(totalGranted 등)로 자동 변환되어 저장된다. 대문자 키로 조회하면
			// 항상 null이 되어(=잔여 0으로 오판) 모든 신청이 차단되는 치명적 버그가 되므로 반드시 camelCase로 조회할 것.
			Object grantedObj = balanceInfo.get("totalGranted");
			Object usedObj = balanceInfo.get("totalUsed");

			double totalGranted = 0.0;
			double totalUsed = 0.0;

			if (grantedObj != null) {
				if (grantedObj instanceof Number) {
					totalGranted = ((Number) grantedObj).doubleValue();
				} else {
					try {
						totalGranted = Double.parseDouble(grantedObj.toString());
					} catch (NumberFormatException e) {
						// 파싱 실패 시 0으로 설정
					}
				}
			}

			if (usedObj != null) {
				if (usedObj instanceof Number) {
					totalUsed = ((Number) usedObj).doubleValue();
				} else {
					try {
						totalUsed = Double.parseDouble(usedObj.toString());
					} catch (NumberFormatException e) {
						// 파싱 실패 시 0으로 설정
					}
				}
			}

			if (totalGranted == 0) {
				return "유효한 포상휴가 지급 내역이 없거나 신청일자가 지급 유효기간을 벗어났습니다.";
			}

			if (totalUsed + deductDays > totalGranted) {
				double remaining = totalGranted - totalUsed;
				return "잔여 포상휴가 일수(" + String.format("%.1f", remaining) + "일)를 초과하여 신청할 수 없습니다.";
			}

			return null; // 통과
		} catch (Exception e) {
			return "포상휴가 검증 중 오류 발생: " + e.getMessage();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> insertVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 마감 검증: stDt, edDt
		String stDt = paramMap.get("stDt");
		String edDt = paramMap.get("edDt");
		if ((stDt != null && !stDt.isEmpty()) || (edDt != null && !edDt.isEmpty())) {
			pm30Svc.assertNotClosed(stDt, edDt);
		}

		// 시작일/종료일 주말(휴일) 검증: 시작일과 종료일이 모두 주말이면 등록 불가
		if (isWeekend(stDt) && isWeekend(edDt)) {
			result.put("resultCode", "500");
			result.put("resultMessage", "시작일자(" + stDt + ")와 종료일자(" + edDt + ")가 모두 휴일(주말)이므로 신청할 수 없습니다.");
			return result;
		}

		// 백엔드 DB 저장 직전에 차감일수 및 휴가일수 최종 평가/산정
		evaluateVacationAndDeductDays(paramMap);

		// 포상휴가 잔여 검증
		String awardBalanceError = validateAwardVacationBalance(paramMap);
		if (awardBalanceError != null) {
			result.put("resultCode", "409");
			result.put("resultMessage", awardBalanceError);
			return result;
		}

		List<Map<String, String>> overlapList = findVacationOverlap(paramMap, null);
		if (overlapList != null && !overlapList.isEmpty()) {
			result.put("resultCode", "409");
			result.put("resultMessage", "이전에 신청한 휴가일자(" + overlapList.get(0).get("stDt") + " ~ " + overlapList.get(0).get("edDt") + ")와 겹쳐 등록할 수 없습니다.");
			return result;
		}

		String reqNo = pm07Mapper.selectVacationReqNoNext(paramMap);
		paramMap.put("reqNo", reqNo);
		// 결재선이 지정된 건은 결재요청(REQ), 결재선 없이 저장한 건만 임시저장(TEMP).
		// 화면은 TEMP 가 아니면 수정 불가로 잠근다.
		String reqApprovalArr = paramMap.get("approvalArr");
		paramMap.put("sanctnSts", (reqApprovalArr != null && !reqApprovalArr.isEmpty() && !"[]".equals(reqApprovalArr.trim())) ? "REQ" : "TEMP");
		paramMap.put("reqDt", getCurrentDate());

		int insertResult = pm07Mapper.insertVacation(paramMap);

		if (insertResult > 0) {
			List<Map<String, String>> approvalList = null;
			String approvalArr = paramMap.get("approvalArr");
			if (approvalArr != null && !approvalArr.isEmpty()) {
				Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
				Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				approvalList = gsonDtl.fromJson(approvalArr, dtlMap);

				for (Map<String, String> approval : approvalList) {
					approval.put("todoNo", reqNo);
					// WB20 결재선 순차결재 판정(BEFORE_NOT_CONFIRM)은 (TODO_NO, SALES_CD)로 같은 결재선을 묶어서 비교한다.
					// PM07은 SALES_CD 개념이 없는 모듈이라 문서키(reqNo)를 넣는 것이 관례인데, 신규 등록 시점엔
					// 프론트가 reqNo를 아직 몰라 빈 값으로 보낸다 - Oracle/Tibero는 빈 문자열을 NULL로 취급해
					// NULL=NULL 비교가 항상 거짓이 되므로, 서버가 발급한 reqNo로 여기서 강제로 채워야 한다.
					approval.put("salesCd", reqNo);
					approval.put("etcField1", reqNo);
					approval.put("todoTitl", paramMap.get("reqTitl"));
					if (!approval.containsKey("coCd") || approval.get("coCd") == null || approval.get("coCd").isEmpty()) approval.put("coCd", paramMap.get("coCd"));
					if (!approval.containsKey("todoCoCd") || approval.get("todoCoCd") == null || approval.get("todoCoCd").isEmpty()) approval.put("todoCoCd", paramMap.get("coCd"));
					boolean isShare = "공유".equals(approval.get("gb"));
					if (approval.get("todoDiv2CodeId") == null || approval.get("todoDiv2CodeId").isEmpty()) {
						approval.put("todoDiv2CodeId", isShare ? "TODODIV1300" : "TODODIV2300");
					}
					if (approval.get("todoDiv1CodeId") == null || approval.get("todoDiv1CodeId").isEmpty()) {
						approval.put("todoDiv1CodeId", isShare ? "TODODIV10" : "TODODIV20");
					}
					if (!approval.containsKey("userId") || approval.get("userId") == null || approval.get("userId").isEmpty()) approval.put("userId", paramMap.get("userId"));
					if (!approval.containsKey("pgmId") || approval.get("pgmId") == null || approval.get("pgmId").isEmpty()) approval.put("pgmId", "PM0701P01");

					String existingPgParam = approval.get("pgParam");
					if (existingPgParam != null && !existingPgParam.isEmpty()) {
						Type pgParamType = new TypeToken<Map<String, String>>() {}.getType();
						try {
							Map<String, String> pgParamMap = gsonDtl.fromJson(existingPgParam, pgParamType);
							pgParamMap.put("reqNo", reqNo);
							approval.put("pgParam", gsonDtl.toJson(pgParamMap));
						} catch (Exception e) {
							Map<String, String> pgParamMap = new HashMap<>();
							pgParamMap.put("reqNo", reqNo);
							approval.put("pgParam", gsonDtl.toJson(pgParamMap));
						}
					} else {
						Map<String, String> pgParamMap = new HashMap<>();
						pgParamMap.put("reqNo", reqNo);
						approval.put("pgParam", gsonDtl.toJson(pgParamMap));
					}
				}

				paramMap.put("approvalArr", gsonDtl.toJson(approvalList));
				paramMap.put("etcField1", reqNo);
				if (!paramMap.containsKey("todoNo") || paramMap.get("todoNo") == null) paramMap.put("todoNo", reqNo);
				if (!paramMap.containsKey("todoDiv2CodeId") || paramMap.get("todoDiv2CodeId") == null) paramMap.put("todoDiv2CodeId", "TODODIV2300");
				// 기안자 본인 자체승인 시 END 오판 방어 로직은 applyVacationApprovedInner 안으로
				// 이전됨(모든 호출 경로를 보호하고, 실시간 결재선을 직접 조회하는 더 정확한 판정).
				wb20Svc.insertTodoMaster(paramMap);
			}

			String vacDtArr = paramMap.get("vacDtArr");
			if (vacDtArr != null && !vacDtArr.isEmpty()) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Type vacDtType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> vacDtList = gson.fromJson(vacDtArr, vacDtType);
				for (Map<String, String> vacDt : vacDtList) {
					Map<String, String> insertDtMap = new HashMap<>();
					insertDtMap.put("coCd", paramMap.get("coCd"));
					insertDtMap.put("reqNo", reqNo);
					insertDtMap.put("vacDt", vacDt.get("vacDt"));
					insertDtMap.put("workHour", vacDt.get("workHour"));
					insertDtMap.put("userId", paramMap.get("userId"));
					pm07Mapper.insertVacationDates(insertDtMap);
				}
			}

			// 첨부파일 처리 (삭제분 먼저 반영 후 업로드 - COMON_CD = 'FITR9902' 무조건 고정)
			deleteAttachedFiles(paramMap.get("deleteFileArr"));
			paramMap.put("comonCd", "FITR9902");
			cm08Svc.uploadFile("PM0701P01", reqNo, mRequest);

			// 등록 시 결재자(TODODIV20, gb != '공유')가 존재하면 작업일보(TB_PM01M01)에 휴가 근태 자동 생성
			// (결재상태는 건드리지 않음 - ensureDailyWorkReport는 일지 생성만)
			boolean hasApprover = checkHasApprover(reqNo, approvalList);
			if (hasApprover) {
				Map<String, String> pm07Param = new HashMap<>();
				pm07Param.put("reqNo", reqNo);
				pm07Param.put("coCd", paramMap.get("coCd"));
				ensureDailyWorkReport(pm07Param);
			}

			result.put("resultCode", "200");
			result.put("resultMessage", "휴가신청이 등록되었습니다.");
			result.put("reqNo", reqNo);
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴가신청 등록에 실패했습니다.");
		}

		return result;
	}

	/**
	 * 수정/삭제 가능 여부를 서버에서 재조회해 판정한다.
	 * 프론트엔드(PC/모바일)에서 화면 로드 시점에 판단한 값은 그 사이 결재가 진행됐을 수 있어 신뢰할 수 없으므로,
	 * 실제 처리(수정/삭제) 직전에 DB 최신 결재선(TB_WB20M03)을 다시 조회해서 확인한다.
	 *
	 * 판정 기준: 결재선에 본인 외 결재자가 "등록"만 되어 있는 것은 무방하다(아직 순서를 기다리는 대기자일
	 * 뿐이므로). 본인 외 누군가가 "실제로 결재(승인/반려)를 완료"했을 때만 수정/삭제를 막는다.
	 * (SANCTN_STS 파생 문자열만으로 판단하면 안 된다 - 결재선에 본인 한 명만 있는 경우 COUNT(전체)==
	 * SUM(확정건)이 우연히 참이 되어 END(완료)로 잘못 계산되는 문제가 있다.)
	 */
	private boolean isVacationEditable(String coCd, String reqNo) {
		Map<String, String> dtlQuery = new HashMap<String, String>();
		dtlQuery.put("coCd", coCd);
		dtlQuery.put("reqNo", reqNo);
		Map<String, String> currentDtl = pm07Mapper.selectVacationDtl(dtlQuery);
		if (currentDtl == null) {
			return true; // 신청 본체를 찾을 수 없으면(신규 등록 등) 하위 처리에서 자연 실패하도록 통과
		}

		String reqId = currentDtl.get("reqId");

		Map<String, String> approvalQuery = new HashMap<String, String>();
		approvalQuery.put("todoNo", reqNo);
		List<Map<String, String>> approvalList = wb20Svc.selectGetApprovalList(approvalQuery);
		if (approvalList == null || approvalList.isEmpty()) {
			return true; // 결재선이 아직 없으면(임시저장) 편집/삭제 가능
		}

		for (Map<String, String> approval : approvalList) {
			String todoId = approval.get("todoId");
			String todoDiv1CodeId = approval.get("todoDiv1CodeId");
			boolean isApproverLine = (todoDiv1CodeId == null || todoDiv1CodeId.isEmpty() || "TODODIV20".equals(todoDiv1CodeId));
			if (!isApproverLine || todoId == null || todoId.equals(reqId)) {
				continue; // 본인이거나 공유선(TODODIV10)이면 판단 대상 아님
			}

			String sanctnSttus = approval.get("sanctnSttus");
			String sanctnSttusNm = approval.get("sanctnSttusNm");

			// 실제 상급 결재자 결재(승인/반려) 완료 여부 정밀 판정:
			// 1) SANCTN_STTUS 컬럼이 'Y'(승인) 이거나 'R'(반려) 인 경우
			// 2) SANCTN_STTUS_NM 이 '승인'('미승인' 제외) 이거나 '반려' 인 경우
			boolean isApprovedOrRejected = "Y".equalsIgnoreCase(sanctnSttus)
					|| "R".equalsIgnoreCase(sanctnSttus)
					|| (sanctnSttusNm != null && sanctnSttusNm.contains("승인") && !sanctnSttusNm.contains("미승인"))
					|| (sanctnSttusNm != null && sanctnSttusNm.contains("반려"));

			if (isApprovedOrRejected) {
				return false; // 본인 외 결재자가 실제로 결재(승인/반려)를 완료했으므로 수정/삭제 불가
			}
		}
		return true; // 본인 외에는 결재자가 여러 명 지정되어 있어도 아직 결재를 완료하지 않은 대기중 상태 -> 수정/삭제 100% 가능!
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> updateVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 마감 검증: stDt, edDt
		String stDt = paramMap.get("stDt");
		String edDt = paramMap.get("edDt");
		if ((stDt != null && !stDt.isEmpty()) || (edDt != null && !edDt.isEmpty())) {
			pm30Svc.assertNotClosed(stDt, edDt);
		}

		// 시작일/종료일 주말(휴일) 검증: 시작일과 종료일이 모두 주말이면 수정 불가
		if (isWeekend(stDt) && isWeekend(edDt)) {
			result.put("resultCode", "500");
			result.put("resultMessage", "시작일자(" + stDt + ")와 종료일자(" + edDt + ")가 모두 휴일(주말)이므로 저장할 수 없습니다.");
			return result;
		}

		// 백엔드 DB 저장 직전에 차감일수 및 휴가일수 최종 평가/산정
		evaluateVacationAndDeductDays(paramMap);

		// 포상휴가 잔여 검증
		String awardBalanceError = validateAwardVacationBalance(paramMap);
		if (awardBalanceError != null) {
			result.put("resultCode", "409");
			result.put("resultMessage", awardBalanceError);
			return result;
		}

		if (!isVacationEditable(paramMap.get("coCd"), paramMap.get("reqNo"))) {
			result.put("resultCode", "409");
			result.put("resultMessage", "이미 결재가 진행 중이거나 완료되어 수정할 수 없습니다. 화면을 새로고침 해주세요.");
			return result;
		}

		List<Map<String, String>> overlapList = findVacationOverlap(paramMap, paramMap.get("reqNo"));
		if (overlapList != null && !overlapList.isEmpty()) {
			result.put("resultCode", "409");
			result.put("resultMessage", "이전에 신청한 휴가일자(" + overlapList.get(0).get("stDt") + " ~ " + overlapList.get(0).get("edDt") + ")와 겹쳐 수정할 수 없습니다.");
			return result;
		}

		int updateResult = pm07Mapper.updateVacation(paramMap);

		if (updateResult > 0) {
			List<Map<String, String>> approvalList = null;
			String approvalArr = paramMap.get("approvalArr");
			if (approvalArr != null && !approvalArr.isEmpty()) {
				Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
				Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				approvalList = gsonDtl.fromJson(approvalArr, dtlMap);

				for (Map<String, String> approval : approvalList) {
					approval.put("todoNo", paramMap.get("reqNo"));
					// insertVacation과 동일한 이유로 SALES_CD를 문서키(reqNo)로 강제 고정한다
					// (프론트가 정상적으로 채워 보내는 값이지만, 서버에서도 방어적으로 보정한다).
					approval.put("salesCd", paramMap.get("reqNo"));
					approval.put("etcField1", paramMap.get("reqNo"));
					approval.put("todoTitl", paramMap.get("reqTitl"));
					if (!approval.containsKey("coCd") || approval.get("coCd") == null || approval.get("coCd").isEmpty()) approval.put("coCd", paramMap.get("coCd"));
					if (!approval.containsKey("todoCoCd") || approval.get("todoCoCd") == null || approval.get("todoCoCd").isEmpty()) approval.put("todoCoCd", paramMap.get("coCd"));
					boolean isShare = "공유".equals(approval.get("gb"));
					if (approval.get("todoDiv2CodeId") == null || approval.get("todoDiv2CodeId").isEmpty()) {
						approval.put("todoDiv2CodeId", isShare ? "TODODIV1300" : "TODODIV2300");
					}
					if (approval.get("todoDiv1CodeId") == null || approval.get("todoDiv1CodeId").isEmpty()) {
						approval.put("todoDiv1CodeId", isShare ? "TODODIV10" : "TODODIV20");
					}
					if (!approval.containsKey("userId") || approval.get("userId") == null || approval.get("userId").isEmpty()) approval.put("userId", paramMap.get("userId"));
					if (!approval.containsKey("pgmId") || approval.get("pgmId") == null || approval.get("pgmId").isEmpty()) approval.put("pgmId", "PM0701P01");

					String existingPgParam = approval.get("pgParam");
					if (existingPgParam != null && !existingPgParam.isEmpty()) {
						Type pgParamType = new TypeToken<Map<String, String>>() {}.getType();
						try {
							Map<String, String> pgParamMap = gsonDtl.fromJson(existingPgParam, pgParamType);
							pgParamMap.put("reqNo", paramMap.get("reqNo"));
							approval.put("pgParam", gsonDtl.toJson(pgParamMap));
						} catch (Exception e) {
							Map<String, String> pgParamMap = new HashMap<>();
							pgParamMap.put("reqNo", paramMap.get("reqNo"));
							approval.put("pgParam", gsonDtl.toJson(pgParamMap));
						}
					} else {
						Map<String, String> pgParamMap = new HashMap<>();
						pgParamMap.put("reqNo", paramMap.get("reqNo"));
						approval.put("pgParam", gsonDtl.toJson(pgParamMap));
					}
				}

				paramMap.put("approvalArr", gsonDtl.toJson(approvalList));
				paramMap.put("etcField1", paramMap.get("reqNo"));
				if (!paramMap.containsKey("todoNo") || paramMap.get("todoNo") == null) paramMap.put("todoNo", paramMap.get("reqNo"));
				if (!paramMap.containsKey("todoDiv2CodeId") || paramMap.get("todoDiv2CodeId") == null) paramMap.put("todoDiv2CodeId", "TODODIV2300");
				wb20Svc.insertTodoMaster(paramMap);
			}

			// 수정 시 기존 작업일지 삭제
			pm07Mapper.deleteDailyWorkReportByVacation(paramMap);

			pm07Mapper.deleteVacationDates(paramMap);
			String vacDtArr = paramMap.get("vacDtArr");
			if (vacDtArr != null && !vacDtArr.isEmpty()) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Type vacDtType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> vacDtList = gson.fromJson(vacDtArr, vacDtType);
				for (Map<String, String> vacDt : vacDtList) {
					Map<String, String> insertDtMap = new HashMap<>();
					insertDtMap.put("coCd", paramMap.get("coCd"));
					insertDtMap.put("reqNo", paramMap.get("reqNo"));
					insertDtMap.put("vacDt", vacDt.get("vacDt"));
					insertDtMap.put("workHour", vacDt.get("workHour"));
					insertDtMap.put("userId", paramMap.get("userId"));
					pm07Mapper.insertVacationDates(insertDtMap);
				}
			}

			// 수정 시 결재자(TODODIV20, gb != '공유')가 존재하면 새로 세팅된 일자로 작업일보(TB_PM01M01) 휴가 자동 생성/갱신
			// (결재상태는 건드리지 않음 - ensureDailyWorkReport는 일지 생성만)
			boolean hasApprover = checkHasApprover(paramMap.get("reqNo"), approvalList);
			if (hasApprover) {
				Map<String, String> pm07Param = new HashMap<>();
				pm07Param.put("reqNo", paramMap.get("reqNo"));
				pm07Param.put("coCd", paramMap.get("coCd"));
				ensureDailyWorkReport(pm07Param);
			}

			// 첨부파일 처리 (COMON_CD = 'FITR9902' 무조건 고정)
			paramMap.put("comonCd", "FITR9902");
			cm08Svc.uploadFile("PM0701P01", paramMap.get("reqNo"), mRequest);

			result.put("resultCode", "200");
			result.put("resultMessage", "휴가신청이 수정되었습니다.");
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴가신청 수정에 실패했습니다.");
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> deleteVacation(Map<String, String> paramMap) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 0. 삭제 시도 대상 자료가 이미 다른 화면에서 삭제되어 DB에 존재하지 않는 경우
		Map<String, String> dtlQuery = new HashMap<String, String>();
		dtlQuery.put("coCd", paramMap.get("coCd"));
		dtlQuery.put("reqNo", paramMap.get("reqNo"));
		Map<String, String> currentDtl = pm07Mapper.selectVacationDtl(dtlQuery);
		if (currentDtl == null || currentDtl.isEmpty()) {
			result.put("resultCode", "200");
			result.put("resultMessage", "이미 삭제완료된 상태입니다.");
			return result;
		}

		// 마감 검증: 기존 DB의 stDt, edDt
		String stDt = currentDtl.get("stDt");
		String edDt = currentDtl.get("edDt");
		if ((stDt != null && !stDt.isEmpty()) || (edDt != null && !edDt.isEmpty())) {
			pm30Svc.assertNotClosed(stDt, edDt);
		}

		// 프론트엔드(PC 목록화면/모바일 상세화면)에서 화면 로드 시점에 판단한 삭제가능 여부는
		// 그 사이 다른 결재자가 처리했을 수 있어 신뢰할 수 없으므로, 삭제 직전 서버에서 재검증한다.
		if (!isVacationEditable(paramMap.get("coCd"), paramMap.get("reqNo"))) {
			result.put("resultCode", "409");
			result.put("resultMessage", "이미 결재가 진행 중이거나 완료되어 삭제할 수 없습니다. 화면을 새로고침 해주세요.");
			return result;
		}

		// 1. 해당 휴가신청건으로 작업일보(TB_PM01M01)에 등록되었던 근태 자료 자동 제거
		pm07Mapper.deleteDailyWorkReportByVacation(paramMap);

		// 2. 결재선 Master 및 Detail 삭제 (TODO_NO = REQ_NO 매칭)
		paramMap.put("todoNo", paramMap.get("reqNo"));
		// 2-1. 결재선 (TODODIV2300) 삭제
		paramMap.put("todoDiv2CodeId", "TODODIV2300");
		wb20Svc.deleteTodoMasterByTodoNo(paramMap);
		// 2-2. 공유선 (TODODIV1300) 삭제
		paramMap.put("todoDiv2CodeId", "TODODIV1300");
		wb20Svc.deleteTodoMasterByTodoNo(paramMap);
		// 2-3. 혹시 다른 결재/공유코드로 등록된 잔여 결재선까지 REQ_NO 기준으로 100% CASCADE 삭제
		pm07Mapper.deleteApprovalLineByReqNo(paramMap);

		// 3. 휴가 일자 디테일 및 휴가 신청 본체 삭제
		pm07Mapper.deleteVacationDates(paramMap);
		int deleteResult = pm07Mapper.deleteVacation(paramMap);

		// 4. 첨부파일 DB 레코드 및 서버 물리 파일 연쇄 삭제 (FILE_TRGT_TYP = 'PM0701P01' & FILE_TRGT_KEY = REQ_NO)
		try {
			Map<String, String> fileSearchMap = new HashMap<>();
			fileSearchMap.put("fileTrgtTyp", "PM0701P01");
			fileSearchMap.put("fileTrgtKey", paramMap.get("reqNo"));
			List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(fileSearchMap);
			if (deleteFileList != null && !deleteFileList.isEmpty()) {
				for (Map<String, String> delFile : deleteFileList) {
					String fKey = delFile.get("fileKey");
					if (fKey == null || fKey.isEmpty()) {
						fKey = delFile.get("file_key");
					}
					if (fKey == null || fKey.isEmpty()) {
						fKey = delFile.get("FILE_KEY");
					}
					if (fKey != null && !fKey.isEmpty()) {
						cm08Svc.deleteFile(fKey);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 해당 자료가 이미 삭제되어 없던 상태(deleteResult == 0)이어도 오류 500을 뱉지 않고 200 정상 코드로 종료
		result.put("resultCode", "200");
		if (deleteResult > 0) {
			result.put("resultMessage", "휴가신청이 삭제되었습니다.");
		} else {
			result.put("resultMessage", "이미 삭제완료된 상태입니다.");
		}

		return result;
	}

	@Override
	public Map<String, String> selectAnnualBalance(Map<String, String> paramMap) {
		Map<String, String> result = new HashMap<String, String>();

		try {
			String yy = paramMap.getOrDefault("yy", String.valueOf(LocalDate.now().getYear()));
			paramMap.put("yy", yy);

			Map<String, String> balanceInfo = pm07Mapper.selectAnnualBalanceInfo(paramMap);

			if (balanceInfo != null) {
				result.put("resultCode", "200");
				String autoYn = balanceInfo.get("autoYn");
				String enterDt = balanceInfo.get("enterDt");
				int grantDays = 0;

				if ("N".equals(autoYn) && balanceInfo.get("grantDays") != null && Double.parseDouble(String.valueOf(balanceInfo.get("grantDays"))) > 0) {
					// 관리자 수기 지정인 경우 DB 저장값 사용
					grantDays = (int) Double.parseDouble(String.valueOf(balanceInfo.get("grantDays")));
				} else {
					// 자동계산: TB_PM07M02 존재 여부와 무관하게 1년 미만자 및 일반 사원 실시간 오늘 기준 발생일수 산출
					grantDays = calcRealtimeAnnualGrantDays(enterDt, yy, LocalDate.now());
				}

				double usedDays = Double.parseDouble(String.valueOf(balanceInfo.getOrDefault("usedDays", "0")));
				double workSubstDays = Double.parseDouble(String.valueOf(balanceInfo.getOrDefault("workSubstDays", "0")));
				double summerVacDays = Double.parseDouble(String.valueOf(balanceInfo.getOrDefault("summerVacDays", "0")));
				double balanceDays = grantDays - workSubstDays - summerVacDays - usedDays;

				result.put("grantDays", String.valueOf(grantDays));
				result.put("usedDays", String.valueOf(usedDays));
				result.put("balanceDays", String.valueOf(balanceDays));
			} else {
				result.put("resultCode", "200");
				result.put("grantDays", "0");
				result.put("usedDays", "0");
				result.put("balanceDays", "0");
			}
		} catch (Exception e) {
			result.put("resultCode", "500");
			result.put("resultMessage", "연차 조회에 실패했습니다.");
		}

		return result;
	}

	/**
	 * 실시간 조회 시점 기준 발생 연차일수 산출 (1년 미만자 조회시점 1~11개 및 15 만근 근무자 15~25개 계산)
	 * TB_PM07M02 데이터 존재 유무와 무관하게 동작
	 */
	public int calcRealtimeAnnualGrantDays(String enterDt, String yy, LocalDate baseDate) {
		if (enterDt == null || enterDt.isEmpty() || yy == null || yy.isEmpty()) {
			return 0;
		}

		try {
			String cleanDt = enterDt.replaceAll("[^0-9]", "");
			if (cleanDt.length() < 8) return 0;

			int enterYear = Integer.parseInt(cleanDt.substring(0, 4));
			int enterMonth = Integer.parseInt(cleanDt.substring(4, 6));
			int enterDay = Integer.parseInt(cleanDt.substring(6, 8));
			int targetYear = Integer.parseInt(yy);

			LocalDate enterLocalDate = LocalDate.of(enterYear, enterMonth, enterDay);
			LocalDate now = (baseDate != null) ? baseDate : LocalDate.now();

			// 미래 입사자: 0일
			if (enterYear > targetYear || enterLocalDate.isAfter(now)) {
				return 0;
			}

			// 1. 15 만근 근무자 판별 (15일 발생 대상):
			// 회계연도(01/01) 기준 직전 1년(전년도 1/1 ~ 12/31) 전체 만근 여부 판별:
			// -> (targetYear - 2)년 12월 31일 이하 입사자 (즉, 전년도 1월 1일 이전 입사자)
			//    예) 2026년 기준: 2024-12-31 이하 입사자 (2025년 입사자는 2026년 중 최대 11개 월별 누적)
			//    예) 2027년 기준: 2025-12-31 이하 입사자 (2025년 입사자 전원 15개 정상 발생)
			LocalDate cutoffDate = LocalDate.of(targetYear - 2, 12, 31);
			boolean is15ManKeun = !enterLocalDate.isAfter(cutoffDate);

			if (is15ManKeun) {
				LocalDate yearStartDate = LocalDate.of(targetYear, 1, 1);
				long workedYears = ChronoUnit.YEARS.between(enterLocalDate, yearStartDate);
				int addDays = (int) (workedYears - 1) / 2;
				return Math.min(15 + addDays, 25);
			}

			// 2. 15 만근 근무자가 아닌 사람 (부여연도 1월 1일 기준 만 1년 미만자, 예: 2026년 기준 2025-01-01 이후 입사자):
			//    입사일 기준 만 1개월 넘을 때마다 +1개씩 (최대 11개) 발생하여 12/31까지 유지,
			//    다음해 1월 1일(부여연도 1월 1일 기준 만 1년 경과 시점)에 15개 발생
			LocalDate yearEndDate = LocalDate.of(targetYear, 12, 31);
			LocalDate evalDate = now.isAfter(yearEndDate) ? yearEndDate : now;

			int passedMonths = 0;
			LocalDate checkDate = enterLocalDate;
			for (int m = 1; m <= 11; m++) {
				checkDate = checkDate.plusMonths(1);
				if (!checkDate.isAfter(evalDate)) {
					passedMonths++;
				} else {
					break;
				}
			}
			return Math.min(passedMonths, 11);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public Map<String, String> saveAnnualGrant(Map<String, String> paramMap) {
		Map<String, String> result = new HashMap<String, String>();

		try {
			int existCount = pm07Mapper.selectAnnualGrantCount(paramMap);

			if (existCount > 0) {
				Map<String, String> existing = pm07Mapper.selectAnnualGrantByUser(paramMap);
				if (existing != null && "N".equals(existing.get("autoYn"))) {
					result.put("resultCode", "200");
					result.put("resultMessage", "관리자 수기 보정 상태로, 자동산정으로 덮어쓰지 않습니다.");
					return result;
				}

				int updateResult = pm07Mapper.updateAnnualGrant(paramMap);
				if (updateResult > 0) {
					result.put("resultCode", "200");
					result.put("resultMessage", "연차부여일수가 수정되었습니다.");
				} else {
					result.put("resultCode", "500");
					result.put("resultMessage", "연차부여일수 수정에 실패했습니다.");
				}
			} else {
				int insertResult = pm07Mapper.insertAnnualGrant(paramMap);
				if (insertResult > 0) {
					result.put("resultCode", "200");
					result.put("resultMessage", "연차부여일수가 저장되었습니다.");
				} else {
					result.put("resultCode", "500");
					result.put("resultMessage", "연차부여일수 저장에 실패했습니다.");
				}
			}
		} catch (Exception e) {
			result.put("resultCode", "500");
			result.put("resultMessage", "연차부여일수 저장에 실패했습니다.");
		}

		return result;
	}

	@Override
	public List<Map<String, String>> selectAnnualGrantList(Map<String, String> paramMap) {
		return pm07Mapper.selectAnnualGrantList(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> saveAnnualGrantList(Map<String, Object> paramMap) {
		Map<String, String> result = new HashMap<>();
		try {
			List<Map<String, String>> saveList = (List<Map<String, String>>) paramMap.get("saveList");
			List<Map<String, String>> deleteList = (List<Map<String, String>>) paramMap.get("deleteList");

			if (deleteList != null && !deleteList.isEmpty()) {
				for (Map<String, String> delItem : deleteList) {
					pm07Mapper.deleteAnnualGrant(delItem);
				}
			}

			if (saveList != null && !saveList.isEmpty()) {
				for (Map<String, String> item : saveList) {
					if (!item.containsKey("coCd") || item.get("coCd") == null || item.get("coCd").toString().isEmpty()) {
						item.put("coCd", "GUN");
					}
					if (!item.containsKey("autoYn") || item.get("autoYn") == null) {
						item.put("autoYn", "N");
					}
					if (!item.containsKey("rmk") || item.get("rmk") == null) {
						item.put("rmk", "");
					}
					int cnt = pm07Mapper.selectAnnualGrantCount(item);
					if (cnt > 0) {
						pm07Mapper.updateAnnualGrant(item);
					} else {
						pm07Mapper.insertAnnualGrant(item);
					}
				}
			}

			result.put("resultCode", "200");
			result.put("resultMessage", "저장되었습니다.");
		} catch (Exception e) {
			result.put("resultCode", "500");
			result.put("resultMessage", "저장 중 오류가 발생했습니다: " + e.getMessage());
		}
		return result;
	}

	@Override
	public int calcAnnualGrantDays(String enterDt, String yy) {
		if (enterDt == null || enterDt.isEmpty() || yy == null || yy.isEmpty()) {
			return 15;
		}

		try {
			String cleanDt = enterDt.replaceAll("[^0-9]", "");
			if (cleanDt.length() < 8) return 15;

			int enterYear = Integer.parseInt(cleanDt.substring(0, 4));
			int enterMonth = Integer.parseInt(cleanDt.substring(4, 6));
			int enterDay = Integer.parseInt(cleanDt.substring(6, 8));
			int targetYear = Integer.parseInt(yy);

			LocalDate enterLocalDate = LocalDate.of(enterYear, enterMonth, enterDay);
			LocalDate now = LocalDate.now();

			if (enterYear > targetYear || enterLocalDate.isAfter(now)) {
				return 0; // 미래 입사자
			}

			int diffYears = targetYear - enterYear;

			// 부여연도 1월 1일 기준 15 만근 근무자 판별:
			// 회계연도(01/01) 기준 직전 1년(전년도 1/1 ~ 12/31) 전체 만근 여부 판별:
			// -> (targetYear - 2)년 12월 31일 이하 입사자 (즉, 전년도 1월 1일 이전 입사자)
			//    예) 2026년 기준: 2024-12-31 이하 입사자 (2025년 입사자는 2026년 중 최대 11개 월별 누적)
			//    예) 2027년 기준: 2025-12-31 이하 입사자 (2025년 입사자 전원 15개 정상 발생)
			LocalDate cutoffDate = LocalDate.of(targetYear - 2, 12, 31);
			boolean is15ManKeun = !enterLocalDate.isAfter(cutoffDate);

			// 1. 15 만근 근무자인 경우: 15일 부여 + 3년차 이상 만 2년마다 1개씩 추가 (최대 25일)
			if (is15ManKeun) {
				LocalDate yearStartDate = LocalDate.of(targetYear, 1, 1);
				long workedYears = ChronoUnit.YEARS.between(enterLocalDate, yearStartDate);
				int addDays = (int) (workedYears - 1) / 2;
				return Math.min(15 + addDays, 25);
			}

			// 2. 15 만근 근무자가 아니면(2025-01-01 이후 입사자): DB에는 0개로 저장됨 (화면 로드 시 자동계산 표출)
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}

	// 미등록 재직자 연차지급기준 자동계산 목록 산출 (15 만근 근무자만 자동계산 목록 대상)
	@Override
	public List<Map<String, String>> selectAutoCalcAnnualGrantList(Map<String, String> paramMap) {
		String yy = paramMap.get("yy");
		String coCd = paramMap.get("coCd");
		if (coCd == null || coCd.isEmpty()) {
			coCd = "GUN";
			paramMap.put("coCd", coCd);
		}

		List<Map<String, String>> unregList = pm07Mapper.selectUnregisteredUserList(paramMap);
		List<Map<String, String>> resultList = new ArrayList<>();
		if (unregList != null && !unregList.isEmpty()) {
			int targetYear = (yy != null && !yy.isEmpty()) ? Integer.parseInt(yy) : LocalDate.now().getYear();
			LocalDate cutoffDate = LocalDate.of(targetYear - 1, 1, 1);

			for (Map<String, String> user : unregList) {
				String enterDt = user.get("enterDt");
				String cleanEnterDt = (enterDt != null) ? enterDt.replaceAll("[^0-9]", "") : "";
				int grantDays = 0;
				if (cleanEnterDt.length() >= 8) {
					grantDays = calcAnnualGrantDays(cleanEnterDt, yy);
				}

				user.put("coCd", coCd);
				user.put("yy", yy);
				user.put("grantDays", String.valueOf(grantDays));
				user.put("usedDays", "0");
				user.put("balanceDays", String.valueOf(grantDays));
				user.put("autoYn", "Y");
				user.put("rmk", (grantDays > 0) ? "1월 1일 기준 만1년이상 자동계산" : "1년미만 (기본0개, 조회시점 자동계산)");
				resultList.add(user);
			}
		}
		return resultList;
	}

	// 화면에서 삭제한 첨부파일 반영. (CM16SvcImpl 과 동일 패턴)
	// 이 처리가 없으면 화면에서 첨부를 지워도 서버에는 그대로 남는다.
	private void deleteAttachedFiles(String deleteFileArrJson) {
		if (deleteFileArrJson == null || deleteFileArrJson.isEmpty()) return;
		try {
			String[] fileKeys = new Gson().fromJson(deleteFileArrJson, String[].class);
			if (fileKeys == null) return;
			for (String fileKey : fileKeys) {
				if (fileKey != null && !fileKey.isEmpty()) {
					cm08Svc.deleteFile(fileKey);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 일지 생성 없이 결재상태만 END 로 확정한다. (부서 업무코드/휴가일자가 없어 일지를 못 만드는 경우)
	private void markApprovalEnd(String coCd, String reqNo) {
		Map<String, String> endMap = new HashMap<>();
		endMap.put("coCd", coCd);
		endMap.put("reqNo", reqNo);
		endMap.put("sanctnSts", "END");
		pm07Mapper.updateVacationApprovalStatus(endMap);
	}

	// 작업일보(TB_PM01M01) 생성만 담당. SANCTN_STS를 건드리지 않는다.
	// CODE_ETC 유무에 따라 휴가(9901) 또는 기타(9903) 업무코드로 일지 INSERT.
	// 생성된 일지 건수를 반환.
	private int generateDailyWorkReport(String coCd, String reqNo, Map<String, String> vacationInfo) {
		if (vacationInfo == null) {
			return 0;
		}

		String reqId = vacationInfo.get("reqId");
		String reqDeptId = vacationInfo.get("reqDeptId");
		String reqRmk = vacationInfo.get("reqRmk");
		String codeEtc = vacationInfo.get("codeEtc");
		String vacTypeNm = vacationInfo.get("vacTypeNm");
		String ampmNm = vacationInfo.get("ampmNm");
		String stTm = vacationInfo.get("stTm");
		String edTm = vacationInfo.get("edTm");
		String rawStTm = vacationInfo.get("rawStTm");
		String rawEdTm = vacationInfo.get("rawEdTm");

		// 비고 내용 조립: [휴가유형] [구분] (시각: stTm~edTm) 사유
		StringBuilder rmkSb = new StringBuilder();
		if (vacTypeNm != null && !vacTypeNm.trim().isEmpty()) {
			rmkSb.append("[").append(vacTypeNm.trim()).append("]");
		}
		if (ampmNm != null && !ampmNm.trim().isEmpty()) {
			if (rmkSb.length() > 0) rmkSb.append(" ");
			rmkSb.append("[").append(ampmNm.trim()).append("]");
		}
		if (stTm != null && !stTm.trim().isEmpty() && edTm != null && !edTm.trim().isEmpty()) {
			if (rmkSb.length() > 0) rmkSb.append(" ");
			rmkSb.append("(").append(stTm.trim()).append(" ~ ").append(edTm.trim()).append(")");
		}
		if (reqRmk != null && !reqRmk.trim().isEmpty()) {
			if (rmkSb.length() > 0) rmkSb.append(" ");
			rmkSb.append(reqRmk.trim());
		}
		String finalRmk = rmkSb.toString();

		Map<String, String> workCodeMap = new HashMap<>();
		workCodeMap.put("deptId", reqDeptId);
		Map<String, String> workRptCode = pm07Mapper.selectWorkRptCodeByDept(workCodeMap);

		String workRptL = (workRptCode != null) ? workRptCode.get("workRptL") : null;
		String workRptM = (workRptCode != null) ? workRptCode.get("workRptM") : null;
		String workRptS = (workRptCode != null) ? workRptCode.get("workRptS") : null;

		String dept5 = "GUN00";
		if (reqDeptId != null && !reqDeptId.trim().isEmpty()) {
			String cleanDept = reqDeptId.trim();
			if (cleanDept.length() >= 5) {
				dept5 = cleanDept.substring(0, 5); // 예: GUN76, GUN30, GUN80 등
			} else {
				dept5 = (cleanDept + "00000").substring(0, 5);
			}
		}

		// CODE_ETC 에 값이 있으면 휴가(9901), CODE_ETC 가 null/빈값이면 부서코드 + 9903
		boolean isVacationCode = (codeEtc != null && !codeEtc.trim().isEmpty());
		if (isVacationCode) {
			if (workRptS == null || workRptS.isEmpty()) {
				workRptS = dept5 + "9901";
			}
		} else {
			workRptS = dept5 + "9903";
		}

		if (workRptM == null || workRptM.isEmpty()) {
			workRptM = dept5 + "99";
		}
		if (workRptL == null || workRptL.isEmpty()) {
			workRptL = dept5;
		}

		Map<String, String> vacDateQuery = new HashMap<>();
		vacDateQuery.put("coCd", coCd);
		vacDateQuery.put("reqNo", reqNo);
		List<Map<String, String>> vacDateList = pm07Mapper.selectVacationDateList(vacDateQuery);

		if (vacDateList == null || vacDateList.isEmpty()) {
			return 0;
		}

		// CODE_ETC 가 null 인 경우 시간 계산 (시각이 있으면 계산, 없으면 1시간)
		double calcHours = 0;
		if (!isVacationCode) {
			calcHours = calculateTimeDiffInHours(stTm, edTm, rawStTm, rawEdTm);
		}

		int result = 0;
		Map<String, String> emptyParamMap = new HashMap<>();
		for (Map<String, String> vacDate : vacDateList) {
			String workRptNo = pm07Mapper.selectDailyWorkReportSeqNext(emptyParamMap);
			String fileTrgtKey = pm07Mapper.selectFileTrgtKeyNext();
			String vacDt = vacDate.get("vacDt");
			String workHour = vacDate.get("workHour");
			String vacCoCd = vacDate.get("coCd");

			if (!isVacationCode) {
				if (calcHours > 0) {
					workHour = (calcHours == (long) calcHours) ? String.valueOf((long) calcHours) : String.valueOf(calcHours);
				} else {
					workHour = "1";
				}
			}

			Map<String, String> insertMap = new HashMap<>();
			insertMap.put("fileTrgtKey", fileTrgtKey);
			insertMap.put("coCd", coCd);
			insertMap.put("workRptNo", workRptNo);
			insertMap.put("workRptId", reqId);
			insertMap.put("workRptDt", vacDt);
			insertMap.put("workRptL", workRptL);
			insertMap.put("workRptM", workRptM);
			insertMap.put("workRptS", workRptS);
			insertMap.put("workRptHour", workHour);
			insertMap.put("workRptRmk", finalRmk);
			insertMap.put("issueYn", "N");
			insertMap.put("userId", reqId);

			pm07Mapper.insertDailyWorkReport(insertMap);

			// TB_PM07M03의 해당 일자에 생성된 작업일보번호 기록
			Map<String, String> updateWorkRptNoMap = new HashMap<>();
			updateWorkRptNoMap.put("coCd", vacCoCd);
			updateWorkRptNoMap.put("reqNo", reqNo);
			updateWorkRptNoMap.put("vacDt", vacDt);
			updateWorkRptNoMap.put("workRptNo", workRptNo);
			pm07Mapper.updateVacationDateWorkRptNo(updateWorkRptNoMap);

			result++;
		}
		return result;
	}

	private double calculateTimeDiffInHours(String stTmStr, String edTmStr, String rawStTm, String rawEdTm) {
		String st = (rawStTm != null && !rawStTm.trim().isEmpty()) ? rawStTm : stTmStr;
		String ed = (rawEdTm != null && !rawEdTm.trim().isEmpty()) ? rawEdTm : edTmStr;
		if (st == null || ed == null) return 0;

		st = st.replaceAll("[^0-9]", "");
		ed = ed.replaceAll("[^0-9]", "");

		if (st.length() >= 4 && ed.length() >= 4) {
			try {
				int stH = Integer.parseInt(st.substring(0, 2));
				int stM = Integer.parseInt(st.substring(2, 4));
				int edH = Integer.parseInt(ed.substring(0, 2));
				int edM = Integer.parseInt(ed.substring(2, 4));

				int totalStMins = stH * 60 + stM;
				int totalEdMins = edH * 60 + edM;
				int diffMins = totalEdMins - totalStMins;

				if (diffMins > 0) {
					double hours = diffMins / 60.0;
					return Math.round(hours * 10.0) / 10.0;
				}
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	// 등록/수정 시 결재자가 있으면 작업일보만 생성/재생성한다. 결재상태는 건드리지 않는다.
	// (최종 승인 시 상태 전이는 applyVacationApprovedInner가 담당)
	@Override
	public int ensureDailyWorkReport(Map<String, String> paramMap) {
		String reqNo = paramMap.get("reqNo");
		String coCd = paramMap.get("coCd");

		if (reqNo == null || reqNo.trim().isEmpty()) {
			return 0;
		}

		// selectVacationDtl은 Mapper 호출 (서비스는 JSON 변환을 추가하므로 직접 Mapper 호출)
		Map<String, String> dtlQuery = new HashMap<>();
		dtlQuery.put("coCd", coCd);
		dtlQuery.put("reqNo", reqNo);
		Map<String, String> vacationInfo = pm07Mapper.selectVacationDtl(dtlQuery);

		if (vacationInfo == null) {
			return 0;
		}

		// 기존 일지 삭제 (멱등성 보장)
		try {
			pm07Mapper.deleteDailyWorkReportByVacation(paramMap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 새로운 일지 생성
		int count = generateDailyWorkReport(coCd, reqNo, vacationInfo);

		// workRptYn만 갱신 (sanctnSts는 건드리지 않음)
		if (count > 0) {
			Map<String, String> updateYnMap = new HashMap<>();
			updateYnMap.put("coCd", coCd);
			updateYnMap.put("reqNo", reqNo);
			updateYnMap.put("workRptYn", "Y");
			pm07Mapper.updateVacationApprovalStatus(updateYnMap);
		}

		return count;
	}

	// WB20 결재처리(insertApprovalLine)에서 호출된다.
	//
	// 여기에 REQUIRES_NEW 를 붙이면 안 된다. 바깥 결재 트랜잭션이 살아있는 채로 별도 커넥션/트랜잭션이
	// 열려 같은 TB_PM07M01 행을 건드리게 되고, 결재 승인이 무한 대기(자체 교착)에 빠진다.
	// (HikariCP maximum-pool-size=5 인데 요청당 커넥션을 2개 잡아 몇 건만 쌓여도 풀이 고갈된다.)
	// PM51 의 updatePm51AprvSts 도 같은 위치에서 바깥 트랜잭션 안에서 그대로 실행된다 - 동일하게 맞춘다.
	//
	// 대신 이 메서드는 예외를 밖으로 던지지 않는다. 던지면 REQUIRED 특성상 결재 트랜잭션이
	// rollback-only 로 마킹되어 호출부에서 catch 해도 결재 전체가 롤백되기 때문이다.
	@Override
	public int applyVacationApproved(Map<String, String> paramMap) {
		try {
			return applyVacationApprovedInner(paramMap);
		} catch (Exception e) {
			// 결재 트랜잭션에 예외를 전파하지 않는다 (설계서 §4-3: 후처리 실패해도 결재는 정상 처리).
			e.printStackTrace();
			return 0;
		}
	}

	private int applyVacationApprovedInner(Map<String, String> paramMap) {
		int result = 0;

		// 일일업무일지 반영: TB_PM07M03 (영업일 판정은 화면의 workingDayCalc.js listWorkingDays() 결과)

		String todoYn = paramMap.get("todoYn");
		String reqNo = paramMap.get("todoNo");
		// 결함 1: todoNo와 reqNo 둘 다 허용
		if (reqNo == null || reqNo.trim().isEmpty()) {
			reqNo = paramMap.get("reqNo");
		}
		paramMap.put("reqNo", reqNo); // deleteDailyWorkReportByVacation 등 쿼리 바인딩용 필수 주입
		String coCd = paramMap.get("coCd");

		// double-brace 익명 클래스 제거 (effectively final 제약 해제)
		Map<String, String> dtlQuery = new HashMap<>();
		dtlQuery.put("coCd", coCd);
		dtlQuery.put("reqNo", reqNo);
		Map<String, String> vacationInfo = pm07Mapper.selectVacationDtl(dtlQuery);

		if (vacationInfo == null) {
			return 0;
		}

		// [신규 등록 시점 기안자 자체승인 오판 방어]
		// todoYn == "Y" 로 넘어왔더라도, 결재선에 상급 결재자(SANCTN_SN > 1, gb != '공유')가 존재하는데
		// 아직 미승인 상태라면 이는 실제 최종 완료가 아니라 기안자 본인 행만 먼저 들어가서 발생한 오판이다.
		// 이 경우 END로 확정하지 않고 신청(REQ) 상태를 유지한다.
		if ("Y".equals(todoYn)) {
			Map<String, String> apprQuery = new HashMap<>();
			apprQuery.put("todoNo", reqNo);
			List<Map<String, String>> currentApprList = wb20Svc.selectGetApprovalList(apprQuery);
			boolean hasPendingUpperApprover = false;
			if (currentApprList != null && !currentApprList.isEmpty()) {
				for (Map<String, String> app : currentApprList) {
					String snStr = app.get("sanctnSn") != null ? String.valueOf(app.get("sanctnSn")) : String.valueOf(app.get("SANCTN_SN"));
					int sn = 0;
					try { sn = Integer.parseInt(snStr); } catch (Exception ignored) {}
					String div1 = app.get("todoDiv1CodeId") != null ? String.valueOf(app.get("todoDiv1CodeId")) : String.valueOf(app.get("TODO_DIV1_CODE_ID"));
					boolean isApprover = (div1 == null || div1.isEmpty() || "TODODIV20".equals(div1));
					if (sn > 1 && isApprover) {
						String sttus = app.get("sanctnSttus") != null ? String.valueOf(app.get("sanctnSttus")) : String.valueOf(app.get("SANCTN_STTUS"));
						if (!"Y".equalsIgnoreCase(sttus)) {
							hasPendingUpperApprover = true;
							break;
						}
					}
				}
			}
			if (hasPendingUpperApprover) {
				Map<String, String> reqMap = new HashMap<>();
				reqMap.put("coCd", coCd);
				reqMap.put("reqNo", reqNo);
				reqMap.put("sanctnSts", "REQ");
				pm07Mapper.updateVacationApprovalStatus(reqMap);
				return 1;
			}
		}

		// 중간 결재자가 승인한 시점(todoYn != "Y")에는 상태만 갱신하고 일일업무일지는 만들지 않는다.
		// selectVacationDtl 이 이미 실시간 결재선(TB_WB20M03) 기준으로 REQ/ING/RTN 을 정확히 계산해서
		// vacationInfo.sanctnSts 로 내려주므로, 그 값을 그대로 쓴다 (하드코딩된 "ING"를 쓰면 기안자
		// 본인 자체승인만 된 신규 등록 시점에도 "진행중"으로 앞서가 버리는 문제가 있다 - 실제 상급
		// 결재자가 한 명도 승인하지 않았다면 여전히 REQ 여야 한다).
		if (!"Y".equals(todoYn)) {
			String liveSts = vacationInfo.get("sanctnSts");
			Map<String, String> ingMap = new HashMap<>();
			ingMap.put("coCd", coCd);
			ingMap.put("reqNo", reqNo);
			ingMap.put("sanctnSts", (liveSts != null && !liveSts.isEmpty()) ? liveSts : "ING");
			pm07Mapper.updateVacationApprovalStatus(ingMap);
			return 1;
		}

		if ("Y".equals(todoYn)) {
			// TB_PM07M01 갱신은 이 메서드 끝에서 단 한 번만 한다.
			// (SANCTN_STS 를 먼저 UPDATE 하고 일지 생성 후 WORK_RPT_YN 을 또 UPDATE 하면
			//  같은 행을 한 트랜잭션 안에서 두 번 잠그게 되어, 중간에 다른 커넥션이 끼어들 경우
			//  REQUIRES_NEW 로 분리된 이 트랜잭션과 바깥 결재 트랜잭션 사이에 교착이 생길 수 있다.)

			// 결함 3: 기존 일지 삭제 (등록/수정 경로에서 이미 생성되었을 수 있음)
			try {
				Map<String, String> delMap = new HashMap<>(paramMap);
				delMap.put("reqNo", reqNo);
				delMap.put("coCd", coCd);
				pm07Mapper.deleteDailyWorkReportByVacation(delMap);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 결함 2: 일지 생성 로직 분리
			result = generateDailyWorkReport(coCd, reqNo, vacationInfo);

			if (result > 0) {
				// updateVacation(전체 필드 UPDATE)을 쓰면 안 된다 - 넘기지 않은 컬럼이 전부 NULL 로 덮여
				// 승인 즉시 휴가신청 내용이 지워진다. 상태 전용 UPDATE 를 쓴다.
				// SANCTN_STS 와 WORK_RPT_YN 을 여기서 한 번에 갱신한다 (같은 행 중복 UPDATE 방지).
				Map<String, String> updateYnMap = new HashMap<>();
				updateYnMap.put("coCd", coCd);
				updateYnMap.put("reqNo", reqNo);
				updateYnMap.put("sanctnSts", "END");
				updateYnMap.put("workRptYn", "Y");
				pm07Mapper.updateVacationApprovalStatus(updateYnMap);
			} else {
				// 일지를 못 만든 경우 상태만 END로 확정
				markApprovalEnd(coCd, reqNo);
				result = 1;
			}
		}

		return result;
	}

	private String getCurrentDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		return LocalDate.now().format(formatter);
	}

	private boolean checkHasApprover(String reqNo, List<Map<String, String>> approvalList) {
		if (approvalList != null && !approvalList.isEmpty()) {
			for (Map<String, String> app : approvalList) {
				if (!"공유".equals(app.get("gb")) && !"TODODIV10".equals(app.get("todoDiv1CodeId"))) {
					return true;
				}
			}
		}
		if (reqNo != null && !reqNo.trim().isEmpty()) {
			Map<String, String> qMap = new HashMap<>();
			qMap.put("reqNo", reqNo);
			int count = pm07Mapper.selectApprovalCount(qMap);
			if (count > 0) return true;
		}
		return false;
	}

	@Override
	public List<Map<String, String>> selectAnnualUseStatusList(Map<String, String> paramMap) {
		return pm07Mapper.selectAnnualUseStatusList(paramMap);
	}

	@Override
	public int updateMngRmk(Map<String, String> paramMap) {
		return pm07Mapper.updateMngRmk(paramMap);
	}

	@Override
	public List<Map<String, String>> selectMobileVacationFileList(Map<String, String> paramMap) {
		return pm07Mapper.selectMobileVacationFileList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectAwardVacationList(Map<String, String> paramMap) {
		return pm07Mapper.selectAwardVacationList(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> saveAwardVacationList(Map<String, Object> paramMap) {
		Map<String, Object> result = new HashMap<>();
		try {
			List<Map<String, String>> saveList = (List<Map<String, String>>) paramMap.get("saveList");
			List<Map<String, String>> deleteList = (List<Map<String, String>>) paramMap.get("deleteList");

			// 삭제 처리
			if (deleteList != null && !deleteList.isEmpty()) {
				for (Map<String, String> delItem : deleteList) {
					pm07Mapper.deleteAwardVacation(delItem);
				}
			}

			// 저장 처리 (insert/update 통합)
			if (saveList != null && !saveList.isEmpty()) {
				for (Map<String, String> item : saveList) {
					if (!item.containsKey("coCd") || item.get("coCd") == null || item.get("coCd").toString().isEmpty()) {
						item.put("coCd", "GUN");
					}

					String empNo = item.get("empNo");
					if (empNo == null || empNo.trim().isEmpty()) {
						continue; // 빈 행 스킵
					}

					// EMP_NO로 USER_ID 해석
					Map<String, String> userQuery = new HashMap<>();
					userQuery.put("coCd", item.get("coCd"));
					userQuery.put("empNo", empNo.trim());
					List<Map<String, String>> userList = pm07Mapper.selectUserIdByEmpNo(userQuery);

					if (userList != null && !userList.isEmpty()) {
						Map<String, String> userInfo = userList.get(0);
						item.put("userId", userInfo.get("userId"));
						if (!item.containsKey("empNm") || item.get("empNm") == null || item.get("empNm").toString().isEmpty()) {
							item.put("empNm", userInfo.get("empNm"));
						}
					}

					// 날짜 정규화 (하이픈 제거)
					String stDt = item.get("stDt");
					if (stDt != null && !stDt.isEmpty()) {
						item.put("stDt", stDt.replaceAll("[^0-9]", ""));
					}
					String edDt = item.get("edDt");
					if (edDt != null && !edDt.isEmpty()) {
						item.put("edDt", edDt.replaceAll("[^0-9]", ""));
					}

					if (!item.containsKey("creatId")) {
						item.put("creatId", "SYSTEM");
					}
					if (!item.containsKey("creatPgm")) {
						item.put("creatPgm", "PM0701P04");
					}
					if (!item.containsKey("udtId")) {
						item.put("udtId", item.get("creatId"));
					}
					if (!item.containsKey("udtPgm")) {
						item.put("udtPgm", item.get("creatPgm"));
					}

					pm07Mapper.mergeAwardVacation(item);
				}
			}

			result.put("resultCode", "200");
			result.put("resultMessage", "저장되었습니다.");
		} catch (Exception e) {
			result.put("resultCode", "500");
			result.put("resultMessage", "저장 중 오류가 발생했습니다: " + e.getMessage());
		}
		return result;
	}

	@Override
	public int deleteAwardVacation(Map<String, String> paramMap) {
		return pm07Mapper.deleteAwardVacation(paramMap);
	}

}
