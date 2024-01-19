package com.dksys.biz.user.qm.qm05.service.impl;

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

import com.dksys.biz.user.qm.qm05.mapper.QM05Mapper;
import com.dksys.biz.user.qm.qm05.service.QM05Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM05SvcImpl implements QM05Svc {
	@Autowired
	QM05Mapper qm05Mapper;
	
	@Autowired
	QM05Svc qm05Svc;
	  
	@Override
	public int selectMainGridListCount(Map<String, String> paramMap) {
		return qm05Mapper.selectMainGridListCount(paramMap);
	}
	
	// 그리드 리스트
	@Override
	public List<Map<String, String>> selectMainGridList(Map<String, String> paramMap) {
		return qm05Mapper.selectMainGridList(paramMap);
	}
	
	//multi select 검색
	@Override
	public List<Map<String, String>> select_dept_code(Map<String, String> paramMap) {
		return qm05Mapper.select_dept_code(paramMap);
	}

}
