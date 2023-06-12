package com.dksys.biz.user.wb.wb06.service.impl;

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
import com.dksys.biz.user.wb.wb06.mapper.WB06Mapper;
import com.dksys.biz.user.wb.wb06.service.WB06Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB06SvcImpl implements WB06Svc {

	@Autowired
	WB06Mapper wb06Mapper;
	
	@Autowired
	WB06Svc wb06Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectApprovalListCount(Map<String, String> paramMap) {
		return wb06Mapper.selectApprovalListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return wb06Mapper.selectApprovalList(paramMap);
	}
	

}