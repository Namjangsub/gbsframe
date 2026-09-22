package com.dksys.biz.user.pm.pm51.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm05.service.CM05Svc;
import com.dksys.biz.admin.cm.cm06.mapper.CM06Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.user.pm.pm30.service.PM30Svc;
import com.dksys.biz.user.pm.pm51.mapper.PM51Mapper;
import com.dksys.biz.user.pm.pm51.service.PM51Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM51SvcImpl implements PM51Svc {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PM51Mapper pm51Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	QM01Mapper qm01Mapper;

	@Autowired
	WB20Svc wb20Svc;

	@Autowired
	AM11Svc am11Svc;

	@Autowired
	WB24Mapper wb24Mapper;

	@Autowired
	CM06Mapper cm06Mapper;

	@Autowired
	CM05Svc cm05Svc;

	@Autowired
	PM30Svc pm30Svc;

	@Override
	public int selectTripReqListCount(Map<String, String> paramMap) {
		return pm51Mapper.selectTripReqListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripReqList(Map<String, String> paramMap) {
		return pm51Mapper.selectTripReqList(paramMap);
	}

	@Override
	public int selectTripStatusListCount(Map<String, String> paramMap) {
		return pm51Mapper.selectTripStatusListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripStatusList(Map<String, String> paramMap) {
		return pm51Mapper.selectTripStatusList(paramMap);
	}

	private double parseDoubleSafe(Object obj) {
		if (obj == null) return 0.0;
		String str = String.valueOf(obj).replaceAll("[^0-9.-]", "").trim();
		if (str.isEmpty() || "-".equals(str) || ".".equals(str)) return 0.0;
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return 0.0;
		}
	}

	@Override
	public Map<String, Object> selectTripReqDtl(Map<String, String> paramMap) {
		String tripReqNo = paramMap.get("tripReqNo");
		if (!hasText(tripReqNo)) tripReqNo = paramMap.get("TRIP_REQ_NO");
		if (!hasText(tripReqNo)) tripReqNo = paramMap.get("reqNo");
		if (!hasText(tripReqNo)) tripReqNo = paramMap.get("todoFileTrgtKey");
		if (!hasText(tripReqNo)) tripReqNo = paramMap.get("fileTrgtKey");
		if (hasText(tripReqNo)) {
			paramMap.put("tripReqNo", tripReqNo.trim());
		} else {
			throw new IllegalArgumentException("출장신청서 번호(tripReqNo) 파라미터가 누락되었습니다.");
		}

		Map<String, Object> result = new HashMap<>();
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		if (m01 == null || m01.isEmpty()) {
			throw new IllegalStateException("출장신청서[" + tripReqNo + "] 마스터 정보를 찾을 수 없습니다.");
		}
		List<Map<String, String>> d01 = pm51Mapper.selectTripReqD01(paramMap);
		List<Map<String, String>> d02 = pm51Mapper.selectTripReqD02(paramMap);
		List<Map<String, String>> d03 = pm51Mapper.selectTripReqD03(paramMap);

		boolean hasData = false;
		if (d02 != null) {
			for (Map<String, String> map : d02) {
				double krw = parseDoubleSafe(map.get("krwAmt"));
				double usd = parseDoubleSafe(map.get("usdAmt"));
				if (krw > 0 || usd > 0) {
					hasData = true;
					break;
				}
			}
		}

		if (!hasData) {
			List<Map<String, String>> rptExpense = pm51Mapper.selectTripRptExpenseSummaryByReqNo(paramMap);
			boolean hasRptData = false;
			if (rptExpense != null && !rptExpense.isEmpty()) {
				for (Map<String, String> rpt : rptExpense) {
					double krw = parseDoubleSafe(rpt.get("krwAmt"));
					if (krw > 0) {
						hasRptData = true;
						break;
					}
				}
				if (hasRptData) {
					d02 = rptExpense;
				}
			}
		}

		if (m01 != null && hasText(m01.get("tripReqNo"))) {
			try {
				Map<String, String> docIdParam = new HashMap<>();
				docIdParam.put("tripReqNo", m01.get("tripReqNo"));
				docIdParam.put("coCd", hasText(m01.get("coCd")) ? m01.get("coCd") : "GUN");
				String amDocId = pm51Mapper.selectAmDocIdByTripReqNo(docIdParam);
				if (!hasText(amDocId)) {
					syncTripReqToAm(m01);
				}
			} catch (Exception e) {
				logger.warn("출장신청서 AM 전자결재 자동 동기화 예외 (상세 조회 계속 진행): tripReqNo={}, error={}", m01.get("tripReqNo"), e.getMessage());
			}
		}

		result.put("m01", m01);
		result.put("d01", d01);
		result.put("d02", d02);
		result.put("d03", d03);
		return result;
	}

	@Override
	public int insertTripReqChg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> current = pm51Mapper.selectTripReqM01(paramMap);
		if (current == null) throw new RuntimeException("출장신청서를 찾을 수 없습니다.");
		if (!"APRVSTS03".equals(current.get("aprvStsCd"))) throw new RuntimeException("결재 완료된 신청서만 변경신청할 수 있습니다.");
		paramMap.put("tripReqNo", current.get("tripReqNo"));
		if (!hasText(paramMap.get("chgReason"))) throw new RuntimeException("변경사유를 입력해주세요.");
		normalizeTripReqMasterParam(paramMap);
		validateTripReqMasterParam(paramMap);

		// 마감 검증: tripStDtm, tripEdDtm (14자리 YYYYMMDDHHMMSS)
		String tripStDtm = paramMap.get("tripStDtm");
		String tripEdDtm = paramMap.get("tripEdDtm");
		if ((tripStDtm != null && !tripStDtm.isEmpty()) || (tripEdDtm != null && !tripEdDtm.isEmpty())) {
			pm30Svc.assertNotClosed(tripStDtm, tripEdDtm);
		}

		return saveTripReqChgDetail(paramMap, mRequest);
	}

	private int saveTripReqChgDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		// 다음 REV_NO 계산
		int revNo = pm51Mapper.selectTripReqNextRevNo(paramMap);
		paramMap.put("revNo", String.valueOf(revNo));

		// travelers와 expenses 검증/파싱
		List<Map<String, String>> travelers = gson.fromJson(paramMap.get("travelerArr"), listType);
		validateTravelerDateOverlap(paramMap, travelers);
		List<Map<String, String>> expenses = gson.fromJson(paramMap.get("expenseArr"), listType);

		// 1. 현재본을 이력으로 백업 (M01/D01/D02)
		pm51Mapper.insertTripReqHistM01(paramMap);
		pm51Mapper.insertTripReqHistD01(paramMap);
		pm51Mapper.insertTripReqHistD02(paramMap);

		// 2. M01에 변경 내용 직접 적용
		pm51Mapper.updateTripReqM01(paramMap);

		// 3. D01/D02 삭제 후 새로 insert
		pm51Mapper.deleteTripReqD01(paramMap);
		if (travelers != null) {
			for (Map<String, String> row : travelers) {
				row.put("tripReqNo", paramMap.get("tripReqNo"));
				pm51Mapper.insertTripReqD01(row);
			}
		}

		pm51Mapper.deleteTripReqD02(paramMap);
		if (expenses != null) {
			for (Map<String, String> row : expenses) {
				row.put("tripReqNo", paramMap.get("tripReqNo"));
				pm51Mapper.insertTripReqD02(row);
			}
		}

		// 4. 파일 업로드
		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
		paramMap.put("pgmId", "PM5101P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		return 1;
	}

	@Override
	public int insertTripReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		String tripReqNo = pm51Mapper.selectTripReqListCount(paramMap) >= 0 ? "BT" + getCurrentDateString() + "-" : null;
		normalizeTripReqMasterParam(paramMap);
		validateTripReqMasterParam(paramMap);

		// 마감 검증: tripStDtm, tripEdDtm (14자리 YYYYMMDDHHMMSS)
		String tripStDtm = paramMap.get("tripStDtm");
		String tripEdDtm = paramMap.get("tripEdDtm");
		if ((tripStDtm != null && !tripStDtm.isEmpty()) || (tripEdDtm != null && !tripEdDtm.isEmpty())) {
			pm30Svc.assertNotClosed(tripStDtm, tripEdDtm);
		}

		int result = pm51Mapper.insertTripReqM01(paramMap);

		List<Map<String, String>> travelerArr = gsonDtl.fromJson(paramMap.get("travelerArr"), dtlMap);
		validateTravelerDateOverlap(paramMap, travelerArr);
		if (travelerArr != null && !travelerArr.isEmpty()) {
			for (Map<String, String> travelerMap : travelerArr) {
				travelerMap.put("tripReqNo", paramMap.get("tripReqNo"));
				travelerMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripReqD01(travelerMap);
			}
		}

		List<Map<String, String>> expenseArr = gsonDtl.fromJson(paramMap.get("expenseArr"), dtlMap);
		if (expenseArr != null && !expenseArr.isEmpty()) {
			for (Map<String, String> expenseMap : expenseArr) {
				expenseMap.put("tripReqNo", paramMap.get("tripReqNo"));
				expenseMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripReqD02(expenseMap);
			}
		}

		List<Map<String, String>> projectArr = gsonDtl.fromJson(paramMap.get("projectArr"), dtlMap);
		if (projectArr != null && !projectArr.isEmpty()) {
			for (Map<String, String> projectMap : projectArr) {
				projectMap.put("tripReqNo", paramMap.get("tripReqNo"));
				projectMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripReqD03(projectMap);
			}
		}

		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
		paramMap.put("pgmId", "PM5101P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		if (paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripReqApplicantApprovals(paramMap, approvalArr);
			approvalArr = reorderGeneralApprovalArr(paramMap, approvalArr);
			if (approvalArr != null && approvalArr.size() > 0) {
				paramMap.put("reqNo", paramMap.get("tripReqNo"));
				paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));

				String pgParam1 = "{\"actionType\":\"" + "T" + "\",";
				pgParam1 += "\"gubun\":\"" + "개인" + "\",";
				pgParam1 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam1 += "\"tripReqNo\":\"" + paramMap.get("tripReqNo") + "\",";
				pgParam1 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				String pgParam2 = "{\"actionType\":\"" + "S" + "\",";
				pgParam2 += "\"gubun\":\"" + "개인" + "\",";
				pgParam2 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam2 += "\"tripReqNo\":\"" + paramMap.get("tripReqNo") + "\",";
				pgParam2 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				int iSharng = 1;
				int iApproval = 1;
				// 자체승인(1번 결재자=작성자) 처리는 결재선 전체가 등록된 뒤로 미룬다.
				// 루프 도중에 처리하면 결재선이 1건뿐인 상태에서 결재완료 여부(MIN(SANCTN_STTUS))가 판정되어
				// 출장신청서 결재상태가 완료(APRVSTS03)로 잘못 기록된다.
				List<Map<String, String>> selfApprovalList = new ArrayList<>();
				// 타인(대리 등록자)이 신청서를 등록하는 경우에도 CREAT_ID는 등록자가 아니라 출장 신청인(reqId)을 가리켜야 한다.
				String originalReqId = resolveOriginalRequesterId(paramMap);
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", approvalSalesCd(paramMap));
					fillApprovalBaseParam(approvalMap, paramMap);
					String actingUserId = approvalMap.get("userId");
					if (hasText(originalReqId)) {
						approvalMap.put("userId", originalReqId); // CREAT_ID를 출장 신청인으로 고정
					}

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						insertWbsSharngListSync(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						insertWbsApprovalListSync(approvalMap);
						iApproval++;
						approvalMap.put("userId", actingUserId); // 자동승인 판정/UDT_ID는 실제 행위자 기준으로 복원
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm")) && "1".equals(approvalMap.get("sanCtnSn"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));

							Object value = approvalMap.get("toDoKey");
							if (value != null) {
								approvalMap.put("todoKey", value.toString());
							}

							selfApprovalList.add(approvalMap);
						}
					}
				}
				for (Map<String, String> selfApprovalMap : selfApprovalList) {
					wb20Svc.insertApprovalLine(selfApprovalMap);
				}
			}
		}
		processTripReqApprovalArr(paramMap, gsonDtl, dtlMap, "mngApprovalArr", "관리부서");
		syncTripReqToAm(paramMap);

		return result;
	}

	@Override
	public int updateTripReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		paramMap.put("reqNo", paramMap.get("tripReqNo"));
		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));

		Map<String, String> orgMap = pm51Mapper.selectTripReqM01(paramMap);
		if (orgMap == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		if (hasText(orgMap.get("payDt"))) {
			throw new RuntimeException("지급완료된 출장신청서는 수정할 수 없습니다.");
		}

		String aprvStsCd = orgMap.get("aprvStsCd");
		boolean salesDept = isSalesDept(paramMap.get("deptId"));
		boolean accountingDept = isAccountingDept(paramMap.get("deptId"));
		boolean currentMngApprover = isCurrentManagementApproverPending(paramMap);
		boolean payMode = "Y".equals(paramMap.get("payMode"));

		String reqUserId = orgMap.get("userId");
		String creatId = orgMap.get("creatId");
		String loginUserId = paramMap.get("userId");
		boolean isAuthor = (hasText(reqUserId) && hasText(loginUserId) && reqUserId.trim().equalsIgnoreCase(loginUserId.trim()))
		                || (hasText(creatId) && hasText(loginUserId) && creatId.trim().equalsIgnoreCase(loginUserId.trim()));

		boolean hasCompletedApproval = false;
		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			hasCompletedApproval = cnt != null && !"0".equals(cnt);
		}

		// 본인 외 타인 결재가 진행되었거나 최종 결재완료(APRVSTS03)된 경우: 영업팀 또는 회계팀만 수정 가능
		// 등록자 본인(isAuthor)이면서 본인 결재만 진행된 상태(!hasCompletedApproval && !"APRVSTS03".equals(aprvStsCd))에서는 본문 수정 허용
		// 자금담당자(SPECRTS15)는 회계팀 부서가 아니어도 지급완료 전 출장기간 등 수정이 가능해야 하므로 예외 허용(클라이언트 isTravelerPeriodEditBlocked와 동일 기준)
		boolean acctMngApprover = isAcctMngApprover(loginUserId);
		if (!salesDept && !accountingDept && !currentMngApprover && !acctMngApprover) {
			if (!isAuthor || hasCompletedApproval || "APRVSTS03".equals(aprvStsCd)) {
				throw new RuntimeException("결재 진행 이후에는 영업팀 또는 회계팀만 수정할 수 있습니다.");
			}
		}
		if (salesDept && "APRVSTS03".equals(aprvStsCd)) {
			throw new RuntimeException("결재완료 이후에는 회계팀만 수정할 수 있습니다.");
		}
