package com.dksys.biz.user.bm.bm18.service.impl;

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
import com.dksys.biz.user.bm.bm18.mapper.BM18Mapper;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM18SvcImpl implements BM18Svc {

	@Autowired
	BM18Mapper bm18Mapper;
	
	@Autowired
	BM18Svc bm18Svc;

	@Autowired
	ExceptionThrower thrower;

	//알림톡수신 카운트
	@Override
	public int selectReceptionMessageListCount(Map<String, String> paramMap) {
		return bm18Mapper.selectReceptionMessageListCount(paramMap);
	}

	//알림톡수신 조회
	@Override
	public List<Map<String, String>> selectReceptionMessageList(Map<String, String> paramMap) {
		return bm18Mapper.selectReceptionMessageList(paramMap);
	}
	
	//수신번호 채번	
	public List<Map<String, String>> selectMaxMessageId(Map<String, String> paramMap) {
		String kakaoSeq = bm18Mapper.selectKakaoSeqNext(paramMap);
		paramMap.put("kakaoSeq", kakaoSeq);				
		return bm18Mapper.selectMaxMessageId(paramMap);
	}	
	
	//todo 수신번호 메시지 내용 채번	
	public List<Map<String, String>> selectMaxMessageIdTodo(Map<String, String> paramMap) {
		String kakaoSeq = bm18Mapper.selectKakaoSeqNext(paramMap);
		paramMap.put("kakaoSeq", kakaoSeq);		
		
		return bm18Mapper.selectMaxMessageIdTodo(paramMap);
	}		
	
	@Override
	public int insertKakaoMessage(Map<String, String> paramMap) throws Exception {

		int result = 0;		
	    result = bm18Mapper.insertKakaoMessage(paramMap);	

		return result;
	}	
}