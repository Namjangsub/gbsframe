package com.dksys.biz.user.bm.bm15.service.impl;

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

import com.dksys.biz.user.bm.bm15.mapper.BM15Mapper;
import com.dksys.biz.user.bm.bm15.service.BM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM15SvcImpl implements BM15Svc {
	
	@Autowired
	BM15Mapper bm15Mapper;
	
	@Autowired
	BM15Svc bm15Svc;

	// 그리드 카운트
	@Override
	public int select_bm15_Count(Map<String, String> paramMap) {
		return bm15Mapper.select_bm15_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_bm15_List(Map<String, String> paramMap) {
		return bm15Mapper.select_bm15_List(paramMap);
	}

	// @Override
	// public List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap) {
	// 	return bm15Mapper.selectPmntmtdCd(paramMap);
	// }
}
