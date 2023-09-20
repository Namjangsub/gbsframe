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
	public int insertMessageTempl(Map<String, String> paramMap) throws Exception {

		int result = 0;	    		
	    String maxFileTrgtKey = bm17Mapper.selectMaxFileTrgtKey(paramMap);	
	    paramMap.put("maxFileTrgtKey", maxFileTrgtKey);
	    	    	    
		result = bm17Mapper.insertMessageTempl(paramMap);
		
		return result;
	}
	
	//결재선 수정시 입력과 삭제 분리하여 쿼리 실행
	@Override
	public int updateMessageTempl(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	   						
		
		int result = 0;
		result = bm17Mapper.updateMessageTempl(paramMap);
	   
		return result;
	  }	
	
	@Override
	public int deleteMessageTempl(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    		
		int result = bm17Mapper.deleteMessageTempl(paramMap);
		  		
		return result;
	  }	
	
	
}