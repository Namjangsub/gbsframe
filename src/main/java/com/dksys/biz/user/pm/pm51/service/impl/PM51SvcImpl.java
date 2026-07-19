package com.dksys.biz.user.pm.pm51.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
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

	@Autowired
	PM51Mapper pm51Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	QM01Mapper qm01Mapper;

	@Autowired
	WB20Svc wb20Svc;

	@Autowired
	WB24Mapper wb24Mapper;

	@Override
	public int selectTripReqListCount(Map<String, String> paramMap) {
		return pm51Mapper.selectTripReqListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectTripReqList(Map<String, String> paramMap) {
		return pm51Mapper.selectTripReqList(paramMap);
	}

	@Override
	public Map<String, Object> selectTripReqDtl(Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		List<Map<String, String>> d01 = pm51Mapper.selectTripReqD01(paramMap);
		List<Map<String, String>> d02 = pm51Mapper.selectTripReqD02(paramMap);

		boolean hasData = false;
		if (d02 != null) {
			for (Map<String, String> map : d02) {
				double krw = map.get("krwAmt") != null ? Double.parseDouble(String.valueOf(map.get("krwAmt"))) : 0;
				double usd = map.get("usdAmt") != null ? Double.parseDouble(String.valueOf(map.get("usdAmt"))) : 0;
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
					double krw = rpt.get("krwAmt") != null ? Double.parseDouble(String.valueOf(rpt.get("krwAmt"))) : 0;
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

		result.put("m01", m01);
		result.put("d01", d01);
		result.put("d02", d02);
		return result;
	}

	@Override
	public int insertTripReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		String tripReqNo = pm51Mapper.selectTripReqListCount(paramMap) >= 0 ? "BT" + getCurrentDateString() + "-" : null;
		normalizeTripReqMasterParam(paramMap);
		validateTripReqMasterParam(paramMap);
		int result = pm51Mapper.insertTripReqM01(paramMap);

		List<Map<String, String>> travelerArr = gsonDtl.fromJson(paramMap.get("travelerArr"), dtlMap);
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

		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
		paramMap.put("pgmId", "Y".equals(paramMap.get("payMode")) ? "PM5101P02" : "PM5101P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		if (paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripReqApplicantApprovals(paramMap, approvalArr);
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
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", paramMap.get("salesCd"));
					fillApprovalBaseParam(approvalMap, paramMap);

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						qm01Mapper.insertWbsSharngList(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						qm01Mapper.insertWbsApprovalList(approvalMap);
						iApproval++;
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));

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
		boolean payMode = "Y".equals(paramMap.get("payMode"));
		if (!"APRVSTS01".equals(aprvStsCd) && !salesDept && !accountingDept) {
			throw new RuntimeException("결재 진행 이후에는 영업팀 또는 회계팀만 수정할 수 있습니다.");
		}
		if (salesDept && "APRVSTS03".equals(aprvStsCd)) {
			throw new RuntimeException("결재완료 이후에는 회계팀만 수정할 수 있습니다.");
		}
//		if (accountingDept && !payMode && !"APRVSTS03".equals(aprvStsCd)) {
//			throw new RuntimeException("회계팀 경비 수정은 결재완료 이후 가능합니다.");
//		}
		if (!payMode) {
			normalizeTripReqMasterParam(paramMap);
			validateTripReqMasterParam(paramMap);
		}

		boolean hasCompletedApproval = false;
		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			hasCompletedApproval = cnt != null && !"0".equals(cnt);
			if (hasCompletedApproval && !salesDept && !accountingDept) {
				throw new RuntimeException("결재가 진행중입니다");
			}
		}
		boolean hasCompletedMngApproval = hasCompletedApproval(paramMap, true);

		int result = payMode ? pm51Mapper.updateTripReqPayAmounts(paramMap) : pm51Mapper.updateTripReqM01(paramMap);

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripReqNo", paramMap.get("tripReqNo"));
		delParam.put("reqNo", paramMap.get("tripReqNo"));
		delParam.put("salesCd", paramMap.get("salesCd"));

		if (!hasCompletedApproval) {
			pm51Mapper.deleteTripReqApprovalLines(delParam);
		} else if (accountingDept && !hasCompletedMngApproval) {
			pm51Mapper.deleteTripReqMngApprovalLines(delParam);
		}

		if (!payMode) {
			pm51Mapper.deleteTripReqD01(delParam);
		}
		pm51Mapper.deleteTripReqD02(delParam);

		if (!payMode) {
			List<Map<String, String>> travelerArr = gsonDtl.fromJson(paramMap.get("travelerArr"), dtlMap);
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

		paramMap.put("fileTrgtKey", paramMap.get("tripReqNo"));
		paramMap.put("pgmId", "Y".equals(paramMap.get("payMode")) ? "PM5101P02" : "PM5101P01");
		cm08Svc.fileUpload(paramMap, mRequest);

		if (!hasCompletedApproval && paramMap.containsKey("approvalArr")) {
			List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
			approvalArr = appendTripReqApplicantApprovals(paramMap, approvalArr);
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
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", approvalSalesCd(paramMap));
					fillApprovalBaseParam(approvalMap, paramMap);

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						qm01Mapper.insertWbsSharngList(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						qm01Mapper.insertWbsApprovalList(approvalMap);
						iApproval++;
						if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))) {
							approvalMap.put("todoCfOpn", "자체승인");
							approvalMap.put("todoNo", approvalMap.get("reqNo"));

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
		if (!hasCompletedApproval || (accountingDept && !hasCompletedMngApproval)) {
			processTripReqApprovalArr(paramMap, gsonDtl, dtlMap, "mngApprovalArr", "관리부서");
		}

		return result;
	}

	private void processTripReqApprovalArr(Map<String, String> paramMap, Gson gsonDtl, Type dtlMap, String arrKey, String gubun) {
		if (!paramMap.containsKey(arrKey)) {
			return;
		}

		List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get(arrKey), dtlMap);
		if (approvalArr == null || approvalArr.size() == 0) {
			return;
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

		int iSharng = 1;
		int iApproval = 1;
		for (Map<String, String> approvalMap : approvalArr) {
			approvalMap.put("reqNo", paramMap.get("reqNo"));
			approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			approvalMap.put("salesCd", approvalSalesCd(paramMap));
			fillApprovalBaseParam(approvalMap, paramMap);

			if (isShareApproval(approvalMap)) {
				approvalMap.put("sanCtnSn", Integer.toString(iSharng));
				approvalMap.put("pgParam", pgParam1);
				qm01Mapper.insertWbsSharngList(approvalMap);
				iSharng++;
			} else {
				approvalMap.put("sanCtnSn", Integer.toString(iApproval));
				approvalMap.put("pgParam", pgParam2);
				qm01Mapper.insertWbsApprovalList(approvalMap);
				iApproval++;
				if (approvalMap.get("userId").equals(approvalMap.get("usrNm"))) {
					approvalMap.put("todoCfOpn", "자동승인");
					approvalMap.put("todoNo", approvalMap.get("reqNo"));

					Object value = approvalMap.get("toDoKey");
					if (value != null) {
						approvalMap.put("todoKey", value.toString());
					}

					wb20Svc.insertApprovalLine(approvalMap);
				}
			}
		}
	}

	@Override
	public int deleteTripReq(Map<String, String> paramMap) throws Exception {
		paramMap.put("reqNo", paramMap.get("tripReqNo"));

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재가 진행중입니다");
			}
		}

		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		String salesCd = paramMap.get("tripReqNo");
		if (m01 != null) {
			String m01SalesCd = m01.get("salesCd");
			if (hasText(m01SalesCd)) {
				salesCd = m01SalesCd;
			}
		}

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripReqNo", paramMap.get("tripReqNo"));
		delParam.put("reqNo", paramMap.get("tripReqNo"));
		delParam.put("salesCd", salesCd);

		List<Map<String, String>> sharngChk = qm01Mapper.deleteWbsSharngListChk(delParam);
		if (sharngChk.size() > 0) {
			qm01Mapper.deleteWbsSharngList(delParam);
		}

		pm51Mapper.deleteTripReqD01(delParam);
		pm51Mapper.deleteTripReqD02(delParam);
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
		return pm51Mapper.updateTripReqPayDone(paramMap);
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
		return pm51Mapper.updateTripReqPayCancel(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap) {
		return pm51Mapper.selectSignResUserlstInit(paramMap);
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
		Map<String, String> m02 = pm51Mapper.selectTripRptM02(paramMap);
		List<Map<String, String>> d03 = pm51Mapper.selectTripRptD03(paramMap);
		List<Map<String, String>> d02 = pm51Mapper.selectTripRptD02(paramMap);
		if (m02 != null) {
			paramMap.put("tripReqNo", m02.get("tripReqNo"));
		}
		Map<String, String> m01 = pm51Mapper.selectTripReqM01(paramMap);
		List<Map<String, String>> d01 = pm51Mapper.selectTripReqD01(paramMap);
		result.put("m02", m02);
		result.put("d02", d02);
		result.put("d03", d03);
		result.put("m01", m01);
		result.put("d01", d01);
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

		int result = pm51Mapper.insertTripRptM02(paramMap);

		List<Map<String, String>> rptTravelerArr = gsonDtl.fromJson(paramMap.get("rptTravelerArr"), dtlMap);
		if (rptTravelerArr != null && !rptTravelerArr.isEmpty()) {
			for (Map<String, String> travelerMap : rptTravelerArr) {
				travelerMap.put("tripRptNo", paramMap.get("tripRptNo"));
				travelerMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD02(travelerMap);
			}
		}

		List<Map<String, String>> expenseDtlArr = gsonDtl.fromJson(paramMap.get("expenseDtlArr"), dtlMap);
		if (expenseDtlArr != null && !expenseDtlArr.isEmpty()) {
			for (Map<String, String> expenseDtlMap : expenseDtlArr) {
				expenseDtlMap.put("tripRptNo", paramMap.get("tripRptNo"));
				expenseDtlMap.put("coCd", paramMap.get("coCd"));
				normalizeTripRptExpenseDate(expenseDtlMap);
				pm51Mapper.insertTripRptD03(expenseDtlMap);
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
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", paramMap.get("salesCd"));
					fillApprovalBaseParam(approvalMap, paramMap);

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						qm01Mapper.insertWbsSharngList(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						qm01Mapper.insertWbsApprovalList(approvalMap);
						iApproval++;
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

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재가 진행중입니다");
			}
		}

		int result = pm51Mapper.updateTripRptM02(paramMap);

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

		List<Map<String, String>> rptTravelerArr = gsonDtl.fromJson(paramMap.get("rptTravelerArr"), dtlMap);
		if (rptTravelerArr != null && !rptTravelerArr.isEmpty()) {
			for (Map<String, String> travelerMap : rptTravelerArr) {
				travelerMap.put("tripRptNo", paramMap.get("tripRptNo"));
				travelerMap.put("coCd", paramMap.get("coCd"));
				pm51Mapper.insertTripRptD02(travelerMap);
			}
		}

		List<Map<String, String>> expenseDtlArr = gsonDtl.fromJson(paramMap.get("expenseDtlArr"), dtlMap);
		if (expenseDtlArr != null && !expenseDtlArr.isEmpty()) {
			for (Map<String, String> expenseDtlMap : expenseDtlArr) {
				expenseDtlMap.put("tripRptNo", paramMap.get("tripRptNo"));
				expenseDtlMap.put("coCd", paramMap.get("coCd"));
				normalizeTripRptExpenseDate(expenseDtlMap);
				pm51Mapper.insertTripRptD03(expenseDtlMap);
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
				for (Map<String, String> approvalMap : approvalArr) {
					approvalMap.put("reqNo", paramMap.get("reqNo"));
					approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					approvalMap.put("salesCd", paramMap.get("salesCd"));
					fillApprovalBaseParam(approvalMap, paramMap);

					if ("공유".equals(approvalMap.get("gb"))) {
						approvalMap.put("sanCtnSn", Integer.toString(iSharng));
						approvalMap.put("pgParam", pgParam1);
						qm01Mapper.insertWbsSharngList(approvalMap);
						iSharng++;
					} else {
						approvalMap.put("sanCtnSn", Integer.toString(iApproval));
						approvalMap.put("pgParam", pgParam2);
						qm01Mapper.insertWbsApprovalList(approvalMap);
						iApproval++;
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

		return result;
	}

	@Override
	public int deleteTripRpt(Map<String, String> paramMap) throws Exception {
		paramMap.put("reqNo", paramMap.get("tripRptNo"));

		List<Map<String, String>> approvalChkList = pm51Mapper.selectApprovalChk(paramMap);
		if (approvalChkList != null && approvalChkList.size() > 0) {
			Map<String, String> chkMap = approvalChkList.get(0);
			String cnt = chkMap.get("cnt");
			if (cnt != null && !"0".equals(cnt)) {
				throw new RuntimeException("결재가 진행중입니다");
			}
		}

		Map<String, String> delParam = new HashMap<>();
		delParam.put("tripRptNo", paramMap.get("tripRptNo"));
		pm51Mapper.deleteTripRptD02(delParam);
		pm51Mapper.deleteTripRptD03(delParam);
		int result = pm51Mapper.deleteTripRptM02(delParam);
		return result;
	}

	private String getCurrentDateString() {
		return new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	}

	private boolean isSalesDept(String deptId) {
		return deptId != null && (deptId.startsWith("GUN30") || deptId.startsWith("TRN30"));
	}

	private boolean isAccountingDept(String deptId) {
		return deptId != null && (deptId.startsWith("GUN20") || deptId.startsWith("GUN80"));
	}

	private boolean hasText(String value) {
		return value != null && value.trim().length() > 0;
	}

	private String approvalSalesCd(Map<String, String> paramMap) {
		if (hasText(paramMap.get("salesCd"))) {
			return paramMap.get("salesCd");
		}
		if (hasText(paramMap.get("tripRptNo"))) {
			return paramMap.get("tripRptNo");
		}
		return paramMap.get("tripReqNo");
	}

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

		for (Map<String, String> leaderRow : leaderArr) {
			String userId = leaderRow.get("usrNm");
			if (!hasText(userId) || !registeredUserIds.add(userId)) {
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
		for (String userId : new String[] { "cjm", "youngman", "EMJ8105" }) {
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

	private void fillApprovalBaseParam(Map<String, String> approvalMap, Map<String, String> paramMap) {
		String pgmId = paramMap.get("pgmId");
		approvalMap.put("pgmId", pgmId);
		approvalMap.put("pgPath", approvalPgPath(pgmId));
		approvalMap.put("userId", paramMap.get("userId"));
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

	private String approvalPgPath(String pgmId) {
		if ("PM5102P01".equals(pgmId)) {
			return "/user/pm/pm51/PM5102P01.html";
		}
		if ("PM5101P02".equals(pgmId)) {
			return "/user/pm/pm51/PM5101P02.html";
		}
		return "/user/pm/pm51/PM5101P01.html";
	}

	private String approvalTitle(Map<String, String> paramMap) {
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

	@Override
	public Map<String, Object> selectTripRptPaySummary(Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		result.put("expenseSums", pm51Mapper.selectTripRptPayExpenseSum(paramMap));
		result.put("eatCntSum", pm51Mapper.selectTripRptEatCntSum(paramMap));
		result.put("travelerExpenseSum", pm51Mapper.selectTripRptTravelerExpenseSum(paramMap));
		return result;
	}

}
