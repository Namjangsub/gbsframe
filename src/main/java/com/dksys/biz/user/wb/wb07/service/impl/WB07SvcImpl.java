package com.dksys.biz.user.wb.wb07.service.impl;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.wb.wb07.service.WB07Svc;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.dksys.biz.user.wb.wb07.mapper.WB07Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB07SvcImpl implements WB07Svc {
	

	@Autowired
	WB07Mapper wb07Mapper;

	@Autowired
	WB22Mapper wb22Mapper;

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_wb07_List(Map paramMap) {
		return wb07Mapper.select_wb07_List(paramMap);
	}



	@Override
	public int updateWbsRemarks(Map<String, String> paramMap) {
		return wb07Mapper.updateWbsRemarks(paramMap);
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

//            result += wb07Mapper.updateWbsScheduleVersionUpWbsCode(dtl);
			result += wb07Mapper.updateWbsSchedule(dtl); //WBS_PLAN_CODE_KIND = 'WBSCODE'
			result += wb07Mapper.wbsLevel2PlanChange(dtl); //WBS_PLAN_CODE_KIND = #{wbsplancodeid}
			
			saved_salesCd = processe_salesCd;
		}
		return result;
	}

	@Override
	public int wbsLevel2PlanChange(Map<String, String> paramMap) throws Exception {
		// Level2(TASK) 계획/실적 동일하게 수정 반영
		int result = 0;
		result += wb07Mapper.wbsLevel2PlanChange(paramMap);
		result += wb07Mapper.wbsLevel2ActChange(paramMap);
		return result;
	}

	

	@Override
	public Map<String, String> selectWb22d02ExtraInfo(Map<String, String> paramMap) {
		return wb07Mapper.selectWb22d02ExtraInfo(paramMap);
	}

	@Override
	public Map<String, Object> updateWb22d02ExtraInfo(Map<String, String> paramMap) throws Exception {
		Map<String, Object> out = new HashMap<>();

		final String coCd = String.valueOf(paramMap.getOrDefault("coCd", ""));
		final String salesCd = String.valueOf(paramMap.getOrDefault("salesCd", ""));
		final String userId = String.valueOf(paramMap.getOrDefault("userId", ""));
		final String pgmId = String.valueOf(paramMap.getOrDefault("pgmId", ""));
		final String udtDttm = String.valueOf(paramMap.getOrDefault("udtDttm", ""));

		if (coCd.isEmpty() || salesCd.isEmpty() || userId.isEmpty() || pgmId.isEmpty()) {
			throw new RuntimeException("필수 파라미터가 누락되었습니다.");
		}

		int updated = wb07Mapper.updateWb22d02ExtraInfo(paramMap);
		if (updated == 1) {
			Map<String, String> latest = wb07Mapper.selectWb22d02ExtraInfo(paramMap);
			out.put("resultCode", 200);
			out.put("result", latest);
			return out;
		}

		Map<String, String> current = wb07Mapper.selectWb22d02ExtraInfo(paramMap);

		if (current != null && !current.isEmpty() && udtDttm.isEmpty()) {
			String currentUdt = String.valueOf(current.getOrDefault("udtDttm", current.getOrDefault("UDT_DTTM", "")));
			if (!currentUdt.isEmpty()) {
				paramMap.put("udtDttm", currentUdt);
				int retried = wb07Mapper.updateWb22d02ExtraInfo(paramMap);
				if (retried == 1) {
					Map<String, String> latest = wb07Mapper.selectWb22d02ExtraInfo(paramMap);
					out.put("resultCode", 200);
					out.put("result", latest);
					return out;
				}
			}
		}

		if (current == null || current.isEmpty()) {
			try {
				int inserted = wb07Mapper.insertWb22d02ExtraInfo(paramMap);
				if (inserted == 1) {
					Map<String, String> latest = wb07Mapper.selectWb22d02ExtraInfo(paramMap);
					out.put("resultCode", 200);
					out.put("result", latest);
					return out;
				}
			} catch (Exception e) {
				Map<String, String> after = wb07Mapper.selectWb22d02ExtraInfo(paramMap);
				if (after != null && !after.isEmpty()) {
					out.put("resultCode", 409);
					out.put("resultMessage", "다른 사용자가 먼저 생성/수정했습니다. 최신 데이터를 다시 불러와 주세요.");
					out.put("result", after);
					return out;
				}
				throw e;
			}

			out.put("resultCode", 500);
			out.put("resultMessage", "저장에 실패했습니다.");
			return out;
		}

		out.put("resultCode", 409);
		out.put("resultMessage", "다른 사용자가 먼저 수정했습니다. 최신 데이터를 다시 불러와 주세요.");
		out.put("result", current);
		return out;
	}

	
	@Override
	public List<Map<String, String>> selectWb22d02ExtraInfoGrid(Map<String, Object> paramMap) {
		return wb07Mapper.selectWb22d02ExtraInfoGrid(paramMap);
	}

	@Override
	public Map<String, Object> updateWb22d02ExtraInfoBatch(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> out = new HashMap<>();
		String userId = String.valueOf(paramMap.getOrDefault("userId", ""));
		String pgmId = String.valueOf(paramMap.getOrDefault("pgmId", ""));
		String coCd = String.valueOf(paramMap.getOrDefault("coCd", ""));

		Object rawRows = paramMap.get("rows");
		if (!(rawRows instanceof List)) {
			throw new RuntimeException("rows 파라미터가 누락되었습니다.");
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rows = (List<Map<String, Object>>) rawRows;
		List<Map<String, String>> conflicts = new ArrayList<>();
		int successCount = 0;

		for (Map<String, Object> row : rows) {
			Map<String, String> upd = new HashMap<>();
			upd.put("coCd", toStr(row.get("coCd"), coCd));
			upd.put("salesCd", toStr(row.get("salesCd"), ""));
			upd.put("userId", userId);
			upd.put("pgmId", pgmId);
			upd.put("expectedUdtDttm", toStr(row.get("expectedUdtDttm"), ""));

			upd.put("designMng", toStr(row.get("designMng"), ""));
			upd.put("deliverDay", toStr(row.get("deliverDay"), ""));
			upd.put("modelClntNm", toStr(row.get("modelClntNm"), ""));
			upd.put("modelCompDt", toStr(row.get("modelCompDt"), ""));
			upd.put("outClntNm", toStr(row.get("outClntNm"), ""));
			upd.put("outCompDt", toStr(row.get("outCompDt"), ""));
			upd.put("camClntNm", toStr(row.get("camClntNm"), ""));
			upd.put("camCompDt", toStr(row.get("camCompDt"), ""));
			upd.put("machinMngNm", toStr(row.get("machinMngNm"), ""));
			upd.put("mmMemo", toStr(row.get("mmMemo"), ""));
			upd.put("dwgMemo", toStr(row.get("dwgMemo"), ""));
			upd.put("prdMemo", toStr(row.get("prdMemo"), ""));
			upd.put("salesMemo", toStr(row.get("salesMemo"), ""));
			upd.put("pmMemo", toStr(row.get("pmMemo"), ""));
			upd.put("remark", toStr(row.get("remark"), ""));

			if (upd.get("coCd").isEmpty() || upd.get("salesCd").isEmpty()) {
				continue;
			}

			int updated = wb07Mapper.updateWb22d02ExtraInfo(upd);
			if (updated == 1) {
				successCount++;
				continue;
			}

			Map<String, String> cur = wb07Mapper.selectWb22d02ExtraInfo(upd);
			if (cur == null || cur.isEmpty()) {
				try {
					int inserted = wb07Mapper.insertWb22d02ExtraInfo(upd);
					if (inserted == 1) {
						successCount++;
						continue;
					}
				} catch (Exception e) {
					Map<String, String> after = wb07Mapper.selectWb22d02ExtraInfo(upd);
					if (after != null && !after.isEmpty()) {
						conflicts.add(after);
					}
					continue;
				}
				continue;
			}

			conflicts.add(cur);
		}

		out.put("successCount", successCount);
		out.put("conflictCount", conflicts.size());
		out.put("conflicts", conflicts);
		if (conflicts.isEmpty()) {
			out.put("resultCode", 200);
			out.put("resultMessage", "저장되었습니다.");
		} else {
			out.put("resultCode", 409);
			out.put("resultMessage", "일부 행이 다른 사용자에 의해 선행 수정되었습니다.");
		}
		return out;
	}


	private String toStr(Object v, String dft) {
		if (v == null) return dft;
		String s = String.valueOf(v).trim();
		return s.isEmpty() ? dft : s;
	}
	
	@Override
	public int updateWbsLevel2ActGantt(Map<String, String> paramMap) throws Exception {
		int result = 0;

		//지연이면 지연완료예정일 업데이트
		// 지연 상태코드
		// WBSPLANSTS10 : 계획
		// WBSPLANSTS20 : 진행중
		// WBSPLANSTS30 : 지연
		// WBSPLANSTS40 : 지연완료
		// WBSPLANSTS50 : 정상완료

		// 실적 저장 전 상태 및 지연 여부 자동 계산
		String wbsPlaneDt = paramMap.get("wbsPlaneDt");     // 계획종료일
		String wbsRsltseDt = paramMap.get("wbsRsltseDt");   // 실적종료일
		String wbsRsltsRate = paramMap.get("wbsRsltsRate"); // 진척율
		String wbsPlanStsCodeId = "WBSPLANSTS50";

		// 1. 일자 비교를 통한 지연 여부 판단 (YYYY-MM-DD 또는 YYYYMMDD 등 포맷에 상관없도록 숫자만 추출하여 비교)
		boolean isOverdue = false;
		 if (wbsRsltseDt == null || "".equals(wbsRsltseDt)) {
			 wbsPlanStsCodeId = "WBSPLANSTS10";
		 }
		if(wbsPlaneDt != null && !wbsPlaneDt.trim().isEmpty()) {
			String planeDtClean = wbsPlaneDt.replaceAll("[^0-9]", "");
			String rsltseDtClean = (wbsRsltseDt != null) ? wbsRsltseDt.replaceAll("[^0-9]", "") : "";
			
			// rsltseDtClean 값이 없으면 오늘 날짜 기준으로 적용
			if(rsltseDtClean.isEmpty()) {
				rsltseDtClean = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
			}
			
			if(planeDtClean.length() >= 8 && rsltseDtClean.length() >= 8) {
				// 앞에 8자리(YYYYMMDD)만 비교 (planeDtClean < rsltseDtClean 인경우만 isOverdue = true 적용)
				if(planeDtClean.substring(0,8).compareTo(rsltseDtClean.substring(0,8)) < 0) {
					isOverdue = true;
				}
			}
		}

		// 2. 진척율 및 지연 여부에 따른 상태 코드(wbsPlanStsCodeId) 조정
		if ("100".equals(wbsRsltsRate)) {
			paramMap.put("planCloseYn", "Y");
			if (isOverdue) {
				wbsPlanStsCodeId = "WBSPLANSTS40"; // 지연완료
			} else {
				wbsPlanStsCodeId = "WBSPLANSTS50"; // 정상완료
			}
		} else {
			// 진척율이 100%가 아닌 경우 실적종료일은 저장하지 않음 (비즈니스 룰)
			paramMap.put("wbsRsltseDt", "");
			paramMap.put("planCloseYn", "N");
			
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
		result = wb07Mapper.updateWbsLevel2MetaGantt_revisedFinishDt(paramMap);
		
		return result;
	}

	
	@Override
	public int createActualUnconfirmed(Map<String, String> paramMap) throws Exception {
		if (!wbsOwnerChk(paramMap)) {
			throw new RuntimeException("담당자 또는 담당팀장외 실적 변경 할 수 없습니다.");
		}
		int result = 0;
		result += completeActualConfirmed(paramMap);
		return result;
	}
	

	@Override
	public int completeActualConfirmed(Map<String, String> paramMap) throws Exception {
		if (!wbsOwnerChk(paramMap)) {
			throw new RuntimeException("담당자 또는 담당팀장외 실적 변경 할 수 없습니다.");
		}
		int result = 0;

		// Task 계획 저장시 해당 과제가 확정(Y)이면서 Task등록하는 일정이있어야하며 해당 일정도 Y인지 Check
		paramMap.put("verNo", paramMap.get("planVerNo"));
		paramMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeId"));
		int wbsLevel2InsertChk = wb22Mapper.wbsLevel2InsertChk(paramMap);
		if (wbsLevel2InsertChk == 0) {
			throw new RuntimeException("Task 계획을 저장할 수 없습니다. 일정확인 후 다시 시도해주세요.");
		}
		

		String planFileTrgtKey = "";
		String wbsPlanNo = "";
		String codeId = "";
		//  Level2(TASK) 계획이 2개이상이거나 실적에 문제가 등록되어 있으면 삭제처리 불가
		//  각 단계별  하나의 Level2(TASK)계획과 실적은 또한 1건만으로 처리하고자함.
		//  이전 생성된 TASK 계획이 2건 이상이면 삭제하고 Level2(TASK) : 실적을= 1:1 관계 유지하기 위함
		int selectWbsPlanLevel2KindCount = wb07Mapper.selectWbsPlanLevel2KindCount(paramMap);
		int wbsIssueExistChk = wb22Mapper.wbsIssueExistChk(paramMap);
		if (selectWbsPlanLevel2KindCount > 1 && wbsIssueExistChk > 0) {
			throw new RuntimeException("Task에 등록된 문제가 있습니다.");
		} else if (selectWbsPlanLevel2KindCount == 1) {
			// Level2(TASK)계획이 1건이면
			List<Map<String, String>> planLevel2 = wb22Mapper.wbsPlanListChk(paramMap);
			planFileTrgtKey = planLevel2.get(0).get("fileTrgtKey");
			wbsPlanNo = planLevel2.get(0).get("fileTrgtKey");
			codeId = planLevel2.get(0).get("wbsPlanCodeId") + "01";
		} else {
			// Level2(TASK) 실적 삭제, 계획 삭제
			deleteActual(paramMap);
			
			codeId = paramMap.get("wbsPlanCodeId") + "01";
			planFileTrgtKey =  wb22Mapper.selectWbsSeqNext(paramMap);
			
			String currentYear = String.valueOf(LocalDate.now().getYear());
			paramMap.put("year", currentYear);
			wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
		}
		

		// TB_WB22M01 (Plan) 처리
		// level2 계획이 존재하면 계획과 동일하게 실적처리
		
		//        계획이 없은면 계획 신규 등록  및 확정처리
		//        계획과 동일한 실적처리 100%, Y,  입력된 시작일, 종료일로 처리

		paramMap.put("planFileTrgtKey", planFileTrgtKey);
		paramMap.put("fileTrgtKey", planFileTrgtKey);
		paramMap.put("wbsPlanNo", wbsPlanNo);
		paramMap.put("wbsPlanCodeId", codeId);

		paramMap.put("verNo", paramMap.get("planVerNo"));
		paramMap.put("wbsPlansDt", paramMap.get("wbsPlansDtFm"));
		paramMap.put("wbsPlaneDt", paramMap.get("wbsPlaneDtFm"));
		paramMap.put("creatId", paramMap.get("userId"));
		paramMap.put("creatPgm", paramMap.get("pgmId"));
		paramMap.put("verUpReason", "간트바에서 자동 생성");
		paramMap.put("revisedFinishDt", paramMap.get("wbsPlaneDtFm"));
		
		paramMap.put("wbsRsltssDt", paramMap.get("wbsPlansDtFm"));
		if (paramMap.get("action").equals("completeActualConfirmed")) {
			paramMap.put("wbsRsltsRate", "100");
			paramMap.put("wbsRsltseDt", paramMap.get("wbsPlaneDtFm"));
			paramMap.put("actCloseYn", "Y");
			paramMap.put("wbsPlanStsCodeId", "WBSPLANSTS50");
		} else {
			paramMap.put("wbsRsltsRate", "1");
			paramMap.put("wbsRsltseDt", "");
			paramMap.put("actCloseYn", "N");
			paramMap.put("wbsPlanStsCodeId", "WBSPLANSTS20");
		}


		// 워킹데이(토/일 제외) 계산 로직 추가
		long workingDays = calculateWorkingDays(paramMap.get("wbsPlansDtFm"), paramMap.get("wbsPlaneDtFm"));
		paramMap.put("wbsPlanMh", String.valueOf(workingDays));		//실적테이블 계획공수
		paramMap.put("wbsRsltsMh", String.valueOf(workingDays));	//실적테이블 실적공수
		paramMap.put("daycnt", String.valueOf(workingDays));		//계획테이블 소요일수
		paramMap.put("expectMh", String.valueOf(workingDays));		//계획테이블 계획공수
		paramMap.put("rsltsDaycnt", String.valueOf(workingDays)); 

		// Level2 계획(TB_WB22M01) 등록 및 확정
		result = wb07Mapper.updateWbsActualLevel2Insert(paramMap);


		// 1. 실적(TB_WB23M01) 용 fileTrgtKey 확보 (Insert 시 생성된 ID 확인을 위해 사전 처리)
		//    - 기존 자료가 있으면 해당 키 사용, 없으면 신규 시퀀스 생성
		String rsltsFileTrgtKey = wb07Mapper.selectWbsActualFileTrgtKey(paramMap);
		if (rsltsFileTrgtKey == null || rsltsFileTrgtKey.isEmpty()) {
//			rsltsFileTrgtKey = String.valueOf(wb22Mapper.selectWbsRstlsSeqNext(paramMap));
			rsltsFileTrgtKey = planFileTrgtKey;
		}
		paramMap.put("rsltsFileTrgtKey", rsltsFileTrgtKey); 
		
		// 2. 실적등록 및 완료처리 (위에서 확보한 fileTrgtKey가 MERGE의 Insert 시 사용됨)
		if (paramMap.get("action").equals("completeActualConfirmed")) {
			paramMap.put("wbsRsltssDt", paramMap.get("computedStart"));
			paramMap.put("wbsRsltseDt", paramMap.get("computedEnd"));
		}
		result += wb07Mapper.updateWbsActualComplete(paramMap);
		
		//상태변경
		// 실적 저장 전 상태 및 지연 여부 자동 계산
		updateWbsLevel2ActGantt(paramMap);


		return result;
	}

	@Override
	public int resetActualUnconfirmed(Map<String, String> paramMap) throws Exception {
		if (!wbsOwnerChk(paramMap)) {
			throw new RuntimeException("담당자 또는 담당팀장외 실적 변경 할 수 없습니다.");
		}
		int result = 0;
		paramMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeId"));
		paramMap.put("wbsRsltsRate", "1");
		paramMap.put("wbsRsltseDt", "");
		paramMap.put("wbsPlanStsCodeId", "WBSPLANSTS20");
		result += wb07Mapper.updateWbsActualReset(paramMap);
		

		//상태변경
		// 실적 저장 전 상태 및 지연 여부 자동 계산
		paramMap.put("planCloseYn", "N"); 
		result += updateWbsLevel2ActGantt(paramMap);
		
		return result;
	}

	@Override
	public int deleteActual(Map<String, String> paramMap) throws Exception {
		if (!wbsOwnerChk(paramMap)) {
			throw new RuntimeException("담당자 또는 담당팀장외 실적 삭제 할 수 없습니다.");
		}
		
		int result = 0;
		paramMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeId"));
		
		// 1) 실적(TB_WB24M02) 문제 등록시 삭제 불가
		//    관련 실적에 문제 등록되어 있으면  삭제 불가
		//     salesCd + wbsPlanCodeId 로 TB_WB24M02 자료 존재 체크
		int wbsLevel2InsertChk = wb22Mapper.wbsIssueExistChk(paramMap);
		if (wbsLevel2InsertChk > 0) {
			throw new RuntimeException("Task에 등록된 문제가 있습니다.");
		}
		

		// 2) 실적(TB_WB23M01) 삭제
		result += wb07Mapper.deleteWbsActual(paramMap);
		// 3) Level 2 계획 (TB_WB22M01) 삭제
		result += wb07Mapper.deleteWbsLevel2Plan(paramMap);
		
		//상태변경
//		paramMap.put("wbsPlanStsCodeId", "WBSPLANSTS20"); 
//		paramMap.put("planCloseYn", "N"); 
//		result += updateWbsLevel2ActGantt(paramMap);

		return result;
	}


	private boolean wbsOwnerChk(Map<String, String> paramMap) {

		String userId = paramMap.get("userId");
		if (userId.equals(paramMap.get("teamMngId")) || userId.equals(paramMap.get("wbsPlanMngId")) || userId.equals("js.nam")) {
			return true;
		}
		return false;
		
	}

	private long calculateWorkingDays(String startStr, String endStr) {
		if (startStr == null || endStr == null || startStr.isEmpty() || endStr.isEmpty()) return 0;
		try {
			// 숫자만 추출하여 yyyyMMdd 포맷으로 파싱
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate start = LocalDate.parse(startStr.replaceAll("[^0-9]", ""), formatter);
			LocalDate end = LocalDate.parse(endStr.replaceAll("[^0-9]", ""), formatter);
			
			if (start.isAfter(end)) return 0;
			
			long count = 0;
			LocalDate curr = start;
			while (!curr.isAfter(end)) {
				DayOfWeek dow = curr.getDayOfWeek();
				if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
					count++;
				}
				curr = curr.plusDays(1);
			}
			return count;
		} catch (Exception e) {
			return 0;
		}
	}
}
