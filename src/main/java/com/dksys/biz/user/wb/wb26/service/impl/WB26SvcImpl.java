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
	public List<Map<String, String>> select_wb06_List(Map<String, String> paramMap) {
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
			if (!processe_salesCd.equals(saved_salesCd)) {
				saveMap.put("salesCd", processe_salesCd);
				int copyCnt = wb26Mapper.insertWbsScheduleHIST(saveMap);
				if (copyCnt > 0) result += wb26Mapper.updateWbsScheduleVersionUp(saveMap);
			}
			dtl.put("userId", userId);
			dtl.put("pgmId", pgmId);
			dtl.put("coCd", coCd);
			
			result += wb26Mapper.updateWbsSchedule(dtl);
			saved_salesCd = processe_salesCd;
		}
		return result;
	}
}
