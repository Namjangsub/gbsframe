package com.dksys.biz.user.wb.wb26.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
}
