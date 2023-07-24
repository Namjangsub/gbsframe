package com.dksys.biz.user.sm.sm12.service.impl;

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
import com.dksys.biz.user.sm.sm12.mapper.SM12Mapper;
import com.dksys.biz.user.sm.sm12.service.SM12Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM12SvcImpl implements SM12Svc {

	@Autowired
	SM12Mapper sm12Mapper;	
	
	@Autowired
	SM12Svc sm12Svc;	
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectPurchaseListCount(Map<String, String> paramMap) {
		return sm12Mapper.selectPurchaseListCount(paramMap);
	}
	

	@Override
	public List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap) {
		return sm12Mapper.selectPurchaseList(paramMap);
	}	
		
	@Override
	public List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap) {
		return sm12Mapper.selectPurchaseExcelList(paramMap);
	}		

	@Override
	public String selectMaxPchsNo(Map<String, String> paramMap) {
		return sm12Mapper.selectMaxPchsNo(paramMap);
	}		
		  
	/* 입고 등록시 발주 list */
	@Override
	public List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap) {
		return sm12Mapper.selectPurchaseDetailList(paramMap);
	}			
	
	@Override
	public int insertPurchaseDetail(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
		
		int result = 0;	    

	    //MAX ORDGR_NO - 최조 입력일 경우만
		String maxPchsNo = "";
		String pchsNo = "";	 		
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxPchsNo", maxPchsNo);
			pchsNo = dtl.get("pchsNo").toString();
			//뒤 5자리
			pchsNo = pchsNo.substring(pchsNo.length()-5, pchsNo.length());
			if( pchsNo.equals("00000") ) {
				maxPchsNo = sm12Svc.selectMaxPchsNo(paramMap);
				dtl.put("maxPchsNo", maxPchsNo);
	    		result += sm12Mapper.insertPurchaseDetail(dtl);				
			} else if( dtl.get("actFlag").equals("U") ) {
	    		result += sm12Mapper.updatePurchaseDetail(dtl);				
			}
		}			
		return result;
	}
	
	//매입확정 DETAIL 수정
	@Override
	public int updatePurchaseDetail(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    	
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
    		result += sm12Mapper.updatePurchaseDetail(dtl);			
		}			

	    
		return result;
	}	
	
	@Override
	public int deletePurchaseDetail(Map<String, String> param) {
		return sm12Mapper.deletePurchaseDetail(param);
	}	
}