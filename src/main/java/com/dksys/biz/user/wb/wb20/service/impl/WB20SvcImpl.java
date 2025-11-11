package com.dksys.biz.user.wb.wb20.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm16.mapper.CM16Mapper;
import com.dksys.biz.user.im.im01.mapper.IM01Mapper;
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

	@Autowired
	WB20Mapper wb20Mapper;

	@Autowired
	WB24Mapper wb24Mapper;

	@Autowired
	QM01Mapper qm01Mapper;

	@Autowired
	CM16Mapper cm16Mapper;

    @Autowired
    IM01Mapper im01Mapper;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectToDoCount(Map<String, String> paramMap) {
//		return wb20Mapper.selectToDoCount(paramMap);
		return wb20Mapper.selectToDoCountNewSql(paramMap);
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
		result += wb20Mapper.updateApprovalLine(paramMap);
		/*
		 * paramMap 내용 { todoId=js.nam, todoCfOpn=, issNo=RES2400144, coCd=GUN, todoDiv1CodeId=TODODIV20, todoDiv2CodeId=TODODIV2030, pgmId=WB2001M01,
		 * salesCd=24000-00DUMMY, todoDiv1CodeNm=결재, todoDiv2CodeNm=발주/출장요청(결과), todoFileTrgtKey=1287, todoTitl=(주)건양아이티티-기타정상발주및출장요청서결과,
		 * userId=js.nam, todoKey=8504, sanctnSn=1, todoNo=RES2400144 }
		 */
		String tempReqNo = paramMap.get("todoNo");
		String todoDiv2CodeId = paramMap.get("todoDiv2CodeId");
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
			if ("TEAM01".equals(paramMap.get("actTeamManager"))) {
    			if ("Y".equals(paramMap.get("sameTimeResultChk"))) {
    				if ("GUN30".equals(paramMap.get("deptId")) ||
    					"GUN40".equals(paramMap.get("deptId")) ||
    					"TRN50".equals(paramMap.get("deptId")) ||
    					"GUN60".equals(paramMap.get("deptId"))) {
    						result += qm01Mapper.updateReqActMnRslt(paramMap);	// 결과자료 투입시간 업데이트
    					}
    			}
			}

			// TODODIV2030:발주 및 출장 요청 결과자료 상태코드 바꾸기
		} else if ("TODODIV2030".equals(todoDiv2CodeId)) {
			// REQ_ST: REQST02 --> REQST03 로 상태 변경처리
			paramMap.put("reqNo", "REQ" + tempReqNo.substring(3, 10));
			result += qm01Mapper.updateReqStRslt(paramMap);

			if ("TEAM01".equals(paramMap.get("actTeamManager"))) {
				if ("GUN30".equals(paramMap.get("deptId")) ||
					"GUN40".equals(paramMap.get("deptId")) ||
					"TRN50".equals(paramMap.get("deptId")) ||
					"GUN60".equals(paramMap.get("deptId"))) {
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
			if ("TEAM01".equals(paramMap.get("actTeamManager"))) { 
    				if ("GUN30".equals(paramMap.get("deptId")) ||
    					"GUN40".equals(paramMap.get("deptId")) ||
    					"TRN50".equals(paramMap.get("deptId")) ||
    					"GUN60".equals(paramMap.get("deptId"))) {
    						result += wb24Mapper.updateWbsIssueActMn(paramMap);	// 이슈조치 투입시간 업데이트
    				}
			}
		} else if ("TODODIV2130".equals(todoDiv2CodeId)) {
			// ISS_STS: ISSSTS01 --> ISSSTS02 로 상태 변경처리
			result += cm16Mapper.updateItoaIssueStChk(paramMap);
        } else if ("TODODIV2150".equals(todoDiv2CodeId)) { // 개선 제안서 작성부서 결재라인
            result += im01Mapper.updateImprvmStsCd(paramMap);
            result += im01Mapper.updateImprvmReqIdTxt(paramMap);
        } else if ("TODODIV2160".equals(todoDiv2CodeId)) { // 개선 제안서 조치부서 결재라인
            result += im01Mapper.updateExecStsCd(paramMap);
            result += im01Mapper.updateExecTeamIdTxt(paramMap);
		}



		// 최종결재 완료시 알림톡 발송 대상인지 확인
		Map<String, String> resultMap = wb20Mapper.selectTodoFinalYn(paramMap);
		resultMap.put("resultCount", Integer.toString(result));

		return resultMap;
	}
	
	/* 공통결재 보완요청 insert */
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
			// 결제라인 insert
			for (Map<String, String> dtl : detailMap) {
				// 입력, 수정
				String tempKey = dtl.get("todoKey");
				if (tempKey == null || tempKey.equals("")) {
					maxTodoKey = wb20Mapper.selectmaxTodoKey(dtl);
					dtl.put("todoKey", maxTodoKey);
				}
				dtl.put("createDttm", sysCreateDttm);
				result += wb20Mapper.insertTodoMaster(dtl);

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
	public Map<String, String> selectMobileTodoSelect(Map<String, String> paramMap) {
		return wb20Mapper.selectMobileTodoSelect(paramMap);
	}

	@Override
	public Map<String, String> selectTodoFinalYn(Map<String, String> paramMap) {
		return wb20Mapper.selectTodoFinalYn(paramMap);
	}

	@Override
	public int updateApprovalCancle(Map<String, String> paramMap) {
		int result = wb20Mapper.updateApprovalCancle(paramMap);

		
		/***************************************************************************************
		 * 결재 취소 처리시 팀장인경우에만 투입공수 Clear 처리 가능함 -- 처리시작
		 ***************************************************************************************/
		// 허용할 팀장 부서 접두어 목록 검사

		String deptId = paramMap.get("deptId");
		boolean isManagerDept = deptId != null && (deptId.startsWith("GUN30")|| deptId.startsWith("GUN40")|| deptId.startsWith("TRN50")|| deptId.startsWith("GUN60"));
		// deptId 로 팀장 id 가져오기~~
		Map<String, String> detailMap = wb24Mapper.selectDept2TeamManagerInfo(paramMap);
		
		if (detailMap.get("id").equals(paramMap.get("userId")) && isManagerDept) {
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
		/***************************************************************************************
		 * 결재 취소 처리시 팀장인경우에만 투입공수 Clear 처리 가능함 -- 처리종료
		 ***************************************************************************************/
		
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
}