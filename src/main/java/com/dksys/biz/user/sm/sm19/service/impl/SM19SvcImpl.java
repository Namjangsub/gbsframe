package com.dksys.biz.user.sm.sm19.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.sm.sm19.mapper.SM19Mapper;
import com.dksys.biz.user.sm.sm19.service.SM19Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM19SvcImpl implements SM19Svc {
	
	@Autowired
	SM19Mapper sm19Mapper;
	
	@Autowired
	SM19Svc sm19Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	// 그리드 카운트
	@Override
	public int select_sm19_Count(Map<String, String> paramMap) {
		return sm19Mapper.select_sm19_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_sm19_List(Map<String, String> paramMap) {
		return sm19Mapper.select_sm19_List(paramMap);
	}
	
	// 창고콤보
	@Override
	public List<Map<String, Object>> selectWhCd(Map<String, String> paramMap) {
		return sm19Mapper.selectWhCd(paramMap);
	}
	
	// 수불유형 콤보
	@Override
	public List<Map<String, Object>> selectinoutDiv(Map<String, String> paramMap) {
		return sm19Mapper.selectinoutDiv(paramMap);
	}
}
