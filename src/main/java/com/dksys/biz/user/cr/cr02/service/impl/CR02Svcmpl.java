package com.dksys.biz.user.cr.cr02.service.impl;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr01.mapper.CR01Mapper;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
@Transactional(rollbackFor = Exception.class)
public class CR02Svcmpl implements CR02Svc {
	@Autowired
	CR02Mapper cr02Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Override
	public int selectOrdrsCount(Map<String, String> param) {
		return cr02Mapper.selectOrdrsCount(param);
	}

	@Override
	public List<Map<String, Object>> selectOrdrsList(Map<String, String> param) {
		return cr02Mapper.selectOrdrsList(param);
	}

	@Override
	public Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap) {


		Map<String, Object> ordrsInfo = cr02Mapper.selectOrdrsInfo(paramMap);
		List<Map<String, Object>> ordrsPaymentPlan = cr02Mapper.selectPmntPlan(paramMap);
		ordrsInfo.put("list", ordrsPaymentPlan);


		return ordrsInfo;


	}



	// ...
}