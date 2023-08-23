package com.dksys.biz.user.cr.cr12.service.impl;

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

import com.dksys.biz.user.cr.cr12.mapper.CR12Mapper;
import com.dksys.biz.user.cr.cr12.service.CR12Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR12SvcImpl implements CR12Svc {
	
	@Autowired
	CR12Mapper cr12Mapper;
	
	@Autowired
	CR12Svc cr12Svc;

	// 그리드 카운트
	@Override
	public int select_cr12_Count(Map<String, String> paramMap) {
		return cr12Mapper.select_cr12_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr12_List(Map<String, String> paramMap) {
		return cr12Mapper.select_cr12_List(paramMap);
	}
}
