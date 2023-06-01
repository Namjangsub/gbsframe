package com.dksys.biz.user.wb.wb02.service.impl;

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
import com.dksys.biz.user.wb.wb02.mapper.WB02Mapper;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB02SvcImpl implements WB02Svc {
    
    @Autowired
    WB02Mapper wb02Mapper;

    
    @Autowired
    WB02Svc wb02Svc;
    

    @Autowired
    ExceptionThrower thrower;


    @Override
    public int selectWbsRsltsPlanListCount(Map<String, String> paramMap) {
        return 0;
    }

    @Override
	public List<Map<String, String>> selectWbsRsltsPlanList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsPlanList(paramMap);
	}

}