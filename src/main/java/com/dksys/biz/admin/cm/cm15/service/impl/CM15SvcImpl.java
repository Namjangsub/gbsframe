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
    
    @Autowired
    ExceptionThrower thrower;

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
	public int selectTreeAuthCount(Map<String, String> paramMap) {
		return cm15Mapper.selectTreeAuthCount(paramMap);
	}
	
    @Override
    public List<Map<String, String>> selectTreeAuthList(Map<String, String> paramMap) {
        return cm15Mapper.selectTreeAuthList(paramMap);
    }

    //파잁트리 접근 권한관리
    //  파라메터 jobType = 작업유형 전달
    //        userId = 사용자 ID
    //        comonCd = 파일트리 ID( 현재 공통코드로 관리되는  CODE_ID)
    //  접근 통제는 Exception 발생
	@Override
	public void selectFileAuthCheck(Map<String, String> paramMap) throws Exception {
        Map<String, String> fileInfo = selectFileAuthInfo(paramMap);
		switch(paramMap.get("jobType")) {
			case "LIST":
				if (!fileInfo.get("fileList").equals("Y")) thrower.throwCommonException("noFileList");
				break;
			case "fileUp":
				if (!fileInfo.get("fileUp").equals("Y")) thrower.throwCommonException("noFileUp");
				break;
			case "fileDown":
				if (!fileInfo.get("fileDown").equals("Y")) thrower.throwCommonException("noFileDown");
				break;
			case "fileUpdate":
				if (!fileInfo.get("fileUpdate").equals("Y")) thrower.throwCommonException("noFileUpdate");
				break;
			case "fileDelete":
				if (!fileInfo.get("fileDelete").equals("Y")) thrower.throwCommonException("noFileDelete");
				break;
			default:
				thrower.throwCommonException("noFileAuth");
				break;
		}
	}
	
    @Override
    public Map<String, String> selectFileAuthInfo(Map<String, String> paramMap) {
        return cm15Mapper.selectFileAuthInfo(paramMap);
    }

	@Override
	@SuppressWarnings("all")
	public int deleteFileAuthInfo(Map<String, Object> param) {
		int result = 0;
		List<Map<String, String>> detailList = (List<Map<String, String>>) param.get("detailArr");
		for(Map<String, String> detailMap : detailList) {
			result += cm15Mapper.deleteFileAuthInfo(detailMap);
		}
		return result;
	}
	
}