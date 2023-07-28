package com.dksys.biz.user.sm.sm07.service.impl;

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
import com.dksys.biz.user.sm.sm07.mapper.SM07Mapper;
import com.dksys.biz.user.sm.sm07.service.SM07Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM07SvcImpl implements SM07Svc {

	@Autowired
	SM07Mapper sm07Mapper;
	
	@Autowired
	SM07Svc sm07Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;

	/*마스터 페이지 조회*/
	@Override
	public int master_grid_selectCount(Map<String, String> paramMap) {
		return sm07Mapper.master_grid_selectCount(paramMap);
	}
	@Override
	public List<Map<String, String>> master_grid_selectList(Map<String, String> paramMap) {
		return sm07Mapper.master_grid_selectList(paramMap);
	}	
	/*마스터 페이지 조회*/
	
	//모달 그리드 조회 -수정화면 정보
 	@Override
	public Map<String, String> select_sm07_Info(Map<String, String> paramMap) {
 		return sm07Mapper.select_sm07_Info(paramMap);
	}
	
}