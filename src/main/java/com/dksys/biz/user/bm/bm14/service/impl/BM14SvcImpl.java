package com.dksys.biz.user.bm.bm14.service.impl;

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

import com.dksys.biz.user.bm.bm14.mapper.BM14Mapper;
import com.dksys.biz.user.bm.bm14.service.BM14Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM14SvcImpl implements BM14Svc {
	
	@Autowired
	BM14Mapper bm14Mapper;
	
	@Autowired
	BM14Svc bm14Svc;

	// 그리드 카운트
	@Override
	public int select_bm14_Count(Map<String, String> paramMap) {
		return bm14Mapper.select_bm14_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_bm14_List(Map<String, String> paramMap) {
		return bm14Mapper.select_bm14_List(paramMap);
	}

	// @Override
	// public List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap) {
	// 	return bm14Mapper.selectPmntmtdCd(paramMap);
	// }
}
