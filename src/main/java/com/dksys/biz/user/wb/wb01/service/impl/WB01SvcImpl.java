package com.dksys.biz.user.wb.wb01.service.impl;

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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.wb.wb01.mapper.WB01Mapper;
import com.dksys.biz.user.wb.wb01.service.WB01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB01SvcImpl implements WB01Svc {
    
    @Autowired
    WB01Mapper wb01Mapper;

    
    @Autowired
    WB01Svc wb01Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
	@Override
	public List<Map<String, String>> selectWbsList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsList01(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsList01(paramMap);
	}
	
	@Override
	public int selectWbsCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsCount(paramMap);
	}
}