package com.dksys.biz.user.wb.wb20.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm16.mapper.CM16Mapper;
import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.admin.cm.cm25.mapper.CM25Mapper;
import com.dksys.biz.user.im.im01.mapper.IM01Mapper;
import com.dksys.biz.user.pm.pm07.service.PM07Svc;
import com.dksys.biz.user.pm.pm08.service.PM08Svc;
import com.dksys.biz.user.pm.pm51.mapper.PM51Mapper;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.mapper.WB20Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB20SvcImpl implements WB20Svc {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	WB20Mapper wb20Mapper;

	@Autowired
	WB24Mapper wb24Mapper;

	@Autowired
	QM01Mapper qm01Mapper;

	@Autowired
	CM16Mapper cm16Mapper;

	@Autowired
	CM25Mapper cm25Mapper;

    @Autowired
    IM01Mapper im01Mapper;

	@Autowired
	PM51Mapper pm51Mapper;

	@Autowired
	PM07Svc pm07Svc;

	@Autowired
	PM08Svc pm08Svc;

	@Autowired
	@Lazy
	private AM11Svc am11Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectToDoCount(Map<String, String> paramMap) {
//		return wb20Mapper.selectToDoCount(paramMap);
		return wb20Mapper.selectToDoCountNewSql(paramMap);
	}

	@Override
	public Map<String, Integer> selectToDoCountGrouped(Map<String, String> paramMap) {
		List<Map<String, String>> list = wb20Mapper.selectToDoCountGrouped(paramMap);
		Map<String, Integer> resultMap = new HashMap<>();
		int approvalCnt = 0;
		int shareCnt = 0;
		if (list != null) {
			for (Map<String, String> item : list) {
				String div1 = String.valueOf(item.get("todoDiv1CodeId"));
				Object cntObj = item.get("cnt");
				int cnt = 0;
				if (cntObj != null) {
					cnt = Integer.parseInt(String.valueOf(cntObj));
				}
				// TODODIV20: 결재, TODODIV10: 공유
				if ("TODODIV20".equals(div1)) {
					approvalCnt += cnt;
				} else if ("TODODIV10".equals(div1)) {
					shareCnt += cnt;
				}
			}
		}
		resultMap.put("approvalCnt", approvalCnt);
		resultMap.put("shareCnt", shareCnt);
		return resultMap;
	}

	@Override
	public List<Map<String, String>> selectToDoList(Map<String, String> paramMap) {
//		return wb20Mapper.selectToDoList(paramMap);
		return wb20Mapper.selectToDoListNewSql(paramMap);
	}

	@Override
	public int toDoCfDtUpdate(Map<String, String> paramMap) {
		int result = wb20Mapper.toDoCfDtUpdate(paramMap);
		return result;
	}

	@Override
	public int toDoMindMapApprovalCancel(Map<String, String> paramMap) {
		int result = wb20Mapper.toDoMindMapApprovalCancel(paramMap);
		return result;
	}

	@Override
	public int updateRsltsApproval(Map<String, String> paramMap) {
		int result = wb20Mapper.updateRsltsApproval(paramMap);
		return result;
	}

	@Override
	public List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap) {
		return wb20Mapper.selectApprovalChk(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTodoDivList(Map<String, String> paramMap) {
		return wb20Mapper.selectTodoDivList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectApprovalYnList(Map<String, String> paramMap) {
		return wb20Mapper.selectApprovalYnList(paramMap);
	}

	@Override
	public int updateQmMobileApproval(Map<String, String> paramMap) {
		int result = wb20Mapper.updateQmMobileApproval(paramMap);
		return result;
	}

	@Override
	public List<Map<String, String>> selectGetDeptList(Map<String, String> paramMap) {
		return wb20Mapper.selectGetDeptList(paramMap);
	}

	/* 공통결재 라인 read */
	@Override
	public List<Map<String, String>> selectGetApprovalList(Map<String, String> paramMap) {
		return wb20Mapper.selectGetApprovalList(paramMap);
	}

	/* 공통결재 라인 insert */
	@Override
	public Map<String, String> insertApprovalLine(Map<String, String> paramMap) {

		int result = 0;
		/*
		 * paramMap 내용 { todoId=js.nam, todoCfOpn=, issNo=RES2400144, coCd=GUN, todoDiv1CodeId=TODODIV20, todoDiv2CodeId=TODODIV2030, pgmId=WB2001M01,
		 * salesCd=24000-00DUMMY, todoDiv1CodeNm=결재, todoDiv2CodeNm=발주/출장요청(결과), todoFileTrgtKey=1287, todoTitl=(주)건양아이티티-기타정상발주및출장요청서결과,
		 * userId=js.nam, todoKey=8504, sanctnSn=1, todoNo=RES2400144 }
		 */
		// pgParam JSON 문자열 파싱하여 최상위 paramMap의 비어있는 핵심 정보(issNo, deptId, actMh, reqNo 등) 주입
		if (paramMap.containsKey("pgParam") && paramMap.get("pgParam") != null) {
			try {
				String pgParamStr = paramMap.get("pgParam");
				if (pgParamStr.trim().startsWith("{")) {
					Gson gson = new Gson();
					Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
					Map<String, Object> pgMap = gson.fromJson(pgParamStr, mapType);
					if (pgMap != null) {
						for (Map.Entry<String, Object> entry : pgMap.entrySet()) {
							if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
								if (!paramMap.containsKey(entry.getKey()) || paramMap.get(entry.getKey()) == null || paramMap.get(entry.getKey()).isEmpty()) {
									paramMap.put(entry.getKey(), entry.getValue().toString());
								}
							}
						}
					}
				}
			} catch (Exception e) {
				// JSON 파싱 무시
			}
		}

		String tempReqNo = paramMap.get("todoNo");
		String todoDiv2CodeId = paramMap.get("todoDiv2CodeId");
		String todoCfOpn = paramMap.get("todoCfOpn");
		validatePm51SalesApproval(paramMap);
		validatePm51SequentialApproval(paramMap);

		// AM 연동 판별 및 위임 (1차진입일 때만)
		boolean isReentry = "Y".equals(paramMap.get("amLinkedApproval"));
		if (!isReentry && am11Svc != null) {
			String erpBizKey = paramMap.get("todoNo");
			String coCd = paramMap.get("coCd");

			// 진행 중인 AM 연동 문서가 있을 때만 위임한다. 없으면 기존 WB 흐름으로 처리한다.
			String docId = findActiveAmDocId(paramMap);

			// AM 연동 문서 존재 시 위임
			if (docId != null && !docId.isEmpty()) {
				Map<String, Object> amParam = new HashMap<>();
				amParam.put("docId", docId);
				amParam.put("userId", paramMap.get("userId"));
				String userNm = paramMap.get("userNm");
				if (userNm != null && !userNm.isEmpty()) {
					amParam.put("userNm", userNm);
				}
				amParam.put("apprOpinion", todoCfOpn != null ? todoCfOpn : "");

				Map<String, Object> amResult = am11Svc.approveDocument(amParam);

				// AM 엔진 결과 판정: 200 아니면 실패 반환
				if (amResult == null || !"200".equals(amResult.get("resultCode"))) {
					Map<String, String> response = new HashMap<>();
					response.put("resultCount", "0");
					response.put("RESULT_COUNT", "0");
					if (amResult != null && amResult.get("resultMessage") != null) {
						response.put("resultMessage", String.valueOf(amResult.get("resultMessage")));
					}
					return response;
				}

				// 위임 경로: WB20 재조회 후 응답계약 구성
				Map<String, String> queryParam = new HashMap<>();
				queryParam.put("todoNo", tempReqNo);
				queryParam.put("coCd", coCd);
				Map<String, String> todoYnResult = wb20Mapper.selectTodoFinalYn(queryParam);
				String todoYn = (todoYnResult != null) ? todoYnResult.get("todoYn") : "N";

				Map<String, String> response = new HashMap<>();
				response.put("resultCount", "1");
				response.put("RESULT_COUNT", "1");
				response.put("todoYn", todoYn);
				response.put("notifyHandledByAm", "Y");
				return response;
			}
		}

		// 비연동 또는 재진입: 기존 WB20 결재 흐름 실행
		result += wb20Mapper.updateApprovalLine(paramMap);

		// 출장신청 관리부서 회계 승인(TODODIV2191) 시 신청서 자동 지급완료 처리 연동
		if ("TODODIV2191".equals(todoDiv2CodeId) && "Y".equals(paramMap.get("sanctnSttus"))) {
			java.util.Map<String, String> payParam = new java.util.HashMap<>(paramMap);
			payParam.put("tripReqNo", tempReqNo);
			payParam.put("userId", paramMap.get("todoId"));
			java.util.Map<String, String> m01 = pm51Mapper.selectTripReqM01(payParam);
			if (m01 != null && (m01.get("payDt") == null || "".equals(m01.get("payDt").toString().trim()))) {
				pm51Mapper.updateTripReqPayDone(payParam);
			}
		}

		boolean pfuShareTarget = false;
		// TODODIV2020:발주 및 출장 요청 상태코드 바꾸기
		if ("TODODIV2020".equals(todoDiv2CodeId)) {
			// 발주요청서 진행상태 변경 처리
			// REQ_ST: REQST01 --> REQST02 로 상태 변경처
			// result += qm01Mapper.updateReqStChk(paramMap);
			paramMap.put("reqNo", tempReqNo);
			// String currReqSt = qm01Mapper.selectReqStCurrentStatus(paramMap);
			Map<String, String> currReqSt = qm01Mapper.selectReqStCurrentStatus(paramMap);
			if ("REQST03".equals(currReqSt.get("reqSt"))) {
				// 이미 완료처리이면 상태코드 변경안함
				// 발주요청서 결재전에 발주요청서 결과 등록하여 결재완료인 경우 상태코드가 REQST03 으로 바뀌어 있음.
				// 이후 발주요청서 결재처리하게되면 상태코드가 진행증:REQST02 로 바뀌는 문제 발생됨
			} else {
				result += qm01Mapper.updateReqSt(paramMap);			// REQST02로 변경
			}
			// 동시처리건이면 팀장결재일때만 상태코드를 REQST03(완료)로 변경
			if ("Y".equals(currReqSt.get("sameTimeResult"))) {
				result += qm01Mapper.updateReqStRslt(paramMap);
			}
			// 결과일괄 등록 자료 결재시 투입공수 업데이트
			if ("TEAM01".equals(paramMap.get("actTeamManager")) || "평가".equals(paramMap.get("actTeamManager")) || "Y".equals(paramMap.get("actTeamManager")) || "자체승인".equals(paramMap.get("todoCfOpn"))) {
    			if ("Y".equals(paramMap.get("sameTimeResultChk"))) {
    				if ("GUN30".equals(paramMap.get("deptId")) ||
    					"GUN40".equals(paramMap.get("deptId")) ||
    					"TRN50".equals(paramMap.get("deptId")) ||
    					"GUN60".equals(paramMap.get("deptId")) ||
    					"GUN70".equals(paramMap.get("deptId"))) {
    						result += qm01Mapper.updateReqActMnRslt(paramMap);	// 결과자료 투입시간 업데이트
    					}
    			}
			}

			// TODODIV2030:발주 및 출장 요청 결과자료 상태코드 바꾸기
		} else if ("TODODIV2030".equals(todoDiv2CodeId)) {
			// REQ_ST: REQST02 --> REQST03 로 상태 변경처리
			if (tempReqNo != null && tempReqNo.startsWith("RES")) {
				paramMap.put("reqNo", "REQ" + tempReqNo.substring(3));
			} else {
				paramMap.put("reqNo", tempReqNo);
			}
			result += qm01Mapper.updateReqStRslt(paramMap);

			// actMh 및 deptId 보강
			if ((paramMap.get("actMh") == null || paramMap.get("actMh").isEmpty()) && paramMap.get("etcField1") != null) {
				paramMap.put("actMh", paramMap.get("etcField1"));
			}
			String appDeptId = paramMap.get("deptId");
			if (appDeptId == null || appDeptId.isEmpty()) {
				appDeptId = paramMap.get("actDeptId");
			}
			if (appDeptId == null || appDeptId.isEmpty()) {
				appDeptId = paramMap.get("resDeptCd");
			}
			if (appDeptId != null && appDeptId.length() >= 5) {
				appDeptId = appDeptId.substring(0, 5);
			}
			paramMap.put("deptId", appDeptId);

			// 문제 연동 발주/결과건(workRptNo 또는 issNo 존재)에 한해서만 결과자료 투입시간 업데이트
			String workRptNo = paramMap.get("workRptNo");
			if (workRptNo == null || workRptNo.isEmpty()) {
				workRptNo = paramMap.get("issNo");
			}
			if (workRptNo != null && !workRptNo.isEmpty() && paramMap.get("actMh") != null && !paramMap.get("actMh").isEmpty()) {
				if ("GUN30".equals(appDeptId) ||
					"GUN40".equals(appDeptId) ||
					"TRN50".equals(appDeptId) ||
					"GUN60".equals(appDeptId) ||
					"GUN70".equals(appDeptId)) {
					result += qm01Mapper.updateReqActMnRslt(paramMap);	// 결과자료 투입시간 업데이트
				}
			}
			// TODODIV2060:WBS이슈 발생에 대한 결재이면 이슈상태 변경처리
		} else if ("TODODIV2060".equals(todoDiv2CodeId)) {
			// ISS_STS: ISSSTS01 --> ISSSTS02 로 상태 변경처리
			result += wb24Mapper.updateWbsIssueStChk(paramMap);

			// TODODIV2090:WBS조치 이슈조치결과 담당팀장 위험도 평가내역 수정 처리
		} else if ("TODODIV2090".equals(todoDiv2CodeId)) {
			if (paramMap.containsKey("actDngEval")) {
				String value = paramMap.get("actDngEval");
				if (value != null && !value.isEmpty()) {
					result += wb24Mapper.updateWbsIssueResultEvaluate(paramMap);
				}
			}
			// actMh 및 deptId 보강
			if ((paramMap.get("actMh") == null || paramMap.get("actMh").isEmpty()) && paramMap.get("etcField1") != null) {
				paramMap.put("actMh", paramMap.get("etcField1"));
			}
			String appDeptId = paramMap.get("deptId");
			if (appDeptId == null || appDeptId.isEmpty()) {
				appDeptId = paramMap.get("actDeptId");
			}
			if (appDeptId != null && appDeptId.length() >= 5) {
				appDeptId = appDeptId.substring(0, 5);
			}
			paramMap.put("deptId", appDeptId);

			// 팀장 투입시간 업데이트
			if ("GUN30".equals(appDeptId) ||
				"GUN40".equals(appDeptId) ||
				"TRN50".equals(appDeptId) ||
				"GUN60".equals(appDeptId) ||
				"GUN70".equals(appDeptId)) {
				result += wb24Mapper.updateWbsIssueActMn(paramMap);	// 이슈조치 투입시간 업데이트
			}
			// 조치 결재시 문제에 결재 미완료를 완료로 변경
			result += wb20Mapper.updateWbsIssueApprovalSync(paramMap);
		} else if ("TODODIV2130".equals(todoDiv2CodeId)) {
			// ISS_STS: ISSSTS02(진행중)으로 상태 변경처리
			result += cm16Mapper.updateItoaIssueStChk(paramMap);
		} else if ("TODODIV2120".equals(todoDiv2CodeId)) {
			pfuShareTarget = true;
        } else if ("TODODIV2150".equals(todoDiv2CodeId)) { // 개선 제안서 작성부서 결재라인
            result += im01Mapper.updateImprvmStsCd(paramMap);
            result += im01Mapper.updateImprvmReqIdTxt(paramMap);
        } else if ("TODODIV2160".equals(todoDiv2CodeId)) { // 개선 제안서 조치부서 결재라인
            result += im01Mapper.updateExecStsCd(paramMap);
            result += im01Mapper.updateExecTeamIdTxt(paramMap);
		}



		// 최종결재 완료시 알림톡 발송 대상인지 확인
		Map<String, String> resultMap = wb20Mapper.selectTodoFinalYn(paramMap);
		if ("TODODIV2202".equals(todoDiv2CodeId)) {
			paramMap.put("expendNo", paramMap.get("todoNo"));
			cm25Mapper.updateExpendSts(paramMap);
		}
		if ("TODODIV2190".equals(todoDiv2CodeId)) {
			updatePm51AprvSts(paramMap, (resultMap != null && "Y".equals(resultMap.get("todoYn"))) ? "APRVSTS03" : "APRVSTS02");
		}
		// 출장복명서(TODODIV2200)도 신청서(2190)와 같은 기준으로 결재 진행/완료 상태를 갱신한다.
		if ("TODODIV2200".equals(todoDiv2CodeId)) {
			updatePm52AprvSts(paramMap, (resultMap != null && "Y".equals(resultMap.get("todoYn"))) ? "APRVSTS03" : "APRVSTS02");
		}

		// PM07 휴가신청서: 결재 상태 갱신 + 최종승인 시 일일업무일지(TB_PM01M01) 반영
		if ("TODODIV2300".equals(todoDiv2CodeId)) {
			try {
				Map<String, String> pm07Param = new HashMap<>();
				pm07Param.put("todoNo", paramMap.get("todoNo"));
				pm07Param.put("coCd", paramMap.get("coCd"));
				pm07Param.put("todoYn", (resultMap != null && resultMap.get("todoYn") != null) ? resultMap.get("todoYn") : "N");
				pm07Svc.applyVacationApproved(pm07Param);
			} catch (Exception e) {
				// 후처리 실패 시 결재는 정상 처리 (로그만 남김).
				// applyVacationApproved 는 바깥(이) 트랜잭션 안에서 실행되며(PM51 updatePm51AprvSts 와 동일),
				// 내부에서 예외를 자체 흡수하므로 여기까지 전파되지 않는다. 이 catch 는 방어용이다.
				e.printStackTrace();
			}
		}

		// PM08 휴일대체근무: 신청결재(TODODIV2410) 완료 후처리
		if ("TODODIV2410".equals(todoDiv2CodeId)) {
			try {
				Map<String, String> pm08Param = new HashMap<>();
				pm08Param.put("todoNo", paramMap.get("todoNo"));
				pm08Param.put("reqNo", paramMap.get("todoNo"));
				pm08Param.put("coCd", paramMap.get("coCd"));
				pm08Param.put("userId", paramMap.get("userId"));
				pm08Param.put("todoId", paramMap.get("todoId"));
				pm08Param.put("todoCfOpn", todoCfOpn);
				pm08Param.put("todoYn", (resultMap != null && resultMap.get("todoYn") != null) ? resultMap.get("todoYn") : "N");
				pm08Svc.applySubstituteWorkApproved(pm08Param);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// PM08 휴일대체근무: 결과결재(TODODIV2420) 완료 후처리
		if ("TODODIV2420".equals(todoDiv2CodeId)) {
			try {
				Map<String, String> pm08Param = new HashMap<>();
				pm08Param.put("todoNo", paramMap.get("todoNo"));
				pm08Param.put("reqNo", paramMap.get("todoNo"));
				pm08Param.put("coCd", paramMap.get("coCd"));
				pm08Param.put("userId", paramMap.get("userId"));
				pm08Param.put("todoId", paramMap.get("todoId"));
				pm08Param.put("todoCfOpn", todoCfOpn);
				pm08Param.put("todoYn", (resultMap != null && resultMap.get("todoYn") != null) ? resultMap.get("todoYn") : "N");
				pm08Svc.applySubstituteWorkResultApproved(pm08Param);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// PFU결재완료 시 추가 공유자 설정
		resultMap.put("RESULT_COUNT", Integer.toString(result));
		resultMap.put("PFU_SHARE_TARGET_YN", "N");
		resultMap.put("PFU_SHARE_RESULT_CODE", "");
		resultMap.put("PFU_SHARE_RESULT_COUNT", "0");

		if (pfuShareTarget && ("Y".equals(resultMap.get("todoYn")) || (todoCfOpn != null && !todoCfOpn.trim().isEmpty()))) {
			resultMap.put("PFU_SHARE_TARGET_YN", "Y");
			try {
				int pfuShareResult = insertPfuShareUser(paramMap);
				resultMap.put("PFU_SHARE_RESULT_CODE", pfuShareResult != 0 ? "200" : "500");
				resultMap.put("PFU_SHARE_RESULT_COUNT", Integer.toString(pfuShareResult));
			} catch (Exception e) {
				resultMap.put("PFU_SHARE_RESULT_CODE", "900");
			}
		}

		return resultMap;
	}

	/* 공통결재 보완요청 insert */
	private void validatePm51SalesApproval(Map<String, String> paramMap) {
		if (!isPm51SalesApproval(paramMap)) {
			return;
		}

		Map<String, String> tripParam = new HashMap<>();
		tripParam.put("tripReqNo", paramMap.get("todoNo"));
		Map<String, String> tripReq = pm51Mapper.selectTripReqM01(tripParam);
		if (tripReq == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		// 신청인의 본인 결재는 영업부서 소속이어도 영업 확인 결재와 구분한다.
		if (hasText(tripReq.get("reqId")) && tripReq.get("reqId").equals(paramMap.get("todoId"))) {
			return;
		}

		if (!checked(tripReq.get("salesCnfrmYn"))) {
			throw new RuntimeException("영업팀 확인을 체크해주세요.");
		}
		if (!hasText(tripReq.get("tripCondCd"))) {
			throw new RuntimeException("출장조건을 선택해주세요.");
		}
		if ("TRIPCOND99".equals(tripReq.get("tripCondCd")) && !hasText(tripReq.get("etc")) && !hasText(tripReq.get("tripCondEtc"))) {
			throw new RuntimeException("출장조건 기타 내용을 입력해주세요.");
		}
		boolean supportChecked = checked(tripReq.get("sprtTrfcYn"))
				|| checked(tripReq.get("sprtLdgYn"))
				|| checked(tripReq.get("sprtMealYn"))
				|| checked(tripReq.get("sprtEtcYn"));
		if (!supportChecked) {
			throw new RuntimeException("고객사지원범위를 선택해주세요.");
		}
		if (checked(tripReq.get("sprtEtcYn")) && !hasText(tripReq.get("sprtEtcTxt"))) {
			throw new RuntimeException("고객사지원범위 기타 내용을 입력해주세요.");
		}
	}

	// 신청부서(일반) 결재코드 -> 관리부서 결재코드 매핑 (순차결재: 신청부서 결재가 모두 완료되어야 관리부서 결재 진행 가능)
	// PM51(출장신청서): TODODIV2190 -> TODODIV2191, PM52(출장복명서): TODODIV2200 -> TODODIV2201
	private static final Map<String, String> GENERAL_TO_MNG_TODODIV2 = new HashMap<>();
	private static final Map<String, String> GENERAL_TO_MNG_TODODIV2_REVERSE = new HashMap<>();
	static {
		GENERAL_TO_MNG_TODODIV2.put("TODODIV2191", "TODODIV2190");
		GENERAL_TO_MNG_TODODIV2.put("TODODIV2201", "TODODIV2200");
		GENERAL_TO_MNG_TODODIV2.put("TODODIV2204", "TODODIV2202");

		GENERAL_TO_MNG_TODODIV2_REVERSE.put("TODODIV2190", "TODODIV2191");
		GENERAL_TO_MNG_TODODIV2_REVERSE.put("TODODIV2200", "TODODIV2201");
		GENERAL_TO_MNG_TODODIV2_REVERSE.put("TODODIV2202", "TODODIV2204");
	}

	// 순차결재 취소 검증: 다음 차례 결재자가 이미 승인한 상태에서는 이전 결재자가 결재를 취소할 수 없다 (역순 취소 원칙)
	/**
	 * 두 라인이 같은 병렬 AGREE 런에 속하는지 판정
	 * - 둘 다 AGREE이고
	 * - 두 라인 사이에 비-AGREE 구속라인(APPR,POST)이 없으면 true
	 */
	private boolean isSameStep(Map<String, String> currentLine, Map<String, String> prevLine, List<Map<String, String>> allLines) {
		if (currentLine == null || prevLine == null) return false;
		String currType = nvlLineType(currentLine.get("lineType"));
		String prevType = nvlLineType(prevLine.get("lineType"));
		// currentLine은 AGREE여야 함, prevLine은 AGREE 또는 APPR 헤드
		if (!"AGREE".equals(currType) || (!("AGREE".equals(prevType) || "APPR".equals(prevType)))) return false;

		// 두 라인 사이에 비-AGREE 구속 라인(APPR/POST)이 없는지 확인
		int currSn = Integer.parseInt(String.valueOf(currentLine.get("sanctnSn")));
		int prevSn = Integer.parseInt(String.valueOf(prevLine.get("sanctnSn")));
		int minSn = Math.min(currSn, prevSn);
		int maxSn = Math.max(currSn, prevSn);

		for (Map<String, String> line : allLines) {
			try {
				int sn = Integer.parseInt(String.valueOf(line.get("sanctnSn")));
				if (sn > minSn && sn < maxSn) {
					String type = nvlLineType(line.get("lineType"));
					if ("APPR".equals(type) || "POST".equals(type)) {
						return false;  // 구속 라인 발견
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
		return true;
	}

	private boolean isSameAgreeRun(Map<String, String> currentLine, Map<String, String> prevLine, List<Map<String, String>> allLines) {
		return isSameStep(currentLine, prevLine, allLines);
	}

	/**
	 * lineType 정규화: null → 'APPR'
	 */
	private String nvlLineType(String lineType) {
		return (lineType == null || "".equals(lineType.trim())) ? "APPR" : lineType.trim();
	}

	private void validatePm51SequentialCancel(Map<String, String> paramMap) {
		String todoDiv1CodeId = paramMap.get("todoDiv1CodeId");
		String todoDiv2CodeId = paramMap.get("todoDiv2CodeId");
		if (!"TODODIV20".equals(todoDiv1CodeId)) {
			return;
		}
		boolean isGeneralLine = "TODODIV2190".equals(todoDiv2CodeId) || "TODODIV2200".equals(todoDiv2CodeId)
			|| "TODODIV2202".equals(todoDiv2CodeId)
			|| "TODODIV2300".equals(todoDiv2CodeId) || "TODODIV2410".equals(todoDiv2CodeId) || "TODODIV2420".equals(todoDiv2CodeId);
		boolean isMngLine = GENERAL_TO_MNG_TODODIV2.containsKey(todoDiv2CodeId);
		if (!isGeneralLine && !isMngLine) {
			return;
		}
		String todoNo = paramMap.get("todoNo");
		if (!hasText(todoNo)) {
			return;
		}
		int currentSn;
		try {
			currentSn = Integer.parseInt(String.valueOf(paramMap.get("sanctnSn")));
		} catch (Exception e) {
			return;
		}

		// 1. 현재 결재선 내에서 나보다 뒷 순번(sn > currentSn)의 결재자가 이미 결재(sanctnSttus == 'Y')했는지 검사
		Map<String, String> selfParam = new HashMap<>();
		selfParam.put("todoNo", todoNo);
		selfParam.put("todoDiv1CodeId", "TODODIV20");
		selfParam.put("todoDiv2CodeId", todoDiv2CodeId);
		List<Map<String, String>> lines = wb20Mapper.selectApprovalLineOrder(selfParam);
		Map<String, String> prevLine = null;
		for (Map<String, String> line : lines) {
			int sn;
			try {
				sn = Integer.parseInt(String.valueOf(line.get("sanctnSn")));
			} catch (Exception e) {
				continue;
			}
			String lineType = nvlLineType(line.get("lineType"));

			// COOP/POST면 선행차단 하지 않음
			if ("COOP".equals(lineType) || "POST".equals(lineType)) {
				prevLine = line;
				continue;
			}

			// 같은 AGREE 런이면 상호 비차단
			if (prevLine != null && isSameAgreeRun(line, prevLine, lines)) {
				prevLine = line;
				continue;
			}

			if (sn > currentSn && "Y".equals(line.get("sanctnSttus"))) {
				String nextName = pm51ApproverNameWithJik(line);
				throw new RuntimeException("다음 결재자 " + (hasText(nextName) ? nextName + "님이 " : "") + "이미 결재를 완료하여 결재 취소할 수 없습니다.\n다음 결재자의 결재를 먼저 취소해야 합니다.");
			}
			prevLine = line;
		}

		// 2. 신청부서 결재선 취소 시, 관리부서 결재선에서 이미 결재가 진행된 건이 있는지 검사
		if (isGeneralLine && GENERAL_TO_MNG_TODODIV2_REVERSE.containsKey(todoDiv2CodeId)) {
			Map<String, String> mngParam = new HashMap<>();
			mngParam.put("todoNo", todoNo);
			mngParam.put("todoDiv1CodeId", "TODODIV20");
			mngParam.put("todoDiv2CodeId", GENERAL_TO_MNG_TODODIV2_REVERSE.get(todoDiv2CodeId));
			List<Map<String, String>> mngLines = wb20Mapper.selectApprovalLineOrder(mngParam);
			for (Map<String, String> line : mngLines) {
				if ("Y".equals(line.get("sanctnSttus"))) {
					String mngName = pm51ApproverNameWithJik(line);
					throw new RuntimeException("관리부서 결재자 " + (hasText(mngName) ? mngName + "님이 " : "") + "이미 결재를 진행하여 결재 취소할 수 없습니다.\n관리부서 결재를 먼저 취소해야 합니다.");
				}
			}
		}
	}

	private void validatePm51SequentialApproval(Map<String, String> paramMap) {
		String todoDiv1CodeId = paramMap.get("todoDiv1CodeId");
		String todoDiv2CodeId = paramMap.get("todoDiv2CodeId");
		if (!"TODODIV20".equals(todoDiv1CodeId)) {
			return;
		}
		boolean isGeneralLine = "TODODIV2190".equals(todoDiv2CodeId) || "TODODIV2200".equals(todoDiv2CodeId)
			|| "TODODIV2202".equals(todoDiv2CodeId)
			|| "TODODIV2300".equals(todoDiv2CodeId) || "TODODIV2410".equals(todoDiv2CodeId) || "TODODIV2420".equals(todoDiv2CodeId); // PM07 휴가신청서, PM08 휴일대체근무 신청/결과도 대상
		boolean isMngLine = GENERAL_TO_MNG_TODODIV2.containsKey(todoDiv2CodeId);
		if (!isGeneralLine && !isMngLine) {
			return;
		}
		String todoNo = paramMap.get("todoNo");
		if (!hasText(todoNo)) {
			return;
		}
		int currentSn;
		try {
			currentSn = Integer.parseInt(String.valueOf(paramMap.get("sanctnSn")));
		} catch (Exception e) {
			return;
		}

		if (isMngLine) {
			Map<String, String> reqParam = new HashMap<>();
			reqParam.put("todoNo", todoNo);
			reqParam.put("todoDiv1CodeId", "TODODIV20");
			reqParam.put("todoDiv2CodeId", GENERAL_TO_MNG_TODODIV2.get(todoDiv2CodeId));
			List<Map<String, String>> reqLines = wb20Mapper.selectApprovalLineOrder(reqParam);
			for (Map<String, String> line : reqLines) {
				if (!"Y".equals(line.get("sanctnSttus"))) {
					throw new RuntimeException(pm51PendingApproverMessage("신청부서 결재자", line));
				}
			}
		}
		// 지급처리 등록자가 관리부서 1번(최정민) 또는 2번(이영만)인 경우에는 결재선 생성 직후
		// 동일 문서의 이전 관리부서 이력이 남아 있어도 자동승인을 막지 않는다.
		// 신청부서 결재 완료 검증은 위에서 그대로 수행된다.
		if (isMngLine && (currentSn == 1 || currentSn == 2) && "자동승인".equals(paramMap.get("todoCfOpn"))) {
			return;
		}

		Map<String, String> selfParam = new HashMap<>();
		selfParam.put("todoNo", todoNo);
		selfParam.put("todoDiv1CodeId", "TODODIV20");
		selfParam.put("todoDiv2CodeId", todoDiv2CodeId);
		List<Map<String, String>> lines = wb20Mapper.selectApprovalLineOrder(selfParam);
		Map<String, String> prevLine = null;
		for (Map<String, String> line : lines) {
			int sn;
			try {
				sn = Integer.parseInt(String.valueOf(line.get("sanctnSn")));
			} catch (Exception e) {
				continue;
			}
			String lineType = nvlLineType(line.get("lineType"));

			// COOP/POST면 선행차단 하지 않음
			if ("COOP".equals(lineType) || "POST".equals(lineType)) {
				prevLine = line;
				continue;
			}

			// 같은 AGREE 런이면 상호 비차단
			if (prevLine != null && isSameAgreeRun(line, prevLine, lines)) {
				prevLine = line;
				continue;
			}

			// 관리부서(1번 최정민, 2번 이영만)는 상호 병행 결재가 가능해야 하므로,
			// 2번 결재자(이영만)가 결재할 때 1번(최정민)의 미승인은 차단 사유가 되지 않는다.
			// 단, 3번 이상(부사장 등)은 1번과 2번이 모두 승인되어야만 결재 가능하다.
			if (isMngLine && currentSn == 2 && sn == 1) {
				prevLine = line;
				continue;
			}
			if (sn < currentSn && !"Y".equals(line.get("sanctnSttus"))) {
				throw new RuntimeException(pm51PendingApproverMessage("이전결재자", line));
			}
			prevLine = line;
		}
	}

	// 순차결재 안내문구 - 화면(commApproval.js / PM5101P01.html)의 안내와 동일한 형식으로 맞춘다.
	// 예) 이전결재자 홍길동팀장이 승인하지 않은 상태입니다.\n이전 결재가 완료되어야 결재를 진행할 수 있습니다.
	private String pm51PendingApproverMessage(String approverDesc, Map<String, String> line) {
		String suffix = "\n이전 결재가 완료되어야 결재를 진행할 수 있습니다.";
		String nameWithJik = pm51ApproverNameWithJik(line);
		if (nameWithJik.isEmpty()) {
			return approverDesc + "가 승인하지 않은 상태입니다." + suffix;
		}
		return approverDesc + " " + nameWithJik + pm51SubjectParticle(nameWithJik) + " 승인하지 않은 상태입니다." + suffix;
	}

	// 결재자 표기: 이름 뒤에 직급을 붙인다(직급 가운데 공백 제거). 예) 홍길동팀장
	private String pm51ApproverNameWithJik(Map<String, String> line) {
		if (line == null) {
			return "";
		}
		String name = line.get("todoNm") == null ? "" : String.valueOf(line.get("todoNm")).trim();
		String jik = line.get("jik") == null ? "" : String.valueOf(line.get("jik")).replaceAll("\\s", "");
		return name + jik;
	}

	// 주격조사 선택: 마지막 글자에 받침이 있으면 '이', 없으면 '가'. 예) 홍길동팀장이 / 김영희대리가
	private String pm51SubjectParticle(String text) {
		if (text == null || text.isEmpty()) {
			return "이";
		}
		char last = text.charAt(text.length() - 1);
		if (last >= 0xAC00 && last <= 0xD7A3) {
			return ((last - 0xAC00) % 28) > 0 ? "이" : "가";
		}
		return "이";
	}

	/**
	 * WB20 결재함 승인/반려를 AM 엔진에 위임할 "진행 중인 AM 문서"를 찾는다. 없으면 null(=기존 WB 흐름).
	 * WB20 결재행 식별키는 TODO_DIV1_CODE_ID + TODO_DIV2_CODE_ID + TODO_ID + TODO_NO + ETC_FIELD2(CR02 차수)이며,
	 * TODO_KEY는 수정 시 바뀔 수 있어 고유키로 쓰지 않는다. 키가 하나라도 비면 미연동으로 본다.
	 * CR02는 WB에만 있고 AM 문서가 없는 자료가 있으며, 그런 자료는 AM을 쓰지 않고 기존 WB 모듈로 처리한다.
	 */
	private String findActiveAmDocId(Map<String, String> paramMap) {
		if (am11Svc == null) {
			return null;
		}
		if (!hasText(paramMap.get("todoNo")) || !hasText(paramMap.get("todoId"))
				|| !hasText(paramMap.get("todoDiv1CodeId")) || !hasText(paramMap.get("todoDiv2CodeId"))) {
			return null;
		}
		Map<String, String> keyParam = new HashMap<>();
		keyParam.put("todoNo", paramMap.get("todoNo"));
		keyParam.put("todoId", paramMap.get("todoId"));
		keyParam.put("todoDiv1CodeId", paramMap.get("todoDiv1CodeId"));
		keyParam.put("todoDiv2CodeId", paramMap.get("todoDiv2CodeId"));
		keyParam.put("coCd", paramMap.get("coCd"));
		String docId = wb20Mapper.selectAmDocIdByWbLineKey(keyParam);
		return hasText(docId) ? docId : null;
	}

	/**
	 * WB20 결재취소 시 되돌릴 AM 문서를 특정한다. 없으면 null(=AM 미연동, 동기화 생략).
	 * 취소자(userId)가 곧 취소 대상 결재행의 TODO_ID이며, 마지막 결재자가 취소하면 문서가 COMPLETED일 수 있어 완료 문서를 포함한다.
	 */
	private String findAmDocIdForCancel(Map<String, String> paramMap) {
		if (!hasText(paramMap.get("todoNo")) || !hasText(paramMap.get("userId"))
				|| !hasText(paramMap.get("todoDiv1CodeId")) || !hasText(paramMap.get("todoDiv2CodeId"))) {
			return null;
		}
		Map<String, String> keyParam = new HashMap<>();
		keyParam.put("todoNo", paramMap.get("todoNo"));
		keyParam.put("todoId", paramMap.get("userId"));
		keyParam.put("todoDiv1CodeId", paramMap.get("todoDiv1CodeId"));
		keyParam.put("todoDiv2CodeId", paramMap.get("todoDiv2CodeId"));
		keyParam.put("coCd", paramMap.get("coCd"));
		keyParam.put("includeCompleted", "Y");
		String docId = wb20Mapper.selectAmDocIdByWbLineKey(keyParam);
		return hasText(docId) ? docId : null;
	}

	private boolean isPm51SalesApproval(Map<String, String> paramMap) {
		return "TODODIV2190".equals(paramMap.get("todoDiv2CodeId"))
				&& isSalesDept(paramMap.get("deptId"))
				&& hasText(paramMap.get("todoNo"));
	}

	private void updatePm51AprvSts(Map<String, String> paramMap, String aprvStsCd) {
		if (paramMap == null || !hasText(paramMap.get("todoNo"))) {
			return;
		}
		String operatorId = hasText(paramMap.get("todoId")) ? paramMap.get("todoId") : paramMap.get("userId");
		if (!hasText(operatorId)) {
			throw new RuntimeException("결재 처리자 ID(todoId/userId)가 누락되어 출장신청서 결재상태를 갱신할 수 없습니다.");
		}
		Map<String, String> tripParam = new HashMap<>();
		tripParam.put("tripReqNo", paramMap.get("todoNo"));
		tripParam.put("aprvStsCd", aprvStsCd);
		tripParam.put("todoId", operatorId);
		pm51Mapper.updateTripReqAprvStsCd(tripParam);
	}

	private void updatePm52AprvSts(Map<String, String> paramMap, String aprvStsCd) {
		if (paramMap == null || !hasText(paramMap.get("todoNo"))) {
			return;
		}
		String operatorId = hasText(paramMap.get("todoId")) ? paramMap.get("todoId") : paramMap.get("userId");
		if (!hasText(operatorId)) {
			throw new RuntimeException("결재 처리자 ID(todoId/userId)가 누락되어 출장복명서 결재상태를 갱신할 수 없습니다.");
		}
		Map<String, String> rptParam = new HashMap<>();
		rptParam.put("tripRptNo", paramMap.get("todoNo"));
		rptParam.put("aprvStsCd", aprvStsCd);
		rptParam.put("todoId", operatorId);
		pm51Mapper.updateTripRptAprvStsCd(rptParam);
	}

	private boolean isSalesDept(String deptId) {
		return deptId != null && (deptId.startsWith("GUN30") || deptId.startsWith("TRN30"));
	}

	private boolean checked(String value) {
		return "Y".equals(value);
	}

	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

	@Override
	public Map<String, String> insertApprovalMemoComment(Map<String, String> paramMap) {

		int result = 0;
		result += wb20Mapper.insertApprovalMemoComment(paramMap);

		// 최종결재 완료시 알림톡 발송 대상인지 확인
		Map<String, String> resultMap = wb20Mapper.selectMobileTodoSelect(paramMap);
		resultMap.put("resultCount", Integer.toString(result));

		return resultMap;
	}

	// 결재라인 싱글 셀렉트 read
	@Override
	public List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap) {
		return wb20Mapper.selectSignResUserlst(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap) {
		return wb20Mapper.selectSignResUserlstInit(paramMap);
	}

	// 결재라인 부서명등 select
	@Override
	public List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap) {
		return wb20Mapper.selectShareUserInfo(paramMap);
	}

	@Override
	public String selectmaxTodoKey(Map<String, String> paramMap) {
		return wb20Mapper.selectmaxTodoKey(paramMap);
	}

	// wb20 결재 insert
    /***********************************************************************************************
     * 1. 결재정보 저장할때 동일건은 등록일시가 동일하게 처리해야함. 각 이벤트별 결재자를 하나로 묶어주는것을 날자시간으로 처리함. 2. 결재정보 처리절차 2-1 이전에 등록된 결재자 정보 삭제처리 TODO_DIV2_CODE_ID =
     * #{todoDiv2CodeId} AND SALES_CD = #{salesCd} AND TODO_NO = #{todoNo} 2-2 새로운 결재 내역 등록 (1번의 날자시간은 동일하게 처리함)
     ***********************************************************************************************/
	@Override
	public int insertTodoMaster(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);

		int result = 0;
		String maxTodoKey = "";
		if (paramMap.containsKey("approvalArr")) {
			// 수정시 삭제된 부분만 처리가 불가하여 전체 삭제후 저장
			for (Map<String, String> dtl : detailMap) {
				wb20Mapper.deleteAllTodoMaster(dtl);
			}
			String sysCreateDttm = wb20Mapper.selectSystemCreateDttm(paramMap);
			// 1) 결재라인 전체를 먼저 INSERT 한다.
			for (Map<String, String> dtl : detailMap) {
				// 입력, 수정
				String tempKey = dtl.get("todoKey");
				if (tempKey == null || tempKey.equals("")) {
					maxTodoKey = wb20Mapper.selectmaxTodoKey(dtl);
					dtl.put("todoKey", maxTodoKey);
				}
				dtl.put("createDttm", sysCreateDttm);
				if (dtl.get("userId") == null || dtl.get("userId").equals("")) {
					dtl.put("userId", paramMap.get("userId"));
				}
				if (dtl.get("pgmId") == null || dtl.get("pgmId").equals("")) {
					dtl.put("pgmId", paramMap.get("pgmId"));
				}
				if (dtl.get("todoNo") == null || dtl.get("todoNo").equals("")) {
					dtl.put("todoNo", paramMap.get("todoNo"));
				}
				if (dtl.get("coCd") == null || dtl.get("coCd").equals("")) {
					dtl.put("coCd", paramMap.get("coCd"));
				}
				if (dtl.get("todoCoCd") == null || dtl.get("todoCoCd").equals("")) {
					dtl.put("todoCoCd", paramMap.get("todoCoCd"));
				}
				if (dtl.get("salesCd") == null || dtl.get("salesCd").equals("")) {
					dtl.put("salesCd", paramMap.get("salesCd"));
				}

				result += wb20Mapper.insertTodoMaster(dtl);
			}

			// 2) 전체 INSERT가 끝난 뒤에 기안자 본인 자체승인 후처리를 진행한다.
			// (이전에는 위 INSERT 루프 안에서 각 행을 넣자마자 바로 자체승인 처리를 했는데,
			//  이 시점엔 아직 뒤 순번(상급자) 결재행이 TB_WB20M03에 들어가기 전이라
			//  selectTodoFinalYn 등 결재선 전체를 다시 조회하는 로직들이 "본인 1명만 있는
			//  결재선"으로 착각해 전원 승인완료로 오판하는 문제가 있었다. 결재라인 전체가
			//  DB에 반영된 뒤에 자체승인을 처리하도록 순서를 바꿔서 근본적으로 해결한다.)
			for (Map<String, String> dtl : detailMap) {
                // 조치자가 팀장일경우 insertWbsApprovalList 에서 결재완료처리로 등록되므로 상태코드를 진행으로 변경하기 위해 아래 쿼리 실행함
                // insertWbsApprovalList --> usrNm 을 todoId 에 저장하고 있음
                if (dtl.get("userId").equals(dtl.get("todoId"))) {
                    if ("TODODIV2150".equals(dtl.get("todoDiv2CodeId")) || "TODODIV2160".equals(dtl.get("todoDiv2CodeId"))) { // 개선 제안서이면
                        //개선 제안서이면 자체 승인처리 없음.  결재하면서 의견등록하기 위함
                    } else {
                        dtl.put("todoCfOpn", "자체승인");
                        insertApprovalLine(dtl);
                    }
                }
			}
		}
		return result;
	}

	// wb20 todo 삭제
	@Override
	public int deleteTodoMaster(Map<String, String> param) {
		int result = wb20Mapper.deleteTodoMaster(param);
		result = wb20Mapper.updateTodoMasterSanctnSn(param);
		return result;
	}

	@Override
	public int deleteTodoMasterByTodoNo(Map<String, String> param) {
		return wb20Mapper.deleteTodoMasterByTodoNo(param);
	}

	@Override
	public Map<String, String> selectMobileTodoSelect(Map<String, String> paramMap) {
		return wb20Mapper.selectMobileTodoSelect(paramMap);
	}

	@Override
	public Map<String, String> selectTodoFinalYn(Map<String, String> paramMap) {
		return wb20Mapper.selectTodoFinalYn(paramMap);
	}

	@Override
	public int updateApprovalCancle(Map<String, String> paramMap) {
		validatePm51SequentialCancel(paramMap);
		int result = wb20Mapper.updateApprovalCancle(paramMap);

		// 기존 WB20 결재취소 결과를 동일 업무의 AM 전자결재 문서/결재선/이력에 반영
		// 대상 AM 문서를 식별키(DIV1+DIV2+TODO_ID+TODO_NO, CR02만 ETC_FIELD2)로 한 번 특정해 넘기고, 미연동이면 건너뛴다.
		String amDocId = findAmDocIdForCancel(paramMap);
		if (hasText(amDocId)) {
			paramMap.put("amDocId", amDocId);
			wb20Mapper.syncAmApprovalCancelNextLine(paramMap);
			wb20Mapper.syncAmApprovalCancelLine(paramMap);
			wb20Mapper.syncAmApprovalCancelHist(paramMap);
			wb20Mapper.syncAmApprovalCancelDocument(paramMap);
		}
		// PM51 신청결재 완료를 취소하면 업무 상태도 진행중으로 복구한다.
		if ("TODODIV2190".equals(paramMap.get("todoDiv2CodeId"))) {
			updatePm51AprvSts(paramMap, "APRVSTS02");
		}
		// PM52 복명서 신청부서 결재 완료를 취소하면 복명서 상태도 진행중으로 복구한다(신청서와 동일 기준).
		if ("TODODIV2200".equals(paramMap.get("todoDiv2CodeId"))) {
			updatePm52AprvSts(paramMap, "APRVSTS02");
		}


		/***************************************************************************************
		 * 결재 취소 처리시 팀장인경우에만 투입공수 Clear 처리 가능함 -- 처리시작
		 ***************************************************************************************/
		// 허용할 팀장 부서 접두어 목록 검사

		String deptId = paramMap.get("deptId");
		boolean isManagerDept = deptId != null && (deptId.startsWith("GUN30")|| deptId.startsWith("GUN40")|| deptId.startsWith("TRN50")|| deptId.startsWith("GUN60"));
		// deptId 로 팀장 id 가져오기~~
		Map<String, String> detailMap = wb24Mapper.selectDept2TeamManagerInfo(paramMap);

		if (detailMap != null && detailMap.get("id").equals(paramMap.get("userId")) && isManagerDept) {
			if ("TODODIV2030".equals(paramMap.get("todoDiv2CodeId"))) {	// 발주요청서 따로 결과등록
				if ("GUN30".equals(paramMap.get("deptId")) ||
					"GUN40".equals(paramMap.get("deptId")) ||
					"TRN50".equals(paramMap.get("deptId")) ||
					"GUN60".equals(paramMap.get("deptId"))) {
        				paramMap.put("reqNo", paramMap.get("todoNo"));
        				result += qm01Mapper.updateReqActMdCancle(paramMap);
				}
			} else if ("TODODIV2090".equals(paramMap.get("todoDiv2CodeId"))) {	// 문제조치
				if ("GUN30".equals(paramMap.get("deptId")) ||
					"GUN40".equals(paramMap.get("deptId")) ||
					"TRN50".equals(paramMap.get("deptId")) ||
					"GUN60".equals(paramMap.get("deptId"))) {
        				paramMap.put("issNo", paramMap.get("todoNo"));
        				result += wb24Mapper.updateWbsIssueActMdCancle(paramMap);
				}
			} else if ("TODODIV2020".equals(paramMap.get("todoDiv2CodeId"))){
				// 동시 입력건인지 체크하고 결과동시 등로건이면 각 부서별 투입공수 초기화
				Map<String, String> paramMap2 = new HashMap<>();
				paramMap2.put("fileTrgtKey", paramMap.get("todoFileTrgtKey"));
				paramMap2.put("reqNo", paramMap.get("todoNo"));
				Map<String, String> selectQtyReqInfo = qm01Mapper.selectQtyReqInfo(paramMap2);
				if (!selectQtyReqInfo.isEmpty() && "Y".equals(selectQtyReqInfo.get("sameTimeResult"))) {
					if ("GUN30".equals(paramMap.get("deptId")) ||
						"GUN40".equals(paramMap.get("deptId")) ||
						"TRN50".equals(paramMap.get("deptId")) ||
						"GUN60".equals(paramMap.get("deptId"))) {
        					paramMap.put("reqNo", paramMap.get("todoNo"));
        					result += qm01Mapper.updateReqActMdCancle(paramMap);
					}
				}
			}
		}
		if ("TODODIV2130".equals(paramMap.get("todoDiv2CodeId"))) {
			result += cm16Mapper.updateItoaIssueStCancelChk(paramMap);
		}
		if ("TODODIV2202".equals(paramMap.get("todoDiv2CodeId"))) {
			paramMap.put("expendNo", paramMap.get("todoNo"));
			result += cm25Mapper.updateExpendSts(paramMap);
		}
		/***************************************************************************************
		 * 결재 취소 처리시 팀장인경우에만 투입공수 Clear 처리 가능함 -- 처리종료
		 ***************************************************************************************/

		// 출장신청 관리부서 결재(TODODIV2191) 취소 시: 지급을 집행한 담당자(PAY_ID) 본인이 취소할 때만 지급완료 정보 롤백 (팀장/임원 결재취소는 본인 승인만 취소)
		if ("TODODIV2191".equals(paramMap.get("todoDiv2CodeId"))) {
			Map<String, String> payParam = new HashMap<>();
			payParam.put("tripReqNo", paramMap.get("todoNo"));
			payParam.put("userId", paramMap.get("userId"));
			payParam.put("pgmId", paramMap.get("pgmId") != null ? paramMap.get("pgmId") : "WB2001M01");
			Map<String, String> m01 = pm51Mapper.selectTripReqM01(payParam);
			if (m01 != null && paramMap.get("userId") != null && paramMap.get("userId").equals(m01.get("payId"))) {
				pm51Mapper.updateTripReqPayCancel(payParam);
			}
		}

		// 출장복명서 관리부서 결재(TODODIV2201) 취소 시: 지급/정산을 집행한 담당자(PAY_ID) 본인이 취소할 때만 경비정산/지급완료 롤백 (팀장/임원 결재취소는 본인 승인만 취소)
		if ("TODODIV2201".equals(paramMap.get("todoDiv2CodeId"))) {
			Map<String, String> payParam = new HashMap<>();
			payParam.put("tripRptNo", paramMap.get("todoNo"));
			payParam.put("userId", paramMap.get("userId"));
			payParam.put("pgmId", paramMap.get("pgmId") != null ? paramMap.get("pgmId") : "WB2001M01");
			Map<String, String> m02 = pm51Mapper.selectTripRptM01(payParam);
			if (m02 != null && paramMap.get("userId") != null && paramMap.get("userId").equals(m02.get("payId"))) {
				pm51Mapper.deleteTripRptD01(payParam);
				pm51Mapper.clearTripExpenseStatusByRptNo(payParam);
				pm51Mapper.updateTripRptPayCancel(payParam);
			}
		}

		return result;
	}

	@Override
	public int M08selectToDoCount(Map<String, String> paramMap) {
		return wb20Mapper.M08selectToDoCount(paramMap);
	}

	@Override
	public List<Map<String, String>> M08selectToDoList(Map<String, String> paramMap) {
		return wb20Mapper.M08selectToDoList(paramMap);
	}


	@Override
	public List<Map<String, String>> selectCurrentUserApprovalDataList(Map<String, String> paramMap) {
		return wb20Mapper.selectCurrentUserApprovalDataList(paramMap);
	}

    @Override
    public List<Map<String, String>> selectCurrentUserApprovalDataListFromTodoKey(Map<String, String> paramMap) {
        return wb20Mapper.selectCurrentUserApprovalDataListFromTodoKey(paramMap);
    }

    @Override
    public List<Map<String, String>> selectKakaoReceiveList(Map<String, String> paramMap) {
        return wb20Mapper.selectKakaoReceiveList(paramMap);
    }

    @Override
    public int saveKakaoReceiveList(Map<String, Object> paramMap) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        List<Map<String, String>> detailList = gson.fromJson((String) paramMap.get("detailArr"), listType);

        int result = 0;
        String coCd = (String) paramMap.get("coCd");
        String userId = (String) paramMap.get("userId");
        String chkUserId = (String) paramMap.get("chkUserId");
        String pgmId = (String) paramMap.get("pgmId");

        for (Map<String, String> detail : detailList) {
            String remark = detail.get("remark");  // 결재/공유 둘 다에 같은 remark 저장

            // 결재 CODE_ID가 있으면 저장
            String apprCodeId = detail.get("apprCodeId");
            if (apprCodeId != null && !apprCodeId.isEmpty()) {
                Map<String, String> apprParam = new HashMap<>();
                apprParam.put("coCd", coCd);
                apprParam.put("codeId", apprCodeId);
                apprParam.put("userId", userId);
                apprParam.put("receiveYn", detail.get("apprReceiveYn"));
                apprParam.put("chkUserId", chkUserId);
                apprParam.put("pgmId", pgmId);
                apprParam.put("remark", remark);
                result += wb20Mapper.mergeKakaoReceive(apprParam);
            }

            // 공유 CODE_ID가 있으면 저장
            String shareCodeId = detail.get("shareCodeId");
            if (shareCodeId != null && !shareCodeId.isEmpty()) {
                Map<String, String> shareParam = new HashMap<>();
                shareParam.put("coCd", coCd);
                shareParam.put("codeId", shareCodeId);
                shareParam.put("userId", userId);
                shareParam.put("receiveYn", detail.get("shareReceiveYn"));
                shareParam.put("chkUserId", chkUserId);
                shareParam.put("pgmId", pgmId);
                shareParam.put("remark", remark);
                result += wb20Mapper.mergeKakaoReceive(shareParam);
            }
        }
        return result;
    }

	@Override
	public int insertPfuShareUser(Map<String, String> paramMap) throws Exception {
		int result = 0;
		String pgParam =
			createPgParam(
					"T",
					paramMap.get("todoFileTrgtKey"),
					paramMap.get("coCd"),
					paramMap.get("salesCd").substring(0, 5),
					paramMap.get("salesCd")
			);
		String todoTitle = paramMap.get("todoTitl").replace("결재", "공유").replace("[신규]", "").trim();;

		List<Map<String, String>> selectPfuShareList = wb20Mapper.selectPfuShareList(paramMap);
		int i = 0;
		for (Map<String, String> shareUser : selectPfuShareList) {
			// shareUser.putAll(paramMap);
			int todoKey = wb20Mapper.selectNextTodoKey(paramMap);
			Map<String, String> param = new HashMap<>();
			param.put("toDoKey", Integer.toString(todoKey));
			param.put("reqNo", paramMap.get("todoFileTrgtKey"));
			param.put("salesCd", paramMap.get("salesCd"));
			param.put("fileTrgtKey", paramMap.get("todoFileTrgtKey"));
			param.put("pgmId", "CR5001P01");
			param.put("userId", paramMap.get("userId"));
			param.put("usrNm", shareUser.get("userId"));
			param.put("histNo", paramMap.get("histNo"));
			param.put("coCd", paramMap.get("coCd"));
			param.put("todoCoCd", paramMap.get("coCd"));
			param.put("sanCtnSn", Integer.toString(++i));
			param.put("todoDiv1CodeId", "TODODIV10");
			param.put("todoDiv2CodeId", "TODODIV1120");
			param.put("pgPath", "/user/cr/cr50/CR5001P01.html");
			param.put("pgParam", pgParam);
			param.put("todoTitle", todoTitle);

			wb20Mapper.insertPfuSharngList(param);
			result++;
		}

		return result;
	}

	private String createPgParam(String actionType, String fileTrgtKey, String coCd, String ordrsNo, String salesCd) {
        return String.format("{\"actionType\":\"%s\",\"fileTrgtKey\":\"%s\",\"coCd\":\"%s\",\"ordrsNo\":\"%s\",\"salesCd\":\"%s\"}",
                             actionType, fileTrgtKey, coCd, ordrsNo, salesCd);
    }

	@Override
	public List<Map<String, String>> selectToDoMindMap(Map<String, String> paramMap) {
		return wb20Mapper.selectToDoMindMap(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> rejectApprovalLine(Map<String, String> paramMap) {
		Map<String, String> result = new HashMap<>();

		// AM 연동 판별 및 위임 (1차진입일 때만)
		boolean isReentry = "Y".equals(paramMap.get("amLinkedApproval"));
		if (!isReentry && am11Svc != null) {
			String erpBizKey = paramMap.get("todoNo");
			String coCd = paramMap.get("coCd");

			// 진행 중인 AM 연동 문서가 있을 때만 위임한다. 없으면 기존 WB 흐름으로 처리한다.
			String docId = findActiveAmDocId(paramMap);

			// AM 연동 문서 존재 시 위임
			if (docId != null && !docId.isEmpty()) {
				Map<String, Object> amParam = new HashMap<>();
				amParam.put("docId", docId);
				amParam.put("userId", paramMap.get("userId"));
				String rejectOpinion = paramMap.get("rejectOpinion");
				if (rejectOpinion == null || rejectOpinion.trim().isEmpty()) {
					rejectOpinion = paramMap.get("todoCfOpn");
				}
				if (rejectOpinion == null || rejectOpinion.trim().isEmpty()) {
					rejectOpinion = "";
				}
				amParam.put("apprOpinion", rejectOpinion);

				Map<String, Object> amResult = am11Svc.rejectDocument(amParam);

				// AM 엔진 결과 판정: 200 아니면 실패 반환
				if (amResult == null || !"200".equals(amResult.get("resultCode"))) {
					result.put("resultCount", "0");
					result.put("RESULT_COUNT", "0");
					if (amResult != null && amResult.get("resultMessage") != null) {
						result.put("resultMessage", String.valueOf(amResult.get("resultMessage")));
					}
					return result;
				}

				// 엔진 rejectDocument는 AGREE 반려 시에만 WB20 전파를 스킵한다(AM11SvcImpl L955).
				// 따라서 반려한 라인의 LINE_TYPE이 AGREE일 때만 WB20 원본행을 직접 반려한다.
				// (APPR은 엔진 콜백이 이미 반려 처리하므로 직접 반려하면 이중 처리가 된다.
				//  COOP/REF는 엔진이 반려 자체를 차단하므로 이 지점에 도달하지 않는다.)
				Map<String, String> queryParam = new HashMap<>();
				queryParam.put("todoNo", erpBizKey);
				queryParam.put("coCd", coCd);
				List<Map<String, String>> wb20Lines = selectGetApprovalList(queryParam);

				// 반려 라인 식별: TODO_DIV1_CODE_ID + TODO_DIV2_CODE_ID + TODO_ID (TODO_NO는 위 조회로 한정). TODO_KEY는 쓰지 않는다.
				String actingLineType = "APPR";
				for (Map<String, String> line : wb20Lines) {
					if (hasText(paramMap.get("todoId")) && paramMap.get("todoId").equals(line.get("todoId"))
							&& hasText(paramMap.get("todoDiv1CodeId")) && paramMap.get("todoDiv1CodeId").equals(line.get("todoDiv1CodeId"))
							&& hasText(paramMap.get("todoDiv2CodeId")) && paramMap.get("todoDiv2CodeId").equals(line.get("todoDiv2CodeId"))) {
						String lt = line.get("lineType");
						actingLineType = hasText(lt) ? lt : "APPR";
						break;
					}
				}
				boolean needsWb20Reject = "AGREE".equals(actingLineType);

				if (needsWb20Reject) {
					int wbCount = wb20Mapper.rejectApprovalLine(paramMap);
					result.put("resultCount", String.valueOf(wbCount));
					result.put("RESULT_COUNT", String.valueOf(wbCount));
				} else {
					result.put("resultCount", "1");
					result.put("RESULT_COUNT", "1");
				}
				return result;
			}
		}

		// 비연동 또는 재진입: 기존 WB20 반려 흐름 실행
		int count = wb20Mapper.rejectApprovalLine(paramMap);
		result.put("resultCount", String.valueOf(count));
		result.put("RESULT_COUNT", String.valueOf(count));
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int resetRejectedApprovalLines(Map<String, String> paramMap) {
		return wb20Mapper.resetRejectedApprovalLines(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<Map<String, Object>> syncApprovalLinesFromAm(Map<String, Object> paramMap) {
		List<Map<String, Object>> results = new ArrayList<>();

		if (paramMap == null || paramMap.get("todoNo") == null) {
			return results;
		}

		String todoNo = String.valueOf(paramMap.get("todoNo"));
		String coCd = String.valueOf(paramMap.get("coCd"));
		String userId = String.valueOf(paramMap.get("userId"));
		Object histNoObj = paramMap.get("histNo");
		String histNo = (histNoObj != null) ? String.valueOf(histNoObj) : null;
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> lineList = (List<Map<String, Object>>) paramMap.get("lineList");

		logger.info("[AM→WB sync] 진입. todoNo={}, coCd={}, lineList.size={}",
				todoNo, coCd, (lineList == null ? "null" : lineList.size()));

		if (lineList == null || lineList.isEmpty()) {
			logger.warn("[AM→WB sync] 조기종료: lineList 비어있음. todoNo={}", todoNo);
			return results;
		}

		// 0) 현재 WB20 결재체인 스냅샷 로드
		Map<String, String> snapParam = new HashMap<>();
		snapParam.put("todoNo", todoNo);
		snapParam.put("coCd", coCd);
		List<Map<String, String>> wb20Rows = wb20Mapper.selectGetApprovalList(snapParam);

		if (wb20Rows == null || wb20Rows.isEmpty()) {
			logger.warn("[AM→WB sync] 조기종료: selectGetApprovalList 0건(todoNo/coCd 불일치 의심). todoNo={}, coCd={}",
					todoNo, coCd);
			return results;
		}
		logger.info("[AM→WB sync] 스냅샷 로드. wb20Rows.size={}, todoNo={}, coCd={}", wb20Rows.size(), todoNo, coCd);

		// A) CREAT_* 상속: 문서 최초 생성정보(CREAT_ID/PGM/DTTM)를 원본값에서 상속
		Map<String, String> origCreatParam = new HashMap<>();
		origCreatParam.put("todoNo", todoNo);
		origCreatParam.put("coCd", coCd);
		Map<String, String> origCreatInfo = wb20Mapper.selectOrigCreatInfo(origCreatParam);

		if (origCreatInfo == null || origCreatInfo.isEmpty()) {
			logger.warn(
					"syncApprovalLinesFromAm 스킵: 기존 결재행이 없거나 NULL CREAT_DTTM 상태. todoNo={}, coCd={}. 먼저 DELETE FROM TB_WB20M03 WHERE TODO_NO=? AND CREAT_PGM='AM1201M01' 실행 필요.",
					todoNo, coCd);
			return results;
		}

		String creatId = origCreatInfo.get("creatId");
		String creatPgm = origCreatInfo.get("creatPgm");
		String createDttm = origCreatInfo.get("createDttm");

		if (creatId == null || creatPgm == null || createDttm == null) {
			logger.warn(
					"syncApprovalLinesFromAm 스킵: 원본 생성정보 불완전. todoNo={}, coCd={}, creatId={}, creatPgm={}, createDttm={}",
					todoNo, coCd, creatId, creatPgm, createDttm);
			return results;
		}

		// 1) 링크보유 / 신규 분류: wb20TodoKey를 단일 판정 기준으로 사용
		// (linked: 값 있음 → WB20 기존 행 매칭 가능 / brandNew: null 또는 빈 문자열 → 신규 생성)
		List<Map<String, Object>> linked = new ArrayList<>();
		List<Map<String, Object>> brandNew = new ArrayList<>();

		for (Map<String, Object> line : lineList) {
			Object wb20TodoKey = line.get("wb20TodoKey");
			Object wb20SanctnSn = line.get("wb20SanctnSn");
			Object wb20Div1CodeId = line.get("wb20Div1CodeId");
			Object wb20Div2CodeId = line.get("wb20Div2CodeId");

			// wb20TodoKey 단일 기준: null 또는 trim 후 empty인지 판정
			String todoKeyStr = wb20TodoKey == null ? null : String.valueOf(wb20TodoKey).trim();
			boolean hasLinkedKey = todoKeyStr != null && !todoKeyStr.isEmpty();

			if (hasLinkedKey) {
				// linked: wb20TodoKey 값 있음
				linked.add(line);
				logger.info(
						"[AM→WB sync] 분류: linked. approverId={}, lineType={}, wb20TodoKey=[{}], wb20SanctnSn=[{}], wb20Div1=[{}], wb20Div2=[{}]",
						line.get("approverId"), line.get("lineType"), todoKeyStr,
						wb20SanctnSn, wb20Div1CodeId, wb20Div2CodeId);
			} else {
				// brandNew: wb20TodoKey 없음 (기존 필드 부분유무는 무시)
				brandNew.add(line);
				logger.info(
						"[AM→WB sync] 분류: brandNew. approverId={}, lineType={}, wb20TodoKey=[{}], wb20SanctnSn=[{}], wb20Div1=[{}], wb20Div2=[{}]",
						line.get("approverId"), line.get("lineType"), todoKeyStr,
						wb20SanctnSn, wb20Div1CodeId, wb20Div2CodeId);
			}
		}

		// 2) 링크보유 라인 upsert (승인행은 손대지 않음, insertTodoMasterForAmLink로 자동승인 회피)
		for (Map<String, Object> line : linked) {
			// String.valueOf(null) 방어: wb20 필드 null 체크
			String wb20TodoKey = line.get("wb20TodoKey") != null ? String.valueOf(line.get("wb20TodoKey")) : null;
			String wb20SanctnSn = line.get("wb20SanctnSn") != null ? String.valueOf(line.get("wb20SanctnSn")) : null;
			String wb20CoCd = line.get("wb20CoCd") != null ? String.valueOf(line.get("wb20CoCd")) : null;
			String wb20TodoNo = line.get("wb20TodoNo") != null ? String.valueOf(line.get("wb20TodoNo")) : null;
			String wb20Div1CodeId = line.get("wb20Div1CodeId") != null ? String.valueOf(line.get("wb20Div1CodeId")) : null;
			String wb20Div2CodeId = line.get("wb20Div2CodeId") != null ? String.valueOf(line.get("wb20Div2CodeId")) : null;

			// 필수 필드 검증
			if (wb20TodoKey == null || wb20TodoKey.isEmpty()) {
				logger.warn("[AM→WB sync] linked 라인 스킵: wb20TodoKey null/empty. approverId={}, lineType={}", line.get("approverId"), line.get("lineType"));
				continue;
			}

			// 기존 WB20 행이 존재하는지 확인
			boolean matchedRowExists = false;
			for (Map<String, String> row : wb20Rows) {
				if (String.valueOf(row.get("todoKey")).equals(wb20TodoKey)) {
					matchedRowExists = true;
					break;
				}
			}

			// 기존 행이 존재하면 그대로 보존하고 upsert하지 않지만, LINE_TYPE만 갱신한다.
			// (keepKeys로 step3 삭제 대상에서도 제외됨. upsert하면 selectGetApprovalList가
			//  반환하지 않는 컬럼 - TODO_TITL, PG_PARAM 등 - 이 NULL로 덮여 값이 사라진다.)
			if (matchedRowExists) {
				// LINE_TYPE 갱신: AM에서 변경된 구분이 기존 WB행에도 반영되게
				String lineType = line.get("lineType") != null ? String.valueOf(line.get("lineType")) : "APPR";
				// AGREE/COOP/POST는 값을 그대로 보존, REF/REFERENCE는 'REF', 그 외(APPR/null)는 'APPR'
				// (기존 isApprGroup?"APPR":"REF" 는 AGREE/COOP/POST를 APPR로 뭉개는 버그였음)
				String wbLineType = ("AGREE".equals(lineType) || "COOP".equals(lineType) || "POST".equals(lineType)) ? lineType
						: (("REF".equals(lineType) || "REFERENCE".equals(lineType)) ? "REF" : "APPR");

				Map<String, Object> lineTypeParam = new HashMap<>();
				lineTypeParam.put("todoNo", todoNo);
				lineTypeParam.put("coCd", coCd);
				lineTypeParam.put("todoKey", wb20TodoKey);
				lineTypeParam.put("lineType", wbLineType);
				lineTypeParam.put("userId", userId);
				lineTypeParam.put("pgmId", "AM1201M01");

				int updCnt = wb20Mapper.updateTodoLineTypeByKey(lineTypeParam);
				logger.info("[AM→WB sync] linked 라인 LINE_TYPE 갱신. wb20TodoKey={}, lineType={}, wbLineType={}, updCnt={}",
						wb20TodoKey, lineType, wbLineType, updCnt);
				continue;
			}

			// MERGE upsert via insertTodoMasterForAmLink (자동승인 회피)
			String linkedApproverId = line.get("approverId") != null ? String.valueOf(line.get("approverId")).trim() : null;
			if (linkedApproverId == null || linkedApproverId.isEmpty()) {
				logger.warn("[AM→WB sync] linked 라인 upsert 스킵: approverId null/empty. wb20TodoKey={}, lineType={}", wb20TodoKey, line.get("lineType"));
				continue;
			}

			Map<String, String> upsertParam = new HashMap<>();
			upsertParam.put("todoKey", wb20TodoKey);
			upsertParam.put("sanctnSn", wb20SanctnSn);
			upsertParam.put("coCd", wb20CoCd);
			upsertParam.put("todoDiv1CodeId", wb20Div1CodeId);
			upsertParam.put("todoDiv2CodeId", wb20Div2CodeId);
			upsertParam.put("todoId", linkedApproverId);
			upsertParam.put("todoNo", wb20TodoNo);
			upsertParam.put("sanctnSttus", "N");
			upsertParam.put("pgmId", "AM1201M01");
			upsertParam.put("userId", userId);
			upsertParam.put("creatId", creatId);
			upsertParam.put("creatPgm", creatPgm);
			upsertParam.put("createDttm", createDttm);
			upsertParam.put("etcField2", histNo);

			// 부가 컬럼: snapshot에서 상속
			for (Map<String, String> row : wb20Rows) {
				if (String.valueOf(row.get("todoKey")).equals(wb20TodoKey)) {
					// selectGetApprovalList 반환 컬럼
					if (row.get("salesCd") != null) {
						upsertParam.put("salesCd", row.get("salesCd"));
					}
					if (row.get("pgPath") != null) {
						upsertParam.put("pgPath", row.get("pgPath"));
					}
					if (row.get("pgParam") != null) {
						upsertParam.put("pgParam", row.get("pgParam"));
					}
					if (row.get("todoCoCd") != null) {
						upsertParam.put("todoCoCd", row.get("todoCoCd"));
					}
					if (row.get("todoCodeKind") != null) {
						upsertParam.put("todoCodeKind", row.get("todoCodeKind"));
					}
					if (row.get("todoCodeId") != null) {
						upsertParam.put("todoCodeId", row.get("todoCodeId"));
					}
					if (row.get("todoFileTrgtKey") != null) {
						upsertParam.put("todoFileTrgtKey", row.get("todoFileTrgtKey"));
					}
					break;
				}
			}

			logger.info(
					"[AM→WB sync] linked 라인 upsert. wb20TodoKey={}, approverId={}, div1={}, div2={}, sanctnSn={}",
					wb20TodoKey, linkedApproverId, wb20Div1CodeId, wb20Div2CodeId, wb20SanctnSn);

			wb20Mapper.insertTodoMasterForAmLink(upsertParam);
		}

		// 3) 삭제: WB20 잔여 미승인 행 중 AM 목록에 없는 것 제거 (APPR/REF 모두)
		// keepKeys: linked 라인들의 TODO_KEY 세트
		Map<String, Object> keepKeys = new HashMap<>();
		for (Map<String, Object> line : linked) {
			keepKeys.put(String.valueOf(line.get("wb20TodoKey")), true);
		}

		// P2) 삭제 이후 문서가 비었는지 판정 & 스냅샷 템플릿 확보
		// 판정 조건: 생존 행의 존재 여부 (승인 'Y' 또는 linked 유지행 또는 non-APPR/REF)
		Map<String, String> snapshotTemplate = null;
		Integer minSanctnSn = null;
		String minSanctnSnDiv1 = null;
		String minSanctnSnDiv2 = null;

		for (Map<String, String> row : wb20Rows) {
			String rowTodoKey = String.valueOf(row.get("todoKey"));
			String rowSanctnSttus = row.get("sanctnSttus");
			String rowDiv1CodeId = row.get("todoDiv1CodeId");
			String rowDiv2CodeId = row.get("todoDiv2CodeId");
			int rowSn = Integer.parseInt(String.valueOf(row.get("sanctnSn")));

			// Survival 조건: 승인 'Y' 또는 non-APPR/REF 또는 linked 유지행
			boolean survives = "Y".equals(rowSanctnSttus)
					|| (!("TODODIV20".equals(rowDiv1CodeId) || "TODODIV10".equals(rowDiv1CodeId)))
					|| keepKeys.containsKey(rowTodoKey);

			if (survives && minSanctnSn == null) {
				// 최소 SANCTN_SN 행 기록: 생존 행 중 첫 번째
				minSanctnSn = rowSn;
				minSanctnSnDiv1 = rowDiv1CodeId;
				minSanctnSnDiv2 = rowDiv2CodeId;
			}
		}

		// 생존 행이 없으면 스냅샷 템플릿 확보: APPR 그룹의 최소 SN 행 선택
		if (minSanctnSn == null && !brandNew.isEmpty()) {
			for (Map<String, String> row : wb20Rows) {
				String rowDiv1CodeId = row.get("todoDiv1CodeId");
				if ("TODODIV20".equals(rowDiv1CodeId)) {
					int rowSn = Integer.parseInt(String.valueOf(row.get("sanctnSn")));
					if (minSanctnSn == null || rowSn < minSanctnSn) {
						minSanctnSn = rowSn;
						minSanctnSnDiv1 = "TODODIV20";
					}
				}
			}

			// 스냅샷 템플릿 확보
			if (minSanctnSn != null) {
				for (Map<String, String> row : wb20Rows) {
					String rowDiv1 = row.get("todoDiv1CodeId");
					int rowSn = Integer.parseInt(String.valueOf(row.get("sanctnSn")));
					if ("TODODIV20".equals(rowDiv1) && rowSn == minSanctnSn) {
						snapshotTemplate = new HashMap<>(row);
						logger.info("[AM→WB sync] P2 방어: 스냅샷 템플릿 확보. min_SN={}, div1={}, salesCd={}, pgPath={}, todoNo={}",
								minSanctnSn, "TODODIV20", row.get("salesCd"), row.get("pgPath"), todoNo);
						break;
					}
				}
			}
		}

		for (Map<String, String> row : wb20Rows) {
			String rowTodoKey = String.valueOf(row.get("todoKey"));
			String rowSanctnSttus = row.get("sanctnSttus");
			String rowDiv1CodeId = row.get("todoDiv1CodeId");

			// 조건: 미승인 AND (APPR(TODODIV20) OR REF(TODODIV10)) AND AM 목록에 없음
			if (!"Y".equals(rowSanctnSttus) && ("TODODIV20".equals(rowDiv1CodeId) || "TODODIV10".equals(rowDiv1CodeId))
					&& !keepKeys.containsKey(rowTodoKey)) {

				Map<String, Object> deleteParam = new HashMap<>();
				deleteParam.put("todoNo", todoNo);
				deleteParam.put("coCd", coCd);
				deleteParam.put("todoKey", rowTodoKey);
				deleteParam.put("sanctnSn", String.valueOf(row.get("sanctnSn")));
				deleteParam.put("todoDiv1CodeId", rowDiv1CodeId);
				deleteParam.put("todoDiv2CodeId", row.get("todoDiv2CodeId"));

				wb20Mapper.deleteRemainingTodoLine(deleteParam);
			}
		}

		// 4) 신규 라인 생성 (brandNew lines, APPR/REF 모두)
		// C) REF 라인의 DIV2 도출 규칙: linked lines가 있으면 그들의 DIV2에서 페어링, 없으면 검증
		String linkedDiv2 = null;
		if (!linked.isEmpty()) {
			Object firstLinkedDiv2 = linked.get(0).get("wb20Div2CodeId");
			if (firstLinkedDiv2 != null) {
				linkedDiv2 = String.valueOf(firstLinkedDiv2);
			}
		}

		// D) approvalDiv2Set 사전 계산: APPR 그룹의 DIV2 목록 (brandNew 루프에서 공유)
		Set<String> approvalDiv2Set = new HashSet<>();
		for (Map<String, String> row : wb20Rows) {
			if ("TODODIV20".equals(row.get("todoDiv1CodeId"))) {
				approvalDiv2Set.add(row.get("todoDiv2CodeId"));
			}
		}

		// E) 그룹별 SANCTN_SN 러닝 카운터: 루프 전에 각 그룹의 MAX SN 초기화
		// step3에서 삭제되는 행(미승인 & keepKeys 미포함)은 제외하고, 생존 행(승인 'Y' 또는 linked 유지행)만 기준으로
		// 초기화해야 신규 순번이 AM 순번과 촘촘히 일치한다(삭제될 순번을 건너뛰지 않도록).
		Map<String, Integer> groupSeqMap = new HashMap<>();
		for (Map<String, String> row : wb20Rows) {
			String rowTodoKey = String.valueOf(row.get("todoKey"));
			boolean survives = "Y".equals(row.get("sanctnSttus")) || keepKeys.containsKey(rowTodoKey);
			if (!survives) {
				continue; // step3에서 삭제될 행은 순번 계산에서 제외
			}
			String groupKey = row.get("todoDiv1CodeId") + "|" + row.get("todoDiv2CodeId");
			int sn = Integer.parseInt(String.valueOf(row.get("sanctnSn")));
			if (!groupSeqMap.containsKey(groupKey) || groupSeqMap.get(groupKey) < sn) {
				groupSeqMap.put(groupKey, sn);
			}
		}

		for (Map<String, Object> line : brandNew) {
			String lineType = (String) line.get("lineType");
			if (lineType == null) {
				lineType = "APPR";
			}

			// div1CodeId: isApprGroup (APPR/AGREE/COOP/POST) → TODODIV20, REF → TODODIV10
			boolean isApprGroup = "APPR".equals(lineType) || "AGREE".equals(lineType) || "COOP".equals(lineType) || "POST".equals(lineType);
			String div1CodeId = isApprGroup ? "TODODIV20" : "TODODIV10";
			String div2CodeId = null;

			// wbLineType: AGREE/COOP/POST면 lineType, 아니면 "APPR"
			String wbLineType;
			if ("AGREE".equals(lineType) || "COOP".equals(lineType) || "POST".equals(lineType)) {
				wbLineType = lineType;
			} else {
				wbLineType = "APPR";
			}

			if ("APPR".equals(lineType)) {
				// APPR: DIV2는 linked 라인에서 도출, 또는 문서의 유일한 APPR 그룹
				if (linkedDiv2 != null) {
					div2CodeId = linkedDiv2;
				} else {
					// brandNew만 있는 경우: DIV2 다중 그룹 검증 (approvalDiv2Set은 루프 전에 사전 계산됨)
					if (approvalDiv2Set.isEmpty()) {
						logger.warn(
								"[AM→WB sync] 신규 결재자 생성 실패: 같은 문서에 APPR(TODODIV20) 그룹이 없음. approverId={}",
								line.get("approverId"));
						continue;
					} else if (approvalDiv2Set.size() > 1) {
						logger.error(
								"[AM→WB sync] 신규 결재자 생성 중단: 같은 문서에 여러 APPR DIV2 그룹 존재하는데 linked line이 없음(DIV2 판정 불가). "
									+ "이는 AM→WB 동기화 페이로드 구조의 어긋남. todoNo={}, groups={}, approverId={}",
								todoNo, approvalDiv2Set, line.get("approverId"));
						return results;
					} else {
						div2CodeId = approvalDiv2Set.iterator().next();
					}
				}
			} else {
				// REF: DIV2는 linked의 APPR DIV2를 페어링으로 변환
				if (linkedDiv2 == null) {
					// REF만 있고 APPR linked가 없는 경우: 기존 공유 라인에서 상속
					for (Map<String, String> row : wb20Rows) {
						if ("TODODIV10".equals(row.get("todoDiv1CodeId"))) {
							div2CodeId = row.get("todoDiv2CodeId");
							break;
						}
					}

					if (div2CodeId == null) {
						// 폴백: 문서의 APPR 그룹 DIV2를 사용해서 페어링으로 도출
						if (approvalDiv2Set.isEmpty()) {
							logger.warn(
									"[AM→WB sync] 신규 공유자 생성 실패: 같은 문서에 기존 REF 라인도 APPR도 없음. approverId={}",
									line.get("approverId"));
							continue;
						} else if (approvalDiv2Set.size() > 1) {
							logger.warn(
									"[AM→WB sync] 신규 공유자 생성 실패: 같은 문서에 여러 APPR DIV2 그룹 존재(REF DIV2 판정 불가). "
										+ "approvalDiv2Set={}, approverId={}",
									approvalDiv2Set, line.get("approverId"));
							continue;
						} else {
							// 문서의 유일한 APPR DIV2를 페어링으로 변환
							String approvalDiv2 = approvalDiv2Set.iterator().next();
							if (approvalDiv2.startsWith("TODODIV2")) {
								div2CodeId = "TODODIV1" + approvalDiv2.substring(8);
								logger.info(
										"[AM→WB sync] REF DIV2 폴백: APPR DIV2 페어링. approvalDiv2={} → refDiv2={}, approverId={}",
										approvalDiv2, div2CodeId, line.get("approverId"));
							} else {
								logger.warn(
										"[AM→WB sync] 신규 공유자 생성 실패: APPR DIV2의 페어링 규칙 미적용(startsWith TODODIV2 아님). "
											+ "approvalDiv2={}, approverId={}",
										approvalDiv2, line.get("approverId"));
								continue;
							}
						}
					}
				} else {
					// linkedDiv2를 TODODIV2xxx → TODODIV1xxx로 페어링
					if (linkedDiv2.startsWith("TODODIV2")) {
						div2CodeId = "TODODIV1" + linkedDiv2.substring(8);
					} else {
						logger.warn(
								"[AM→WB sync] 신규 공유자 생성 실패: linked DIV2의 페어링 규칙 미적용(startsWith TODODIV2 아님). "
									+ "linkedDiv2={}, approverId={}",
								linkedDiv2, line.get("approverId"));
						continue;
					}
				}
			}

			// approverId null 검증
			String approverId = line.get("approverId") != null ? String.valueOf(line.get("approverId")).trim() : null;
			if (approverId == null || approverId.isEmpty()) {
				logger.warn("[AM→WB sync] 신규 행 스킵: approverId null/empty. lineType={}, div1={}, div2={}", lineType, div1CodeId, div2CodeId);
				continue;
			}

			// TODO_KEY 채번
			Map<String, String> keyParam = new HashMap<>();
			Integer nextTodoKey = wb20Mapper.selectNextTodoKey(keyParam);

			// SANCTN_SN: 그룹별 러닝 카운터 ++
			String groupKey = div1CodeId + "|" + div2CodeId;
			Integer currentSeq = groupSeqMap.getOrDefault(groupKey, 0);
			int newSn = currentSeq + 1;
			groupSeqMap.put(groupKey, newSn);

			// P2) 신규 행 INSERT: empty 판정에 따라 라우팅
			if (snapshotTemplate != null) {
				// 스냅샷 템플릿 기반 insertTodoMasterForAmLink 사용
				Map<String, String> forAmParam = new HashMap<>();
				forAmParam.put("todoKey", String.valueOf(nextTodoKey));
				forAmParam.put("sanctnSn", String.valueOf(newSn));
				forAmParam.put("coCd", coCd);
				forAmParam.put("todoDiv1CodeId", div1CodeId);
				forAmParam.put("todoDiv2CodeId", div2CodeId);
				forAmParam.put("todoId", approverId);
				forAmParam.put("todoNo", todoNo);
				forAmParam.put("sanctnSttus", "N");
				forAmParam.put("pgmId", "AM1201M01");
				forAmParam.put("userId", userId);
				forAmParam.put("creatId", creatId);
				forAmParam.put("creatPgm", creatPgm);
				forAmParam.put("createDttm", createDttm);
				forAmParam.put("etcField2", histNo);
				forAmParam.put("lineType", wbLineType);

				// 스냅샷에서 부가컬럼 추출
				if (snapshotTemplate.get("salesCd") != null) {
					forAmParam.put("salesCd", snapshotTemplate.get("salesCd"));
				}
				if (snapshotTemplate.get("pgPath") != null) {
					forAmParam.put("pgPath", snapshotTemplate.get("pgPath"));
				}
				if (snapshotTemplate.get("pgParam") != null) {
					forAmParam.put("pgParam", snapshotTemplate.get("pgParam"));
				}
				if (snapshotTemplate.get("todoCoCd") != null) {
					forAmParam.put("todoCoCd", snapshotTemplate.get("todoCoCd"));
				}
				if (snapshotTemplate.get("todoCodeKind") != null) {
					forAmParam.put("todoCodeKind", snapshotTemplate.get("todoCodeKind"));
				}
				if (snapshotTemplate.get("todoCodeId") != null) {
					forAmParam.put("todoCodeId", snapshotTemplate.get("todoCodeId"));
				}
				if (snapshotTemplate.get("todoFileTrgtKey") != null) {
					forAmParam.put("todoFileTrgtKey", snapshotTemplate.get("todoFileTrgtKey"));
				}
				if (snapshotTemplate.get("todoTitl") != null) {
					forAmParam.put("todoTitl", snapshotTemplate.get("todoTitl"));
				}

				logger.info(
						"[AM→WB sync] 신규 행 insert (스냅샷 기반). todoKey={}, approverId={}, div1={}, div2={}, sanctnSn={}",
						nextTodoKey, approverId, div1CodeId, div2CodeId, newSn);

				wb20Mapper.insertTodoMasterForAmLink(forAmParam);
			} else {
				// 라이브 템플릿 기반 insertTodoMasterCopyTemplate 사용
				Map<String, String> insertParam = new HashMap<>();
				insertParam.put("todoKey", String.valueOf(nextTodoKey));
				insertParam.put("sanctnSn", String.valueOf(newSn));
				insertParam.put("coCd", coCd);
				insertParam.put("todoDiv1CodeId", div1CodeId);
				insertParam.put("todoDiv2CodeId", div2CodeId);
				insertParam.put("todoId", approverId);
				insertParam.put("todoNo", todoNo);
				insertParam.put("pgmId", "AM1201M01");
				insertParam.put("userId", userId);
				insertParam.put("etcField2", histNo);
				insertParam.put("lineType", wbLineType);

				logger.info(
						"[AM→WB sync] 신규 행 insert (라이브 템플릿 복사). todoKey={}, approverId={}, div1={}, div2={}, sanctnSn={}",
						nextTodoKey, approverId, div1CodeId, div2CodeId, newSn);

				wb20Mapper.insertTodoMasterCopyTemplate(insertParam);
			}

			// P4) 신규 행 링크정보 수집 (백필용)
			Map<String, Object> linkInfo = new HashMap<>();
			linkInfo.put("approverId", approverId);
			linkInfo.put("lineType", lineType);
			linkInfo.put("wb20TodoKey", nextTodoKey);
			linkInfo.put("wb20CoCd", coCd);
			linkInfo.put("wb20TodoNo", todoNo);
			linkInfo.put("wb20SanctnSn", newSn);
			linkInfo.put("wb20Div1CodeId", div1CodeId);
			linkInfo.put("wb20Div2CodeId", div2CodeId);
			results.add(linkInfo);

			logger.info("[AM→WB sync] 신규 행 링크정보 수집 (P4 백필용). approverId={}, lineType={}, wb20TodoKey={}, wb20SanctnSn={}",
					approverId, lineType, nextTodoKey, newSn);
		}

		// ===== PHASE A/B/C: WB20 SANCTN_SN을 AM(lineList) 순서와 일치시키는 재번호 =====
		// selectApprovalYn(wb20.xml)이 SUM('Y')=MAX(SANCTN_SN)로 완료판정하므로,
		// 비승인 포함 SANCTN_SN이 gap없이 1..n 연속이어야 한다. PHASE C 검증 필수(잔류 시 롤백).
		final int SANCTN_OFFSET = 1000; // SANCTN_SN 자릿수 미확인이나 결재선 규모상 충분

		// 그룹별 승인행('Y') 최대 SANCTN_SN = base
		Map<String, Integer> groupApprovedMax = new HashMap<>();
		for (Map<String, String> row : wb20Rows) {
			if (!"Y".equals(row.get("sanctnSttus"))) continue;
			String gk = row.get("todoDiv1CodeId") + "|" + row.get("todoDiv2CodeId");
			int sn = Integer.parseInt(String.valueOf(row.get("sanctnSn")));
			groupApprovedMax.merge(gk, sn, Math::max);
		}

		// brandNew 라인의 todoKey/div 조회용(results = step4에서 생성된 신규행 링크정보)
		Map<String, Map<String, Object>> brandNewByKey = new HashMap<>();
		for (Map<String, Object> r : results) {
			brandNewByKey.put(String.valueOf(r.get("approverId")) + "|" + String.valueOf(r.get("lineType")), r);
		}

		// PHASE A: 비승인 APPR/REF 행을 임시 오프셋으로 이동(목표 구간 비우기)
		Map<String, Object> offParam = new HashMap<>();
		offParam.put("todoNo", todoNo);
		offParam.put("coCd", coCd);
		offParam.put("offset", SANCTN_OFFSET);
		if (histNo != null && !histNo.isEmpty()) {
			offParam.put("histNo", histNo);
		}
		wb20Mapper.offsetNonApprovedSanctnSn(offParam);

		// PHASE B: lineList(AM 순서) 순회하며 목표 SANCTN_SN 배정. P4 백필용 results 재구성.
		List<Map<String, Object>> reorderResults = new ArrayList<>();
		Map<String, Integer> groupCounter = new HashMap<>();
		for (Map<String, Object> line : lineList) {
			Object wb20TodoKeyObj = line.get("wb20TodoKey");
			String todoKeyStr = (wb20TodoKeyObj == null) ? null : String.valueOf(wb20TodoKeyObj).trim();
			boolean isLinked = todoKeyStr != null && !todoKeyStr.isEmpty();

			String lineType = (String) line.get("lineType");
			if (lineType == null) lineType = "APPR";
			String approverId = line.get("approverId") != null ? String.valueOf(line.get("approverId")).trim() : null;

			String todoKey;
			String div1;
			String div2;
			if (isLinked) {
				todoKey = todoKeyStr;
				div1 = String.valueOf(line.get("wb20Div1CodeId"));
				div2 = String.valueOf(line.get("wb20Div2CodeId"));
			} else {
				Map<String, Object> r = brandNewByKey.get(approverId + "|" + lineType);
				if (r == null) {
					// step4에서 생성 스킵된 라인(div2 미도출/approverId 없음 등) → 재번호 대상 없음
					continue;
				}
				todoKey = String.valueOf(r.get("wb20TodoKey"));
				div1 = String.valueOf(r.get("wb20Div1CodeId"));
				div2 = String.valueOf(r.get("wb20Div2CodeId"));
			}

			String gk = div1 + "|" + div2;
			int base = groupApprovedMax.getOrDefault(gk, 0);
			int target = groupCounter.getOrDefault(gk, base) + 1;

			Map<String, Object> p = new HashMap<>();
			p.put("todoNo", todoNo);
			p.put("coCd", coCd);
			p.put("todoKey", todoKey);
			p.put("sanctnSn", target);
			p.put("userId", userId);
			p.put("pgmId", "AM1201M01");
			int upd = wb20Mapper.updateTodoLineSanctnSnByKey(p);
			if (upd == 0) {
				// 대상 행이 없으면(희귀) 목표 번호를 소비하지 않아 순번 gap을 방지
				logger.warn("[AM→WB sync] SANCTN_SN 재배정 대상행 없음(스킵). approverId={}, lineType={}, todoKey={}",
						approverId, lineType, todoKey);
				continue;
			}
			groupCounter.put(gk, target);

			// P4 백필용 정보: 목표 SANCTN_SN으로(AM11D01.WB20_SANCTN_SN까지 동기화)
			Map<String, Object> info = new HashMap<>();
			info.put("approverId", approverId);
			info.put("lineType", lineType);
			info.put("wb20TodoKey", todoKey);
			info.put("wb20CoCd", coCd);
			info.put("wb20TodoNo", todoNo);
			info.put("wb20SanctnSn", target);
			info.put("wb20Div1CodeId", div1);
			info.put("wb20Div2CodeId", div2);
			reorderResults.add(info);

			logger.info("[AM→WB sync] SANCTN_SN 재배정. approverId={}, lineType={}, todoKey={}, target={}",
					approverId, lineType, todoKey, target);
		}

		// PHASE C: offset 잔류행(재배정 누락) 검증 — 있으면 순번 gap 손상 → 롤백
		Map<String, Object> chkParam = new HashMap<>();
		chkParam.put("todoNo", todoNo);
		chkParam.put("coCd", coCd);
		chkParam.put("offset", SANCTN_OFFSET);
		if (histNo != null && !histNo.isEmpty()) {
			chkParam.put("histNo", histNo);
		}
		int leftover = wb20Mapper.countOffsetLeftoverSanctnSn(chkParam);
		if (leftover > 0) {
			logger.error("[AM→WB sync] PHASE C 실패: offset 잔류 {}건 → 롤백. todoNo={}", leftover, todoNo);
			throw new IllegalStateException("SANCTN_SN 재번호 불완전(offset 잔류 " + leftover + "건): todoNo=" + todoNo);
		}

		logger.info("[AM→WB sync] 완료. 재배정/생성 {}건(신규행 {}개)", reorderResults.size(), brandNew.size());
		return reorderResults;
	}

}
