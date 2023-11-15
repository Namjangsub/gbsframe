package com.dksys.biz.user.sm.sm18.service.impl;

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
import com.dksys.biz.user.sm.sm18.mapper.SM18Mapper;
import com.dksys.biz.user.sm.sm18.service.SM18Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM18SvcImpl implements SM18Svc {
	@Autowired
	SM18Mapper sm18Mapper;	
	
	@Autowired
	SM18Svc sm18Svc;	
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;

	//카운트
	@Override
	public int sm18_gridView_selectCount(Map<String, String> paramMap) {
		return sm18Mapper.sm18_gridView_selectCount(paramMap);
	}
	
	//리스트
	@Override
	public List<Map<String, String>> sm18_gridView_selectList(Map<String, String> paramMap) {
		return sm18Mapper.sm18_gridView_selectList(paramMap);
	}
	
	//카운트
	@Override
	public int sm18_gridView_selectCountNew(Map<String, String> paramMap) {
		return sm18Mapper.sm18_gridView_selectCountNew(paramMap);
	}
	
	//리스트
	@Override
	public List<Map<String, String>> sm18_gridView_selectListNew(Map<String, String> paramMap) {
		return sm18Mapper.sm18_gridView_selectListNew(paramMap);
	}
}