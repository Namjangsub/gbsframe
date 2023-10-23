package com.dksys.biz.user.sm.sm14.service.impl;

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
import com.dksys.biz.user.sm.sm14.mapper.SM14Mapper;
import com.dksys.biz.user.sm.sm14.service.SM14Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM14SvcImpl implements SM14Svc {

	@Autowired
	SM14Mapper sm14Mapper;	
	
	@Autowired
	SM14Svc sm14Svc;	
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectPurchaseListCount(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseListCount(paramMap);
	}
	

	@Override
	public List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseList(paramMap);
	}	
		
	@Override
	public List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseExcelList(paramMap);
	}		

	@Override
	public String selectMaxPchsNo(Map<String, String> paramMap) {
		return sm14Mapper.selectMaxPchsNo(paramMap);
	}		
		  
	/* 입고 등록시 발주 list */
	@Override
	public List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseDetailList(paramMap);
	}			
	
	@Override
	public int insertPurchaseBillDetail(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
		
		int result = 0;	    

	    //MAX ORDGR_NO - 최조 입력일 경우만
		String maxPchsNo = "";
		String pchsNo = "";	 		
		String preOrdrgNo = "";
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxPchsNo", maxPchsNo);
			pchsNo = dtl.get("pchsNo").toString();
			//뒤 5자리
			pchsNo = pchsNo.substring(pchsNo.length()-5, pchsNo.length());
			//같은 발주번호인지 비교를 위해서 쓴다.
			if( pchsNo.equals("00000") || dtl.get("actFlag").equals("I") ) {
				//최초 실행이거나 이전발주번호 현재 발주번호가 다를때 신규매입번호 select
				if( !preOrdrgNo.equals(dtl.get("ordrgNo")) || preOrdrgNo.equals("") ) {					
					maxPchsNo = sm14Svc.selectMaxPchsNo(paramMap);					
					preOrdrgNo = dtl.get("ordrgNo");			
				} else if( preOrdrgNo.equals(dtl.get("ordrgNo")) ){

				}
				dtl.put("maxPchsNo", maxPchsNo);
	    		result += sm14Mapper.insertPurchaseBillDetail(dtl);
				//TB_SM12M01 TB_SM12M01 UPDATE	- DETAIL 에서 처리 사용안함(20231005)
				/*
				if( dtl.get("cmpletYn").equals("Y") ) {
		    		sm14Mapper.updatePurchaseMaster(dtl);    			
				}
				*/
			}	
			if( dtl.get("actFlag").equals("U") ) {
				System.out.println("cmpletYn>>>" + dtl.get("cmpletYn")+"<<<");
				dtl.put("maxPchsNo", dtl.get("pchsNo"));				
	    		result += sm14Mapper.updatePurchaseBillDetail(dtl);			    		
			}				
			//TB_SM12M01 TB_SM12M01 UPDATE - DETAIL 에서 처리 사용안함(20231005)
			/*
			if( dtl.get("cmpletYn").equals("Y") ) {
	    		sm14Mapper.updatePurchaseMaster(dtl);    			
			}
			*/				
		}
		return result;
	}
	
	//매입확정 DETAIL 수정
	@Override
	public int updatePurchaseBillDetail(Map<String, String> paramMap) throws Exception {

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
			
    		//result += sm14Mapper.updatePurchaseBillDetail(dtl);	
    		
    		//TB_SM12M01 TB_SM12M01 UPDATE -- 사용안함
    		if( paramMap.get("cmpletYn").equals("Y") ) {
        		//sm14Mapper.updatePurchaseMaster(dtl);    			
    		}
		}			

	    
		return result;
	}	
	
	@Override
	public int deletePurchaseDetail(Map<String, String> param) {
		int result = 0;
		result += sm14Mapper.deletePurchaseDetail(param);
		result += sm14Mapper.deletePurchaseMaster(param);
		return result;
	}	
	
	//세금계산서발행여부 
	@Override
	public int updateBillYn(Map<String, String> paramMap) throws Exception {

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
			
    		result += sm14Mapper.updateBillYn(dtl);	
    
		}			

		return result;
	}	
}