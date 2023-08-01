package com.dksys.biz.user.sm.sm07.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm07.mapper.SM07Mapper;
import com.dksys.biz.user.sm.sm07.service.SM07Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM07SvcImpl implements SM07Svc {

	@Autowired
	SM07Mapper sm07Mapper;

	@Autowired
	SM07Svc sm07Svc;

	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	ExceptionThrower thrower;

	
	@Override
	public int selectOrderListCount(Map<String, String> paramMap) {
		return sm07Mapper.selectOrderListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrderList(Map<String, String> paramMap) {
		return sm07Mapper.selectOrderList(paramMap);
	}	
	
	/* 발주등록수정시 bom list */
	@Override
	public List<Map<String, String>> selectOrderDetailList(Map<String, String> paramMap) {
		return sm07Mapper.selectOrderDetailList(paramMap);
	}	
	
	
	//발주관리 구매 bom 수정	
	@Override
	public int updateOrderDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		// Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		int result = 0;
		
		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("U".equals(dataChk)) {
				//데이터 처리
	    		result = sm07Mapper.updateOrderDetail(dtl);
	    	} 
	    }			   
		
		return result;
	}
	
	
	/*
	 * 마스터 페이지 조회
	 * 
	 * @Override public int master_grid_selectCount(Map<String, String> paramMap) {
	 * return sm07Mapper.master_grid_selectCount(paramMap); }
	 * 
	 * @Override public List<Map<String, String>> master_grid_selectList(Map<String,
	 * String> paramMap) { return sm07Mapper.master_grid_selectList(paramMap); } 마스터
	 * 페이지 조회
	 * 
	 * //모달 그리드 조회 -수정화면 정보
	 * 
	 * @Override public Map<String, String> select_sm07_Info(Map<String, String>
	 * paramMap) { return sm07Mapper.select_sm07_Info(paramMap); }
	 * 
	 * //모달창 안에 그리드 grid-modal
	 * 
	 * @Override public List<Map<String, String>> grid_Modal_selectList(Map<String,
	 * String> paramMap) { return sm07Mapper.grid_Modal_selectList(paramMap); }
	 */

}