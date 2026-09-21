package com.dksys.biz.user.pm.pm08.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.util.Collections;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.user.pm.pm08.mapper.PM08Mapper;
import com.dksys.biz.user.pm.pm08.service.PM08Svc;
import com.dksys.biz.user.pm.pm30.service.PM30Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM08SvcImpl implements PM08Svc {

	@Autowired
	PM08Mapper pm08Mapper;

	@Autowired
	WB20Svc wb20Svc;

	@Autowired
	WB24Svc wb24Svc;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	PM30Svc pm30Svc;

	@Autowired
	AM11Svc am11Svc;

	@Override
	public int selectSubstituteWorkCount(Map<String, String> paramMap) {
		return pm08Mapper.selectSubstituteWorkCount(paramMap);
	}

	@Override
	public java.util.List<Map<String, String>> selectSubstituteWorkList(Map<String, String> paramMap) {
		return pm08Mapper.selectSubstituteWorkList(paramMap);
	}

	@Override
	public Map<String, Object> selectSubstituteWorkDtl(Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		Map<String, String> m01 = pm08Mapper.selectSubstituteWorkDtl(paramMap);
		List<Map<String, String>> projectList = pm08Mapper.selectSubstituteWorkProjectList(paramMap);

		if (m01 != null) {
			result.putAll(m01);
		}
		result.put("projectList", projectList);
		return result;
	}

	@Override
	public List<Map<String, String>> selectMobileSubstituteWorkFileList(Map<String, String> paramMap) {
		return pm08Mapper.selectMobileSubstituteWorkFileList(paramMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> insertSubstituteWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 마감 검증: holidayDt (하이픈 포함 가능성: YYYY-MM-DD 또는 YYYYMMDD)
		String holidayDt = paramMap.get("holidayDt");
		if (holidayDt != null && !holidayDt.isEmpty()) {
			pm30Svc.assertNotClosed(holidayDt);
		}

		// 1. 중복 신청 검사: CO_CD, REQ_ID, HOLIDAY_DT 조합 (self 제외)
		int duplicateCount = pm08Mapper.selectSubstituteWorkDuplicateCheck(paramMap);
		if (duplicateCount > 0) {
			result.put("resultCode", "409");
			result.put("resultMessage", "해당 휴일(" + paramMap.get("holidayDt") + ")에 이미 신청한 내역이 있습니다.");
			return result;
		}

		// 2. REQ_NO 채번 (HSW + YYYYMMDD + 시퀀스)
		String reqNo = pm08Mapper.selectSubstituteWorkReqNoNext(paramMap);
		paramMap.put("reqNo", reqNo);

		// 3. 신청상태 판정: 결재선 있으면 REQ, 없으면 NULL(자료없음)
		String approvalArr = paramMap.get("approvalArr");
		// REQ_STATUS/RESULT_STATUS는 REQ/ING/END 3가지만 사용, 자료없음은 NULL로 표현
		if (approvalArr != null && !approvalArr.isEmpty() && !"[]".equals(approvalArr.trim())) {
			paramMap.put("reqStatus", "REQ");
		} else {
			paramMap.remove("reqStatus");
		}
		paramMap.remove("resultStatus"); // 결과는 아직 입력 전이므로 NULL(자료없음)로 초기화
		paramMap.put("creatId", paramMap.get("userId"));

		int insertResult = pm08Mapper.insertSubstituteWork(paramMap);

		if (insertResult > 0) {
			// 4. 참여 프로젝트 목록 등록
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			// 4. 프로젝트 목록 등록
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> projectArr = gsonDtl.fromJson(paramMap.get("projectArr"), dtlMap);
			String firstSalesCd = null;
			if (projectArr != null && !projectArr.isEmpty()) {
				for (Map<String, String> projectMap : projectArr) {
					projectMap.put("reqNo", reqNo);
					projectMap.put("coCd", paramMap.get("coCd"));
					pm08Mapper.insertSubstituteWorkProjectList(projectMap);
				}
				firstSalesCd = projectArr.get(0).get("salesCd");
				if (firstSalesCd == null || firstSalesCd.trim().isEmpty()) {
					firstSalesCd = projectArr.get(0).get("SALES_CD");
				}
			}
			if (firstSalesCd == null || firstSalesCd.trim().isEmpty()) {
				firstSalesCd = paramMap.get("salesCd");
			}
			if (firstSalesCd == null || firstSalesCd.trim().isEmpty()) {
				firstSalesCd = reqNo;
			}

			// 5. 결재선 등록 (TODODIV2410: 신청결재, 공통 모듈 호출 규격 100% 주입)
			String approvalArrStr = paramMap.get("approvalArr");
			if (approvalArrStr != null && !approvalArrStr.isEmpty()) {
				List<Map<String, String>> approvalList = gsonDtl.fromJson(approvalArrStr, dtlMap);
				approvalList = deduplicateApprovalAndShare(approvalList);

					for (Map<String, String> apprItem : approvalList) {
						apprItem.put("todoNo", reqNo);
						apprItem.put("todoFileTrgtKey", reqNo);
						if (apprItem.get("pgPath") == null || apprItem.get("pgPath").isEmpty()) {
							apprItem.put("pgPath", "/user/pm/pm08/PM0801P01.html");
						}
						if (apprItem.get("todoTitl") == null || apprItem.get("todoTitl").isEmpty()) {
							apprItem.put("todoTitl", "휴일대체근무 신청서");
						}
						String itemSalesCd = apprItem.get("salesCd");
						if (itemSalesCd == null || itemSalesCd.trim().isEmpty() || reqNo.equals(itemSalesCd)) {
							apprItem.put("salesCd", firstSalesCd);
						}

						String curCodeId = apprItem.get("todoDiv2CodeId");
						if (curCodeId == null || curCodeId.isEmpty()) {
							curCodeId = "TODODIV2410";
							apprItem.put("todoDiv2CodeId", curCodeId);
						}

						Map<String, Object> pgMap = new HashMap<>();
						pgMap.put("coCd", paramMap.get("coCd") != null ? paramMap.get("coCd") : "GUN");
						pgMap.put("reqNo", reqNo);
						pgMap.put("todoFileTrgtKey", reqNo);
						pgMap.put("actionType", "A");
						pgMap.put("openStage", "REQ");
						pgMap.put("todoDiv2CodeId", curCodeId);
						pgMap.put("pgmId", "PM0801P01");

						apprItem.put("pgParam", gsonDtl.toJson(pgMap));
					}
					paramMap.put("approvalArr", gsonDtl.toJson(approvalList));
					paramMap.put("salesCd", firstSalesCd);
					paramMap.put("todoNo", reqNo);
					paramMap.put("todoFileTrgtKey", reqNo);
					paramMap.put("todoDiv2CodeId", "TODODIV2410");
					paramMap.put("etcField1", reqNo);
					wb20Svc.insertTodoMaster(paramMap);
					syncSubstituteWorkToAm(paramMap, "TODODIV2410");
				}

			// 6. 첨부파일 처리
			deleteAttachedFiles(paramMap.get("deleteFileArr"));
			paramMap.put("comonCd", "FITR9902");
			cm08Svc.uploadFile("PM0801P01", reqNo, mRequest);

			result.put("resultCode", "200");
			result.put("resultMessage", "휴일대체근무 신청이 등록되었습니다.");
			result.put("reqNo", reqNo);
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴일대체근무 신청 등록에 실패했습니다.");
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> updateSubstituteWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		boolean isResultStage = "RESULT".equals(paramMap.get("approvalStage"));

		// 마감 검증: holidayDt (신청서 수정 및 결과 상신 모두 마감 검증 수행)
		String holidayDt = paramMap.get("holidayDt");
		if (holidayDt == null || holidayDt.isEmpty()) {
			Map<String, String> dtlQuery = new HashMap<>();
			dtlQuery.put("coCd", paramMap.get("coCd"));
			dtlQuery.put("reqNo", paramMap.get("reqNo"));
			Map<String, String> currentDtl = pm08Mapper.selectSubstituteWorkDtl(dtlQuery);
			if (currentDtl != null) {
				holidayDt = currentDtl.get("holidayDt");
				if (holidayDt == null || holidayDt.isEmpty()) {
					holidayDt = currentDtl.get("HOLIDAY_DT");
				}
			}
		}
		if (holidayDt != null && !holidayDt.isEmpty()) {
			pm30Svc.assertNotClosed(holidayDt);
		}

		// 1. 중복 신청 검사 (신청서 수정 시에만 수행, 결과상신 시에는 검사 스킵)
		if (!isResultStage) {
			if (paramMap.get("reqNo") != null && !paramMap.get("reqNo").isEmpty()) {
				paramMap.put("excludeReqNo", paramMap.get("reqNo"));
			}
			int duplicateCount = pm08Mapper.selectSubstituteWorkDuplicateCheck(paramMap);
			if (duplicateCount > 0) {
				result.put("resultCode", "409");
				result.put("resultMessage", "해당 휴일(" + paramMap.get("holidayDt") + ")에 이미 신청한 내역이 있습니다.");
				return result;
			}
		}

		// DB UPDATE 직전 백엔드 최종 점검: 해당 결재단계(신청결재 TODODIV2410 vs 결과결재 TODODIV2420)의 결재 진행(신청자 외 승인 'Y') 여부 확인
		String targetCheckCode = isResultStage ? "TODODIV2420" : "TODODIV2410";
		Map<String, String> checkMap = new HashMap<>(paramMap);
		checkMap.put("todoDiv2CodeId", targetCheckCode);

		int approvedCnt = pm08Mapper.selectApprovedCountExceptApplicant(checkMap);
		if (approvedCnt > 0) {
			result.put("resultCode", "500");
			result.put("resultMessage", isResultStage ? "이미 결과결재가 진행된 건은 수정할 수 없습니다." : "이미 신청자 외의 결재가 진행된 건은 수정할 수 없습니다.");
			return result;
		}

		paramMap.put("udtId", paramMap.get("userId"));

		// 2. 결과 작성/상신 모드(RESULT)일 때: 신청서 필드는 절대 수정하지 않고, 결과 영역(REAL_ST_TM, REAL_ED_TM, WORK_RESULT)만 수정
		int updateResult = isResultStage ? pm08Mapper.updateSubstituteWorkResult(paramMap) : pm08Mapper.updateSubstituteWork(paramMap);

		if (updateResult > 0) {
			// 3. 참여 프로젝트 목록 처리 (신청서 수정 모드일 때만 수행)
			if (!isResultStage) {
				String projectArrStr = paramMap.get("projectArr");
				if (projectArrStr != null && !projectArrStr.isEmpty()) {
					Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
					Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
					List<Map<String, String>> projectArr = gsonDtl.fromJson(projectArrStr, dtlMap);

					// 기존 프로젝트 목록 삭제
					pm08Mapper.deleteSubstituteWorkProjectList(paramMap);

					// 신규 프로젝트 목록 등록
					if (projectArr != null && !projectArr.isEmpty()) {
						for (Map<String, String> projectMap : projectArr) {
							projectMap.put("reqNo", paramMap.get("reqNo"));
							projectMap.put("coCd", paramMap.get("coCd"));
							pm08Mapper.insertSubstituteWorkProjectList(projectMap);
						}
					}
				}
			}

			// 3. 결재선 변경 시 재등록 (기존 삭제 후 새로 등록)
			// approvalStage: "RESULT"=결과결재(TODODIV2420) 상신, 그 외(기본값)="REQ"=신청결재(TODODIV2410) 상신
			String approvalArrStr = paramMap.get("approvalArr");
			String todoDiv2CodeId = isResultStage ? "TODODIV2420" : "TODODIV2410";

			if (approvalArrStr != null && !approvalArrStr.isEmpty()) {
				String reqNoVal = paramMap.get("reqNo");
				Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
				Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
				List<Map<String, String>> approvalList = gsonDtl.fromJson(approvalArrStr, dtlMap);
				approvalList = deduplicateApprovalAndShare(approvalList);

				// 첫 번째 프로젝트의 salesCd 추출 (TB_WB20M03.SALES_CD 대입용)
				String firstSalesCd = null;
				if (paramMap.get("salesCd") != null && !paramMap.get("salesCd").trim().isEmpty() && !reqNoVal.equals(paramMap.get("salesCd"))) {
					firstSalesCd = paramMap.get("salesCd");
				} else {
					List<Map<String, String>> dbProjectList = pm08Mapper.selectSubstituteWorkProjectList(paramMap);
					if (dbProjectList != null && !dbProjectList.isEmpty()) {
						firstSalesCd = dbProjectList.get(0).get("salesCd");
						if (firstSalesCd == null || firstSalesCd.trim().isEmpty()) {
							firstSalesCd = dbProjectList.get(0).get("SALES_CD");
						}
					}
				}
				if (firstSalesCd == null || firstSalesCd.trim().isEmpty()) {
					firstSalesCd = reqNoVal;
				}

				if (approvalList != null && !approvalList.isEmpty()) {
					for (Map<String, String> apprItem : approvalList) {
						apprItem.put("todoNo", reqNoVal);
						apprItem.put("todoFileTrgtKey", reqNoVal);
						if (apprItem.get("pgPath") == null || apprItem.get("pgPath").isEmpty()) {
							apprItem.put("pgPath", "/user/pm/pm08/PM0801P01.html");
						}
						if (apprItem.get("todoTitl") == null || apprItem.get("todoTitl").isEmpty()) {
							apprItem.put("todoTitl", isResultStage ? "휴일대체근무 결과보고서" : "휴일대체근무 신청서");
						}
						String itemSalesCd = apprItem.get("salesCd");
						if (itemSalesCd == null || itemSalesCd.trim().isEmpty() || reqNoVal.equals(itemSalesCd)) {
							apprItem.put("salesCd", firstSalesCd);
						}

						String curCodeId = apprItem.get("todoDiv2CodeId");
						if (curCodeId == null || curCodeId.isEmpty()) {
							curCodeId = todoDiv2CodeId;
							apprItem.put("todoDiv2CodeId", curCodeId);
						}

						Map<String, Object> pgMap = new HashMap<>();
						pgMap.put("coCd", paramMap.get("coCd") != null ? paramMap.get("coCd") : "GUN");
						pgMap.put("reqNo", reqNoVal);
						pgMap.put("todoFileTrgtKey", reqNoVal);
						pgMap.put("actionType", "A");
						pgMap.put("openStage", isResultStage ? "RESULT" : "REQ");
						pgMap.put("todoDiv2CodeId", curCodeId);
						pgMap.put("pgmId", "PM0801P01");

						apprItem.put("pgParam", gsonDtl.toJson(pgMap));
					}
					paramMap.put("approvalArr", gsonDtl.toJson(approvalList));
					paramMap.put("salesCd", firstSalesCd);

					// 기존 결재선 삭제 (결재 및 공유선 모두 삭제)
					Map<String, String> deleteParam = new HashMap<>();
					deleteParam.put("todoNo", reqNoVal);
					deleteParam.put("todoDiv2CodeId", todoDiv2CodeId);
					wb20Svc.deleteTodoMasterByTodoNo(deleteParam);

					Map<String, String> deleteShareParam = new HashMap<>();
					deleteShareParam.put("todoNo", reqNoVal);
					deleteShareParam.put("todoDiv2CodeId", isResultStage ? "TODODIV1420" : "TODODIV1410");
					wb20Svc.deleteTodoMasterByTodoNo(deleteShareParam);

					// 신규 결재선 등록
					paramMap.put("todoNo", reqNoVal);
					paramMap.put("todoFileTrgtKey", reqNoVal);
					paramMap.put("todoDiv2CodeId", todoDiv2CodeId);
					paramMap.put("salesCd", firstSalesCd);
					paramMap.put("etcField1", reqNoVal);
					wb20Svc.insertTodoMaster(paramMap);
					syncSubstituteWorkToAm(paramMap, todoDiv2CodeId);

					// 상태 갱신
					Map<String, String> statusUpdate = new HashMap<>();
					statusUpdate.put("coCd", paramMap.get("coCd"));
					statusUpdate.put("reqNo", reqNoVal);
					statusUpdate.put("udtId", paramMap.get("userId"));
					if (isResultStage) {
						statusUpdate.put("resultStatus", "REQ");
						pm08Mapper.updateSubstituteWorkResultStatus(statusUpdate);
					} else {
						statusUpdate.put("reqStatus", "REQ");
						pm08Mapper.updateSubstituteWorkReqStatus(statusUpdate);
					}
				}
			}

			// 4. 첨부파일 처리
			deleteAttachedFiles(paramMap.get("deleteFileArr"));
			paramMap.put("comonCd", "FITR9902");
			cm08Svc.uploadFile("PM0801P01", paramMap.get("reqNo"), mRequest);

			result.put("resultCode", "200");
			result.put("resultMessage", "휴일대체근무 신청이 수정되었습니다.");
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "휴일대체근무 신청 수정에 실패했습니다.");
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> deleteSubstituteWork(Map<String, String> paramMap) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

		// 1. 해당 신청 존재 여부 확인
		Map<String, String> dtlQuery = new HashMap<>();
		dtlQuery.put("coCd", paramMap.get("coCd"));
		dtlQuery.put("reqNo", paramMap.get("reqNo"));
		Map<String, String> currentDtl = pm08Mapper.selectSubstituteWorkDtl(dtlQuery);
		if (currentDtl == null || currentDtl.isEmpty()) {
			result.put("resultCode", "200");
			result.put("resultMessage", "이미 삭제완료된 상태입니다.");
			return result;
		}

		// 마감 검증: 기존 DB의 holidayDt
		String holidayDt = currentDtl.get("holidayDt");
		if (holidayDt == null || holidayDt.isEmpty()) {
			holidayDt = currentDtl.get("HOLIDAY_DT");
		}
		if (holidayDt != null && !holidayDt.isEmpty()) {
			pm30Svc.assertNotClosed(holidayDt);
		}

		// 특별 정책: 휴일대체근무 삭제는 신청자가 결재진행중 또는 완료상태에서도 자료 삭제 허용
		// 단, 타인의 신청건 무단 삭제 방지를 위해 신청자 본인 여부 검증
		String loginUserId = paramMap.get("userId");
		String reqId = currentDtl.get("reqId");
		if (reqId == null) {
			reqId = currentDtl.get("REQ_ID");
		}
		if (loginUserId != null && !loginUserId.trim().isEmpty() && reqId != null && !reqId.trim().isEmpty()) {
			if (!loginUserId.trim().equalsIgnoreCase(reqId.trim())) {
				result.put("resultCode", "500");
				result.put("resultMessage", "휴일대체근무 신청자 본인만 삭제할 수 있습니다.");
				return result;
			}
		}

		// 2. 결재선/공유선 일괄 CASCADE 삭제 (신청결재/신청공유/결과결재/결과공유)
		Map<String, String> deleteParam1 = new HashMap<>();
		deleteParam1.put("todoNo", paramMap.get("reqNo"));
		deleteParam1.put("todoDiv2CodeId", "TODODIV2410");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam1);

		Map<String, String> deleteParam1Share = new HashMap<>();
		deleteParam1Share.put("todoNo", paramMap.get("reqNo"));
		deleteParam1Share.put("todoDiv2CodeId", "TODODIV1410");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam1Share);

		Map<String, String> deleteParam2 = new HashMap<>();
		deleteParam2.put("todoNo", paramMap.get("reqNo"));
		deleteParam2.put("todoDiv2CodeId", "TODODIV2420");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam2);

		Map<String, String> deleteParam2Share = new HashMap<>();
		deleteParam2Share.put("todoNo", paramMap.get("reqNo"));
		deleteParam2Share.put("todoDiv2CodeId", "TODODIV1420");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam2Share);

		// 혹시 모를 잔여 결재/공유선까지 REQ_NO 기준으로 100% 일괄 삭제
		pm08Mapper.deleteApprovalLineByReqNo(paramMap);
		deleteSubstituteWorkAmDocs(paramMap.get("reqNo"), paramMap.get("coCd"));

		// 3. 참여 프로젝트 목록 삭제
		pm08Mapper.deleteSubstituteWorkProjectList(paramMap);

		// 4. 첨부파일 삭제
		Map<String, String> fileSearchMap = new HashMap<>();
		fileSearchMap.put("fileTrgtTyp", "PM0801P01");
		fileSearchMap.put("fileTrgtKey", paramMap.get("reqNo"));
		java.util.List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(fileSearchMap);
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

		// 5. 본체 삭제
		int deleteResult = pm08Mapper.deleteSubstituteWork(paramMap);

		result.put("resultCode", "200");
		if (deleteResult > 0) {
			result.put("resultMessage", "휴일대체근무 신청이 삭제되었습니다.");
		} else {
			result.put("resultMessage", "이미 삭제완료된 상태입니다.");
		}

		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> deleteSubstituteWorkResult(Map<String, String> paramMap) throws Exception {
		Map<String, String> result = new HashMap<>();

		// 마감 검증: DB의 holidayDt
		Map<String, String> dtlQuery = new HashMap<>();
		dtlQuery.put("coCd", paramMap.get("coCd"));
		dtlQuery.put("reqNo", paramMap.get("reqNo"));
		Map<String, String> currentDtl = pm08Mapper.selectSubstituteWorkDtl(dtlQuery);
		if (currentDtl != null) {
			String holidayDt = currentDtl.get("holidayDt");
			if (holidayDt == null || holidayDt.isEmpty()) {
				holidayDt = currentDtl.get("HOLIDAY_DT");
			}
			if (holidayDt != null && !holidayDt.isEmpty()) {
				pm30Svc.assertNotClosed(holidayDt);
			}
		}

		// 결과삭제 사전 검증: 신청자 외 결과 결재(TODODIV2420)가 승인('Y') 진행된 경우 삭제 불가
		Map<String, String> resCheckMap = new HashMap<>(paramMap);
		resCheckMap.put("todoDiv2CodeId", "TODODIV2420");
		int approvedCnt = pm08Mapper.selectApprovedCountExceptApplicant(resCheckMap);
		if (approvedCnt > 0) {
			result.put("resultCode", "500");
			result.put("resultMessage", "이미 결과결재가 진행된 건은 결과삭제를 할 수 없습니다.");
			return result;
		}

		// 1. 근무결과 필드 CLEAR 및 RESULT_STATUS = NULL 초기화
		int cnt = pm08Mapper.deleteSubstituteWorkResult(paramMap);

		if (cnt > 0) {
			// 2. 결과 결재선 (TODODIV2420: 결과결재, TODODIV1420: 결과공유) 삭제
			Map<String, String> deleteParam1 = new HashMap<>();
			deleteParam1.put("todoNo", paramMap.get("reqNo"));
			deleteParam1.put("todoDiv2CodeId", "TODODIV2420");
			wb20Svc.deleteTodoMasterByTodoNo(deleteParam1);

			Map<String, String> deleteParam2 = new HashMap<>();
			deleteParam2.put("todoNo", paramMap.get("reqNo"));
			deleteParam2.put("todoDiv2CodeId", "TODODIV1420");
			wb20Svc.deleteTodoMasterByTodoNo(deleteParam2);

			result.put("resultCode", "200");
			result.put("resultMessage", "근무결과가 성공적으로 삭제되었습니다.");
		} else {
			result.put("resultCode", "500");
			result.put("resultMessage", "근무결과 삭제 처리 중 오류가 발생했습니다.");
		}
		return result;
	}

	/**
	 * 신청결재 완료 후처리 (WB20 insertApprovalLine에서 호출)
	 * REQUIRES_NEW 금지 - 바깥 결재 트랜잭션과 같은 행을 접근하면 교착 발생
	 * 예외 자체 흡수 - 예외 전파 시 결재 전체 롤백
	 * 담당팀장 의견(todoCfOpn)이 있을 경우만 DB에 저장
	 */
	@Override
	public int applySubstituteWorkApproved(Map<String, String> paramMap) {
		try {
			String reqNo = paramMap.get("reqNo");
			if (reqNo == null || reqNo.isEmpty()) {
				reqNo = paramMap.get("todoNo");
			}
			String coCd = paramMap.get("coCd");
			String todoId = paramMap.get("todoId");
			String todoCfOpn = paramMap.get("todoCfOpn");
			String todoYn = paramMap.get("todoYn");

			// 신청건 조회 - 신청자 ID 추출
			Map<String, String> queryParam = new HashMap<>();
			queryParam.put("coCd", coCd);
			queryParam.put("reqNo", reqNo);
			Map<String, String> reqDetail = pm08Mapper.selectSubstituteWorkDtl(queryParam);

			// 의견이 있을 경우 담당팀장 의견 저장
			if (todoCfOpn != null && !todoCfOpn.trim().isEmpty() && reqDetail != null && !reqDetail.isEmpty()) {
				String reqId = reqDetail.get("reqId");
				Map<String, String> managerParam = new HashMap<>();
				managerParam.put("userId", reqId);
				Map<String, String> managerInfo = wb24Svc.selectTeamManagerInfo(managerParam);

				if (managerInfo != null && !managerInfo.isEmpty()) {
					String managerId = managerInfo.get("id");
					if (managerId != null && managerId.equals(todoId)) {
						Map<String, String> updateParam = new HashMap<>();
						updateParam.put("coCd", coCd);
						updateParam.put("reqNo", reqNo);
						updateParam.put("reqMngOpn", todoCfOpn);
						pm08Mapper.updateSubstituteWorkReqMngOpn(updateParam);
					}
				}
			}

			// 최종 결재 완료 시 REQ_STATUS 갱신
			if ("Y".equals(todoYn)) {
				Map<String, String> statusParam = new HashMap<>();
				statusParam.put("coCd", coCd);
				statusParam.put("reqNo", reqNo);
				statusParam.put("reqStatus", "END");
				statusParam.put("udtId", todoId);
				pm08Mapper.updateSubstituteWorkReqStatus(statusParam);
			}

			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 결과결재 완료 후처리 (WB20 insertApprovalLine에서 호출)
	 * 신청결재와 동일 로직, resultMngOpn 컬럼 사용
	 */
	@Override
	public int applySubstituteWorkResultApproved(Map<String, String> paramMap) {
		try {
			String reqNo = paramMap.get("reqNo");
			if (reqNo == null || reqNo.isEmpty()) {
				reqNo = paramMap.get("todoNo");
			}
			String coCd = paramMap.get("coCd");
			String todoId = paramMap.get("todoId");
			String todoCfOpn = paramMap.get("todoCfOpn");
			String todoYn = paramMap.get("todoYn");

			// 신청건 조회 - 신청자 ID 추출
			Map<String, String> queryParam = new HashMap<>();
			queryParam.put("coCd", coCd);
			queryParam.put("reqNo", reqNo);
			Map<String, String> reqDetail = pm08Mapper.selectSubstituteWorkDtl(queryParam);

			// 의견이 있을 경우 담당팀장 결과의견 저장
			if (todoCfOpn != null && !todoCfOpn.trim().isEmpty() && reqDetail != null && !reqDetail.isEmpty()) {
				String reqId = reqDetail.get("reqId");
				Map<String, String> managerParam = new HashMap<>();
				managerParam.put("userId", reqId);
				Map<String, String> managerInfo = wb24Svc.selectTeamManagerInfo(managerParam);

				if (managerInfo != null && !managerInfo.isEmpty()) {
					String managerId = managerInfo.get("id");
					if (managerId != null && managerId.equals(todoId)) {
						Map<String, String> updateParam = new HashMap<>();
						updateParam.put("coCd", coCd);
						updateParam.put("reqNo", reqNo);
						updateParam.put("resultMngOpn", todoCfOpn);
						pm08Mapper.updateSubstituteWorkResultMngOpn(updateParam);
					}
				}
			}

			// 최종 결재 완료 시 RESULT_STATUS 갱신
			if ("Y".equals(todoYn)) {
				Map<String, String> statusParam = new HashMap<>();
				statusParam.put("coCd", coCd);
				statusParam.put("reqNo", reqNo);
				statusParam.put("resultStatus", "END");
				statusParam.put("udtId", todoId);
				pm08Mapper.updateSubstituteWorkResultStatus(statusParam);
			}

			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int selectApprovalCount(Map<String, String> paramMap) {
		return pm08Mapper.selectApprovalCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSubstituteVacationStatusList(Map<String, String> paramMap) {
		return pm08Mapper.selectSubstituteVacationStatusList(paramMap);
	}

	// 화면에서 삭제한 첨부파일 반영 (CM16SvcImpl / PM07SvcImpl 과 동일 패턴)
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
	private List<Map<String, String>> deduplicateApprovalAndShare(List<Map<String, String>> approvalList) {
		if (approvalList == null || approvalList.isEmpty()) {
			return approvalList;
		}
		Set<String> approvalUserIds = new HashSet<>();
		for (Map<String, String> item : approvalList) {
			boolean isShare = "공유".equals(item.get("gb")) || "TODODIV10".equals(item.get("todoDiv1CodeId"))
					|| (item.get("todoDiv2CodeId") != null && item.get("todoDiv2CodeId").startsWith("TODODIV1"));
			if (!isShare) {
				String uid = getApproverUserId(item);
				if (uid != null && !uid.isEmpty()) {
					approvalUserIds.add(uid);
				}
			}
		}
		List<Map<String, String>> result = new ArrayList<>();
		int shareSn = 1;
		int appSn = 1;
		for (Map<String, String> item : approvalList) {
			boolean isShare = "공유".equals(item.get("gb")) || "TODODIV10".equals(item.get("todoDiv1CodeId"))
					|| (item.get("todoDiv2CodeId") != null && item.get("todoDiv2CodeId").startsWith("TODODIV1"));
			if (isShare) {
				String uid = getApproverUserId(item);
				if (uid != null && approvalUserIds.contains(uid)) {
					continue; // 동일ID 결재선 존재 시 공유선 자동 제거
				}
				item.put("sanctnSn", String.valueOf(shareSn++));
			} else {
				item.put("sanctnSn", String.valueOf(appSn++));
			}
			result.add(item);
		}
		return result;
	}

	private String getApproverUserId(Map<String, String> item) {
		if (item == null) return null;
		String uid = item.get("todoId");
		if (uid == null || uid.trim().isEmpty()) uid = item.get("usrNm");
		if (uid == null || uid.trim().isEmpty()) uid = item.get("empNo");
		if (uid == null || uid.trim().isEmpty()) uid = item.get("userId");
		return uid == null ? null : uid.trim();
	}

	private void syncSubstituteWorkToAm(Map<String, String> paramMap, String todoDiv2CodeId) {
		try {
			String reqNo = paramMap.get("reqNo");
			if (reqNo == null || reqNo.trim().isEmpty()) {
				reqNo = paramMap.get("todoNo");
			}
			if (reqNo == null || reqNo.trim().isEmpty()) {
				return;
			}

			String coCd = paramMap.get("coCd");
			if (coCd == null || coCd.trim().isEmpty()) {
				coCd = "GUN";
			}

			Map<String, String> wb20Query = new HashMap<>();
			wb20Query.put("todoNo", reqNo);
			wb20Query.put("coCd", coCd);
			List<Map<String, String>> wb20Lines = wb20Svc.selectGetApprovalList(wb20Query);
			if (wb20Lines == null || wb20Lines.isEmpty()) {
				return;
			}

			// 현재 결재 단계(todoDiv2CodeId: TODODIV2410 신청 / TODODIV2420 결과)에 맞는 결재선 필터링
			String targetApprCode = "TODODIV2420".equals(todoDiv2CodeId) ? "TODODIV2420" : "TODODIV2410";
			String targetRefCode = "TODODIV2420".equals(todoDiv2CodeId) ? "TODODIV1420" : "TODODIV1410";

			List<Map<String, String>> apprList = new ArrayList<>();
			List<Map<String, String>> refList = new ArrayList<>();
			for (Map<String, String> row : wb20Lines) {
				String div2 = row.get("todoDiv2CodeId");
				if (targetApprCode.equals(div2)) {
					apprList.add(row);
				} else if (targetRefCode.equals(div2) || "공유".equals(row.get("gb"))) {
					refList.add(row);
				}
			}

			if (apprList.isEmpty()) {
				for (Map<String, String> row : wb20Lines) {
					if ("TODODIV10".equals(row.get("todoDiv1CodeId")) || "공유".equals(row.get("gb"))) {
						refList.add(row);
					} else {
						apprList.add(row);
					}
				}
			}

			Collections.sort(apprList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			Collections.sort(refList, new Comparator<Map<String, String>>() {
				@Override
				public int compare(Map<String, String> o1, Map<String, String> o2) {
					int s1 = parseIntSafe(o1.get("sanctnSn"));
					int s2 = parseIntSafe(o2.get("sanctnSn"));
					return Integer.compare(s1, s2);
				}
			});

			List<Map<String, Object>> amLineList = new ArrayList<>();
			for (Map<String, String> row : apprList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", row.get("todoNm") != null && !row.get("todoNm").trim().isEmpty() ? row.get("todoNm") : row.get("name"));
					amLine.put("deptId", row.get("deptId"));
					amLine.put("lineSeq", row.get("sanctnSn"));
					amLine.put("wb20TodoKey", row.get("todoKey"));
					amLine.put("wb20CoCd", row.get("coCd"));
					amLine.put("wb20TodoNo", row.get("todoNo"));
					amLine.put("wb20SanctnSn", row.get("sanctnSn"));
					amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
					amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("lineType", "APPR");
				amLine.put("sourceApproved", "Y".equalsIgnoreCase(row.get("sanctnSttus")) ? "Y" : "N");
				amLineList.add(amLine);
			}

			for (Map<String, String> row : refList) {
				Map<String, Object> amLine = new HashMap<>();
				amLine.put("approverId", row.get("todoId"));
				amLine.put("approverNm", row.get("todoNm") != null && !row.get("todoNm").trim().isEmpty() ? row.get("todoNm") : row.get("name"));
					amLine.put("deptId", row.get("deptId"));
					amLine.put("lineSeq", row.get("sanctnSn"));
					amLine.put("wb20TodoKey", row.get("todoKey"));
					amLine.put("wb20CoCd", row.get("coCd"));
					amLine.put("wb20TodoNo", row.get("todoNo"));
					amLine.put("wb20SanctnSn", row.get("sanctnSn"));
					amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
					amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
				amLine.put("lineType", "REF");
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

			String applicantId = paramMap.get("reqId");
			if (applicantId == null || applicantId.trim().isEmpty()) {
				applicantId = paramMap.get("userId");
			}
			String applicantNm = paramMap.get("reqNm");
			if (applicantNm == null || applicantNm.trim().isEmpty()) {
				applicantNm = paramMap.get("userNm");
			}

			Map<String, String> docIdParam = new HashMap<>();
			docIdParam.put("reqNo", reqNo);
			docIdParam.put("coCd", coCd);
			docIdParam.put("todoDiv2CodeId", todoDiv2CodeId);
			String existingDocId = pm08Mapper.selectAmDocIdByReqNo(docIdParam);

			Map<String, Object> amParam = new HashMap<>();
			if (existingDocId != null && !existingDocId.trim().isEmpty()) {
				amParam.put("docId", existingDocId);
			}
			amParam.put("coCd", coCd);
			amParam.put("userId", applicantId);
			amParam.put("userNm", applicantNm);
			amParam.put("docTitle", buildSubstituteWorkApprovalTitle(paramMap, todoDiv2CodeId));
			amParam.put("formCd", "PM0801");
			amParam.put("formVer", 1);
			amParam.put("erpBizType", "PM08");
			amParam.put("erpBizKey", reqNo);
			amParam.put("docDataJson", new GsonBuilder().disableHtmlEscaping().create().toJson(paramMap));
			amParam.put("docRenderHtml", buildSubstituteWorkApprovalHtml(paramMap, todoDiv2CodeId));
			amParam.put("pgmId", "PM0801P01");
			amParam.put("lineList", amLineList);
			amParam.put("autoApprovedCount", autoApprovedCount);

			Map<String, Object> amResult = am11Svc.submitApproval(amParam);
			// 기존 AM 문서가 진행 중인 상태라 재상신이 거절된 경우에는
			// PM08에서 변경한 WB20 결재선을 AM 잔여 결재선으로 동기화한다.
			if (existingDocId != null && !existingDocId.trim().isEmpty()
					&& amResult != null && !"200".equals(String.valueOf(amResult.get("resultCode")))) {
				Map<String, Object> lineChangeParam = new HashMap<>(amParam);
				lineChangeParam.put("changeReason", "PM08 결재선 수정 동기화");
				lineChangeParam.put("lineList", amLineList);
				am11Svc.changeApprovalLines(lineChangeParam);
			}
		} catch (Exception e) {
			throw new RuntimeException("전자결재(AM) 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private void deleteSubstituteWorkAmDocs(String reqNo, String coCd) {
		if (reqNo == null || reqNo.trim().isEmpty()) {
			return;
		}
		try {
			Map<String, String> param = new HashMap<>();
			param.put("reqNo", reqNo);
			param.put("coCd", (coCd != null && !coCd.trim().isEmpty()) ? coCd : "GUN");
			String docId = pm08Mapper.selectAmDocIdByReqNo(param);
			if (docId != null && !docId.trim().isEmpty()) {
				Map<String, String> delParam = new HashMap<>();
				delParam.put("docId", docId);
				pm08Mapper.deleteAmD01ByDocId(delParam);
				pm08Mapper.deleteAmM01ByDocId(delParam);
			}
		} catch (Exception e) {
			throw new RuntimeException("전자결재 문서 삭제 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	private int parseIntSafe(String val) {
		if (val == null || val.trim().isEmpty()) return 0;
		try {
			return Integer.parseInt(val.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String buildSubstituteWorkApprovalTitle(Map<String, String> paramMap, String todoDiv2CodeId) {
		String stageName = "TODODIV2420".equals(todoDiv2CodeId) ? "결과보고" : "신청서";
		String reqNm = paramMap.get("reqNm");
		if (reqNm == null || reqNm.trim().isEmpty()) reqNm = paramMap.get("userNm");
		if (reqNm == null || reqNm.trim().isEmpty()) reqNm = paramMap.get("reqId");
		String holidayDt = paramMap.get("holidayDt");
		if (holidayDt == null) holidayDt = paramMap.get("substituteDt");
		if (holidayDt != null && holidayDt.length() == 8) {
			holidayDt = holidayDt.substring(0, 4) + "-" + holidayDt.substring(4, 6) + "-" + holidayDt.substring(6, 8);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[휴일대체근무 ").append(stageName).append("] ");
		if (reqNm != null && !reqNm.trim().isEmpty()) {
			sb.append(reqNm);
		}
		if (holidayDt != null && !holidayDt.trim().isEmpty()) {
			sb.append(" (").append(holidayDt).append(")");
		}
		return sb.toString();
	}

	private String buildSubstituteWorkApprovalHtml(Map<String, String> paramMap, String todoDiv2CodeId) {
		boolean isResult = "TODODIV2420".equals(todoDiv2CodeId);
		String reqNo = paramMap.get("reqNo");
		String reqNm = paramMap.get("reqNm");
		if (reqNm == null) reqNm = paramMap.get("userNm");
		String deptNm = paramMap.get("deptNm");
		String holidayDt = paramMap.get("holidayDt");
		if (holidayDt == null) holidayDt = paramMap.get("substituteDt");
		if (holidayDt != null && holidayDt.length() == 8) {
			holidayDt = holidayDt.substring(0, 4) + "-" + holidayDt.substring(4, 6) + "-" + holidayDt.substring(6, 8);
		}
		String stTm = paramMap.get("stTm");
		String edTm = paramMap.get("edTm");
		if ((stTm == null || stTm.trim().isEmpty()) || (edTm == null || edTm.trim().isEmpty())) {
			Map<String, String> detailQuery = new HashMap<>();
			detailQuery.put("reqNo", reqNo);
			detailQuery.put("coCd", paramMap.get("coCd"));
			Map<String, String> detail = pm08Mapper.selectSubstituteWorkDtl(detailQuery);
			if (detail != null) {
				if (stTm == null || stTm.trim().isEmpty()) stTm = detail.get("stTm");
				if (edTm == null || edTm.trim().isEmpty()) edTm = detail.get("edTm");
				if (stTm == null || stTm.trim().isEmpty()) stTm = detail.get("ST_TM");
				if (edTm == null || edTm.trim().isEmpty()) edTm = detail.get("ED_TM");
				if (stTm == null || stTm.trim().isEmpty()) stTm = detail.get("rawStTm");
				if (edTm == null || edTm.trim().isEmpty()) edTm = detail.get("rawEdTm");
			}
		}
		String planTm = (stTm != null ? stTm : "") + " ~ " + (edTm != null ? edTm : "");
		String realStTm = paramMap.get("realStTm");
		String realEdTm = paramMap.get("realEdTm");
		String realTm = (realStTm != null ? realStTm : "") + " ~ " + (realEdTm != null ? realEdTm : "");
		String reason = paramMap.get("specialReason");
		String workResult = paramMap.get("workResult");
		Map<String, String> projectQuery = new HashMap<>();
		projectQuery.put("reqNo", reqNo);
		projectQuery.put("coCd", paramMap.get("coCd"));
		List<Map<String, String>> projectList = pm08Mapper.selectSubstituteWorkProjectList(projectQuery);

		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"table_input\" style=\"width:100%; border-collapse:collapse; border:1px solid #ddd; margin-bottom:10px;\">");
		sb.append("<colgroup><col style=\"width:18%;\"><col style=\"width:32%;\"><col style=\"width:18%;\"><col style=\"width:32%;\"></colgroup>");
		sb.append("<tr>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">신청번호</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(reqNo)).append("</td>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">구분</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(isResult ? "휴일대체근무 결과보고" : "휴일대체근무 신청서").append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">신청자</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(reqNm)).append("</td>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">부서</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(deptNm)).append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">근무일자</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(holidayDt)).append("</td>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">계획시간</th>");
		sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(planTm)).append("</td>");
		sb.append("</tr>");
		if (isResult) {
			sb.append("<tr>");
			sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">실제근무시간</th>");
			sb.append("<td colspan=\"3\" style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(realTm)).append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">근무결과</th>");
			sb.append("<td colspan=\"3\" style=\"border:1px solid #ddd; padding:8px; white-space:pre-wrap;\">").append(escapeHtml(workResult)).append("</td>");
			sb.append("</tr>");
		}
		sb.append("<tr>");
		sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">사유</th>");
		sb.append("<td colspan=\"3\" style=\"border:1px solid #ddd; padding:8px; white-space:pre-wrap;\">").append(escapeHtml(reason)).append("</td>");
		sb.append("</tr>");
		if (projectList != null && !projectList.isEmpty()) {
			sb.append("<tr><th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">작업 프로젝트</th>");
			sb.append("<td colspan=\"3\" style=\"border:1px solid #ddd; padding:8px;\"><table style=\"width:100%; border-collapse:collapse; table-layout:fixed;\"><colgroup><col style=\"width:12.5%;\"><col style=\"width:25%;\"><col style=\"width:50%;\"><col style=\"width:12.5%;\"></colgroup>");
			sb.append("<tr><th style=\"background:#f9f9f9; border:1px solid #ddd; padding:6px; text-align:center;\">프로젝트</th><th style=\"background:#f9f9f9; border:1px solid #ddd; padding:6px; text-align:center;\">고객사</th><th style=\"background:#f9f9f9; border:1px solid #ddd; padding:6px; text-align:center;\">설비</th><th style=\"background:#f9f9f9; border:1px solid #ddd; padding:6px; text-align:center;\">비고</th></tr>");
			for (Map<String, String> project : projectList) {
				sb.append("<tr><td style=\"border:1px solid #ddd; padding:6px; text-align:center;\">").append(escapeHtml(project.get("pjtNm"))).append("</td><td style=\"border:1px solid #ddd; padding:6px;\">")
					.append(escapeHtml(project.get("clntNm"))).append("</td><td style=\"border:1px solid #ddd; padding:6px;\">")
					.append(escapeHtml(project.get("eqpNm"))).append("</td><td style=\"border:1px solid #ddd; padding:6px;\">")
					.append(escapeHtml(project.get("etc"))).append("</td></tr>");
			}
			sb.append("</table></td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private String escapeHtml(String str) {
		if (str == null) return "";
		return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}
}
