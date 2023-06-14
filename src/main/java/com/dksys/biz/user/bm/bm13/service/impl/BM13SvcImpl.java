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
		    		
		int result = bm13Mapper.insertApproval(paramMap);
		  		
		return result;
	  }	
	
	@Override
	public int updateApproval(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    		
		int result = bm13Mapper.updateApproval(paramMap);
		
		//---------------------------------------------------------------  		
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