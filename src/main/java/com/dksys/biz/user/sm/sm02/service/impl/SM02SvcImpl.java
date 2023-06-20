package com.dksys.biz.user.sm.sm02.service.impl;

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
import com.dksys.biz.user.sm.sm02.mapper.SM02Mapper;
import com.dksys.biz.user.sm.sm02.service.SM02Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM02SvcImpl implements SM02Svc {

	@Autowired
	SM02Mapper sm02Mapper;
	
	@Autowired
	SM02Svc sm02Svc;

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectOrderListCount(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrderList(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderList(paramMap);
	}	
	
	@Override
	public List<Map<String, String>> selectOrderExcelList(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderExcelList(paramMap);
	}		
	
	@Override
	public int insertOrderMaster(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		int result = 0;	    
	
	    	    
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));


				
    		result += sm02Mapper.insertOrderMaster(dtl);			
		}	
		  		
		return result;
	}
	
	//결재선 수정시 입력과 삭제 분리하여 쿼리 실행
	@Override
	public int updateOrderMaster(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	   		
		List<Map<String, String>> insertMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);				
		
		int result = 0;

		System.out.println(">>dtl svcimpl paramMap>>" + paramMap.toString()+"<<<<<");		
	    //입력처리
		for(Map<String, String> dtl : insertMap) {

    		result += sm02Mapper.updateOrderMaster(dtl);			
		}	

		
		return result;
	  }	
	
	@Override
	public int deleteOrderMaster(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    		
		int result = sm02Mapper.deleteOrderMaster(paramMap);
		
		//---------------------------------------------------------------  		
		return result;
	  }		
}