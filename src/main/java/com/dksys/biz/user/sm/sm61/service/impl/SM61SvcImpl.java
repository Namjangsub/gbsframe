package com.dksys.biz.user.sm.sm61.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.sm.sm61.mapper.SM61Mapper;
import com.dksys.biz.user.sm.sm61.service.SM61Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM61SvcImpl implements SM61Svc {

	@Autowired
	SM61Mapper sm61Mapper;
	
	@Autowired
	SM61Svc sm61Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;


	@Override
	public List<Map<String, String>> selectVendInitEstimateList(Map<String, String> paramMap) {
		return sm61Mapper.selectVendInitEstimateList(paramMap);
	}	

		
	//multi select 검색
	@Override
	public List<Map<String, String>> select_mngId_code(Map<String, String> paramMap) {
		return sm61Mapper.select_mngId_code(paramMap);
	}

	//평가점수 Update
	@Override
	public int updateVendInitEstimate(Map<String, String> param) {
		//Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		//데이터처리 시작
		int result = 0;
			
		//상세 납기일자 수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(param.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", param.get("userId"));
	    	dtl.put("pgmId", param.get("pgmId"));
			
			result = sm61Mapper.updateVendInitEstimate(dtl); // insert or update 처리 해야함.
	    }
		//데이터 처리 끝
		return result;
	}	

	@Override
	public int deleteVendInitEstimate(Map<String, String> paramMap) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		//데이터처리 시작
		int result = 0;
		//평가내역 삭제
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			result = sm61Mapper.deleteVendInitEstimate(dtl); // 삭제
	    }	  
		return result;
	}
}