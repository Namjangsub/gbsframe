package com.dksys.biz.user.qm.qm04.service.impl;

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

import com.dksys.biz.user.qm.qm02.mapper.QM02Mapper;
import com.dksys.biz.user.qm.qm02.service.QM02Svc;
import com.dksys.biz.user.qm.qm04.mapper.QM04Mapper;
import com.dksys.biz.user.qm.qm04.service.QM04Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM04SvcImpl implements QM04Svc {
	@Autowired
	QM04Mapper qm04Mapper;
	
	@Autowired
	QM04Svc qm04Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;
	
	
		// 그리드 리스트
		@Override
		public List<Map<String, String>> selectMainGridList(Map<String, String> paramMap) {
			return qm04Mapper.selectMainGridList(paramMap);
		}

}
