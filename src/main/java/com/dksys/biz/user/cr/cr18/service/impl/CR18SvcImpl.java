package com.dksys.biz.user.cr.cr18.service.impl;

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

import com.dksys.biz.user.cr.cr18.mapper.CR18Mapper;
import com.dksys.biz.user.cr.cr18.service.CR18Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR18SvcImpl implements CR18Svc {
	
	@Autowired
	CR18Mapper cr18Mapper;
	
	@Autowired
	CR18Svc cr18Svc;

	// 그리드 카운트
	@Override
	public int select_cr18_Count(Map<String, String> paramMap) {
		return cr18Mapper.select_cr18_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr18_List(Map<String, String> paramMap) {
		return cr18Mapper.select_cr18_List(paramMap);
	}

}
