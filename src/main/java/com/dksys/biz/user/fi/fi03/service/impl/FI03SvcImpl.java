package com.dksys.biz.user.fi.fi03.service.impl;

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

import com.dksys.biz.user.fi.fi03.mapper.FI03Mapper;
import com.dksys.biz.user.fi.fi03.service.FI03Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class FI03SvcImpl implements FI03Svc {
	
	@Autowired
	FI03Mapper fi03Mapper;
	
	@Autowired
	FI03Svc fi03Svc;

	// 그리드 카운트
	@Override
	public int select_fi03_Count(Map<String, String> paramMap) {
		return fi03Mapper.select_fi03_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_fi03_List(Map<String, String> paramMap) {
		return fi03Mapper.select_fi03_List(paramMap);
	}

	// @Override
	// public List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap) {
	// 	return cr05Mapper.selectPmntmtdCd(paramMap);
	// }
}
