package com.dksys.biz.user.cr.cr14.service.impl;

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

import com.dksys.biz.user.cr.cr14.mapper.CR14Mapper;
import com.dksys.biz.user.cr.cr14.service.CR14Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR14SvcImpl implements CR14Svc {
	
	@Autowired
	CR14Mapper cr14Mapper;
	
	@Autowired
	CR14Svc cr14Svc;

	// 그리드 카운트
	@Override
	public int select_cr14_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_cr14_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr14_List(Map<String, String> paramMap) {
		return cr14Mapper.select_cr14_List(paramMap);
	}

	// POPUP 그리드 카운트
	@Override
	public int select_modal_List1_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List1_Count(paramMap);
	}

	@Override
	public int select_modal_List2_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List2_Count(paramMap);
	}

	@Override
	public int select_modal_List3_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List3_Count(paramMap);
	}

	@Override
	public int select_modal_List4_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List4_Count(paramMap);
	}

	@Override
	public int select_modal_List5_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List5_Count(paramMap);
	}

	@Override
	public int select_modal_List6_Count(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List6_Count(paramMap);
	}

	// POPUP 그리드 리스트
	@Override
	public List<Map<String, String>> select_modal_List1(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List1(paramMap);
	}

	@Override
	public List<Map<String, String>> select_modal_List2(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List2(paramMap);
	}

	@Override
	public List<Map<String, String>> select_modal_List3(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List3(paramMap);
	}

	@Override
	public List<Map<String, String>> select_modal_List4(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List4(paramMap);
	}

	@Override
	public List<Map<String, String>> select_modal_List5(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List5(paramMap);
	}

	@Override
	public List<Map<String, String>> select_modal_List6(Map<String, String> paramMap) {
		return cr14Mapper.select_modal_List6(paramMap);
	}

	// 수주버전
	@Override
	public List<Map<String, Object>> select_ordrsHistNo_List(Map<String, String> paramMap) {
		return cr14Mapper.select_ordrsHistNo_List(paramMap);
	}
}
