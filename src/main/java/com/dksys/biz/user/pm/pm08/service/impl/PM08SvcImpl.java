package com.dksys.biz.user.pm.pm08.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.pm.pm08.mapper.PM08Mapper;
import com.dksys.biz.user.pm.pm08.service.PM08Svc;
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
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> insertSubstituteWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, String> result = new HashMap<String, String>();

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
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> projectArr = gsonDtl.fromJson(paramMap.get("projectArr"), dtlMap);
			if (projectArr != null && !projectArr.isEmpty()) {
				for (Map<String, String> projectMap : projectArr) {
					projectMap.put("reqNo", reqNo);
					projectMap.put("coCd", paramMap.get("coCd"));
					pm08Mapper.insertSubstituteWorkProjectList(projectMap);
				}
			}

			// 5. 결재선 등록 (TODODIV2410: 신청결재, 공통 모듈 호출 규격 100% 주입)
			String approvalArrStr = paramMap.get("approvalArr");
			if (approvalArrStr != null && !approvalArrStr.isEmpty()) {
				List<Map<String, String>> approvalList = gsonDtl.fromJson(approvalArrStr, dtlMap);

				if (approvalList != null && !approvalList.isEmpty()) {
					for (Map<String, String> apprItem : approvalList) {
						apprItem.put("todoNo", reqNo);
						apprItem.put("salesCd", reqNo);

						String curCodeId = apprItem.get("todoDiv2CodeId");
						if (curCodeId == null || curCodeId.isEmpty()) {
							curCodeId = "TODODIV2410";
							apprItem.put("todoDiv2CodeId", curCodeId);
						}

						Map<String, Object> pgMap = new HashMap<>();
						pgMap.put("coCd", paramMap.get("coCd") != null ? paramMap.get("coCd") : "GUN");
						pgMap.put("reqNo", reqNo);
						pgMap.put("actionType", "A");
						pgMap.put("openStage", "REQ");
						pgMap.put("todoDiv2CodeId", curCodeId);
						pgMap.put("pgmId", "PM0801P01");

						apprItem.put("pgParam", gsonDtl.toJson(pgMap));
					}
					paramMap.put("approvalArr", gsonDtl.toJson(approvalList));
					paramMap.put("todoNo", reqNo);
					paramMap.put("todoDiv2CodeId", "TODODIV2410");
					paramMap.put("etcField1", reqNo);
					wb20Svc.insertTodoMaster(paramMap);
				}
			}

			// 6. 첨부파일 처리
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

				if (approvalList != null && !approvalList.isEmpty()) {
					for (Map<String, String> apprItem : approvalList) {
						apprItem.put("todoNo", reqNoVal);
						apprItem.put("salesCd", reqNoVal);

						String curCodeId = apprItem.get("todoDiv2CodeId");
						if (curCodeId == null || curCodeId.isEmpty()) {
							curCodeId = todoDiv2CodeId;
							apprItem.put("todoDiv2CodeId", curCodeId);
						}

						Map<String, Object> pgMap = new HashMap<>();
						pgMap.put("coCd", paramMap.get("coCd") != null ? paramMap.get("coCd") : "GUN");
						pgMap.put("reqNo", reqNoVal);
						pgMap.put("actionType", "A");
						pgMap.put("openStage", isResultStage ? "RESULT" : "REQ");
						pgMap.put("todoDiv2CodeId", curCodeId);
						pgMap.put("pgmId", "PM0801P01");

						apprItem.put("pgParam", gsonDtl.toJson(pgMap));
					}
					paramMap.put("approvalArr", gsonDtl.toJson(approvalList));

					// 기존 결재선 삭제
					Map<String, String> deleteParam = new HashMap<>();
					deleteParam.put("todoNo", reqNoVal);
					deleteParam.put("todoDiv2CodeId", todoDiv2CodeId);
					wb20Svc.deleteTodoMasterByTodoNo(deleteParam);

					// 신규 결재선 등록
					paramMap.put("todoNo", reqNoVal);
					paramMap.put("todoDiv2CodeId", todoDiv2CodeId);
					paramMap.put("etcField1", reqNoVal);
					wb20Svc.insertTodoMaster(paramMap);

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

		// 삭제 사전 검증: 근무결과가 이미 작성되었거나 신청자 외 결재가 이미 진행된 경우 삭제 불가
		if (currentDtl.get("workResult") != null && !currentDtl.get("workResult").toString().trim().isEmpty()) {
			result.put("resultCode", "500");
			result.put("resultMessage", "근무결과가 이미 등록된 건은 신청서를 삭제할 수 없습니다.");
			return result;
		}
		Map<String, String> reqCheckMap = new HashMap<>(paramMap);
		reqCheckMap.put("todoDiv2CodeId", "TODODIV2410");
		int approvedCnt = pm08Mapper.selectApprovedCountExceptApplicant(reqCheckMap);
		if (approvedCnt > 0) {
			result.put("resultCode", "500");
			result.put("resultMessage", "이미 신청자 외 결재가 진행된 건은 삭제할 수 없습니다.");
			return result;
		}

		// 2. 결재선 삭제 (신청결재, 결과결재 모두)
		Map<String, String> deleteParam1 = new HashMap<>();
		deleteParam1.put("todoNo", paramMap.get("reqNo"));
		deleteParam1.put("todoDiv2CodeId", "TODODIV2410");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam1);

		Map<String, String> deleteParam2 = new HashMap<>();
		deleteParam2.put("todoNo", paramMap.get("reqNo"));
		deleteParam2.put("todoDiv2CodeId", "TODODIV2420");
		wb20Svc.deleteTodoMasterByTodoNo(deleteParam2);

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

			// 의견이 없으면 저장할 내용 없음
			if (todoCfOpn == null || todoCfOpn.trim().isEmpty()) {
				return 1;
			}

			// 신청건 조회 - 신청자 ID 추출
			Map<String, String> queryParam = new HashMap<>();
			queryParam.put("coCd", coCd);
			queryParam.put("reqNo", reqNo);
			Map<String, String> reqDetail = pm08Mapper.selectSubstituteWorkDtl(queryParam);
			if (reqDetail == null || reqDetail.isEmpty()) {
				return 1;
			}

			String reqId = reqDetail.get("reqId");

			// 신청자의 담당팀장 정보 조회
			Map<String, String> managerParam = new HashMap<>();
			managerParam.put("userId", reqId);
			Map<String, String> managerInfo = wb24Svc.selectTeamManagerInfo(managerParam);

			// 담당팀장이 결재승인한 본인이 맞는지 확인
			if (managerInfo == null || managerInfo.isEmpty()) {
				return 1;
			}

			String managerId = managerInfo.get("id");
			if (managerId == null || !managerId.equals(todoId)) {
				return 1;
			}

			// 담당팀장이 맞으면 의견 저장
			Map<String, String> updateParam = new HashMap<>();
			updateParam.put("coCd", coCd);
			updateParam.put("reqNo", reqNo);
			updateParam.put("reqMngOpn", todoCfOpn);
			pm08Mapper.updateSubstituteWorkReqMngOpn(updateParam);

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

			// 의견이 없으면 저장할 내용 없음
			if (todoCfOpn == null || todoCfOpn.trim().isEmpty()) {
				return 1;
			}

			// 신청건 조회 - 신청자 ID 추출
			Map<String, String> queryParam = new HashMap<>();
			queryParam.put("coCd", coCd);
			queryParam.put("reqNo", reqNo);
			Map<String, String> reqDetail = pm08Mapper.selectSubstituteWorkDtl(queryParam);
			if (reqDetail == null || reqDetail.isEmpty()) {
				return 1;
			}

			String reqId = reqDetail.get("reqId");

			// 신청자의 담당팀장 정보 조회
			Map<String, String> managerParam = new HashMap<>();
			managerParam.put("userId", reqId);
			Map<String, String> managerInfo = wb24Svc.selectTeamManagerInfo(managerParam);

			// 담당팀장이 결재승인한 본인이 맞는지 확인
			if (managerInfo == null || managerInfo.isEmpty()) {
				return 1;
			}

			String managerId = managerInfo.get("id");
			if (managerId == null || !managerId.equals(todoId)) {
				return 1;
			}

			// 담당팀장이 맞으면 의견 저장
			Map<String, String> updateParam = new HashMap<>();
			updateParam.put("coCd", coCd);
			updateParam.put("reqNo", reqNo);
			updateParam.put("resultMngOpn", todoCfOpn);
			pm08Mapper.updateSubstituteWorkResultMngOpn(updateParam);

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
}
