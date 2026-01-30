package com.dksys.biz.user.wb.wb26.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.dksys.biz.user.wb.wb26.mapper.WB26Mapper;
import com.dksys.biz.user.wb.wb26.service.WB26Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB26SvcImpl implements WB26Svc {
	
	@Autowired
	WB26Mapper wb26Mapper;
	
	@Autowired
	WB22Mapper wb22Mapper;
	
	@Autowired
	WB26Svc wb26Svc;

	// 그리드 카운트
	@Override
	public int select_wb26_Count(Map<String, String> paramMap) {
		return wb26Mapper.select_wb26_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb26_List(Map<String, String> paramMap) {
		return wb26Mapper.select_wb26_List(paramMap);
	}
	
	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb06_List(Map paramMap) {
		return wb26Mapper.select_wb06_List(paramMap);
	}

	@Override
	public int wb26save(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("I".equals(dataChk)) {
				//데이터 처리
				result = wb26Mapper.insert_wb26(dtl);
	    	}

			if ("U".equals(dataChk)) {
				//데이터 처리
				result = wb26Mapper.update_wb26(dtl);
				//i++;
	    	}
	    }
		return result;
	}

	@Override
	public int update_wb26_confirmYn(Map<String, String> paramMap) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);
		
		int result = 0;
		
		//upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			
			result += wb26Mapper.update_wb26_confirmYn(dtl);
		}
		return result;
	}

	@Override
	public int update_wb26(Map<String, String> paramMap) {
		return wb26Mapper.update_wb26(paramMap);
	}

	// @Override
	// public List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap) {
	// 	return cr05Mapper.selectPmntmtdCd(paramMap);
	// }
	
	@Override
	public int select_wb2602_List_Count(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2602_List_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb2602_List(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2602_List(paramMap);
	}
	
	@Override
	public int select_wb2603_List_Count(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2603_List_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb2603_List(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2603_List(paramMap);
	}
	

	@Override
	public List<Map<String, String>> select_wb2604_List(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2604_List(paramMap);
	}
	

	@Override
	public List<Map<String, String>> select_wb2605_List(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2605_List(paramMap);
	}

	@Override
	public List<Map<String, String>> select_wb2605_metaList(Map<String, String> paramMap) {
		return wb26Mapper.select_wb2605_metaList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsTaskTempletGantList(Map<String, String> paramMap) {
		return wb26Mapper.selectWbsTaskTempletGantList(paramMap);
	}
	


	@Override
	public int updateWbsRemarks(Map<String, String> paramMap) {
		return wb26Mapper.updateWbsRemarks(paramMap);
	}
	
	
	@Override
	public int updateWbsSchedule(Map<String, String> paramMap) throws Exception {
		/************************************************************************** 
		 * 프론트엔드에서 간트차트를 이용해서 일정계획 수정 작업 완료후 
		 * 백엔드로 수정내역을 일괄 전송하여 처리하는 업무 Process 임.
		 **************************************************************************/
		
		
		/* WBS 실적자료에 문제가 등로되면 삭제 불가
		 * 간트 Bar에서는 담당자 삭제나 수정기능은 없으므로 문제 체크 필요업음 */
//		int wbsIssueExistChk = wb22Mapper.wbsIssueExistChk(paramMap);
//		if (wbsIssueExistChk > 0) {
//			throw new RuntimeException("이미 문제가 등록되어 있어 삭제할 수 없습니다.");
//		}

		/**************************************** 
		 * 일정계획 수립 버젼업처리
		 * 1. 확정되지 않은 자료는 백업 Pass
		 * 2. TB_WB22M01 에서 salesCd 가 동일한 자료 TB_WB22M01_HIST 테이블에 Insert
		 * 3. TB_WB22M01 에서 salesCd 가 동일한 자료의 VER_NO = VER_NO + 1 로 Update
		 ****************************************/
		
		
		/**************************************** 
		 * 상세일정 자료 Update 처리
		 * 
		 * roop 시작
		 *    SalesCd 가 바뀌면 버젼업 처리 (버젼 변경 조건 만족시 처리, else 오류 exception 처리)
		 *    일정 Update 또는 insert
		 * endroop
		 * 
		 * 자료구조 Sample
		 * paramMap = {
		 *    			pgmId=WB0603M02, 
		 *   			userId=js.nam,
		 *   			row=[{
	     *                    "fileTrgtKey": "22214",
	     *                    "sjFileTrgtKey": "2731",
	     *                    "wbsCloseYn": "Y",
	     *                    "clntNm": "(주)서연이화 아산공장",
	     *                    "clntPjtNm": "BJ",
	     *                    "salesCd": "25075-16BJNUP",
	     *                    "deptCd": "S07",
	     *                    "deptNm": "생산",
	     *                    "wbsPlansDtFm": "2025-12-18",
	     *                    "wbsPlaneDtFm": "2026-01-15",
	     *                    "remarks": ""
		 *       		}]
		 * 			}
		 ****************************************/
		
		

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("row"), dtlMap);

		String userId = paramMap.get("userId");
		String pgmId = paramMap.get("pgmId");
		String coCd = paramMap.get("coCd");

		Map<String, String> saveMap = new HashMap<>();
		saveMap.put("userId", userId);
		saveMap.put("pgmId", pgmId);
		saveMap.put("coCd", coCd);

		String processe_salesCd = "";
		String saved_salesCd = "";
		
		int result = 0;
		for(Map<String, String> dtl : detailMap) {
			processe_salesCd = dtl.get("salesCd");
			// SalesCd 가 바뀌면 버젼업 처리
//			if (!processe_salesCd.equals(saved_salesCd)) {
//				saveMap.put("salesCd", processe_salesCd);
//				int copyCnt = wb26Mapper.insertWbsScheduleHIST(saveMap);
//				if (copyCnt > 0) result += wb26Mapper.updateWbsScheduleVersionUp(saveMap);
//			}
			dtl.put("userId", userId);
			dtl.put("pgmId", pgmId);
			dtl.put("coCd", coCd);

            String processe_deptCd = dtl.get("deptCd");
            if ("S01".equals(processe_deptCd) ) {  //SQL문장 select_wb06_List  -> DEPT_TBL 참조 (수주확정, 목표원가/PFU배포)
            	String[] wbsCodes = {"WBSCODE01", "WBSCODE02"}; //(수주확정, 목표원가/PFU배포)
            	for (String code : wbsCodes) {
            	    dtl.put("wbsPlanCodeId", code);
            	    result += wb26Mapper.updateWbsScheduleVersionUpWbsCode(dtl);
            	}
            } else if ("S04".equals(processe_deptCd)) {  //SQL문장 select_wb06_List  -> DEPT_TBL 참조 (
            	String[] wbsCodes = {"WBSCODE17", "WBSCODE04"}; //모델링완료, 구매완료
            	for (String code : wbsCodes) {
            	    dtl.put("wbsPlanCodeId", code);
            	    result += wb26Mapper.updateWbsScheduleVersionUpWbsCode(dtl);
            	}
            } else if ("S09".equals(processe_deptCd)) {  //SQL문장 select_wb06_List  -> DEPT_TBL 참조
            	String[] wbsCodes = {"WBSCODE07", "WBSCODE16"}; //자체시운전완료, 외부제작검수
            	for (String code : wbsCodes) {
            	    dtl.put("wbsPlanCodeId", code);
            	    result += wb26Mapper.updateWbsScheduleVersionUpWbsCode(dtl);
            	}
            } else if ("S14".equals(processe_deptCd)) {  //SQL문장 select_wb06_List  -> DEPT_TBL 참조
            	String[] wbsCodes = {"WBSCODE13", "WBSCODE14"}; //설치시운전(출발일), 설치시운전(복귀일)
            	for (String code : wbsCodes) {
            	    dtl.put("wbsPlanCodeId", code);
            	    result += wb26Mapper.updateWbsScheduleVersionUpWbsCode(dtl);
            	}
            } else {
            	result += wb26Mapper.updateWbsSchedule(dtl);
            }
			
			saved_salesCd = processe_salesCd;
		}
		return result;
	}
	

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb0603p_List(Map paramMap) {
		return wb26Mapper.select_wb0603p_List(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb0603p_Problem_List(Map paramMap) {
		return wb26Mapper.select_wb0603p_Problem_List(paramMap);
	}
	

	@Override
	public int updateWbsLevel2PlanGantt(Map<String, String> paramMap) throws Exception {

		int result = 0;

		//paramMap.get("fileTrgtKey") 가 DB에 있으면 변경
		//없으면 신규 Insert 처리
		Map<String, String> level2IsExist = wb26Mapper.select_wb0603p_fileTrgtKey_List(paramMap);
		String fileTrgtKeyIsExist = null;
		if (level2IsExist != null) {
			fileTrgtKeyIsExist = level2IsExist.get("fileTrgtKey");
		}
		
		String paramFileTrgtKey = paramMap.get("fileTrgtKey");
		
		if (paramFileTrgtKey != null && paramFileTrgtKey.length() > 0 && fileTrgtKeyIsExist != null	&& fileTrgtKeyIsExist.length() > 0) {
			result = wb22Mapper.wbsLevel2Update(paramMap);
		} else {
			paramMap.put("coCd", paramMap.get("coCd"));
			//계획번호 생성 TB_WB22M01 WBS_PLAN_NO + 1, 20260000001
			String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
			paramMap.put("wbsPlanNo", wbsPlanNo);
			paramMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
			//코드ID 생성 TB_WB22M01 WBS_PLAN_CODE_ID + 1, WBSCODE0401, WBSCODE0402
			int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);

			String codeId = "";
			if (wbsPlanCodeId < 10) {
				codeId = "0" + String.valueOf(wbsPlanCodeId);
			} else {
				codeId = String.valueOf(wbsPlanCodeId);
			}
			paramMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);

			String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
			paramMap.put("fileTrgtKey", fileTrgtKey);
			result = wb22Mapper.wbsLevel2Insert(paramMap);
		}

		// 해당 계획에 대한 모든 실적과 Task 계획이 삭제되었으면 도면관리 대장에도 삭제
//		int wbsRsltsLevel2Count = wb22Mapper.selectWbsRsltsLevel2Count(paramMap);
//		if (wbsRsltsLevel2Count ==  0 && "WBSCODE03".equals(paramMap.get("wbsPlanCodeKind"))) {
//			paramMap.put("dwgNo", paramMap.get("salesCd"));
//			result += dw02Mapper.deleteDrawDocItem(paramMap);
//		}

		return result;
	}

	

	@Override
	public int updateWbsLevel2ActGantt(Map<String, String> paramMap) throws Exception {
		int result = 0;

		//지연이면 지연완료예정일 업데이트
		

		// 실적 저장 전 상태 및 지연 여부 자동 계산
		String wbsPlaneDt = paramMap.get("wbsPlaneDt");     // 계획종료일
		String wbsRsltseDt = paramMap.get("wbsRsltseDt");   // 실적종료일
		String wbsRsltsRate = paramMap.get("wbsRsltsRate"); // 진척율
		String wbsPlanStsCodeId = paramMap.get("wbsPlanStsCodeId");

		// 1. 일자 비교를 통한 지연 여부 판단 (YYYY-MM-DD 또는 YYYYMMDD 등 포맷에 상관없도록 숫자만 추출하여 비교)
		boolean isOverdue = false;
		if(wbsPlaneDt != null && wbsRsltseDt != null) {
			String planeDtClean = wbsPlaneDt.replaceAll("[^0-9]", "");
			String rsltseDtClean = wbsRsltseDt.replaceAll("[^0-9]", "");
			
			if(planeDtClean.length() >= 8 && rsltseDtClean.length() >= 8) {
				// 앞에 8자리(YYYYMMDD)만 비교
				if(rsltseDtClean.substring(0,8).compareTo(planeDtClean.substring(0,8)) > 0) {
					isOverdue = true;
				}
			}
		}

		// 2. 진척율 및 지연 여부에 따른 상태 코드(wbsPlanStsCodeId) 조정
		if ("100".equals(wbsRsltsRate)) {
			if (isOverdue) {
				wbsPlanStsCodeId = "WBSPLANSTS40"; // 지연완료
			} else {
				wbsPlanStsCodeId = "WBSPLANSTS50"; // 정상완료
			}
		} else {
			// 진척율이 100%가 아닌 경우 실적종료일은 저장하지 않음 (비즈니스 룰)
			paramMap.put("wbsRsltseDt", "");
			
			if (isOverdue) {
				wbsPlanStsCodeId = "WBSPLANSTS30"; // 지연
			} else if (wbsRsltsRate != null && !"0".equals(wbsRsltsRate) && !"".equals(wbsRsltsRate)) {
				// 진척율이 있고 100% 미만이며 지연이 아닌 경우 진행중 상태 유지
				if (!"WBSPLANSTS30".equals(wbsPlanStsCodeId) && !"WBSPLANSTS40".equals(wbsPlanStsCodeId) && !"WBSPLANSTS50".equals(wbsPlanStsCodeId)) {
					wbsPlanStsCodeId = "WBSPLANSTS20"; // 진행중
				}
			}
		}

		paramMap.put("wbsPlanStsCodeId", wbsPlanStsCodeId);

		// 3. 지연 상태(WBSPLANSTS30)가 아닐 경우 지연완료예정일(revisedFinishDt) 초기화
		if (!"WBSPLANSTS30".equals(wbsPlanStsCodeId)) {
			paramMap.put("revisedFinishDt", "");
		}
		wb26Mapper.updateWbsLevel2MetaGantt_revisedFinishDt(paramMap);
		//실적 고유번호가 0이면 Insert
		//실적 고유번호가 0이 아니면 Update
		int rsltsFileTrgtKey = Integer.parseInt(paramMap.get("rsltsFileTrgtKey"));
		if(rsltsFileTrgtKey < 1) {
			rsltsFileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
			paramMap.put("rsltsFileTrgtKey", Integer.toString(rsltsFileTrgtKey));

			paramMap.put("wbsPlanCodeId2_P", paramMap.get("taskId"));
			paramMap.put("wbsPlanCodeKind2_P", paramMap.get("wbsPlanCodeKind"));
			paramMap.put("salesCd2_P", paramMap.get("salesCd"));
			paramMap.put("wbsRsltsId", paramMap.get("wbsPlanMngId"));

			
			result =  wb22Mapper.wbsRsltsInsert(paramMap);
		} else {
			result = wb22Mapper.wbsRsltsUpdate(paramMap);
		}

		return result;
	}

	@Override
	public int updateWbsLevel2MetaGantt(Map<String, String> paramMap) throws Exception {
		int result = updateWbsLevel2PlanGantt(paramMap);
		result += updateWbsLevel2ActGantt(paramMap);
		
		return result;
	}

	@Override
	public int deleteWbsLevel2Gantt(Map<String, String> paramMap) throws Exception {
		return wb22Mapper.wbsLevel2Delete(paramMap);
	}

	@Override
	public int deleteWbsLevel2GanttAct(Map<String, String> paramMap) throws Exception {
		
		//실적저장된 자료가 아니면 패스처리
		int rsltsFileTrgtKey = Integer.parseInt(paramMap.get("rsltsFileTrgtKey"));
		if(rsltsFileTrgtKey < 1) {
			return 1;
		}
		// 실적 데이터 삭제 전 관련 문제(Issue) 등록 여부 체크
		paramMap.put("wbsPlanCodeId", paramMap.get("taskId"));
		int wbsIssueExistChk = wb22Mapper.wbsIssueExistChk(paramMap);
		if (wbsIssueExistChk > 0) {
			throw new RuntimeException("이미 문제가 등록되어 있어 삭제할 수 없습니다.");
		}

		return wb22Mapper.wbsRsltsDelete(paramMap);
	}

}