//		if (accountingDept && !payMode && !"APRVSTS03".equals(aprvStsCd)) {
//			throw new RuntimeException("회계팀 경비 수정은 결재완료 이후 가능합니다.");
//		}
		if (!payMode) {
			// 마감 검증: 날짜가 바뀌는 일반 수정일 때만 (payMode=Y인 경비/지급처리 제외)
			String tripStDtm = paramMap.get("tripStDtm");
			String tripEdDtm = paramMap.get("tripEdDtm");
			if ((tripStDtm != null && !tripStDtm.isEmpty()) || (tripEdDtm != null && !tripEdDtm.isEmpty())) {
				pm30Svc.assertNotClosed(tripStDtm, tripEdDtm);
			}

			normalizeTripReqMasterParam(paramMap);
			validateTripReqMasterParam(paramMap);
		}

		boolean hasCompletedMngApproval = hasCompletedApproval(paramMap, true);

		int result = payMode ? pm51Mapper.updateTripReqPayAmounts(paramMap) : pm51Mapper.updateTripReqM01(paramMap);

		// 반려된 출장신청서 내용 수정 저장 시 결재선 초기화 및 반려 Flag clear (처음부터 재상신 가능하도록)
		if (!payMode) {
			Map<String, String> resetParam = new HashMap<>();
			resetParam.put("coCd", paramMap.get("coCd"));
			resetParam.put("todoNo", paramMap.get("tripReqNo"));
			resetParam.put("userId", paramMap.get("userId"));
			resetParam.put("pgmId", "PM5101P01");
			wb20Svc.resetRejectedApprovalLines(resetParam);
		}

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripReqNo", paramMap.get("tripReqNo"));
		delParam.put("reqNo", paramMap.get("tripReqNo"));
		delParam.put("salesCd", paramMap.get("salesCd"));

		if (!hasCompletedApproval && !payMode) {
			pm51Mapper.deleteTripReqApprovalLines(delParam);
		} else if (accountingDept && !hasCompletedMngApproval) {
			pm51Mapper.deleteTripReqMngApprovalLines(delParam);
		}

		pm51Mapper.deleteTripReqD01(delParam);
		pm51Mapper.deleteTripReqD02(delParam);
		pm51Mapper.deleteTripReqD03(delParam);

		if (paramMap.get("travelerArr") != null && !paramMap.get("travelerArr").isEmpty()) {
			List<Map<String, String>> travelerArr = gsonDtl.fromJson(paramMap.get("travelerArr"), dtlMap);
			validateTravelerDateOverlap(paramMap, travelerArr);
			if (travelerArr != null && !travelerArr.isEmpty()) {
				for (Map<String, String> travelerMap : travelerArr) {
					travelerMap.put("tripReqNo", paramMap.get("tripReqNo"));
					travelerMap.put("coCd", paramMap.get("coCd"));
					pm51Mapper.insertTripReqD01(travelerMap);
				}
			}
		}

		List<Map<String, String>> expenseArr = gsonDtl.fromJson(paramMap.get("expenseArr"), dtlMap);
		if (expenseArr != null && !expenseArr.isEmpty()) {
			for (Map<String, String> expenseMap : expenseArr) {
				expenseMap.put("tripReqNo", paramMap.get("tripReqNo"));
				expenseMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripReqD02(expenseMap);
			}
		}

		List<Map<String, String>> projectArr = gsonDtl.fromJson(paramMap.get("projectArr"), dtlMap);
		if (projectArr != null && !projectArr.isEmpty()) {
			for (Map<String, String> projectMap : projectArr) {
				projectMap.put("tripReqNo", paramMap.get("tripReqNo"));
				projectMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripReqD03(projectMap);
			}
		}

		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
		paramMap.put("pgmId", "PM5101P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		if (!hasCompletedApproval && !payMode && paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripReqApplicantApprovals(paramMap, approvalArr);
			approvalArr = reorderGeneralApprovalArr(paramMap, approvalArr);
			if (approvalArr != null && approvalArr.size() > 0) {
				String pgParam1 = "{\"actionType\":\"" + "T" + "\",";
				pgParam1 += "\"gubun\":\"" + "팀" + "\",";
				pgParam1 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam1 += "\"tripReqNo\":\"" + paramMap.get("tripReqNo") + "\",";
				pgParam1 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				String pgParam2 = "{\"actionType\":\"" + "S" + "\",";
				pgParam2 += "\"gubun\":\"" + "팀" + "\",";
				pgParam2 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam2 += "\"tripReqNo\":\"" + paramMap.get("tripReqNo") + "\",";
				pgParam2 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				int iSharng = 1;
				int iApproval = 1;
				// 자체승인(1번 결재자=작성자) 처리는 결재선 전체가 등록된 뒤로 미룬다.
				// 루프 도중에 처리하면 결재선이 1건뿐인 상태에서 결재완료 여부(MIN(SANCTN_STTUS))가 판정되어
				// 출장신청서 결재상태가 완료(APRVSTS03)로 잘못 기록된다.
				List<Map<String, String>> selfApprovalList = new ArrayList<>();
				// 타인(대리 등록자)이 신청서를 등록하는 경우에도 CREAT_ID는 등록자가 아니라 출장 신청인(reqId)을 가리켜야 한다.
				String originalReqId = resolveOriginalRequesterId(paramMap);
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", approvalSalesCd(paramMap));
					fillApprovalBaseParam(approvalMap, paramMap);
					String actingUserId = approvalMap.get("userId");
					if (hasText(originalReqId)) {
						approvalMap.put("userId", originalReqId); // CREAT_ID를 출장 신청인으로 고정
					}

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						insertWbsSharngListSync(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						insertWbsApprovalListSync(approvalMap);
						iApproval++;
						approvalMap.put("userId", actingUserId); // 자동승인 판정/UDT_ID는 실제 행위자 기준으로 복원
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm")) && "1".equals(approvalMap.get("sanCtnSn"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));

							Object value = approvalMap.get("toDoKey");
							if (value != null) {
								approvalMap.put("todoKey", value.toString());
							}

							selfApprovalList.add(approvalMap);
						}
					}
				}
				for (Map<String, String> selfApprovalMap : selfApprovalList) {
					wb20Svc.insertApprovalLine(selfApprovalMap);
				}
			}
		}
		if (!payMode && (!hasCompletedApproval || (accountingDept && !hasCompletedMngApproval))) {
			processTripReqApprovalArr(paramMap, gsonDtl, dtlMap, "mngApprovalArr", "관리부서");
		}
		syncTripReqToAm(paramMap);

		return result;
	}

	private boolean isCurrentManagementApproverPending(Map<String, String> paramMap) {
		String userId = paramMap.get("userId");
		if (!hasText(userId)) return false;
		boolean authorized = false;
		for (String approverId : selectAcctMngApproverIds()) {
			if (userId.equalsIgnoreCase(approverId.trim())) {
				authorized = true;
				break;
			}
		}
		if (!authorized) return false;
		Map<String, String> query = new HashMap<>();
		query.put("todoNo", paramMap.get("tripReqNo"));
		query.put("todoDiv2CodeId", "TODODIV2191");
		List<Map<String, String>> lines = wb20Svc.selectGetApprovalList(query);
		if (lines == null) return false;
		for (Map<String, String> line : lines) {
			if (userId.equalsIgnoreCase(String.valueOf(line.get("todoId")))
					&& "N".equals(String.valueOf(line.get("sanctnSttus")))) return true;
		}
		return false;
	}

	private void processTripReqApprovalArr(Map<String, String> paramMap, Gson gsonDtl, Type dtlMap, String arrKey, String gubun) {
		if (!paramMap.containsKey(arrKey)) {
			return;
		}

		List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get(arrKey), dtlMap);
		if (approvalArr == null || approvalArr.size() == 0) {
			return;
		}
		// 관리부서 결재는 고정 결재순번(자금담당자 -> 회계팀장 -> 부사장)을 서버에서도 항상 보장한다.
		if ("mngApprovalArr".equals(arrKey)) {
			approvalArr = reorderPayMngApprovalArr(approvalArr);
		}

		String documentNoKey = hasText(paramMap.get("tripRptNo")) ? "tripRptNo" : "tripReqNo";
		String documentNo = paramMap.get(documentNoKey);
		String pgParam1 = "{\"actionType\":\"" + "T" + "\",";
		pgParam1 += "\"gubun\":\"" + gubun + "\",";
		pgParam1 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
		pgParam1 += "\"" + documentNoKey + "\":\"" + documentNo + "\",";
		pgParam1 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

		String pgParam2 = "{\"actionType\":\"" + "S" + "\",";
		pgParam2 += "\"gubun\":\"" + gubun + "\",";
		pgParam2 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
		pgParam2 += "\"" + documentNoKey + "\":\"" + documentNo + "\",";
		pgParam2 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

		// 이 결재선(mngApprovalArr, 관리부서)은 최초 등록 시점뿐 아니라 지급완료처리 등 이후 시점에도
		// 행위자(paramMap.userId)에 의해 재생성될 수 있다. CREAT_ID는 항상 최초 신청자를 가리켜야 하므로
		// (자동승인 판정/UDT_ID 감사에 쓰이는 실제 행위자와는 별도로) 최초 신청자 ID를 미리 구해둔다.
		String originalReqId = resolveOriginalRequesterId(paramMap);

		int iSharng = 1;
		int iApproval = 1;
		for (Map<String, String> approvalMap : approvalArr) {
			approvalMap.put("reqNo", paramMap.get("reqNo"));
			approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			approvalMap.put("salesCd", approvalSalesCd(paramMap));
			fillApprovalBaseParam(approvalMap, paramMap);
			String actingUserId = approvalMap.get("userId");
			if (hasText(originalReqId)) {
				approvalMap.put("userId", originalReqId); // CREAT_ID를 최초 신청자로 고정
			}

			if (isShareApproval(approvalMap)) {
				approvalMap.put("sanCtnSn", Integer.toString(iSharng));
				approvalMap.put("pgParam", pgParam1);
				insertWbsSharngListSync(approvalMap);
				iSharng++;
			} else {
				approvalMap.put("sanCtnSn", Integer.toString(iApproval));
				approvalMap.put("pgParam", pgParam2);
				insertWbsApprovalListSync(approvalMap);
				iApproval++;
				approvalMap.put("userId", actingUserId); // 이후 자동승인 판정/UDT_ID는 실제 행위자 기준으로 복원
				if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))
						&& ("1".equals(approvalMap.get("sanCtnSn")) || "2".equals(approvalMap.get("sanCtnSn")))) {
					approvalMap.put("todoCfOpn", "자동승인");
					approvalMap.put("todoNo", approvalMap.get("reqNo"));
					// wb20Mapper.updateApprovalLine의 WHERE 절이 SANCTN_SN을 필수로 매칭한다.
					// 이 값이 없으면(null) 0건 매칭되어 자동승인이 DB에 반영되지 않고 "N"(미승인) 상태로 남는다.
					approvalMap.put("sanctnSn", approvalMap.get("sanCtnSn"));

					Object value = approvalMap.get("toDoKey");
					if (value != null) {
						approvalMap.put("todoKey", value.toString());
					}

					wb20Svc.insertApprovalLine(approvalMap);
				}
			}
		}
	}

	private List<Map<String, String>> reorderPayMngApprovalArr(List<Map<String, String>> approvalArr) {
		if (approvalArr == null || approvalArr.isEmpty()) {
			return new ArrayList<>();
		}
		List<Map<String, String>> approvalRows = new ArrayList<>();
		List<Map<String, String>> shareRows = new ArrayList<>();
		for (Map<String, String> row : approvalArr) {
			if (isShareApproval(row)) {
				shareRows.add(row);
			} else {
				approvalRows.add(row);
			}
		}

		List<Map<String, String>> ordered = new ArrayList<>();
		List<Map<String, String>> remaining = new ArrayList<>(approvalRows);
		for (String fixedId : selectPayMngApproverIds()) {
			String normalizedId = fixedId == null ? "" : fixedId.trim();
			if (!hasText(normalizedId)) continue;
			Map<String, String> row = extractApprovalRowById(remaining, normalizedId);
			if (row != null) {
				ordered.add(row);
			}
		}
		ordered.addAll(remaining); // 나머지 결재선
		shareRows = filterDuplicateShareRows(ordered, shareRows);
		ordered.addAll(shareRows); // 공유선
		return ordered;
	}

	private List<Map<String, String>> reorderGeneralApprovalArr(Map<String, String> paramMap,
			List<Map<String, String>> approvalArr) {
		if (approvalArr == null || approvalArr.isEmpty()) {
			return new ArrayList<>();
		}
		List<Map<String, String>> approvalRows = new ArrayList<>();
		List<Map<String, String>> shareRows = new ArrayList<>();
		for (Map<String, String> row : approvalArr) {
			if (isShareApproval(row)) {
				shareRows.add(row);
			} else {
				approvalRows.add(row);
			}
		}

		String reqId = paramMap.get("reqId");
		if (!hasText(reqId)) reqId = paramMap.get("userId");
		if (!hasText(reqId)) reqId = paramMap.get("creatId");
		reqId = reqId == null ? "" : reqId.trim();

		String pmId = paramMap.get("pmId");
		if (!hasText(pmId) && hasText(paramMap.get("tripReqNo"))) {
			try {
				Map<String, String> qParam = new HashMap<>();
				qParam.put("tripReqNo", paramMap.get("tripReqNo"));
				Map<String, String> m01 = pm51Mapper.selectTripReqM01(qParam);
				if (m01 != null && hasText(m01.get("pmId"))) {
					pmId = m01.get("pmId");
				}
			} catch (Exception ignored) {
			}
		}
		pmId = pmId == null ? "" : pmId.trim();

		String teamMgrId = "";
		if (hasText(reqId)) {
			try {
				Map<String, String> uParam = new HashMap<>();
				uParam.put("userId", reqId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(uParam);
				if (uInfo != null && !"TEAM01".equals(uInfo.get("teamManager")) && hasText(uInfo.get("mngId"))) {
					teamMgrId = uInfo.get("mngId").trim();
				}
			} catch (Exception ignored) {
			}
		}

		List<Map<String, String>> remaining = new ArrayList<>(approvalRows);
		List<Map<String, String>> ordered = new ArrayList<>();

		// 1순위: 기안자(신청인) -> 무조건 1번
		Map<String, String> applicantRow = extractApprovalRowById(remaining, reqId);
		// 2순위: 팀장 -> 2번
		Map<String, String> teamMgrRow = hasText(teamMgrId) ? extractApprovalRowById(remaining, teamMgrId) : null;
		// 3순위: 영업PM -> 3번
		Map<String, String> pmRow = hasText(pmId) ? extractApprovalRowById(remaining, pmId) : null;

		if (applicantRow != null) ordered.add(applicantRow);
		if (teamMgrRow != null) ordered.add(teamMgrRow);
		if (pmRow != null) ordered.add(pmRow);
		ordered.addAll(remaining); // 나머지 수동 결재선
		shareRows = filterDuplicateShareRows(ordered, shareRows);
		ordered.addAll(shareRows); // 공유선

		return ordered;
	}

	private List<Map<String, String>> reorderRptGeneralApprovalArr(Map<String, String> paramMap,
			List<Map<String, String>> approvalArr) {
		if (approvalArr == null || approvalArr.isEmpty()) {
			return new ArrayList<>();
		}
		List<Map<String, String>> approvalRows = new ArrayList<>();
		List<Map<String, String>> shareRows = new ArrayList<>();
		for (Map<String, String> row : approvalArr) {
			if (isShareApproval(row)) {
				shareRows.add(row);
			} else {
				approvalRows.add(row);
			}
		}

		String callerId = paramMap.get("userId");
		if (!hasText(callerId)) callerId = paramMap.get("reqId");
		callerId = callerId == null ? "" : callerId.trim();

		List<Map<String, String>> remaining = new ArrayList<>(approvalRows);
		List<Map<String, String>> ordered = new ArrayList<>();

		// 1순위: 기안자 본인 -> 무조건 1번
		Map<String, String> authorRow = extractApprovalRowById(remaining, callerId);
		if (authorRow != null) {
			ordered.add(authorRow);
		}
		ordered.addAll(remaining);
		shareRows = filterDuplicateShareRows(ordered, shareRows);
		ordered.addAll(shareRows);
		return ordered;
	}

	private List<Map<String, String>> filterDuplicateShareRows(List<Map<String, String>> approvalRows,
			List<Map<String, String>> shareRows) {
		if (shareRows == null || shareRows.isEmpty()) {
			return shareRows;
		}
		Set<String> appIds = new HashSet<>();
		if (approvalRows != null) {
			for (Map<String, String> app : approvalRows) {
				String uid = getApproverId(app);
				if (uid != null && !uid.isEmpty()) {
					appIds.add(uid);
				}
			}
		}
		List<Map<String, String>> filtered = new ArrayList<>();
		for (Map<String, String> share : shareRows) {
			String uid = getApproverId(share);
			if (uid == null || !appIds.contains(uid)) {
				filtered.add(share);
			}
		}
		return filtered;
	}

	private String getApproverId(Map<String, String> row) {
		if (row == null) return null;
		String uid = row.get("usrNm");
		if (!hasText(uid)) uid = row.get("todoId");
		if (!hasText(uid)) uid = row.get("empNo");
		if (!hasText(uid)) uid = row.get("userId");
		return uid == null ? null : uid.trim();
	}

	private Map<String, String> extractApprovalRowById(List<Map<String, String>> list, String targetId) {
		if (!hasText(targetId) || list == null) return null;
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> row = list.get(i);
			String rowId = hasText(row.get("usrNm")) ? row.get("usrNm") : row.get("todoId");
			if (targetId.equalsIgnoreCase(rowId == null ? "" : rowId.trim())) {
				return list.remove(i);
			}
		}
		return null;
	}

	@Override
	public int deleteTripReq(Map<String, String> paramMap) throws Exception {
		paramMap.put("reqNo", paramMap.get("tripReqNo"));

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재처리가 이미 진행중이거나 완료된 출장신청서는 삭제할 수 없습니다.");
			}
		}

		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		String salesCd = paramMap.get("tripReqNo");
		if (m01 != null) {
			// 마감 검증: 기존 DB의 tripStDtm, tripEdDtm
			String tripStDtm = m01.get("tripStDtm");
			String tripEdDtm = m01.get("tripEdDtm");
			if ((tripStDtm != null && !tripStDtm.isEmpty()) || (tripEdDtm != null && !tripEdDtm.isEmpty())) {
				pm30Svc.assertNotClosed(tripStDtm, tripEdDtm);
			}

			// 복명서가 작성된 경우 삭제 불가
			if ("Y".equals(m01.get("tripRptYn")) || hasText(m01.get("tripRptNo"))) {
				throw new RuntimeException("이미 복명서가 작성된 출장신청서는 삭제할 수 없습니다.");
			}

			// 결재 완료(APRVSTS03)된 경우 삭제 불가 (신청자 본인 결재만 된 진행중 APRVSTS02 상태는 위 selectApprovalChk 검증 통과 시 삭제 허용)
			String aprvStsCd = m01.get("aprvStsCd");
			if ("APRVSTS03".equals(aprvStsCd)) {
				throw new RuntimeException("결재처리가 이미 완료된 출장신청서는 삭제할 수 없습니다.");
			}

			String currentUserId = paramMap.get("userId");
			String creatId = m01.get("creatId");
			String reqId = m01.get("reqId");
			String userId = m01.get("userId");
			if (hasText(currentUserId)) {
				if (!currentUserId.equals(creatId) && !currentUserId.equals(reqId) && !currentUserId.equals(userId)) {
					throw new RuntimeException("본인이 작성하거나 신청한 출장신청서만 삭제할 수 있습니다.");
				}
			}

			String m01SalesCd = m01.get("salesCd");
			if (hasText(m01SalesCd)) {
				salesCd = m01SalesCd;
			}
		}

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripReqNo", paramMap.get("tripReqNo"));
		delParam.put("reqNo", paramMap.get("tripReqNo"));
		delParam.put("salesCd", salesCd);

		// 1. 공유선 및 결재선 CASCADE 삭제
		List<Map<String, String>> sharngChk = qm01Mapper.deleteWbsSharngListChk(delParam);
		if (sharngChk.size() > 0) {
			qm01Mapper.deleteWbsSharngList(delParam);
		}
		pm51Mapper.deleteTripReqApprovalLines(delParam);
		pm51Mapper.deleteTripReqMngApprovalLines(delParam);
		pm51Mapper.deleteAmApprovalLinesByBizKey(delParam);
		pm51Mapper.deleteAmApprovalDocByBizKey(delParam);

		// 2. 출장 디테일 및 변경이력 CASCADE 삭제
		pm51Mapper.deleteTripReqD01(delParam);
		pm51Mapper.deleteTripReqD02(delParam);
		pm51Mapper.deleteTripReqD03(delParam);
		pm51Mapper.deleteTripReqH(delParam);
		pm51Mapper.deleteTripReqH02(delParam);
		pm51Mapper.deleteTripReqH03(delParam);

		// 3. 첨부파일 CASCADE 삭제 (FILE_TRGT_TYP = 'PM5101P01')
		try {
			Map<String, String> fileSearchMap = new HashMap<>();
			fileSearchMap.put("fileTrgtTyp", "PM5101P01");
			fileSearchMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
			List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(fileSearchMap);
			if (deleteFileList != null && !deleteFileList.isEmpty()) {
				for (Map<String, String> delFile : deleteFileList) {
					String fKey = delFile.get("fileKey");
					if (fKey == null || fKey.isEmpty()) fKey = delFile.get("file_key");
					if (fKey == null || fKey.isEmpty()) fKey = delFile.get("FILE_KEY");
					if (fKey != null && !fKey.isEmpty()) {
						cm08Svc.deleteFile(fKey);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 4. 출장신청서 마스터 삭제
		int result = pm51Mapper.deleteTripReqM01(delParam);
		return result;
	}

	private void normalizeTripReqMasterParam(Map<String, String> paramMap) {
		if (!hasText(paramMap.get("salesCd")) && hasText(paramMap.get("saledCd"))) {
			paramMap.put("salesCd", paramMap.get("saledCd"));
		}
		if (!hasText(paramMap.get("reqId")) && hasText(paramMap.get("userId"))) {
			paramMap.put("reqId", paramMap.get("userId"));
		}
	}

	private void validateTripReqMasterParam(Map<String, String> paramMap) {
		if (!hasText(paramMap.get("tripDiv"))) {
			throw new RuntimeException("출장구분을 선택해주세요.");
		}
		if (!hasText(paramMap.get("reqId"))) {
			throw new RuntimeException("신청인을 선택해주세요.");
		}
		if (!hasText(paramMap.get("salesCd"))) {
			throw new RuntimeException("Sales Code를 선택해주세요.");
		}
		if (!hasText(paramMap.get("clntPjt"))) {
			throw new RuntimeException("프로젝트를 선택해주세요.");
		}
	}

	private void validateTravelerDateOverlap(Map<String, String> paramMap, List<Map<String, String>> travelerArr) {
		if ("Y".equals(paramMap.get("forceOverlap"))) {
			return;
		}
		if (travelerArr == null) return;
		for (Map<String, String> travelerMap : travelerArr) {
			String userId = travelerMap.get("userId");
			String stDtm = travelerMap.get("tripStDtm");
			String edDtm = travelerMap.get("tripEdDtm");
			if (!hasText(userId) || !hasText(stDtm) || !hasText(edDtm) || stDtm.length() < 8 || edDtm.length() < 8) {
				continue;
			}
			Map<String, String> checkParam = new HashMap<>();
			checkParam.put("userId", userId);
			checkParam.put("tripStDate", stDtm.substring(0, 8));
			checkParam.put("tripEdDate", edDtm.substring(0, 8));
			checkParam.put("tripReqNo", paramMap.get("tripReqNo"));
			List<Map<String, String>> overlapList = pm51Mapper.selectTripDateOverlapList(checkParam);
			if (overlapList != null && !overlapList.isEmpty()) {
				Map<String, String> overlap = overlapList.get(0);
				String travelerNm = hasText(travelerMap.get("userNm")) ? travelerMap.get("userNm") : userId;
				throw new RuntimeException(travelerNm + "님의 출장기간이 기등록된 " + overlap.get("docType") + "(" + overlap.get("docNo") + ")와 중복됩니다.");
			}
		}
	}

	private void normalizeTripRptExpenseDate(Map<String, String> expenseDtlMap) {
		String useDt = expenseDtlMap.get("useDt");
		if (useDt != null) {
			expenseDtlMap.put("useDt", useDt.replace("-", ""));
		}
	}

	@Override
	public int updateTripReqPayDone(Map<String, String> paramMap) throws Exception {
		if (!isAccountingDept(paramMap.get("deptId"))) {
			throw new RuntimeException("회계팀만 지급완료 처리할 수 있습니다.");
		}
		Map<String, String> orgMap = pm51Mapper.selectTripReqM01(paramMap);
		if (orgMap == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		if (hasText(orgMap.get("payDt"))) {
			throw new RuntimeException("이미 지급완료 처리된 출장신청서입니다.");
		}
		validateTripReqGeneralApprovalDone(paramMap.get("tripReqNo"));
		approvePendingManagementLineForPayment(paramMap);
		return pm51Mapper.updateTripReqPayDone(paramMap);
	}

	private void approvePendingManagementLineForPayment(Map<String, String> paramMap) {
		if (!isCurrentManagementApproverPending(paramMap)) return;
		Map<String, String> docParam = new HashMap<>();
		docParam.put("tripReqNo", paramMap.get("tripReqNo"));
		Map<String, String> amDoc = pm51Mapper.selectAmDocInfoByTripReqNo(docParam);
		if (amDoc == null || !hasText(amDoc.get("docId"))) throw new RuntimeException("전자결재 문서를 찾을 수 없어 지급완료 처리할 수 없습니다.");
		Map<String, Object> approvalParam = new HashMap<>();
		approvalParam.put("docId", amDoc.get("docId"));
		approvalParam.put("coCd", amDoc.get("coCd"));
		approvalParam.put("userId", paramMap.get("userId"));
		approvalParam.put("pgmId", hasText(paramMap.get("pgmId")) ? paramMap.get("pgmId") : "PM5101P01");
		approvalParam.put("apprOpinion", paramMap.get("apprOpinion"));
		Map<String, Object> result = am11Svc.approveDocument(approvalParam);
		if (result == null || !"200".equals(String.valueOf(result.get("resultCode")))) {
			throw new RuntimeException(result == null ? "관리 결재 자동 승인에 실패했습니다." : String.valueOf(result.get("resultMessage")));
		}
	}

	// 지급완료 처리 전 신청부서(일반) 결재선(TODODIV2190)이 모두 승인되었는지 검증한다.
	// 안내문구는 화면(commApproval.js / PM5101P01.html) 및 WB20SvcImpl의 순차결재 안내와 동일한 형식으로 맞춘다.
	private void validateTripReqGeneralApprovalDone(String tripReqNo) {
		if (!hasText(tripReqNo)) {
			return;
		}
		Map<String, String> lineParam = new HashMap<>();
		lineParam.put("todoNo", tripReqNo);
		lineParam.put("todoDiv2CodeId", "TODODIV2190");
		List<Map<String, String>> reqLines = wb20Svc.selectGetApprovalList(lineParam);
		if (reqLines == null || reqLines.isEmpty()) {
			throw new RuntimeException("일반결재선이 등록되지 않아 지급완료 처리할 수 없습니다.");
		}

		Map<String, String> pendingLine = null;
		int pendingSn = 0;
		for (Map<String, String> line : reqLines) {
			if ("Y".equals(line.get("sanctnSttus"))) {
				continue;
			}
			int sn;
			try {
				sn = Integer.parseInt(String.valueOf(line.get("sanctnSn")));
			} catch (Exception e) {
				continue;
			}
			if (pendingLine == null || sn < pendingSn) {
				pendingLine = line;
				pendingSn = sn;
			}
		}
		if (pendingLine == null) {
			return;
		}

		String suffix = "\n일반결재가 완료되어야 지급완료 처리할 수 있습니다.";
		String name = pendingLine.get("todoNm") == null ? "" : String.valueOf(pendingLine.get("todoNm")).trim();
		String jik = pendingLine.get("jik") == null ? "" : String.valueOf(pendingLine.get("jik")).replaceAll("\\s", "");
		String nameWithJik = name + jik;
		if (nameWithJik.isEmpty()) {
			throw new RuntimeException("일반결재자가 승인하지 않은 상태입니다." + suffix);
		}
		char last = nameWithJik.charAt(nameWithJik.length() - 1);
		String particle = (last >= 0xAC00 && last <= 0xD7A3 && ((last - 0xAC00) % 28) == 0) ? "가" : "이";
		throw new RuntimeException("일반결재자 " + nameWithJik + particle + " 승인하지 않은 상태입니다." + suffix);
	}

	// 지급완료 처리 전 신청부서(일반) 결재선(TODODIV2200)이 모두 승인되었는지 검증한다 (PM5101과 동일).
	private void validateTripRptGeneralApprovalDone(String tripRptNo) {
		if (!hasText(tripRptNo)) {
			return;
		}
		Map<String, String> lineParam = new HashMap<>();
		lineParam.put("todoNo", tripRptNo);
		lineParam.put("todoDiv2CodeId", "TODODIV2200");
		List<Map<String, String>> rptLines = wb20Svc.selectGetApprovalList(lineParam);
		if (rptLines == null || rptLines.isEmpty()) {
			// 혹시 AM 연동 문서가 있으면 AM 문서 상태 확인
			Map<String, String> amDocParam = new HashMap<>();
			amDocParam.put("tripReqNo", tripRptNo);
			Map<String, String> amDoc = pm51Mapper.selectAmDocInfoByTripReqNo(amDocParam);
			if (amDoc != null && hasText(amDoc.get("docId"))) {
				String docStatus = amDoc.get("docStatus");
				if ("PROGRESS".equalsIgnoreCase(docStatus) || "APPROVED".equalsIgnoreCase(docStatus) || "COMPLETED".equalsIgnoreCase(docStatus)) {
					return;
				}
			}
			throw new RuntimeException("일반결재선이 등록되지 않아 지급완료 처리할 수 없습니다.");
		}

		Map<String, String> pendingLine = null;
		int pendingSn = 0;
		for (Map<String, String> line : rptLines) {
			if ("Y".equals(line.get("sanctnSttus")) || hasText(line.get("todoCfDt"))) {
				continue;
			}
			int sn;
			try {
				sn = Integer.parseInt(String.valueOf(line.get("sanctnSn")));
			} catch (Exception e) {
				continue;
			}
			if (pendingLine == null || sn < pendingSn) {
				pendingLine = line;
				pendingSn = sn;
			}
		}
		if (pendingLine == null) {
			return;
		}

		String suffix = "\n일반결재가 완료되어야 지급완료 처리할 수 있습니다.";
		String name = pendingLine.get("todoNm") == null ? "" : String.valueOf(pendingLine.get("todoNm")).trim();
		String jik = pendingLine.get("jik") == null ? "" : String.valueOf(pendingLine.get("jik")).replaceAll("\\s", "");
		String nameWithJik = name + jik;
		if (nameWithJik.isEmpty()) {
			throw new RuntimeException("일반결재자가 승인하지 않은 상태입니다." + suffix);
		}
		char last = nameWithJik.charAt(nameWithJik.length() - 1);
		String particle = (last >= 0xAC00 && last <= 0xD7A3 && ((last - 0xAC00) % 28) == 0) ? "가" : "이";
		throw new RuntimeException("일반결재자 " + nameWithJik + particle + " 승인하지 않은 상태입니다." + suffix);
	}

	@Override
	public int updateTripReqPayCancel(Map<String, String> paramMap) throws Exception {
		if (!isAccountingDept(paramMap.get("deptId"))) {
			throw new RuntimeException("회계팀만 지급완료 취소할 수 있습니다.");
		}
		Map<String, String> orgMap = pm51Mapper.selectTripReqM01(paramMap);
		if (orgMap == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		if (!hasText(orgMap.get("payDt"))) {
			throw new RuntimeException("지급완료 처리된 출장신청서가 아닙니다.");
		}
		int result = pm51Mapper.updateTripReqPayCancel(paramMap);
		// 지급취소는 지급만 취소하고 관리부서 결재선은 유지
		// pm51Mapper.updateTripReqPayCancelMngApproval(paramMap);
		return result;
	}

	@Override
	public List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap) {
		return pm51Mapper.selectSignResUserlstInit(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripDateOverlapList(Map<String, String> paramMap) {
		return pm51Mapper.selectTripDateOverlapList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripScheduleByMonth(Map<String, String> paramMap) {
		return pm51Mapper.selectTripScheduleByMonth(paramMap);
	}

	@Override
	public Map<String, String> selectSalesCodeWbsSchedule(Map<String, String> paramMap) {
		return pm51Mapper.selectSalesCodeWbsSchedule(paramMap);
	}

	@Override
	public int selectTripRptListCount(Map<String, String> paramMap) {
		return pm51Mapper.selectTripRptListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripRptList(Map<String, String> paramMap) {
		return pm51Mapper.selectTripRptList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripReqForRpt(Map<String, String> paramMap) {
		return pm51Mapper.selectTripReqForRpt(paramMap);
	}

	@Override
	public Map<String, Object> selectTripRptDtl(Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		Map<String, String> m02 = pm51Mapper.selectTripRptM01(paramMap);
		List<Map<String, String>> d03 = pm51Mapper.selectTripRptD01(paramMap);
		List<Map<String, String>> d02 = pm51Mapper.selectTripRptD02(paramMap);
		if (m02 != null) {
			paramMap.put("tripReqNo", m02.get("tripReqNo"));
		}
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		List<Map<String, String>> d01 = pm51Mapper.selectTripReqD01(paramMap);
		List<Map<String, String>> reqExpItems = pm51Mapper.selectTripReqD02(paramMap);
		List<Map<String, String>> reqProjectList = pm51Mapper.selectTripReqD03(paramMap);
		List<Map<String, String>> rptProjectList = pm51Mapper.selectTripRptD03(paramMap);
		List<Map<String, String>> rptCardList = pm51Mapper.selectTripRptD04(paramMap);
		List<Map<String, String>> rptDailyList = pm51Mapper.selectTripRptD05(paramMap);

		result.put("m02", m02);
		result.put("d02", d02);
		result.put("d03", d03);
		result.put("m01", m01);
		result.put("d01", d01);
		result.put("reqExpItems", reqExpItems);
		result.put("reqProjectList", reqProjectList);
		result.put("rptProjectList", rptProjectList);
		result.put("rptCardList", rptCardList);
		result.put("rptDailyList", rptDailyList);

		if (m02 != null && hasText(m02.get("tripRptNo"))) {
			try {
				Map<String, String> docIdParam = new HashMap<>();
				docIdParam.put("tripReqNo", m02.get("tripRptNo"));
				docIdParam.put("coCd", hasText(m02.get("coCd")) ? m02.get("coCd") : "GUN");
				String amDocId = pm51Mapper.selectAmDocIdByTripReqNo(docIdParam);
				if (!hasText(amDocId)) {
					syncTripRptToAm(m02);
				}
			} catch (Exception e) {
				logger.warn("출장복명서 AM 전자결재 자동 동기화 예외 (상세 조회 계속 진행): tripRptNo={}, error={}", m02.get("tripRptNo"), e.getMessage());
			}
		}

		return result;
	}

	@Override
	public int insertTripRpt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		int existCnt = pm51Mapper.selectTripRptExists(paramMap);
		if (existCnt > 0) {
			throw new RuntimeException("이미 복명서가 존재합니다");
		}

		// 마감 검증: actStDtm, actEdDtm (14자리 YYYYMMDDHHMMSS)
		String actStDtm = paramMap.get("actStDtm");
		String actEdDtm = paramMap.get("actEdDtm");
		if ((actStDtm != null && !actStDtm.isEmpty()) || (actEdDtm != null && !actEdDtm.isEmpty())) {
			pm30Svc.assertNotClosed(actStDtm, actEdDtm);
		}

		int result = pm51Mapper.insertTripRptM01(paramMap);

		List<Map<String, String>> rptTravelerArr = gsonDtl.fromJson(paramMap.get("rptTravelerArr"), dtlMap);
		if (rptTravelerArr != null && !rptTravelerArr.isEmpty()) {
			for (Map<String, String> travelerMap : rptTravelerArr) {
				travelerMap.put("tripRptNo", paramMap.get("tripRptNo"));
				travelerMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD02(travelerMap);
			}
		}

		List<Map<String, String>> rptProjectArr = gsonDtl.fromJson(paramMap.get("rptProjectArr"), dtlMap);
		if (rptProjectArr != null && !rptProjectArr.isEmpty()) {
			for (Map<String, String> projectMap : rptProjectArr) {
				projectMap.put("tripRptNo", paramMap.get("tripRptNo"));
				projectMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD03(projectMap);
			}
		}

		List<Map<String, String>> rptCardArr = gsonDtl.fromJson(paramMap.get("rptCardArr"), dtlMap);
		if (rptCardArr != null && !rptCardArr.isEmpty()) {
			for (Map<String, String> cardMap : rptCardArr) {
				cardMap.put("tripRptNo", paramMap.get("tripRptNo"));
				cardMap.put("userId", paramMap.get("userId"));
				cardMap.put("pgmId", "PM5102P01");
				pm51Mapper.insertTripRptD04(cardMap);
			}
		}

		List<Map<String, String>> rptDailyArr = gsonDtl.fromJson(paramMap.get("rptDailyArr"), dtlMap);
		if (rptDailyArr != null && !rptDailyArr.isEmpty()) {
			for (Map<String, String> dailyMap : rptDailyArr) {
				dailyMap.put("tripRptNo", paramMap.get("tripRptNo"));
				dailyMap.put("creatId", paramMap.get("userId"));
				dailyMap.put("pgmId", "PM5102P01");
				pm51Mapper.insertTripRptD05(dailyMap);
			}
		}

		paramMap.put("fileTrgtKey", paramMap.get("tripRptNo"));
		paramMap.put("pgmId", "PM5102P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		paramMap.put("salesCd", pm51Mapper.selectTripRptSalesCd(paramMap));

		if (!paramMap.containsKey("approvalArr")) {
			paramMap.put("approvalArr", "[]");
		}
		if (paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripRptTravelerLeaders(paramMap, approvalArr);
			approvalArr = reorderRptGeneralApprovalArr(paramMap, approvalArr);
			if (approvalArr != null && approvalArr.size() > 0) {
				paramMap.put("reqNo", paramMap.get("tripRptNo"));
				paramMap.put("fileTrgtKey", paramMap.get("tripRptNo"));

				String pgParam1 = "{\"actionType\":\"" + "T" + "\",";
				pgParam1 += "\"gubun\":\"" + "개인" + "\",";
				pgParam1 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam1 += "\"tripRptNo\":\"" + paramMap.get("tripRptNo") + "\",";
				pgParam1 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				String pgParam2 = "{\"actionType\":\"" + "S" + "\",";
				pgParam2 += "\"gubun\":\"" + "개인" + "\",";
				pgParam2 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam2 += "\"tripRptNo\":\"" + paramMap.get("tripRptNo") + "\",";
				pgParam2 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				int iSharng = 1;
				int iApproval = 1;
				// 타인(대리 등록자)이 복명서를 등록/수정하는 경우에도 CREAT_ID는 등록자가 아니라 출장 신청인을 가리켜야 한다.
				String originalReqId = resolveOriginalRequesterId(paramMap);
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", approvalSalesCd(paramMap));
					fillApprovalBaseParam(approvalMap, paramMap);
					String actingUserId = approvalMap.get("userId");
					if (hasText(originalReqId)) {
						approvalMap.put("userId", originalReqId); // CREAT_ID를 출장 신청인으로 고정
					}

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						insertWbsSharngListSync(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						insertWbsApprovalListSync(approvalMap);
						iApproval++;
						approvalMap.put("userId", actingUserId); // 자동승인 판정/UDT_ID는 실제 행위자 기준으로 복원
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));
							approvalMap.put("sanctnSn", approvalMap.get("sanCtnSn"));

							Object value = approvalMap.get("toDoKey");
							if (value != null) {
								approvalMap.put("todoKey", value.toString());
							}

							wb20Svc.insertApprovalLine(approvalMap);
						}
					}
				}
			}
		}
		processTripReqApprovalArr(paramMap, gsonDtl, dtlMap, "mngApprovalArr", "관리부서");

		syncTripRptToAm(paramMap);

		return result;
	}

	@Override
	public int updateTripRpt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		paramMap.put("reqNo", paramMap.get("tripRptNo"));
		paramMap.put("fileTrgtKey", paramMap.get("tripRptNo"));
		paramMap.put("salesCd", pm51Mapper.selectTripRptSalesCd(paramMap));

		Map<String, String> m02Param = new HashMap<>();
		m02Param.put("tripRptNo", paramMap.get("tripRptNo"));
		Map<String, String> orgRptMap = pm51Mapper.selectTripRptM01(m02Param);
		if (orgRptMap != null && "APRVSTS03".equals(orgRptMap.get("aprvStsCd"))) {
			throw new RuntimeException("결재완료된 출장복명서는 수정할 수 없습니다.");
		}

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재가 진행중입니다");
			}
		}

		// 마감 검증: actStDtm, actEdDtm (14자리 YYYYMMDDHHMMSS)
		String actStDtm = paramMap.get("actStDtm");
		String actEdDtm = paramMap.get("actEdDtm");
		if ((actStDtm != null && !actStDtm.isEmpty()) || (actEdDtm != null && !actEdDtm.isEmpty())) {
			pm30Svc.assertNotClosed(actStDtm, actEdDtm);
		}

		int result = pm51Mapper.updateTripRptM01(paramMap);

		// 반려된 출장복명서 내용 수정 저장 시 결재선 초기화 및 반려 Flag clear (처음부터 재상신 가능하도록)
		Map<String, String> resetParam = new HashMap<>();
		resetParam.put("coCd", paramMap.get("coCd"));
		resetParam.put("todoNo", paramMap.get("tripRptNo"));
		resetParam.put("userId", paramMap.get("userId"));
		resetParam.put("pgmId", "PM5102P01");
		wb20Svc.resetRejectedApprovalLines(resetParam);

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripRptNo", paramMap.get("tripRptNo"));
		delParam.put("reqNo", paramMap.get("tripRptNo"));
		delParam.put("salesCd", paramMap.get("salesCd"));
		if (!hasCompletedApproval(paramMap, false)) {
			pm51Mapper.deleteTripReqApprovalLines(delParam);
		}
		if (!hasCompletedApproval(paramMap, true)) {
			pm51Mapper.deleteTripReqMngApprovalLines(delParam);
		}

		List<Map<String, String>> sharngChk = qm01Mapper.deleteWbsSharngListChk(delParam);
		if (sharngChk.size() > 0) {
			qm01Mapper.deleteWbsSharngList(delParam);
		}

		pm51Mapper.deleteTripRptD02(delParam);
		pm51Mapper.deleteTripRptD03(delParam);
		// 카드사 명세는 회계 전용 경로(updateTripRptAcctSettle)로도 저장되므로, 요청에 전송된 경우에만 삭제한다
		if (paramMap.get("rptCardArr") != null) {
			pm51Mapper.deleteTripRptD04(delParam);
		}
		// 일비정산 통화별 상세는 요청에 전송된 경우에만 삭제한다
		if (paramMap.get("rptDailyArr") != null) {
			pm51Mapper.deleteTripRptD05(delParam);
		}

		List<Map<String, String>> rptTravelerArr = gsonDtl.fromJson(paramMap.get("rptTravelerArr"), dtlMap);
		if (rptTravelerArr != null && !rptTravelerArr.isEmpty()) {
			for (Map<String, String> travelerMap : rptTravelerArr) {
				travelerMap.put("tripRptNo", paramMap.get("tripRptNo"));
				travelerMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD02(travelerMap);
			}
		}

		List<Map<String, String>> rptProjectArr = gsonDtl.fromJson(paramMap.get("rptProjectArr"), dtlMap);
		if (rptProjectArr != null && !rptProjectArr.isEmpty()) {
			for (Map<String, String> projectMap : rptProjectArr) {
				projectMap.put("tripRptNo", paramMap.get("tripRptNo"));
				projectMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD03(projectMap);
			}
		}

		List<Map<String, String>> rptCardArr = gsonDtl.fromJson(paramMap.get("rptCardArr"), dtlMap);
		if (rptCardArr != null && !rptCardArr.isEmpty()) {
			for (Map<String, String> cardMap : rptCardArr) {
				cardMap.put("tripRptNo", paramMap.get("tripRptNo"));
				cardMap.put("userId", paramMap.get("userId"));
				cardMap.put("pgmId", "PM5102P01");
				pm51Mapper.insertTripRptD04(cardMap);
			}
		}

		List<Map<String, String>> rptDailyArr = gsonDtl.fromJson(paramMap.get("rptDailyArr"), dtlMap);
		if (rptDailyArr != null && !rptDailyArr.isEmpty()) {
			for (Map<String, String> dailyMap : rptDailyArr) {
				dailyMap.put("tripRptNo", paramMap.get("tripRptNo"));
				dailyMap.put("creatId", paramMap.get("userId"));
				dailyMap.put("pgmId", "PM5102P01");
				pm51Mapper.insertTripRptD05(dailyMap);
			}
		}

		paramMap.put("fileTrgtKey", paramMap.get("tripRptNo"));
		paramMap.put("pgmId", "PM5102P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		if (!paramMap.containsKey("approvalArr")) {
			paramMap.put("approvalArr", "[]");
		}
		if (paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripRptTravelerLeaders(paramMap, approvalArr);
			approvalArr = reorderRptGeneralApprovalArr(paramMap, approvalArr);
			if (approvalArr != null && approvalArr.size() > 0) {
				String pgParam1 = "{\"actionType\":\"" + "T" + "\",";
				pgParam1 += "\"gubun\":\"" + "팀" + "\",";
				pgParam1 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam1 += "\"tripRptNo\":\"" + paramMap.get("tripRptNo") + "\",";
				pgParam1 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				String pgParam2 = "{\"actionType\":\"" + "S" + "\",";
				pgParam2 += "\"gubun\":\"" + "팀" + "\",";
				pgParam2 += "\"coCd\":\"" + paramMap.get("coCd") + "\",";
				pgParam2 += "\"tripRptNo\":\"" + paramMap.get("tripRptNo") + "\",";
				pgParam2 += "\"userId\":\"" + paramMap.get("userId") + "\"}";

				int iSharng = 1;
				int iApproval = 1;
				// 타인(대리 등록자)이 복명서를 등록/수정하는 경우에도 CREAT_ID는 등록자가 아니라 출장 신청인을 가리켜야 한다.
				String originalReqId = resolveOriginalRequesterId(paramMap);
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", approvalSalesCd(paramMap));
					fillApprovalBaseParam(approvalMap, paramMap);
					String actingUserId = approvalMap.get("userId");
					if (hasText(originalReqId)) {
						approvalMap.put("userId", originalReqId); // CREAT_ID를 출장 신청인으로 고정
					}

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						insertWbsSharngListSync(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						insertWbsApprovalListSync(approvalMap);
						iApproval++;
						approvalMap.put("userId", actingUserId); // 자동승인 판정/UDT_ID는 실제 행위자 기준으로 복원
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));
							approvalMap.put("sanctnSn", approvalMap.get("sanCtnSn"));

							Object value = approvalMap.get("toDoKey");
							if (value != null) {
								approvalMap.put("todoKey", value.toString());
							}

							wb20Svc.insertApprovalLine(approvalMap);
						}
					}
				}
			}
		}
		processTripReqApprovalArr(paramMap, gsonDtl, dtlMap, "mngApprovalArr", "관리부서");

		syncTripRptToAm(paramMap);

		return result;
	}

	@Override
	public int updateTripRptMngEval(Map<String, String> paramMap) throws Exception {
		paramMap.put("reqNo", paramMap.get("tripRptNo"));

		Map<String, String> m02 = pm51Mapper.selectTripRptM01(paramMap);
		if (m02 == null) {
			throw new RuntimeException("출장복명서 정보를 찾을 수 없습니다.");
		}

		Map<String, String> reqParam = new HashMap<>();
		reqParam.put("tripReqNo", m02.get("tripReqNo"));
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(reqParam);
		if (m01 == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		// 관리부서 결재까지 최종 완료된 출장복명서는 수정 불가
		if (hasCompletedApproval(paramMap, true)) {
			throw new RuntimeException("최종 결재 완료된 출장복명서는 수정할 수 없습니다.");
		}

		String applicantId = m01.get("userId");
		String callerId = paramMap.get("userId");

		Map<String, String> userInfoParam = new HashMap<>();
		userInfoParam.put("userId", applicantId);
		Map<String, String> applicantInfo = cm06Mapper.selectUserInfo(userInfoParam);

		boolean isApplicantTeamManager = applicantInfo != null && "Y".equals(applicantInfo.get("teamManager"));
		boolean isSelfTeamManagerEdit = isApplicantTeamManager && hasText(applicantId) && applicantId.equals(callerId);
		boolean isApproverTeamManager = applicantInfo != null
				&& hasText(applicantInfo.get("mngId"))
				&& applicantInfo.get("mngId").equals(callerId);

		if (!isSelfTeamManagerEdit && !isApproverTeamManager) {
			throw new RuntimeException("담당 팀장만 부서장평가를 수정할 수 있습니다.");
		}

		int result = pm51Mapper.updateTripRptMngEval(paramMap);

		// 부서장평가 저장과 동시에, 호출자 본인의 미결 결재선(TODODIV2200) 결재처리(승인완료) 진행
		// AM 전자결재 연동 문서(PM52)가 존재하는 경우 AM 승인을 우선 호출하면,
		// AM11SvcImpl.executeLinkedWb20Approval을 통해 WB20 결재선도 함께 자동 승인 연계된다.
		String tripRptNo = paramMap.get("tripRptNo");
		Map<String, String> docParam = new HashMap<>();
		docParam.put("tripReqNo", tripRptNo);
		Map<String, String> amDoc = pm51Mapper.selectAmDocInfoByTripReqNo(docParam);

		boolean amApproved = false;
		if (amDoc != null && hasText(amDoc.get("docId"))) {
			String docId = amDoc.get("docId");
			String amCoCd = amDoc.get("coCd");
			Map<String, Object> approvalParam = new HashMap<>();
			approvalParam.put("docId", docId);
			approvalParam.put("coCd", amCoCd);
			approvalParam.put("userId", callerId);
			String approverNm = paramMap.get("userNm");
			if (!hasText(approverNm)) {
				Map<String, String> uParam = new HashMap<>();
				uParam.put("userId", callerId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(uParam);
				if (uInfo != null) {
					approverNm = uInfo.get("name");
				}
			}
			approvalParam.put("userNm", approverNm);
			approvalParam.put("pgmId", hasText(paramMap.get("pgmId")) ? paramMap.get("pgmId") : "PM5102P01");
			approvalParam.put("apprOpinion", hasText(paramMap.get("todoCfOpn")) ? paramMap.get("todoCfOpn") : "부서장평가 승인");
			Map<String, Object> amResult = am11Svc.approveDocument(approvalParam);
			if (amResult != null && "200".equals(String.valueOf(amResult.get("resultCode")))) {
				amApproved = true;
			} else if (amResult != null && !"200".equals(String.valueOf(amResult.get("resultCode")))) {
				throw new RuntimeException("전자결재 승인 연계 실패: " + amResult.get("resultMessage"));
			}
		}

		// AM 연계 승인이 처리되지 않은 경우(레거시 문서 또는 fallback), WB20 결재선 직접 승인 진행
		if (!amApproved) {
			Map<String, String> approvalLineParam = new HashMap<>();
			approvalLineParam.put("todoNo", tripRptNo);
			approvalLineParam.put("todoDiv2CodeId", "TODODIV2200");
			List<Map<String, String>> approvalLines = wb20Svc.selectGetApprovalList(approvalLineParam);
			if (approvalLines != null) {
				for (Map<String, String> line : approvalLines) {
					if (callerId.equals(line.get("todoId")) && !"Y".equals(line.get("sanctnSttus"))) {
						Map<String, String> approveParam = new HashMap<>(line);
						approveParam.put("todoCfOpn", hasText(paramMap.get("todoCfOpn")) ? paramMap.get("todoCfOpn") : "");
						approveParam.put("userId", callerId);
						approveParam.put("pgmId", paramMap.get("pgmId"));
						wb20Svc.insertApprovalLine(approveParam);
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public int updateTripReqSalesInfo(Map<String, String> paramMap) throws Exception {
		Map<String, String> orgMap = pm51Mapper.selectTripReqM01(paramMap);
		if (orgMap == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		if (hasText(orgMap.get("payDt"))) {
			throw new RuntimeException("지급완료된 출장신청서는 수정할 수 없습니다.");
		}

		String pmId = orgMap.get("pmId");
		String callerId = paramMap.get("userId");
		if (!hasText(pmId) || !pmId.equals(callerId)) {
			throw new RuntimeException("등록된 영업PM만 이 항목을 수정할 수 있습니다.");
		}

		// 영업PM 본인의 결재선을 조회하여, 이미 결재완료(Y)했거나 결재 대상으로 등록되어 있지 않으면 거부
		// (결재완료 후에는 영업PM이라도 조회만 가능해야 함 - 임의로 재수정/재결재 불가)
		Map<String, String> approvalLineParam = new HashMap<>();
		approvalLineParam.put("todoNo", paramMap.get("tripReqNo"));
		approvalLineParam.put("todoDiv2CodeId", "TODODIV2190");
		List<Map<String, String>> approvalLines = wb20Svc.selectGetApprovalList(approvalLineParam);
		Map<String, String> myPendingLine = null;
		if (approvalLines != null) {
			for (Map<String, String> line : approvalLines) {
				if (callerId.equals(line.get("todoId"))) {
					if ("Y".equals(line.get("sanctnSttus"))) {
						throw new RuntimeException("이미 결재를 완료하여 수정할 수 없습니다.");
					}
					myPendingLine = line;
					break;
				}
			}
		}
		if (myPendingLine == null) {
			throw new RuntimeException("결재 대상으로 등록되어 있지 않아 수정할 수 없습니다.");
		}

		int result = pm51Mapper.updateTripReqSalesInfo(paramMap);

		Map<String, String> approveParam = new HashMap<>(myPendingLine);
		approveParam.put("todoCfOpn", "");
		approveParam.put("userId", callerId);
		approveParam.put("pgmId", paramMap.get("pgmId"));
		wb20Svc.insertApprovalLine(approveParam);

		return result;
	}

	@Override
	public int deleteTripRpt(Map<String, String> paramMap) throws Exception {
		paramMap.put("reqNo", paramMap.get("tripRptNo"));

		// 마감 검증: 기존 DB의 actStDtm, actEdDtm
		Map<String, String> m01 = pm51Mapper.selectTripRptM01(paramMap);
		if (m01 != null) {
			String actStDtm = m01.get("actStDtm");
			String actEdDtm = m01.get("actEdDtm");
			if ((actStDtm != null && !actStDtm.isEmpty()) || (actEdDtm != null && !actEdDtm.isEmpty())) {
				pm30Svc.assertNotClosed(actStDtm, actEdDtm);
			}
		}

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재처리가 이미 진행중이거나 완료된 복명서는 삭제할 수 없습니다.");
			}
		}

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripRptNo", paramMap.get("tripRptNo"));
		delParam.put("reqNo", paramMap.get("tripRptNo"));
		delParam.put("salesCd", pm51Mapper.selectTripRptSalesCd(paramMap));

		pm51Mapper.deleteTripReqApprovalLines(delParam);
		pm51Mapper.deleteTripReqMngApprovalLines(delParam);

		// AM11 전자결재 문서/결재선 삭제는 ERP_BIZ_KEY(=TRIP_RPT_NO) 유니크 키만으로 특정한다 (CO_CD 조건 불필요).
		Map<String, String> amDelParam = new HashMap<>();
		amDelParam.put("tripReqNo", paramMap.get("tripRptNo"));
		pm51Mapper.deleteAmApprovalLinesByBizKey(amDelParam);
		pm51Mapper.deleteAmApprovalDocByBizKey(amDelParam);

		// 공유선(WBS) CASCADE 삭제 - deleteTripReq/updateTripRpt 와 동일한 관례
		// (기존에 여기 누락되어 복명서에 등록된 공유자가 삭제 후에도 TB_WB20M03 에 고아로 남는 문제가 있었음)
		List<Map<String, String>> sharngChk = qm01Mapper.deleteWbsSharngListChk(delParam);
		if (sharngChk.size() > 0) {
			qm01Mapper.deleteWbsSharngList(delParam);
		}

		pm51Mapper.deleteTripRptD02(delParam);
		pm51Mapper.deleteTripRptD03(delParam);
		pm51Mapper.deleteTripRptD04(delParam);
		pm51Mapper.deleteTripRptD01(delParam);

		// 첨부파일 CASCADE 삭제 (FILE_TRGT_TYP = 'PM5102P01')
		try {
			Map<String, String> fileSearchMap = new HashMap<>();
			fileSearchMap.put("fileTrgtTyp", "PM5102P01");
			fileSearchMap.put("fileTrgtKey", paramMap.get("tripRptNo"));
			List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(fileSearchMap);
			if (deleteFileList != null && !deleteFileList.isEmpty()) {
				for (Map<String, String> delFile : deleteFileList) {
					String fKey = delFile.get("fileKey");
					if (fKey == null || fKey.isEmpty()) fKey = delFile.get("file_key");
					if (fKey == null || fKey.isEmpty()) fKey = delFile.get("FILE_KEY");
					if (fKey != null && !fKey.isEmpty()) {
						cm08Svc.deleteFile(fKey);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int result = pm51Mapper.deleteTripRptM01(delParam);
		return result;
	}

	private String getCurrentDateString() {
		return new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	}

	//영업팀코드 앞자리 (TRN30, GUN30)
	private boolean isSalesDept(String deptId) {
		return deptId != null && (deptId.startsWith("GUN30") || deptId.startsWith("TRN30"));
	}

	//회계팀코드 앞자리 (GUN20, GUN80)
	private boolean isAccountingDept(String deptId) {
		return deptId != null && (deptId.startsWith("GUN20") || deptId.startsWith("GUN80"));
	}

	//공통 Util
	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

	//영업코드 조회
	// 결재행(TB_WB20M03)에 저장할 Sales Code.
	// 지급처리 흐름처럼 화면이 salesCd를 전달하지 않는 저장 경로에서도 값이 비지 않도록 마스터에서 보완한다.
	// SALES_CD가 NULL이면 결재함의 '이전 미결자 존재여부' 판정 조인(S.SALES_CD = T.SALES_CD)이
	// NULL 비교로 항상 어긋나서, 순차결재 대상인데도 이전 미결이 없는 것으로 취급된다.
	private String approvalSalesCd(Map<String, String> paramMap) {
		String salesCd = paramMap.get("salesCd");
		if (hasText(salesCd)) {
			return salesCd;
		}
		try {
			if (hasText(paramMap.get("tripRptNo"))) {			// 출장복명서: 연결된 출장신청서의 Sales Code
				Map<String, String> rptParam = new HashMap<>();
				rptParam.put("tripRptNo", paramMap.get("tripRptNo"));
				salesCd = pm51Mapper.selectTripRptSalesCd(rptParam);
			} else if (hasText(paramMap.get("tripReqNo"))) {	// 출장신청서
				Map<String, String> reqParam = new HashMap<>();
				reqParam.put("tripReqNo", paramMap.get("tripReqNo"));
				Map<String, String> m01 = pm51Mapper.selectTripReqM01(reqParam);
				if (m01 != null && m01.get("salesCd") != null) {
					salesCd = String.valueOf(m01.get("salesCd"));
				}
			}
		} catch (Exception e) {
			// 보완 조회 실패 시에는 기존과 동일하게 전달값(null)을 사용한다.
		}
		return salesCd;
	}

	//출장비 결재확인
	private boolean hasCompletedApproval(Map<String, String> paramMap, boolean managementOnly) {
		List<Map<String, String>> approvalChkList = managementOnly
				? pm51Mapper.selectApprovalMngChk(paramMap)
				: pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList == null || approvalChkList.size() == 0) {
			return false;
		}
		String cnt = approvalChkList.get(0).get("cnt");
		return cnt != null && !"0".equals(cnt);
	}

	private List<Map<String, String>> appendTripRptTravelerLeaders(Map<String, String> paramMap,
			List<Map<String, String>> approvalArr) {
		List<Map<String, String>> sourceArr = approvalArr == null ? new ArrayList<>() : approvalArr;

		// 화면에서 온 결재선 자체의 중복 아이디 제거 (결재/공유 각각 첫 행만 유지)
		List<Map<String, String>> mergedApprovalArr = new ArrayList<>();
		Set<String> registeredUserIds = new HashSet<>();
		Set<String> sharedUserIds = new HashSet<>();
		for (Map<String, String> approvalMap : sourceArr) {
			String userId = approvalMap.get("usrNm");
			if (!hasText(userId)) {
				userId = approvalMap.get("todoId");
			}
			Set<String> idSet = "공유".equals(approvalMap.get("gb")) ? sharedUserIds : registeredUserIds;
			if (hasText(userId) && !idSet.add(userId)) {
				continue;
			}
			mergedApprovalArr.add(approvalMap);
		}

		List<Map<String, String>> leaderArr = pm51Mapper.selectTripRptTravelerApprovalLines(paramMap);
		if (leaderArr == null || leaderArr.isEmpty()) {
			return mergedApprovalArr;
		}

		// [예외] sms가 GUN60(생산팀) 소속 팀원을 대신해 입력하는 경우, 출장자 본인결재는 자동추가하지 않음
		String callerId = paramMap.get("userId");
		String skipSelfApprovalUserId = null;
		if ("sms".equals(callerId)) {
			Map<String, String> reqParam = new HashMap<>();
			reqParam.put("tripReqNo", paramMap.get("tripReqNo"));
			Map<String, String> reqM01 = pm51Mapper.selectTripReqM01(reqParam);
			String applicantId = reqM01 != null ? reqM01.get("userId") : null;
			if (hasText(applicantId) && !applicantId.equals(callerId)) {
				Map<String, String> reqUserInfoParam = new HashMap<>();
				reqUserInfoParam.put("userId", applicantId);
				Map<String, String> reqUserInfo = cm06Mapper.selectUserInfo(reqUserInfoParam);
				String reqDeptId = reqUserInfo != null ? reqUserInfo.get("deptId") : null;
				if (hasText(reqDeptId) && reqDeptId.startsWith("GUN60")) {
					skipSelfApprovalUserId = applicantId;
				}
			}
		}

		for (Map<String, String> leaderRow : leaderArr) {
			String userId = leaderRow.get("usrNm");
			if (!hasText(userId) || userId.equals(skipSelfApprovalUserId) || !registeredUserIds.add(userId)) {
				continue;
			}
			// 조회 결과(CamelMap)는 put 시 키가 변형되므로 일반 HashMap으로 복사해서 사용
			Map<String, String> leaderMap = new HashMap<>(leaderRow);
			leaderMap.put("gb", "결재");
			leaderMap.put("todoDiv2CodeId", "TODODIV2200");
			leaderMap.put("flag", "I");
			leaderMap.put("coCd", paramMap.get("coCd"));
			mergedApprovalArr.add(leaderMap);
		}
		return mergedApprovalArr;
	}

	private List<Map<String, String>> appendTripReqApplicantApprovals(Map<String, String> paramMap,
			List<Map<String, String>> approvalArr) {
		List<Map<String, String>> mergedApprovalArr = approvalArr == null ? new ArrayList<>() : approvalArr;
		Set<String> registeredUserIds = new HashSet<>();
		for (Map<String, String> approvalMap : mergedApprovalArr) {
			String userId = hasText(approvalMap.get("usrNm")) ? approvalMap.get("usrNm") : approvalMap.get("todoId");
			if (hasText(userId)) {
				registeredUserIds.add(userId);
			}
		}

		String reqId = paramMap.get("reqId");
		if (!hasText(reqId)) {
			return mergedApprovalArr;
		}

		// [예외] sms가 GUN60(생산팀) 소속 팀원을 대신해 입력하는 경우, 출장자 본인결재는 자동추가하지 않음
		String callerId = paramMap.get("userId");
		if ("sms".equals(callerId) && !reqId.equals(callerId)) {
			Map<String, String> reqUserInfoParam = new HashMap<>();
			reqUserInfoParam.put("userId", reqId);
			Map<String, String> reqUserInfo = cm06Mapper.selectUserInfo(reqUserInfoParam);
			String reqDeptId = reqUserInfo != null ? reqUserInfo.get("deptId") : null;
			if (hasText(reqDeptId) && reqDeptId.startsWith("GUN60")) {
				return mergedApprovalArr;
			}
		}

		if (registeredUserIds.add(reqId)) {
			Map<String, String> applicantMap = new HashMap<>();
			applicantMap.put("gb", "결재");
			applicantMap.put("usrNm", reqId);
			applicantMap.put("todoId", reqId);
			applicantMap.put("name", hasText(paramMap.get("reqNm")) ? paramMap.get("reqNm") : reqId);
			applicantMap.put("todoDiv2CodeId", "TODODIV2190");
			applicantMap.put("flag", "I");
			mergedApprovalArr.add(applicantMap);
		}


		return mergedApprovalArr;
	}

	private List<Map<String, String>> appendPayMngApprovals(Map<String, String> paramMap,
			List<Map<String, String>> approvalArr) {
		List<Map<String, String>> mergedApprovalArr = approvalArr == null ? new ArrayList<>() : approvalArr;
		Set<String> registeredUserIds = new HashSet<>();
		for (Map<String, String> approvalMap : mergedApprovalArr) {
			String userId = hasText(approvalMap.get("usrNm")) ? approvalMap.get("usrNm") : approvalMap.get("todoId");
			if (hasText(userId)) {
				registeredUserIds.add(userId);
			}
		}
		for (String userId : selectPayMngApproverIds()) {
			if (registeredUserIds.add(userId)) {
				Map<String, String> approvalMap = new HashMap<>();
				approvalMap.put("gb", "결재");
				approvalMap.put("usrNm", userId);
				approvalMap.put("todoId", userId);
				approvalMap.put("name", userId);
				approvalMap.put("todoDiv2CodeId", "TODODIV2191");
				approvalMap.put("flag", "I");
				mergedApprovalArr.add(approvalMap);
			}
		}
		return mergedApprovalArr;
	}

	// 관리부서 결재자 고정 ID 목록 (공통코드 CODE_ID='SPECRTS14'의 CODE_ETC, 콤마 구분)
	private String[] selectPayMngApproverIds() {
		Map<String, String> codeMap = new HashMap<>();
		codeMap.put("codeId", "SPECRTS14");
		Map<String, String> codeDetail = cm05Svc.selectCodeInfo(codeMap);
		if (codeDetail == null) {
			return new String[0];
		}
		String codeEtc = codeDetail.get("codeEtc");
		if (!hasText(codeEtc) || "null".equals(codeEtc)) {
			return new String[0];
		}
		return codeEtc.split(",");
	}

	// 회계담당자 고정 ID 목록 (공통코드 CODE_ID='SPECRTS15'의 CODE_ETC, 콤마 구분)
	private String[] selectAcctMngApproverIds() {
		Map<String, String> codeMap = new HashMap<>();
		codeMap.put("codeId", "SPECRTS15");
		Map<String, String> codeDetail = cm05Svc.selectCodeInfo(codeMap);
		if (codeDetail == null) {
			return new String[0];
		}
		String codeEtc = codeDetail.get("codeEtc");
		if (!hasText(codeEtc) || "null".equals(codeEtc)) {
			return new String[0];
		}
		return codeEtc.split(",");
	}

	// 로그인 사용자가 자금담당자(SPECRTS15) 권한자인지. 회계팀 부서(deptId GUN20/GUN80)와 별개로
	// SPECRTS15 코드 등록자는 지급완료 전 출장기간 등 내용 수정 권한을 가진다.
	private boolean isAcctMngApprover(String loginUserId) {
		if (!hasText(loginUserId)) return false;
		for (String approverId : selectAcctMngApproverIds()) {
			if (approverId != null && loginUserId.trim().equalsIgnoreCase(approverId.trim())) {
				return true;
			}
		}
		return false;
	}

	private static final Object APPROVAL_LOCK = new Object();

	private void insertWbsApprovalListSync(Map<String, String> approvalMap) {
		synchronized (APPROVAL_LOCK) {
			fillApprovalBaseParam(approvalMap, null);
			qm01Mapper.insertWbsApprovalList(approvalMap);
		}
	}

	private void insertWbsSharngListSync(Map<String, String> approvalMap) {
		synchronized (APPROVAL_LOCK) {
			fillApprovalBaseParam(approvalMap, null);
			qm01Mapper.insertWbsSharngList(approvalMap);
		}
	}

	// 관리부서결재선(mngApprovalArr) 행 생성 시 CREAT_ID로 쓸 "최초 신청자" ID를 구한다.
	// 우선순위: 1) 프론트가 보낸 reqId(PM5101 지급처리 흐름에서 전송) 2) DB 조회 폴백
	// (PM5102 복명서 흐름은 reqId를 보내지 않으므로, tripRptNo -> TRIP_REQ_NO -> TB_PM51M01.USER_ID로 역추적한다).
	private String resolveOriginalRequesterId(Map<String, String> paramMap) {
		String reqId = paramMap.get("reqId");
		if (hasText(reqId)) {
			return reqId.trim();
		}
		try {
			if (hasText(paramMap.get("tripReqNo"))) {
				Map<String, String> reqParam = new HashMap<>();
				reqParam.put("tripReqNo", paramMap.get("tripReqNo"));
				Map<String, String> reqM01 = pm51Mapper.selectTripReqM01(reqParam);
				if (reqM01 != null && hasText(reqM01.get("userId"))) {
					return reqM01.get("userId").trim();
				}
			} else if (hasText(paramMap.get("tripRptNo"))) {
				Map<String, String> rptParam = new HashMap<>();
				rptParam.put("tripRptNo", paramMap.get("tripRptNo"));
				Map<String, String> rptM01 = pm51Mapper.selectTripRptM01(rptParam);
				if (rptM01 != null && hasText(rptM01.get("tripReqNo"))) {
					Map<String, String> reqParam = new HashMap<>();
					reqParam.put("tripReqNo", rptM01.get("tripReqNo"));
					Map<String, String> reqM01 = pm51Mapper.selectTripReqM01(reqParam);
					if (reqM01 != null && hasText(reqM01.get("userId"))) {
						return reqM01.get("userId").trim();
					}
				}
			}
		} catch (Exception ignored) {
			// 조회 실패 시 폴백 없이 null 반환 -> 호출부에서 paramMap.userId(행위자)를 그대로 사용
		}
		return null;
	}

	private void fillApprovalBaseParam(Map<String, String> approvalMap, Map<String, String> paramMap) {
		if (paramMap != null) {
			if (!hasText(paramMap.get("coCd"))) {
				throw new IllegalArgumentException("PM51 WB20 필수값(coCd)이 누락되었습니다.");
			}
			String pgmId = paramMap.get("pgmId");
			approvalMap.put("pgmId", pgmId);
			approvalMap.put("pgPath", approvalPgPath(pgmId));
			approvalMap.put("userId", paramMap.get("userId"));
			// TB_WB20M03.CO_CD는 필수키이므로 WB20 생성 호출에 반드시 전달한다.
			approvalMap.put("coCd", paramMap.get("coCd"));
			approvalMap.put("todoDiv1CodeId", isShareApproval(approvalMap) ? "TODODIV10" : "TODODIV20");
			approvalMap.put("todoCoCd", paramMap.get("coCd"));
			approvalMap.put("histNo", "");
			approvalMap.put("sanctnSttus", "N");
			if (!hasText(approvalMap.get("todoTitle"))) {
				approvalMap.put("todoTitle", approvalTitle(paramMap));
			}
			if (!hasText(approvalMap.get("todoTitl"))) {
				approvalMap.put("todoTitl", approvalMap.get("todoTitle"));
			}
		}
		// 신규 결재선 INSERT 시 기존 todoKey/toDoKey/TODO_KEY 속성을 제거하여 MyBatis selectKey가 유니크한 TODO_KEY를 항상 새로 생성하도록 함
		approvalMap.remove("todoKey");
		approvalMap.remove("toDoKey");
		approvalMap.remove("TODO_KEY");
	}

	private String approvalPgPath(String pgmId) {
		if ("PM5102P01".equals(pgmId)) {
			return "/user/pm/pm51/PM5102P01.html";
		}
		return "/user/pm/pm51/PM5101P01.html";
	}

	private String approvalTitle(Map<String, String> paramMap) {
		if (hasText(paramMap.get("approvalTitle"))) {
			return paramMap.get("approvalTitle");
		}
		if (hasText(paramMap.get("tripRptNo"))) {
			return paramMap.get("tripRptNo") + " 출장보고서";
		}
		return paramMap.get("tripReqNo") + " 출장신청서";
	}

	private boolean isShareApproval(Map<String, String> approvalMap) {
		String todoDiv2CodeId = approvalMap.get("todoDiv2CodeId");
		return "TODODIV1190".equals(todoDiv2CodeId)
				|| "TODODIV1191".equals(todoDiv2CodeId)
				|| "TODODIV1200".equals(todoDiv2CodeId)
				|| "TODODIV1201".equals(todoDiv2CodeId)
				|| "공유".equals(approvalMap.get("gb"));
	}

	// 결재/공유 그룹 우선순위: 일반결재(2190/2200) -> 관리부서결재(2191/2201)
	private int getApprovalGroupPriority(String todoDiv2CodeId) {
		if ("TODODIV2190".equals(todoDiv2CodeId) || "TODODIV2200".equals(todoDiv2CodeId)
				|| "TODODIV1190".equals(todoDiv2CodeId) || "TODODIV1200".equals(todoDiv2CodeId)) {
			return 1; // 1단계: 일반 결재선 및 일반 공유선 (1, 2, 3...)
		}
		if ("TODODIV2191".equals(todoDiv2CodeId) || "TODODIV2201".equals(todoDiv2CodeId)
				|| "TODODIV1191".equals(todoDiv2CodeId) || "TODODIV1201".equals(todoDiv2CodeId)) {
			return 2; // 2단계: 관리부서 결재선 및 관리부서 공유선 (1, 2, 3...)
		}
		return 9;
	}

	private void syncTripReqToAm(Map<String, String> paramMap) {
		try {
			String tripReqNo = paramMap.get("tripReqNo");
			if (!hasText(tripReqNo)) {
				tripReqNo = paramMap.get("reqNo");
			}
			if (!hasText(tripReqNo)) {
				return;
			}

			String coCd = hasText(paramMap.get("coCd")) ? paramMap.get("coCd") : "GUN";

			if (!hasText(paramMap.get("reqDt")) || !hasText(paramMap.get("creatDttm"))) {
				Map<String, String> qMap = new HashMap<>();
				qMap.put("tripReqNo", tripReqNo);
				Map<String, String> dbM01 = pm51Mapper.selectTripReqM01(qMap);
				if (dbM01 != null) {
					for (Map.Entry<String, String> entry : dbM01.entrySet()) {
						if (!hasText(paramMap.get(entry.getKey())) && entry.getValue() != null) {
							paramMap.put(entry.getKey(), String.valueOf(entry.getValue()));
						}
					}
				}
			}

			Map<String, String> wb20Query = new HashMap<>();
			wb20Query.put("todoNo", tripReqNo);
			wb20Query.put("coCd", coCd);
			List<Map<String, String>> wb20Lines = wb20Svc.selectGetApprovalList(wb20Query);
			if (wb20Lines == null || wb20Lines.isEmpty()) {
				return;
			}

			// 결재선(TODODIV20)과 공유선(TODODIV10) 분리 후 SANCTN_SN 오름차순 정렬
			List<Map<String, String>> apprList = new ArrayList<>();
			List<Map<String, String>> refList = new ArrayList<>();
			for (Map<String, String> row : wb20Lines) {
				if ("TODODIV10".equals(row.get("todoDiv1CodeId")) || "공유".equals(row.get("gb"))) {
					refList.add(row);
				} else {
					apprList.add(row);
				}
			}

			// 결재선 순서 보장: TODODIV2190 (일반결재 1,2,3...) -> TODODIV2191 (관리부서결재 1,2,3...)
			Collections.sort(apprList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int g1 = getApprovalGroupPriority(o1.get("todoDiv2CodeId"));
					int g2 = getApprovalGroupPriority(o2.get("todoDiv2CodeId"));
					if (g1 != g2) {
						return Integer.compare(g1, g2);
					}
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			// 공유선 순서 보장: TODODIV1190 (일반공유 1,2,3...) -> TODODIV1191 (관리부서공유 1,2,3...)
			Collections.sort(refList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int g1 = getApprovalGroupPriority(o1.get("todoDiv2CodeId"));
					int g2 = getApprovalGroupPriority(o2.get("todoDiv2CodeId"));
					if (g1 != g2) {
						return Integer.compare(g1, g2);
					}
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			List<Map<String, Object>> amLineList = new ArrayList<>();
			for (Map<String, String> row : apprList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", hasText(row.get("todoNm")) ? row.get("todoNm") : row.get("name"));
				amLine.put("deptId", row.get("deptId"));
				amLine.put("lineSeq", row.get("sanctnSn"));
					amLine.put("lineType", "APPR");
					amLine.put("wb20TodoKey", row.get("todoKey"));
					amLine.put("wb20CoCd", row.get("coCd"));
					amLine.put("wb20TodoNo", row.get("todoNo"));
					amLine.put("wb20SanctnSn", row.get("sanctnSn"));
					amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
					amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("sourceApproved", "Y".equalsIgnoreCase(row.get("sanctnSttus")) ? "Y" : "N");
				amLineList.add(amLine);
			}

			for (Map<String, String> row : refList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", hasText(row.get("todoNm")) ? row.get("todoNm") : row.get("name"));
				amLine.put("deptId", row.get("deptId"));
					amLine.put("lineSeq", row.get("sanctnSn"));
					amLine.put("lineType", "REF");
					amLine.put("wb20TodoKey", row.get("todoKey"));
					amLine.put("wb20CoCd", row.get("coCd"));
					amLine.put("wb20TodoNo", row.get("todoNo"));
					amLine.put("wb20SanctnSn", row.get("sanctnSn"));
					amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
					amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("sourceApproved", "N");
				amLineList.add(amLine);
			}

			if (amLineList.isEmpty()) {
				return;
			}

			int autoApprovedCount = 0;
			for (Map<String, Object> line : amLineList) {
				if (!"Y".equals(line.get("sourceApproved"))) break;
				autoApprovedCount++;
			}

			String applicantId = resolveOriginalRequesterId(paramMap);
			if (!hasText(applicantId)) {
				applicantId = paramMap.get("reqId");
			}
			if (!hasText(applicantId)) {
				applicantId = paramMap.get("userId");
			}

			String applicantNm = paramMap.get("reqNm");
			if (!hasText(applicantNm)) {
				applicantNm = paramMap.get("userNm");
			}

			String deptNm = paramMap.get("deptNm");
			String levelNm = paramMap.get("levelNm");

			if (hasText(applicantId) && (!hasText(applicantNm) || !hasText(deptNm) || !hasText(levelNm))) {
				Map<String, String> uParam = new HashMap<>();
				uParam.put("userId", applicantId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(uParam);
				if (uInfo != null) {
					if (!hasText(applicantNm)) applicantNm = uInfo.get("name");
					if (!hasText(deptNm)) deptNm = uInfo.get("deptNm");
					if (!hasText(levelNm)) levelNm = uInfo.get("levelNm");
				}
			}

			Map<String, String> docIdParam = new HashMap<>();
			docIdParam.put("tripReqNo", tripReqNo);
			docIdParam.put("coCd", coCd);
			String existingDocId = pm51Mapper.selectAmDocIdByTripReqNo(docIdParam);

			Map<String, Object> amParam = new HashMap<>();
			if (hasText(existingDocId)) {
				amParam.put("docId", existingDocId);
			}
			amParam.put("coCd", coCd);
			amParam.put("userId", applicantId);
			amParam.put("userNm", applicantNm);
			amParam.put("deptNm", deptNm);
			amParam.put("levelNm", levelNm);
			amParam.put("docTitle", buildTripReqApprovalTitle(paramMap));
			amParam.put("formCd", "PM5101");
			amParam.put("formVer", 1);
			amParam.put("erpBizType", "PM51");
			amParam.put("erpBizKey", tripReqNo);
			amParam.put("docDataJson", new GsonBuilder().disableHtmlEscaping().create().toJson(paramMap));
			amParam.put("docRenderHtml", buildTripReqApprovalHtml(paramMap));
			amParam.put("pgmId", "PM5101P01");
			amParam.put("lineList", amLineList);
			amParam.put("autoApprovedCount", autoApprovedCount);

			Map<String, Object> amResult = am11Svc.submitApproval(amParam);
			if (!"200".equals(String.valueOf(amResult.get("resultCode")))) {
				if (amParam.get("docId") != null) {
					amParam.put("changeReason", "PM51 출장신청서 수정 동기화");
					am11Svc.changeApprovalLines(amParam);
				} else {
					throw new IllegalStateException("AM 결재문서 자동등록 실패: " + amResult.get("resultMessage"));
				}
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException("전자결재 문서 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private String buildTripReqApprovalTitle(Map<String, String> paramMap) {
		String applicant = paramMap.get("reqNm");
		if (!hasText(applicant)) applicant = paramMap.get("userNm");
		if (!hasText(applicant)) applicant = paramMap.get("reqId");
		if (!hasText(applicant)) applicant = "";
		String tripPlace = paramMap.get("tripPlace");
		if (hasText(tripPlace)) {
			return applicant.trim() + " 출장신청서 (" + tripPlace.trim() + ")";
		}
		return applicant.trim() + " 출장신청서";
	}

	// 공통코드 코드값 -> 코드명 변환 (전자결재 문서 표시용). 조회 실패 시 원본 코드값 반환.
	private String resolveCodeNm(String codeId) {
		if (!hasText(codeId)) return "";
		try {
			Map<String, String> codeMap = new HashMap<>();
			codeMap.put("codeId", codeId);
			Map<String, String> codeDetail = cm05Svc.selectCodeInfo(codeMap);
			if (codeDetail != null && hasText(codeDetail.get("codeNm"))) {
				return codeDetail.get("codeNm");
			}
		} catch (Exception e) {
			logger.warn("공통코드 코드명 조회 실패: codeId={}, error={}", codeId, e.getMessage());
		}
		return codeId;
	}

	private String buildTripReqApprovalHtml(Map<String, String> paramMap) {
		String reqDt = formatDateDisplay(paramMap.get("reqDt"));
		if (!hasText(reqDt)) {
			reqDt = formatDateDisplay(paramMap.get("creatDttm"));
		}
		if (!hasText(reqDt)) {
			reqDt = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
		}
		String reqNm = hasText(paramMap.get("reqNm")) ? paramMap.get("reqNm") : paramMap.get("userNm");
		String pmNm = paramMap.get("pmNm");
		String salesCd = paramMap.get("salesCd");
		String clntPjtNm = hasText(paramMap.get("clntPjtNm")) ? paramMap.get("clntPjtNm") : paramMap.get("clntPjt");
		String tripDiv = paramMap.get("tripDiv");
		String tripNationNm = paramMap.get("tripNationNm");
		String tripPlace = paramMap.get("tripPlace");
		String tripStDtm = formatDateDisplay(paramMap.get("tripStDtm"));
		String tripEdDtm = formatDateDisplay(paramMap.get("tripEdDtm"));
		String equipNm = paramMap.get("equipNm");
		String tripPurpose = paramMap.get("tripPurpose");

		StringBuilder html = new StringBuilder();
		html.append("<div class=\"approval-document\" style=\"font-size: 13px; line-height: 1.6;\">");
		html.append("<h3 style=\"text-align: center; margin-bottom: 20px; font-weight: bold;\">출장신청서</h3>");
		html.append("<table class=\"table table-bordered\" style=\"width: 100%; border-collapse: collapse;\">");
		html.append("<colgroup><col style=\"width: 15%;\"><col style=\"width: 35%;\"><col style=\"width: 15%;\"><col style=\"width: 35%;\"></colgroup>");
		html.append("<tr><th>신청서번호</th><td>").append(escapeHtml(paramMap.get("tripReqNo"))).append("</td>");
		html.append("<th>신청일자</th><td>").append(escapeHtml(reqDt)).append("</td></tr>");
		html.append("<tr><th>신청인</th><td>").append(escapeHtml(reqNm)).append("</td>");
		html.append("<th>영업PM</th><td>").append(escapeHtml(pmNm)).append("</td></tr>");
		html.append("<tr><th>Sales Code</th><td>").append(escapeHtml(salesCd)).append("</td>");
		html.append("<th>프로젝트명</th><td>").append(escapeHtml(clntPjtNm)).append("</td></tr>");

		// 참여 프로젝트 목록 조회 및 렌더
		List<Map<String, String>> projectList = null;
		try {
			String tripReqNo = paramMap.get("tripReqNo");
			if (hasText(tripReqNo)) {
				Map<String, String> qMap = new HashMap<>();
				qMap.put("tripReqNo", tripReqNo);
				projectList = pm51Mapper.selectTripReqD03(qMap);
			}
		} catch (Exception e) {
			logger.warn("출장신청서 참여 프로젝트 조회 실패: tripReqNo={}, error={}", paramMap.get("tripReqNo"), e.getMessage());
		}
		String projectTableHtml = buildProjectListTable(projectList);
		if (!projectTableHtml.isEmpty()) {
			html.append("<tr><th>참여 프로젝트</th><td colspan=\"3\">").append(projectTableHtml).append("</td></tr>");
		}

		html.append("<tr><th>출장구분</th><td>").append(escapeHtml(resolveCodeNm(tripDiv)));
		if (hasText(tripNationNm)) {
			html.append(" (").append(escapeHtml(tripNationNm)).append(")");
		}
		html.append("</td><th>출장기간</th><td>").append(escapeHtml(tripStDtm));
		if (hasText(tripStDtm) || hasText(tripEdDtm)) {
			html.append(" ~ ").append(escapeHtml(tripEdDtm));
		}
		html.append("</td></tr>");
		html.append("<tr><th>출장지</th><td colspan=\"3\">").append(escapeHtml(tripPlace)).append("</td></tr>");
		html.append("<tr><th>장비명</th><td colspan=\"3\">").append(escapeHtml(equipNm)).append("</td></tr>");
		html.append("<tr><th>출장목적</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(tripPurpose)).append("</td></tr>");

		// 출장자별 기간 조회 및 렌더
		List<Map<String, String>> travelerList = null;
		try {
			String tripReqNo = paramMap.get("tripReqNo");
			if (hasText(tripReqNo)) {
				Map<String, String> qMap = new HashMap<>();
				qMap.put("tripReqNo", tripReqNo);
				travelerList = pm51Mapper.selectTripReqD01(qMap);
			}
		} catch (Exception e) {
			logger.warn("출장신청서 출장자 조회 실패: tripReqNo={}, error={}", paramMap.get("tripReqNo"), e.getMessage());
		}
		if (travelerList != null && !travelerList.isEmpty()) {
			html.append("<tr><th>출장자</th><td colspan=\"3\"><table class=\"table table-bordered\" style=\"width:100%; border-collapse:collapse; margin-bottom:0;\">");
			html.append("<colgroup><col style=\"width:25%;\"><col style=\"width:25%;\"><col style=\"width:25%;\"><col style=\"width:25%;\"></colgroup>");
			html.append("<tr><th>성명</th><th>소속</th><th>직급</th><th>출장기간</th></tr>");
			for (Map<String, String> traveler : travelerList) {
				String travelerPeriod = "";
				String stDtm = traveler.get("tripStDtm");
				String edDtm = traveler.get("tripEdDtm");
				if (hasText(stDtm) || hasText(edDtm)) {
					travelerPeriod = formatDateDisplay(stDtm);
					if (hasText(edDtm)) {
						travelerPeriod += " ~ " + formatDateDisplay(edDtm);
					}
				}
				html.append("<tr><td>").append(escapeHtml(traveler.get("userNm"))).append("</td>");
				html.append("<td>").append(escapeHtml(traveler.get("deptNm"))).append("</td>");
				html.append("<td>").append(escapeHtml(traveler.get("positionNm"))).append("</td>");
				html.append("<td>").append(escapeHtml(travelerPeriod)).append("</td></tr>");
			}
			html.append("</table></td></tr>");
		}

		html.append("</table></div>");
		return html.toString();
	}

	private void syncTripRptToAm(Map<String, String> paramMap) {
		try {
			String tripRptNo = paramMap.get("tripRptNo");
			if (!hasText(tripRptNo)) {
				tripRptNo = paramMap.get("reqNo");
			}
			if (!hasText(tripRptNo)) {
				return;
			}

			String coCd = hasText(paramMap.get("coCd")) ? paramMap.get("coCd") : "GUN";

			if (!hasText(paramMap.get("rptDt")) || !hasText(paramMap.get("creatDttm"))) {
				Map<String, String> qMap = new HashMap<>();
				qMap.put("tripRptNo", tripRptNo);
				Map<String, String> dbM02 = pm51Mapper.selectTripRptM01(qMap);
				if (dbM02 != null) {
					for (Map.Entry<String, String> entry : dbM02.entrySet()) {
						if (!hasText(paramMap.get(entry.getKey())) && entry.getValue() != null) {
							paramMap.put(entry.getKey(), String.valueOf(entry.getValue()));
						}
					}
				}
			}

			Map<String, String> wb20Query = new HashMap<>();
			wb20Query.put("todoNo", tripRptNo);
			wb20Query.put("coCd", coCd);
			List<Map<String, String>> wb20Lines = wb20Svc.selectGetApprovalList(wb20Query);
			if (wb20Lines == null || wb20Lines.isEmpty()) {
				return;
			}

			// 결재선(TODODIV20)과 공유선(TODODIV10) 분리
			List<Map<String, String>> apprList = new ArrayList<>();
			List<Map<String, String>> refList = new ArrayList<>();
			for (Map<String, String> row : wb20Lines) {
				if ("TODODIV10".equals(row.get("todoDiv1CodeId")) || "공유".equals(row.get("gb"))) {
					refList.add(row);
				} else {
					apprList.add(row);
				}
			}

			// 결재선 순서 보장: TODODIV2200 (일반결재 1,2,3...) -> TODODIV2201 (관리부서결재 1,2,3...)
			Collections.sort(apprList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int g1 = getApprovalGroupPriority(o1.get("todoDiv2CodeId"));
					int g2 = getApprovalGroupPriority(o2.get("todoDiv2CodeId"));
					if (g1 != g2) {
						return Integer.compare(g1, g2);
					}
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			// 공유선 순서 보장: TODODIV1200 (일반공유 1,2,3...) -> TODODIV1201 (관리부서공유 1,2,3...)
			Collections.sort(refList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int g1 = getApprovalGroupPriority(o1.get("todoDiv2CodeId"));
					int g2 = getApprovalGroupPriority(o2.get("todoDiv2CodeId"));
					if (g1 != g2) {
						return Integer.compare(g1, g2);
					}
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			List<Map<String, Object>> amLineList = new ArrayList<>();
			for (Map<String, String> row : apprList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", hasText(row.get("todoNm")) ? row.get("todoNm") : row.get("name"));
				amLine.put("deptId", row.get("deptId"));
				amLine.put("lineSeq", row.get("sanctnSn"));
				amLine.put("lineType", "APPR");
				amLine.put("wb20TodoKey", row.get("todoKey"));
				amLine.put("wb20CoCd", row.get("coCd"));
				amLine.put("wb20TodoNo", row.get("todoNo"));
				amLine.put("wb20SanctnSn", row.get("sanctnSn"));
				amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
				amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("sourceApproved", "Y".equalsIgnoreCase(row.get("sanctnSttus")) ? "Y" : "N");
				amLineList.add(amLine);
			}

			for (Map<String, String> row : refList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", hasText(row.get("todoNm")) ? row.get("todoNm") : row.get("name"));
				amLine.put("deptId", row.get("deptId"));
				amLine.put("lineSeq", row.get("sanctnSn"));
				amLine.put("lineType", "REF");
				amLine.put("wb20TodoKey", row.get("todoKey"));
				amLine.put("wb20CoCd", row.get("coCd"));
				amLine.put("wb20TodoNo", row.get("todoNo"));
				amLine.put("wb20SanctnSn", row.get("sanctnSn"));
				amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
				amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("sourceApproved", "N");
				amLineList.add(amLine);
			}

			if (amLineList.isEmpty()) {
				return;
			}

			int autoApprovedCount = 0;
			for (Map<String, Object> line : amLineList) {
				if (!"Y".equals(line.get("sourceApproved"))) break;
				autoApprovedCount++;
			}

			String applicantId = resolveOriginalRequesterId(paramMap);
			if (!hasText(applicantId)) applicantId = paramMap.get("reqId");
			if (!hasText(applicantId)) applicantId = paramMap.get("userId");

			String applicantNm = paramMap.get("reqNm");
			if (!hasText(applicantNm)) applicantNm = paramMap.get("userNm");

			String deptNm = paramMap.get("deptNm");
			String levelNm = paramMap.get("levelNm");

			if (hasText(applicantId) && (!hasText(applicantNm) || !hasText(deptNm) || !hasText(levelNm))) {
				Map<String, String> uParam = new HashMap<>();
				uParam.put("userId", applicantId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(uParam);
				if (uInfo != null) {
					if (!hasText(applicantNm)) applicantNm = uInfo.get("name");
					if (!hasText(deptNm)) deptNm = uInfo.get("deptNm");
					if (!hasText(levelNm)) levelNm = uInfo.get("levelNm");
				}
			}

			Map<String, String> docIdParam = new HashMap<>();
			docIdParam.put("tripReqNo", tripRptNo);
			docIdParam.put("coCd", coCd);
			String existingDocId = pm51Mapper.selectAmDocIdByTripReqNo(docIdParam);

			Map<String, Object> amParam = new HashMap<>();
			if (hasText(existingDocId)) {
				amParam.put("docId", existingDocId);
			}
			amParam.put("coCd", coCd);
			amParam.put("userId", applicantId);
			amParam.put("userNm", applicantNm);
			amParam.put("deptNm", deptNm);
			amParam.put("levelNm", levelNm);
			amParam.put("docTitle", buildTripRptApprovalTitle(paramMap));
			amParam.put("formCd", "PM5102");
			amParam.put("formVer", 1);
			amParam.put("erpBizType", "PM52");
			amParam.put("erpBizKey", tripRptNo);
			amParam.put("docDataJson", new GsonBuilder().disableHtmlEscaping().create().toJson(paramMap));
			amParam.put("docRenderHtml", buildTripRptApprovalHtml(paramMap));
			amParam.put("pgmId", "PM5102P01");
			amParam.put("lineList", amLineList);
			amParam.put("autoApprovedCount", autoApprovedCount);

			Map<String, Object> amResult = am11Svc.submitApproval(amParam);
			if (!"200".equals(String.valueOf(amResult.get("resultCode")))) {
				if (amParam.get("docId") != null) {
					amParam.put("changeReason", "PM51 출장복명서 수정 동기화");
					am11Svc.changeApprovalLines(amParam);
				} else {
					throw new IllegalStateException("AM 복명서 결재문서 자동등록 실패: " + amResult.get("resultMessage"));
				}
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException("전자결재 복명서 문서 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private String buildTripRptApprovalTitle(Map<String, String> paramMap) {
		String applicant = paramMap.get("reqNm");
		if (!hasText(applicant)) applicant = paramMap.get("userNm");
		if (!hasText(applicant)) applicant = paramMap.get("reqId");
		if (!hasText(applicant)) applicant = "";
		String tripPlace = paramMap.get("tripPlace");
		if (hasText(tripPlace)) {
			return applicant.trim() + " 출장복명서 (" + tripPlace.trim() + ")";
		}
		return applicant.trim() + " 출장복명서";
	}

	private String buildTripRptApprovalHtml(Map<String, String> paramMap) {
		String rptDt = formatDateDisplay(paramMap.get("rptDt"));
		if (!hasText(rptDt)) {
			rptDt = formatDateDisplay(paramMap.get("creatDttm"));
		}
		if (!hasText(rptDt)) {
			rptDt = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
		}
		String reqNm = hasText(paramMap.get("reqNm")) ? paramMap.get("reqNm") : paramMap.get("userNm");
		String pmNm = paramMap.get("pmNm");
		String salesCd = paramMap.get("salesCd");
		String clntPjtNm = hasText(paramMap.get("clntPjtNm")) ? paramMap.get("clntPjtNm") : paramMap.get("clntPjt");
		String tripPlace = paramMap.get("tripPlace");
		String actStDtm = formatDateDisplay(paramMap.get("actStDtm"));
		String actEdDtm = formatDateDisplay(paramMap.get("actEdDtm"));
		String rptContent = paramMap.get("rptContent");
		String tripResult = paramMap.get("tripResult");
		String specialNote = paramMap.get("specialNote");
		String extendRsn = paramMap.get("extendRsn");

		// 복명서 M01 백필: tripResult/specialNote/extendRsn/actStDtm/actEdDtm 중 누락분 조회
		if ((!hasText(tripResult) || !hasText(specialNote) || !hasText(extendRsn) || !hasText(actStDtm) || !hasText(actEdDtm))) {
			try {
				String tripRptNo = paramMap.get("tripRptNo");
				if (hasText(tripRptNo)) {
					Map<String, String> qMap = new HashMap<>();
					qMap.put("tripRptNo", tripRptNo);
					Map<String, String> m01 = pm51Mapper.selectTripRptM01(qMap);
					if (m01 != null) {
						if (!hasText(tripResult)) tripResult = m01.get("tripResult");
						if (!hasText(specialNote)) specialNote = m01.get("specialNote");
						if (!hasText(extendRsn)) extendRsn = m01.get("extendRsn");
						if (!hasText(actStDtm)) actStDtm = formatDateDisplay(m01.get("actStDtm"));
						if (!hasText(actEdDtm)) actEdDtm = formatDateDisplay(m01.get("actEdDtm"));
					}
				}
			} catch (Exception e) {
				logger.warn("출장복명서 마스터 정보 백필 실패: tripRptNo={}, error={}", paramMap.get("tripRptNo"), e.getMessage());
			}
		}

		StringBuilder html = new StringBuilder();
		html.append("<div class=\"approval-document\" style=\"font-size: 13px; line-height: 1.6;\">");
		html.append("<h3 style=\"text-align: center; margin-bottom: 20px; font-weight: bold;\">출장복명서</h3>");
		html.append("<table class=\"table table-bordered\" style=\"width: 100%; border-collapse: collapse;\">");
		html.append("<colgroup><col style=\"width: 15%;\"><col style=\"width: 35%;\"><col style=\"width: 15%;\"><col style=\"width: 35%;\"></colgroup>");
		html.append("<tr><th>복명서번호</th><td>").append(escapeHtml(paramMap.get("tripRptNo"))).append("</td>");
		html.append("<th>복명일자</th><td>").append(escapeHtml(rptDt)).append("</td></tr>");
		html.append("<tr><th>출장자</th><td>").append(escapeHtml(reqNm)).append("</td>");
		html.append("<th>영업PM</th><td>").append(escapeHtml(pmNm)).append("</td></tr>");
		html.append("<tr><th>Sales Code</th><td>").append(escapeHtml(salesCd)).append("</td>");
		html.append("<th>프로젝트명</th><td>").append(escapeHtml(clntPjtNm)).append("</td></tr>");

		// 참여 프로젝트 목록 조회 및 렌더
		List<Map<String, String>> projectList = null;
		try {
			String tripRptNo = paramMap.get("tripRptNo");
			if (hasText(tripRptNo)) {
				Map<String, String> qMap = new HashMap<>();
				qMap.put("tripRptNo", tripRptNo);
				projectList = pm51Mapper.selectTripRptD03(qMap);
			}
		} catch (Exception e) {
			logger.warn("출장복명서 참여 프로젝트 조회 실패: tripRptNo={}, error={}", paramMap.get("tripRptNo"), e.getMessage());
		}
		String projectTableHtml = buildProjectListTable(projectList);
		if (!projectTableHtml.isEmpty()) {
			html.append("<tr><th>참여 프로젝트</th><td colspan=\"3\">").append(projectTableHtml).append("</td></tr>");
		}

		html.append("<tr><th>실제출장기간</th><td colspan=\"3\">").append(escapeHtml(actStDtm));
		if (hasText(actStDtm) || hasText(actEdDtm)) {
			html.append(" ~ ").append(escapeHtml(actEdDtm));
		}
		html.append("</td></tr>");
		html.append("<tr><th>출장지</th><td colspan=\"3\">").append(escapeHtml(tripPlace)).append("</td></tr>");
		html.append("<tr><th>복명내용</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(rptContent)).append("</td></tr>");
		html.append("<tr><th>출장결과</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(tripResult)).append("</td></tr>");
		html.append("<tr><th>특기사항</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(specialNote)).append("</td></tr>");
		html.append("<tr><th>출장연장사유</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(extendRsn)).append("</td></tr>");
		html.append("</table></div>");
		return html.toString();
	}

	private String formatDateDisplay(String dtm) {
		if (!hasText(dtm)) return "";
		String clean = dtm.trim().replace("-", "");
		if (clean.length() >= 8) {
			return clean.substring(0, 4) + "-" + clean.substring(4, 6) + "-" + clean.substring(6, 8);
		}
		return dtm;
	}

	private String escapeHtml(String value) {
		if (value == null) return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

	private int parseIntSafe(String val) {
		if (val == null || val.trim().isEmpty()) return 0;
		try {
			return Integer.parseInt(val.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	private String buildProjectListTable(List<Map<String, String>> projectList) {
		if (projectList == null || projectList.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"table table-bordered\" style=\"width:100%; border-collapse:collapse; margin-bottom:0;\">");
		sb.append("<colgroup><col style=\"width:15%;\"><col style=\"width:20%;\"><col style=\"width:15%;\"><col style=\"width:15%;\"><col style=\"width:20%;\"><col style=\"width:15%;\"></colgroup>");
		sb.append("<tr><th>프로젝트명</th><th>고객사</th><th>설비</th><th>PM</th><th>프로젝트기간</th><th>비고</th></tr>");
		for (Map<String, String> project : projectList) {
			String projectPeriod = "";
			String stDt = project.get("stDt");
			String edDt = project.get("edDt");
			if (hasText(stDt) || hasText(edDt)) {
				projectPeriod = formatDateDisplay(stDt);
				if (hasText(edDt)) {
					projectPeriod += " ~ " + formatDateDisplay(edDt);
				}
			}
			sb.append("<tr><td>").append(escapeHtml(project.get("clntPjtNm"))).append("</td>");
			sb.append("<td>").append(escapeHtml(project.get("clntNm"))).append("</td>");
			sb.append("<td>").append(escapeHtml(project.get("equipNm"))).append("</td>");
			sb.append("<td>").append(escapeHtml(project.get("pmNm"))).append("</td>");
			sb.append("<td>").append(escapeHtml(projectPeriod)).append("</td>");
			sb.append("<td>").append(escapeHtml(project.get("etc"))).append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	@Override
	public Map<String, Object> selectTripRptPaySummary(Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		result.put("expenseSums", pm51Mapper.selectTripRptPayExpenseSum(paramMap));
		result.put("eatCntSum", pm51Mapper.selectTripRptEatCntSum(paramMap));
		result.put("travelerExpenseSum", pm51Mapper.selectTripRptTravelerExpenseSum(paramMap));
		return result;
	}

	@Override
	public List<Map<String, Object>> selectTripExpenseStatusList(Map<String, Object> paramMap) {
		Object ids = paramMap.get("userIds");
		if (!(ids instanceof List) || ((List<?>) ids).isEmpty()) {
			return new ArrayList<>();
		}
		return pm51Mapper.selectTripExpenseStatusList(paramMap);
	}

	@Override
	public int updateTripExpenseStatus(Map<String, Object> paramMap) throws Exception {
		return pm51Mapper.updateTripExpenseStatus(paramMap);
	}

	@Override
	public int updateTripRptAcctSettle(Map<String, Object> paramMap) throws Exception {
		String tripRptNo = (String) paramMap.get("tripRptNo");
		String userId = (String) paramMap.get("userId");

		Map<String, String> m02Param = new HashMap<>();
		m02Param.put("tripRptNo", tripRptNo);
		Map<String, String> m02 = pm51Mapper.selectTripRptM01(m02Param);
		if (m02 == null) {
			throw new RuntimeException("출장복명서 정보를 찾을 수 없습니다.");
		}

		Map<String, String> reqParam = new HashMap<>();
		reqParam.put("tripReqNo", m02.get("tripReqNo"));
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(reqParam);
		if (m01 == null) {
			throw new RuntimeException("출장신청서 정보를 찾을 수 없습니다.");
		}
		if (hasText(m02.get("payDt"))) {
			throw new RuntimeException("이미 지급완료 처리된 출장복명서는 수정할 수 없습니다.");
		}

		// 회계담당자 권한 검증
		String[] acctApprovers = selectAcctMngApproverIds();
		boolean isAcctUser = false;
		for (String approverId : acctApprovers) {
			if (hasText(approverId) && approverId.trim().equals(userId)) {
				isAcctUser = true;
				break;
			}
		}
		if (!isAcctUser) {
			throw new RuntimeException("회계담당자만 정산정보를 수정할 수 있습니다.");
		}

		// 정산 금액 저장
		int result = pm51Mapper.updateTripRptAcctSettle(paramMap);

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripRptNo", tripRptNo);
		pm51Mapper.deleteTripRptD01(delParam);

		// 출장자별 일비 및 일정 상세 (TB_PM52D02) 저장 (전송된 경우)
		if (paramMap.containsKey("rptTravelerArr")) {
			String rptTravelerArrStr = (String) paramMap.get("rptTravelerArr");
			if (hasText(rptTravelerArrStr)) {
				List<Map<String, String>> rptTravelerArr = gsonDtl.fromJson(rptTravelerArrStr, dtlMap);
				if (rptTravelerArr != null && !rptTravelerArr.isEmpty()) {
					pm51Mapper.deleteTripRptD02(delParam);
					for (Map<String, String> travelerMap : rptTravelerArr) {
						travelerMap.put("tripRptNo", tripRptNo);
						travelerMap.put("coCd", (String) paramMap.get("coCd"));
						pm51Mapper.insertTripRptD02(travelerMap);
					}
				}
			}
		}

		if (paramMap.containsKey("expenseDtlArr")) {
			String expenseDtlArrStr = (String) paramMap.get("expenseDtlArr");
			if (hasText(expenseDtlArrStr)) {
				List<Map<String, String>> expenseDtlArr = gsonDtl.fromJson(expenseDtlArrStr, dtlMap);
				if (expenseDtlArr != null && !expenseDtlArr.isEmpty()) {
					for (Map<String, String> expenseDtlMap : expenseDtlArr) {
						expenseDtlMap.put("tripRptNo", tripRptNo);
						expenseDtlMap.put("userId", userId);
						expenseDtlMap.put("pgmId", (String) paramMap.get("pgmId"));
						pm51Mapper.insertTripRptD01(expenseDtlMap);

						// 회계담당자 입력값을 원본 출장경비(TB_PM01D01)에도 동기화 (점유(TRIP_RPT_NO)는 지급완료 시점에만 처리)
						pm51Mapper.updateTripExpenseStatus(new HashMap<String, Object>(expenseDtlMap));
					}
				}
			}
		}

		// 카드사 명세 (요청에 전송된 경우에만 처리, "[]"는 전체 삭제)
		if (paramMap.get("rptCardArr") != null) {
			Map<String, String> cardDelParam = new HashMap<>();
			cardDelParam.put("tripRptNo", tripRptNo);
			cardDelParam.put("reqNo", tripRptNo);
			pm51Mapper.deleteTripRptD04(cardDelParam);

			List<Map<String, String>> rptCardArr = gsonDtl.fromJson((String) paramMap.get("rptCardArr"), dtlMap);
			if (rptCardArr != null && !rptCardArr.isEmpty()) {
				for (Map<String, String> cardMap : rptCardArr) {
					cardMap.put("tripRptNo", tripRptNo);
					cardMap.put("userId", userId);
					cardMap.put("pgmId", "PM5102P01");
					pm51Mapper.insertTripRptD04(cardMap);
				}
			}
		}

		// 일비정산 통화별 상세 (요청에 전송된 경우에만 처리, "[]"는 전체 삭제)
		if (paramMap.get("rptDailyArr") != null) {
			pm51Mapper.deleteTripRptD05(delParam);

			List<Map<String, String>> rptDailyArr = gsonDtl.fromJson((String) paramMap.get("rptDailyArr"), dtlMap);
			if (rptDailyArr != null && !rptDailyArr.isEmpty()) {
				for (Map<String, String> dailyMap : rptDailyArr) {
					dailyMap.put("tripRptNo", tripRptNo);
					dailyMap.put("creatId", userId);
					dailyMap.put("pgmId", "PM5102P01");
					pm51Mapper.insertTripRptD05(dailyMap);
				}
			}
		}

		boolean isPayFlow = "Y".equals(paramMap.get("isPayFlow")) || "PAY".equals(paramMap.get("saveMode"));
		boolean isApproveFlow = "Y".equals(paramMap.get("isApproveFlow")) || "APPROVE".equals(paramMap.get("saveMode"));

		// 3. 결재 승인 처리 (isApproveFlow인 경우에만 수행)
		if (isApproveFlow) {
			Map<String, String> approveReq = new HashMap<>();
			approveReq.put("tripRptNo", tripRptNo);
			approveReq.put("userId", userId);
			approveReq.put("userNm", paramMap.get("userNm") != null ? String.valueOf(paramMap.get("userNm")) : null);
			approveReq.put("pgmId", paramMap.get("pgmId") != null ? String.valueOf(paramMap.get("pgmId")) : "PM5102P01");
			approveReq.put("apprOpinion", "회계정산 결재");
			approveTripRptApprovalLine(approveReq);
		}

		// 4. 지급완료 처리 (isPayFlow인 경우에만 수행)
		if (isPayFlow) {
			// 일반결재 완료 여부 검증 (DB 최신 상태 재조회)
			m02 = pm51Mapper.selectTripRptM01(m02Param);
			if (m02 == null) {
				throw new RuntimeException("출장복명서 정보를 찾을 수 없습니다.");
			}

			// 일반결재 완료 여부 검증 (TODODIV2200 신청부서 일반결재선 전원 승인 완료 여부 - PM5101 동일)
			validateTripRptGeneralApprovalDone(tripRptNo);
			if (hasText(m02.get("payDt"))) {
				throw new RuntimeException("이미 지급완료 처리된 출장복명서입니다.");
			}

			// 지급확정 전 점유 충돌 검사: 다른 복명서가 이미 점유한 경비가 있으면 전체 중단
			List<Map<String, String>> payTargetRows = new ArrayList<>();
			String payTargetArrStr = (String) paramMap.get("expenseDtlArr");
			if (hasText(payTargetArrStr)) {
				List<Map<String, String>> parsedRows = gsonDtl.fromJson(payTargetArrStr, dtlMap);
				if (parsedRows != null) {
					payTargetRows.addAll(parsedRows);
				}
			}
			if (!payTargetRows.isEmpty()) {
				Map<String, Object> occupiedParam = new HashMap<>();
				occupiedParam.put("tripRptNo", tripRptNo);
				occupiedParam.put("rows", payTargetRows);
				List<Map<String, Object>> occupiedList = pm51Mapper.selectTripExpenseOccupiedByOther(occupiedParam);
				if (occupiedList != null && !occupiedList.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("다른 복명서가 이미 점유한 출장경비가 ").append(occupiedList.size()).append("건 있어 지급확정을 중단했습니다.\n");
					int printed = 0;
					for (Map<String, Object> occupied : occupiedList) {
						if (printed >= 5) {
							sb.append("\n... 외 ").append(occupiedList.size() - printed).append("건");
							break;
						}
						sb.append("\n- ").append(occupied.get("workRptDt"))
							.append(" / 금액 ").append(occupied.get("tripRptAmt"))
							.append(" / 복명서 ").append(occupied.get("tripRptNo"));
						printed++;
					}
					sb.append("\n\n경비내역을 재조회한 뒤 다시 확인해 주세요.");
					throw new RuntimeException(sb.toString());
				}
			}

			// 자금담당자(SPECRTS15) 본인 결재처리 시점에만 복명서 지급완료 처리
			Map<String, String> payDoneParam = new HashMap<>();
			payDoneParam.put("tripRptNo", tripRptNo);
			payDoneParam.put("userId", userId);
			payDoneParam.put("pgmId", (String) paramMap.get("pgmId"));
			pm51Mapper.updateTripRptPayDone(payDoneParam);

			// 지급완료 시점에만 출장경비(TB_PM01D01) 점유
			Map<String, String> payClearParam = new HashMap<>();
			payClearParam.put("tripRptNo", tripRptNo);
			payClearParam.put("userId", userId);
			payClearParam.put("pgmId", (String) paramMap.get("pgmId"));
			pm51Mapper.updateTripExpenseStatusClearForPay(payClearParam);

			for (Map<String, String> payRow : payTargetRows) {
				payRow.put("tripRptNo", tripRptNo);
				payRow.put("userId", userId);
				payRow.put("pgmId", (String) paramMap.get("pgmId"));
				pm51Mapper.updateTripExpenseStatusLinkForPay(payRow);
			}
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> approveTripRptApprovalLine(Map<String, String> paramMap) throws Exception {
		String tripRptNo = paramMap.get("tripRptNo");
		String userId = paramMap.get("userId");
		String pgmId = paramMap.get("pgmId") != null ? paramMap.get("pgmId") : "PM5102P01";

		Map<String, Object> returnMap = new HashMap<>();

		// AM 전자결재 연동 문서(PM52)가 존재하는 경우 AM 승인을 우선 호출하여 WB20 결재선과 함께 자동 승인 연계한다.
		Map<String, String> settleDocParam = new HashMap<>();
		settleDocParam.put("tripReqNo", tripRptNo);
		Map<String, String> settleAmDoc = pm51Mapper.selectAmDocInfoByTripReqNo(settleDocParam);

		boolean settleAmApproved = false;
		if (settleAmDoc != null && hasText(settleAmDoc.get("docId"))) {
			Map<String, Object> approvalParam = new HashMap<>();
			approvalParam.put("docId", settleAmDoc.get("docId"));
			approvalParam.put("coCd", settleAmDoc.get("coCd"));
			approvalParam.put("userId", userId);
			String approverNm = paramMap.get("userNm");
			if (!hasText(approverNm)) {
				Map<String, String> uParam = new HashMap<>();
				uParam.put("userId", userId);
				Map<String, String> uInfo = cm06Mapper.selectUserInfo(uParam);
				if (uInfo != null) {
					approverNm = uInfo.get("name");
				}
			}
			approvalParam.put("userNm", approverNm);
			approvalParam.put("pgmId", pgmId);
			approvalParam.put("apprOpinion", paramMap.get("apprOpinion") != null ? paramMap.get("apprOpinion") : "승인");
			Map<String, Object> amRes = am11Svc.approveDocument(approvalParam);
			if (amRes != null && "200".equals(String.valueOf(amRes.get("resultCode")))) {
				settleAmApproved = true;
				returnMap.put("resultCode", 200);
				returnMap.put("resultMessage", "결재 승인되었습니다.");
				returnMap.put("result", amRes);
				return returnMap;
			} else {
				throw new RuntimeException(amRes != null ? String.valueOf(amRes.get("resultMessage")) : "전자결재 승인 처리에 실패했습니다.");
			}
		}

		if (!settleAmApproved) {
			String todoDiv2CodeId = paramMap.get("todoDiv2CodeId");
			if (!hasText(todoDiv2CodeId)) {
				todoDiv2CodeId = "TODODIV2201";
			}
			Map<String, String> approvalLineParam = new HashMap<>();
			approvalLineParam.put("todoNo", tripRptNo);
			approvalLineParam.put("todoDiv2CodeId", todoDiv2CodeId);
			List<Map<String, String>> approvalLines = wb20Svc.selectGetApprovalList(approvalLineParam);
			boolean approved = false;
			if (approvalLines != null) {
				for (Map<String, String> line : approvalLines) {
					if (userId.equals(line.get("todoId")) && !"Y".equals(line.get("sanctnSttus"))) {
						Map<String, String> approveParam = new HashMap<>(line);
						approveParam.put("todoCfOpn", paramMap.get("apprOpinion") != null ? paramMap.get("apprOpinion") : "");
						approveParam.put("userId", userId);
						approveParam.put("pgmId", pgmId);
						Map<String, String> wb20Res = wb20Svc.insertApprovalLine(approveParam);
						approved = true;
						returnMap.put("resultCode", 200);
						returnMap.put("resultMessage", "결재 승인되었습니다.");
						returnMap.put("result", wb20Res);
						break;
					}
				}
			}
			if (!approved) {
				throw new RuntimeException("결재할 차례가 아니거나 이미 결재하셨습니다.");
			}
		}
		return returnMap;
	}

	@Override
	public int updateTripRptPayCancel(Map<String, String> paramMap) throws Exception {
		String tripRptNo = paramMap.get("tripRptNo");
		String userId = paramMap.get("userId");

		Map<String, String> m02Param = new HashMap<>();
		m02Param.put("tripRptNo", tripRptNo);
		Map<String, String> m02 = pm51Mapper.selectTripRptM01(m02Param);
		if (m02 == null) {
			throw new RuntimeException("출장복명서 정보를 찾을 수 없습니다.");
		}
		if (!hasText(m02.get("payDt"))) {
			throw new RuntimeException("지급완료 처리된 출장복명서가 아닙니다.");
		}

		// 회계담당자(SPECRTS15) 권한 검증 (updateTripRptAcctSettle과 동일 패턴 - selectAcctMngApproverIds() 재사용)
		String[] acctApprovers = selectAcctMngApproverIds();
		boolean isAcctUser = false;
		for (String approverId : acctApprovers) {
			if (hasText(approverId) && approverId.trim().equals(userId)) {
				isAcctUser = true;
				break;
			}
		}
		if (!isAcctUser) {
			throw new RuntimeException("회계담당자만 지급완료를 취소할 수 있습니다.");
		}

		// 지급취소는 지급만 취소하고 관리부서 결재선은 유지한다(신청서 updateTripReqPayCancel과 동일 정책).
		// 기존에는 지불담당자(PAY_ID) 본인의 관리결재행을 결재전 상태로 환원했으나, 결재와 지급 기능 분리에 따라 제거함.
		// Map<String, String> approvalCancelParam = new HashMap<>();
		// approvalCancelParam.put("tripRptNo", tripRptNo);
		// approvalCancelParam.put("payId", m02.get("payId"));
		// approvalCancelParam.put("userId", userId);
		// approvalCancelParam.put("pgmId", paramMap.get("pgmId"));
		// pm51Mapper.updateTripRptPayCancelApproval(approvalCancelParam);

		// TB_PM52D01 백업 데이터 삭제 (지급완료 취소이므로 스냅샷 제거)
		Map<String, String> d03DeleteParam = new HashMap<>();
		d03DeleteParam.put("tripRptNo", tripRptNo);
		pm51Mapper.deleteTripRptD01(d03DeleteParam);

		// TB_PM01D01 지급 관련 필드 초기화 (복명서번호/회계등록/지급회차/예비구분/카드환율)
		Map<String, String> clearParam = new HashMap<>();
		clearParam.put("tripRptNo", tripRptNo);
		clearParam.put("userId", userId);
		clearParam.put("pgmId", paramMap.get("pgmId"));
		pm51Mapper.clearTripExpenseStatusByRptNo(clearParam);

		Map<String, String> cancelParam = new HashMap<>();
		cancelParam.put("tripRptNo", tripRptNo);
		cancelParam.put("userId", userId);
		cancelParam.put("pgmId", paramMap.get("pgmId"));
		return pm51Mapper.updateTripRptPayCancel(cancelParam);
	}

	@Override
	public List<Map<String, String>> selectTrnContractList(Map<String, String> paramMap) {
		return pm51Mapper.selectTrnContractList(paramMap);
	}

}
