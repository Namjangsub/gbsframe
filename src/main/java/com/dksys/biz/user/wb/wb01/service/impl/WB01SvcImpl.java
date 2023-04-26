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
	public int selectWbsPlanCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanCount(paramMap);
	}
    
	@Override
	public List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanLvl1List(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanLvl1List(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPlanLvlCboList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanLvlCboList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanSubLvlCboList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanSubLvlCboList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanNoList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsSalesCodeList(paramMap);
	}
	
}