package com.dksys.biz.user.bm.bm13.service.impl;

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
import com.dksys.biz.user.bm.bm13.mapper.BM13Mapper;
import com.dksys.biz.user.bm.bm13.service.BM13Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM13SvcImpl implements BM13Svc {

	@Autowired
	BM13Mapper bm13Mapper;
	
	@Autowired
	BM13Svc bm13Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectApprovalListCount(Map<String, String> paramMap) {
		return bm13Mapper.selectApprovalListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return bm13Mapper.selectApprovalList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectApprovalExcelList(Map<String, String> paramMap) {
		return bm13Mapper.selectApprovalExcelList(paramMap);
	}

	@Override
	public int insertApproval(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		int result = 0;	    
		//selectKey 가 안먹어서 임시로 처리
	    String maxAppNo = bm13Mapper.selectMaxAppNo(paramMap);		
	    	    
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId_P"));
			dtl.put("appDiv", paramMap.get("appDiv"));
			dtl.put("appLineNm", paramMap.get("app_line_nm"));
			dtl.put("useYn", paramMap.get("useYn"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("creatPgm", paramMap.get("pgmId"));
			dtl.put("selAppNo", maxAppNo);
			
		    //MAX APP_SEQ
		    paramMap.put("maxAppNo", maxAppNo);
		    String maxAppSeq = bm13Mapper.selectMaxAppSeq(paramMap);			
			dtl.put("appSeq", maxAppSeq);		
				
    		result += bm13Mapper.insertApproval(dtl);			
		}	
		  		
		return result;
	}
	
	//결재선 수정시 입력과 삭제 분리하여 쿼리 실행
	@Override
	public int updateApproval(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	   						
		
		int result = 0;
	    //입력처리		
		if( paramMap.containsKey("makeArr") ) {		
			List<Map<String, String>> insertMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);			
			for(Map<String, String> dtl : insertMap) {
				dtl.put("coCd", paramMap.get("coCd"));
				dtl.put("userId", paramMap.get("userId_P"));
				dtl.put("appDiv", paramMap.get("appDiv"));
				dtl.put("appLineNm", paramMap.get("app_line_nm"));
				dtl.put("useYn", paramMap.get("useYn"));
				dtl.put("pgmId", paramMap.get("pgmId"));
				dtl.put("creatId", paramMap.get("userId"));
				dtl.put("creatPgm", paramMap.get("pgmId"));
					
			    //MAX APP_SEQ - Insert 시만 실행
				if( dtl.get("actFlag").equals("I") ) {
				    paramMap.put("maxAppNo", dtl.get("appNo"));
				    String maxAppSeq = bm13Mapper.selectMaxAppSeq(paramMap);			
					dtl.put("appSeq", maxAppSeq);						
				} else if( dtl.get("actFlag").equals("U") ) {
					
				}
	    		result += bm13Mapper.updateApproval(dtl);			
			}	
		}
		//삭제 처리
		if( paramMap.containsKey("delArr") ) {
			List<Map<String, String>> deleteMap = gsonDtl.fromJson(paramMap.get("delArr"), dtl2Map);		
			//삭제처리 
			for(Map<String, String> dtl2 : deleteMap) {			
	    		result += bm13Mapper.deleteApproval(dtl2);			
			}				
		}
		return result;
	  }	
	
	@Override
	public int deleteApproval(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    		
		int result = bm13Mapper.deleteApproval(paramMap);
		
		//---------------------------------------------------------------  		
		return result;
	  }	
	
	
}