package com.dksys.biz.user.wb.wb04.service.impl;

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
import com.dksys.biz.user.wb.wb04.mapper.WB04Mapper;
import com.dksys.biz.user.wb.wb04.service.WB04Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB04SvcImpl implements WB04Svc {

	@Autowired
	WB04Mapper wb04Mapper;
	
	@Autowired
	WB04Svc wb04Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap) {
		return wb04Mapper.selectWbsPlanTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectGanttList(Map<String, String> paramMap) {
		return wb04Mapper.selectGanttList(paramMap);
	}
	

	
}