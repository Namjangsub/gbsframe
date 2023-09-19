package com.dksys.biz.user.bm.bm17.service.impl;

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
import com.dksys.biz.user.bm.bm17.mapper.BM17Mapper;
import com.dksys.biz.user.bm.bm17.service.BM17Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM17SvcImpl implements BM17Svc {

	@Autowired
	BM17Mapper bm17Mapper;
	
	@Autowired
	BM17Svc bm17Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectMessageTemplListCount(Map<String, String> paramMap) {
		return bm17Mapper.selectMessageTemplListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectMessageTemplList(Map<String, String> paramMap) {
		return bm17Mapper.selectMessageTemplList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectMessageTemplExcelList(Map<String, String> paramMap) {
		return bm17Mapper.selectMessageTemplExcelList(paramMap);
	}

	@Override
	public int insertMessageTempl(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		int result = 0;	    
		//selectKey 가 안먹어서 임시로 처리
	    String maxAppNo = bm17Mapper.selectMaxFileTrgtKey(paramMap);		
	    	    
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
	
				
    		result += bm17Mapper.insertMessageTempl(dtl);			
		}	
		  		
		return result;
	}
	
	//결재선 수정시 입력과 삭제 분리하여 쿼리 실행
	@Override
	public int updateMessageTempl(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
				    String maxAppSeq = bm17Mapper.selectMaxFileTrgtKey(paramMap);			
					dtl.put("appSeq", maxAppSeq);						
				} else if( dtl.get("actFlag").equals("U") ) {
					
				}
	    		result += bm17Mapper.updateMessageTempl(dtl);			
			}	
		}
		//삭제 처리
		if( paramMap.containsKey("delArr") ) {
			List<Map<String, String>> deleteMap = gsonDtl.fromJson(paramMap.get("delArr"), dtl2Map);		
			//삭제처리 
			for(Map<String, String> dtl2 : deleteMap) {			
	    		result += bm17Mapper.deleteMessageTempl(dtl2);			
			}				
		}
		return result;
	  }	
	
	@Override
	public int deleteMessageTempl(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    		
		int result = bm17Mapper.deleteMessageTempl(paramMap);
		
		//---------------------------------------------------------------  		
		return result;
	  }	
	
	
}