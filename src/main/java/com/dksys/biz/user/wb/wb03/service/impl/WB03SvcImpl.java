package com.dksys.biz.user.wb.wb03.service.impl;

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
import com.dksys.biz.user.wb.wb03.mapper.WB03Mapper;
import com.dksys.biz.user.wb.wb03.service.WB03Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB03SvcImpl implements WB03Svc {
    
    @Autowired
    WB03Mapper wb03Mapper;

    
    @Autowired
    WB03Svc wb03Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
    @Override
	public int selectWbsPlanCount(Map<String, String> paramMap) {
		return wb03Mapper.selectWbsPlanCount(paramMap);
	}
    
	@Override
	public List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap) {
		return wb03Mapper.selectWbsPlanList(paramMap);
	}
	
}