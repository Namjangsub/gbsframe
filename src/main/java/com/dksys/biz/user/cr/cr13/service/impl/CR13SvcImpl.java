package com.dksys.biz.user.cr.cr13.service.impl;

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

import com.dksys.biz.user.cr.cr13.mapper.CR13Mapper;
import com.dksys.biz.user.cr.cr13.service.CR13Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR13SvcImpl implements CR13Svc {
	
	@Autowired
	CR13Mapper cr13Mapper;
	
	@Autowired
	CR13Svc cr13Svc;

	// 그리드 카운트
	@Override
	public int select_cr13_Count(Map<String, String> paramMap) {
		return cr13Mapper.select_cr13_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr13_List(Map<String, String> paramMap) {
		return cr13Mapper.select_cr13_List(paramMap);
	}

	// @Override
	// public List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap) {
	// 	return cr05Mapper.selectPmntmtdCd(paramMap);
	// }
}
