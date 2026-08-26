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

	@Override
	public Map<String, String> insertVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

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
				wb20Svc.insertTodoMaster(paramMap);
			}

			String vacDtArr = paramMap.get("vacDtArr");
			if (vacDtArr != null && !vacDtArr.isEmpty()) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Type vacDtType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				try {
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
				} catch (Exception e) {
					// vacDtArr 파싱 실패 시 로그만 남기고 신청은 정상 처리.
					// 단 TB_PM07M03 이 비면 최종승인 시 일일업무일지가 생성되지 않으므로 반드시 로그를 남긴다.
					e.printStackTrace();
				}
			}

			// 첨부파일 처리 (삭제분 먼저 반영 후 업로드)
			try {
				deleteAttachedFiles(paramMap.get("deleteFileArr"));
				cm08Svc.uploadFile("PM0701P01", reqNo, mRequest);
			} catch (Exception e) {
				// 첨부파일 처리 실패해도 신청은 정상 처리
				e.printStackTrace();
			}

			// 등록 시 결재자(TODODIV20, gb != '공유')가 존재하면 작업일보(TB_PM01M01)에 휴가 근태 자동 생성
			// (결재상태는 건드리지 않음 - ensureDailyWorkReport는 일지 생성만)
			boolean hasApprover = checkHasApprover(reqNo, approvalList);
			if (hasApprover) {
				try {
					Map<String, String> pm07Param = new HashMap<>();
					pm07Param.put("reqNo", reqNo);
					pm07Param.put("coCd", paramMap.get("coCd"));
					ensureDailyWorkReport(pm07Param);
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	@Override
	public Map<String, String> updateVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 저장 버튼 클릭 시 DB 상태를 재확인한다. 화면 진입 후 다른 프로세스(결재선 등록/결재처리 등)로
		// 상태가 바뀌었을 수 있으므로, 화면 로드 시점의 클라이언트 체크(isEditable)만 믿지 않는다.
		// selectVacationDtl 은 TB_WB20M03 기준 실시간 SANCTN_STS 를 산출한다.
		// TEMP(결재선 없음)/REQ(결재선은 있으나 아무도 승인 안 함=결재 진행된 게 없음) 는 수정 가능,
		// ING(1명 이상 승인)/END(완료)/RTN(반려) 는 이미 결재가 진행된 상태이므로 수정 불가.
		// 화면(PM0701P01.html)의 isEditable 판정과 동일 기준을 유지할 것.
		Map<String, String> dtlQuery = new HashMap<String, String>();
		dtlQuery.put("coCd", paramMap.get("coCd"));
		dtlQuery.put("reqNo", paramMap.get("reqNo"));
		Map<String, String> currentDtl = pm07Mapper.selectVacationDtl(dtlQuery);
		String currentSanctnSts = (currentDtl != null) ? currentDtl.get("sanctnSts") : null;
		boolean isEditable = (currentSanctnSts == null || currentSanctnSts.isEmpty()
				|| "TEMP".equals(currentSanctnSts) || "SANCTN01".equals(currentSanctnSts) || "0".equals(currentSanctnSts)
				|| "REQ".equals(currentSanctnSts));
		if (!isEditable) {
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
			try {
				pm07Mapper.deleteDailyWorkReportByVacation(paramMap);
			} catch (Exception e) {
				e.printStackTrace();
			}

			pm07Mapper.deleteVacationDates(paramMap);
			String vacDtArr = paramMap.get("vacDtArr");
			if (vacDtArr != null && !vacDtArr.isEmpty()) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Type vacDtType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 수정 시 결재자(TODODIV20, gb != '공유')가 존재하면 새로 세팅된 일자로 작업일보(TB_PM01M01) 휴가 자동 생성/갱신
			// (결재상태는 건드리지 않음 - ensureDailyWorkReport는 일지 생성만)
			boolean hasApprover = checkHasApprover(paramMap.get("reqNo"), approvalList);
			if (hasApprover) {
				try {
					Map<String, String> pm07Param = new HashMap<>();
					pm07Param.put("reqNo", paramMap.get("reqNo"));
					pm07Param.put("coCd", paramMap.get("coCd"));
					ensureDailyWorkReport(pm07Param);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// 첨부파일 처리
			try {
				cm08Svc.uploadFile("PM0701P01", paramMap.get("reqNo"), mRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}

			result.put("resultCode", "200");
			result.put("resultMessage", "휴가신청이 수정되었습니다.");
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴가신청 수정에 실패했습니다.");
		}

		return result;
	}

	@Override
	public Map<String, String> deleteVacation(Map<String, String> paramMap) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 1. 해당 휴가신청건으로 작업일보(TB_PM01M01)에 등록되었던 근태 자료 자동 제거
		try {
			pm07Mapper.deleteDailyWorkReportByVacation(paramMap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 2. 결재선 Master 삭제 (TODO_NO = REQ_NO 매칭)
		try {
			paramMap.put("todoNo", paramMap.get("reqNo"));
			paramMap.put("todoDiv2CodeId", "TODODIV2300");
			wb20Svc.deleteTodoMasterByTodoNo(paramMap);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 3. 휴가 일자 디테일 및 휴가 신청 본체 삭제
		pm07Mapper.deleteVacationDates(paramMap);
		int deleteResult = pm07Mapper.deleteVacation(paramMap);

		if (deleteResult > 0) {
			result.put("resultCode", "200");
			result.put("resultMessage", "휴가신청이 삭제되었습니다.");
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴가신청 삭제에 실패했습니다.");
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
				result.put("grantDays", balanceInfo.get("grantDays"));
				result.put("usedDays", balanceInfo.get("usedDays"));
				result.put("balanceDays", balanceInfo.get("balanceDays"));
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
			int targetYear = Integer.parseInt(yy);

			if (enterYear > targetYear) {
				return 0; // 미래 입사자
			}

			int diffYears = targetYear - enterYear;
			if (diffYears == 0) {
				// 당해연도 입사자: 회계연도 비례계산 (15 * 잔여근무월수 / 12)
				int workMonths = 12 - enterMonth + 1;
				int propDays = Math.round((15.0f * workMonths) / 12.0f);
				return Math.max(propDays, 1);
			} else if (diffYears < 3) {
				return 15;
			} else {
				int addDays = (diffYears - 1) / 2;
				return Math.min(15 + addDays, 25);
			}
		} catch (Exception e) {
			return 15;
		}
	}

	// 미등록 재직자 연차지급기준 자동계산 목록 산출 (임원실: GUN00, 실적관리용: GUN95 부서는 자동계산 대상에서 제외됨)
	@Override
	public List<Map<String, String>> selectAutoCalcAnnualGrantList(Map<String, String> paramMap) {
		String yy = paramMap.get("yy");
		String coCd = paramMap.get("coCd");
		if (coCd == null || coCd.isEmpty()) {
			coCd = "GUN";
			paramMap.put("coCd", coCd);
		}

		List<Map<String, String>> unregList = pm07Mapper.selectUnregisteredUserList(paramMap);
		if (unregList != null && !unregList.isEmpty()) {
			for (Map<String, String> user : unregList) {
				String enterDt = user.get("enterDt");
				String cleanEnterDt = (enterDt != null) ? enterDt.replaceAll("[^0-9]", "") : "";
				int grantDays = calcAnnualGrantDays(cleanEnterDt, yy);

				user.put("coCd", coCd);
				user.put("yy", yy);
				user.put("grantDays", String.valueOf(grantDays));
				user.put("usedDays", "0");
				user.put("balanceDays", String.valueOf(grantDays));
				user.put("autoYn", "Y");
				user.put("rmk", "연차기준 자동계산");
			}
		}
		return unregList;
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
		String coCd = paramMap.get("coCd");

		// double-brace 익명 클래스 제거 (effectively final 제약 해제)
		Map<String, String> dtlQuery = new HashMap<>();
		dtlQuery.put("coCd", coCd);
		dtlQuery.put("reqNo", reqNo);
		Map<String, String> vacationInfo = pm07Mapper.selectVacationDtl(dtlQuery);

		if (vacationInfo == null) {
			return 0;
		}

		// 중간 결재자가 승인한 시점(todoYn != "Y")에는 진행중(ING)으로만 바꾸고 일일업무일지는 만들지 않는다.
		if (!"Y".equals(todoYn)) {
			Map<String, String> ingMap = new HashMap<>();
			ingMap.put("coCd", coCd);
			ingMap.put("reqNo", reqNo);
			ingMap.put("sanctnSts", "ING");
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
				pm07Mapper.deleteDailyWorkReportByVacation(paramMap);
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

}
