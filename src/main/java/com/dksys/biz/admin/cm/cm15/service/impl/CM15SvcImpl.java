package com.dksys.biz.admin.cm.cm15.service.impl;

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

import com.dksys.biz.admin.cm.cm15.mapper.CM15Mapper;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.ar.ar01.mapper.AR01Mapper;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
public class CM15SvcImpl implements CM15Svc {

    @Autowired
    CM15Mapper cm15Mapper;

    @Autowired
    AR01Mapper ar01Mapper;
    

	@Override
	@SuppressWarnings("all")
	public int updateFileAuth(Map<String, Object> param) {
		int result = 0;
		List<Map<String, String>> detailList = (List<Map<String, String>>) param.get("detailArr");
		for(Map<String, String> detailMap : detailList) {
			result += cm15Mapper.updateFileAuth(detailMap);
		}
		return result;
	}
	@Override
	public int  selectTreeAuthCount(Map<String, String> paramMap) {
		return cm15Mapper.selectTreeAuthCount(paramMap);
	}
	
    @Override
    public List<Map<String, String>> selectTreeAuthList(Map<String, String> paramMap) {
        return cm15Mapper.selectTreeAuthList(paramMap);
    }

//    @Override
//    public Map<String, String> selectFileAutoInfo(Map<String, String> paramMap) {
//        return cm15Mapper.selectFileAutoInfo(fileKey);
//    }
//
//	@Override
//	public int deleteFileAuthInfo(Map<String, String> paramMap) {
//        Map<String, String> fileInfo = selectFileAutoInfo(paramMap);
//        int result = cm15Mapper.deleteFileAuthInfo(paramMap);
//        return result;		
//	}
	
}