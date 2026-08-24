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
	public Map<String, String> selectVacationDtl(Map<String, String> paramMap) {
		Map<String, String> result = pm07Mapper.selectVacationDtl(paramMap);
		if (result != null) {
			List<Map<String, String>> vacationDateList = pm07Mapper.selectVacationDateList(paramMap);
			result.put("vacationDateList", new GsonBuilder().disableHtmlEscaping().create().toJson(vacationDateList));
		}
		return result;
	}

	@Override
	public Map<String, String> insertVacation(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		String reqNo = pm07Mapper.selectVacationReqNoNext(paramMap);
		paramMap.put("reqNo", reqNo);
		// 결재선이 지정된 건은 결재요청(REQ), 결재선 없이 저장한 건만 임시저장(TEMP).
		// 화면은 TEMP 가 아니면 수정 불가로 잠근다.
		String reqApprovalArr = paramMap.get("approvalArr");
		paramMap.put("sanctnSts", (reqApprovalArr != null && !reqApprovalArr.isEmpty() && !"[]".equals(reqApprovalArr.trim())) ? "REQ" : "TEMP");
		paramMap.put("reqDt", getCurrentDate());

		int insertResult = pm07Mapper.insertVacation(paramMap);

		if (insertResult > 0) {
			String approvalArr = paramMap.get("approvalArr");
			if (approvalArr != null && !approvalArr.isEmpty()) {
				Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
				Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> approvalList = gsonDtl.fromJson(approvalArr, dtlMap);

				for (Map<String, String> approval : approvalList) {
					approval.put("todoNo", reqNo);
					// WB20 결재선 식별키는 (TODO_DIV2_CODE_ID, SALES_CD, TODO_NO).
					// PM07 은 Sales Code 가 없으므로 문서키(REQ_NO)를 넣는다.
					// null 이면 deleteAllTodoMaster 의 SALES_CD 바인딩(jdbcType 없음)에서 Tibero 가 거부한다.
					approval.put("salesCd", reqNo);
					approval.put("todoTitl", paramMap.get("reqTitl"));
					if (!approval.containsKey("coCd") || approval.get("coCd") == null || approval.get("coCd").isEmpty()) approval.put("coCd", paramMap.get("coCd"));
					if (!approval.containsKey("todoCoCd") || approval.get("todoCoCd") == null || approval.get("todoCoCd").isEmpty()) approval.put("todoCoCd", paramMap.get("coCd"));
					// 빈 문자열로 두면 안 된다 - Tibero 에서 '' 는 NULL 이라 deleteAllTodoMaster 의
					// SALES_CD = #{salesCd} 조건이 영영 매칭되지 않아 결재선이 중복 누적된다.
					if (approval.get("salesCd") == null || approval.get("salesCd").isEmpty()) approval.put("salesCd", paramMap.get("reqNo"));
					// 결재구분 누락 시 보정. 구분(gb)에 따라 갈라야 한다 - 고정값으로 채우면 공유행이 결재행으로 바뀐다.
					// (TODODIV2050 은 타 모듈 코드였음 - 복사 흔적)
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
				// PM07 은 Sales Code 가 없으므로 문서키(REQ_NO)를 SALES_CD 로 쓴다. 빈 문자열 금지(Tibero 에서 '' = NULL)
				paramMap.put("salesCd", paramMap.get("reqNo"));
				if (!paramMap.containsKey("todoNo") || paramMap.get("todoNo") == null) paramMap.put("todoNo", reqNo);
				// PM07 휴가신청서 결재구분. (TODODIV2010 은 타 모듈 코드였음 - 복사 흔적)
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

		int updateResult = pm07Mapper.updateVacation(paramMap);

		if (updateResult > 0) {
			String approvalArr = paramMap.get("approvalArr");
			if (approvalArr != null && !approvalArr.isEmpty()) {
				Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
				Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> approvalList = gsonDtl.fromJson(approvalArr, dtlMap);

				for (Map<String, String> approval : approvalList) {
					approval.put("todoNo", paramMap.get("reqNo"));
					// 등록과 동일하게 문서키(REQ_NO)를 SALES_CD 로 쓴다. (deleteAllTodoMaster 식별키)
					approval.put("salesCd", paramMap.get("reqNo"));
					approval.put("todoTitl", paramMap.get("reqTitl"));
					if (!approval.containsKey("coCd") || approval.get("coCd") == null || approval.get("coCd").isEmpty()) approval.put("coCd", paramMap.get("coCd"));
					if (!approval.containsKey("todoCoCd") || approval.get("todoCoCd") == null || approval.get("todoCoCd").isEmpty()) approval.put("todoCoCd", paramMap.get("coCd"));
					// 빈 문자열로 두면 안 된다 - Tibero 에서 '' 는 NULL 이라 deleteAllTodoMaster 의
					// SALES_CD = #{salesCd} 조건이 영영 매칭되지 않아 결재선이 중복 누적된다.
					if (approval.get("salesCd") == null || approval.get("salesCd").isEmpty()) approval.put("salesCd", paramMap.get("reqNo"));
					// 결재구분 누락 시 보정. 구분(gb)에 따라 갈라야 한다 - 고정값으로 채우면 공유행이 결재행으로 바뀐다.
					// (TODODIV2050 은 타 모듈 코드였음 - 복사 흔적)
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
				// PM07 은 Sales Code 가 없으므로 문서키(REQ_NO)를 SALES_CD 로 쓴다. 빈 문자열 금지(Tibero 에서 '' = NULL)
				paramMap.put("salesCd", paramMap.get("reqNo"));
				if (!paramMap.containsKey("todoNo") || paramMap.get("todoNo") == null) paramMap.put("todoNo", paramMap.get("reqNo"));
				// PM07 휴가신청서 결재구분. (TODODIV2010 은 타 모듈 코드였음 - 복사 흔적)
				if (!paramMap.containsKey("todoDiv2CodeId") || paramMap.get("todoDiv2CodeId") == null) paramMap.put("todoDiv2CodeId", "TODODIV2300");
				wb20Svc.insertTodoMaster(paramMap);
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
					// vacDtArr 파싱 실패 시 로그만 남기고 수정은 정상 처리.
					// 단 TB_PM07M03 이 비면 최종승인 시 일일업무일지가 생성되지 않으므로 반드시 로그를 남긴다.
					e.printStackTrace();
				}
			}

			// 첨부파일 처리
			try {
				cm08Svc.uploadFile("PM0701P01", paramMap.get("reqNo"), mRequest);
			} catch (Exception e) {
				// 첨부파일 업로드 실패해도 수정은 정상 처리
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
	public int calcAnnualGrantDays(String enterDt, String yy) {
		if (enterDt == null || enterDt.isEmpty() || yy == null || yy.isEmpty()) {
			return 0;
		}

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate enterDate = LocalDate.parse(enterDt, formatter);
			LocalDate baseDate = LocalDate.of(Integer.parseInt(yy), 12, 31);

			if (baseDate.isBefore(enterDate)) {
				return 0;
			}

			long months = ChronoUnit.MONTHS.between(enterDate, baseDate);
			long years = ChronoUnit.YEARS.between(enterDate, baseDate);

			if (years < 1) {
				int monthDays = (int) (months + 1);
				return Math.min(monthDays, 11);
			} else if (years < 3) {
				return 15;
			} else {
				int additionalDays = (int) ((years - 1) / 2);
				int totalDays = 15 + additionalDays;
				return Math.min(totalDays, 25);
			}
		} catch (Exception e) {
			return 0;
		}
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
		String coCd = paramMap.get("coCd");

		Map<String, String> vacationInfo = pm07Mapper.selectVacationDtl(
			new HashMap<String, String>() {{
				put("coCd", coCd);
				put("reqNo", reqNo);
			}}
		);

		if (vacationInfo == null) {
			return 0;
		}

		// 중간 결재자가 승인한 시점(todoYn != "Y")에는 진행중(ING)으로만 바꾸고 일일업무일지는 만들지 않는다.
		if (!"Y".equals(todoYn)) {
			Map<String, String> ingMap = new HashMap<>(paramMap);
			ingMap.put("sanctnSts", "ING");
			pm07Mapper.updateVacationApprovalStatus(ingMap);
			return 1;
		}

		if ("Y".equals(todoYn)) {
			// TB_PM07M01 갱신은 이 메서드 끝에서 단 한 번만 한다.
			// (SANCTN_STS 를 먼저 UPDATE 하고 일지 생성 후 WORK_RPT_YN 을 또 UPDATE 하면
			//  같은 행을 한 트랜잭션 안에서 두 번 잠그게 되어, 중간에 다른 커넥션이 끼어들 경우
			//  REQUIRES_NEW 로 분리된 이 트랜잭션과 바깥 결재 트랜잭션 사이에 교착이 생길 수 있다.)
			String reqId = vacationInfo.get("reqId");
			String reqDeptId = vacationInfo.get("reqDeptId");
			String reqRmk = vacationInfo.get("reqRmk");

			Map<String, String> workCodeMap = new HashMap<>();
			workCodeMap.put("deptId", reqDeptId);
			Map<String, String> workRptCode = pm07Mapper.selectWorkRptCodeByDept(workCodeMap);

			if (workRptCode == null || workRptCode.isEmpty()) {
				markApprovalEnd(coCd, reqNo);
				return 1;
			}

			String workRptL = workRptCode.get("workRptL");
			String workRptM = workRptCode.get("workRptM");
			String workRptS = workRptCode.get("workRptS");

			// 업무분류 3단계가 모두 나오지 않으면 일일업무일지를 만들지 않는다.
			// (설계서 §4-3 : 코드 미존재 시 INSERT 생략, 신청/결재는 정상 처리)
			// L/M/S 가 NULL 인 채로 INSERT 되면 PM60 휴가 집계(WORK_RPT_S LIKE '%9901')에서 누락된다.
			if (workRptL == null || workRptL.isEmpty()
				|| workRptM == null || workRptM.isEmpty()
				|| workRptS == null || workRptS.isEmpty()) {
				System.out.println("[PM07] 부서 업무코드(09901) 미존재로 일일업무일지 생성 생략. reqNo=" + reqNo + ", deptId=" + reqDeptId);
				markApprovalEnd(coCd, reqNo);
				return 1;
			}

			Map<String, String> vacDateQuery = new HashMap<>();
			vacDateQuery.put("coCd", coCd);
			vacDateQuery.put("reqNo", reqNo);
			List<Map<String, String>> vacDateList = pm07Mapper.selectVacationDateList(vacDateQuery);

			if (vacDateList == null || vacDateList.isEmpty()) {
				markApprovalEnd(coCd, reqNo);
				return 1;
			}

			for (Map<String, String> vacDate : vacDateList) {
				String workRptNo = pm07Mapper.selectDailyWorkReportSeqNext(paramMap);
				String vacDt = vacDate.get("vacDt");
				String workHour = vacDate.get("workHour");

				Map<String, String> insertMap = new HashMap<>();
				insertMap.put("fileTrgtKey", "0");
				insertMap.put("coCd", coCd);
				insertMap.put("workRptNo", workRptNo);
				insertMap.put("workRptId", reqId);
				insertMap.put("workRptDt", vacDt);
				insertMap.put("workRptL", workRptL);
				insertMap.put("workRptM", workRptM);
				insertMap.put("workRptS", workRptS);
				insertMap.put("workRptHour", workHour);
				insertMap.put("workRptRmk", reqRmk);
				insertMap.put("userId", reqId);

				pm07Mapper.insertDailyWorkReport(insertMap);
				result++;
			}

			// updateVacation(전체 필드 UPDATE)을 쓰면 안 된다 - 넘기지 않은 컬럼이 전부 NULL 로 덮여
			// 승인 즉시 휴가신청 내용이 지워진다. 상태 전용 UPDATE 를 쓴다.
			// SANCTN_STS 와 WORK_RPT_YN 을 여기서 한 번에 갱신한다 (같은 행 중복 UPDATE 방지).
			Map<String, String> updateYnMap = new HashMap<>();
			updateYnMap.put("coCd", coCd);
			updateYnMap.put("reqNo", reqNo);
			updateYnMap.put("sanctnSts", "END");
			updateYnMap.put("workRptYn", "Y");
			pm07Mapper.updateVacationApprovalStatus(updateYnMap);
		}

		return result;
	}

	private String getCurrentDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		return LocalDate.now().format(formatter);
	}

}
