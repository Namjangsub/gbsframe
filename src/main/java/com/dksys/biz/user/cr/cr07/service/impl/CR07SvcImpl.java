package com.dksys.biz.user.cr.cr07.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr07.mapper.CR07Mapper;
import com.dksys.biz.user.cr.cr07.service.CR07Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR07SvcImpl implements CR07Svc {
	
	@Autowired
	CR07Mapper cr07Mapper;
	
	@Autowired
	CR07Svc cr07Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;
	
	@Autowired
	ExceptionThrower thrower;
	@Override
	public int selectOrdrsDcsnCount(Map<String, String> paramMap) {
		return cr07Mapper.selectOrdrsDcsnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectOrdrsDcsnList(Map<String, String> paramMap) {
		return cr07Mapper.selectOrdrsDcsnList(paramMap);
	}
	
	@Override
	public int selectSellDcsnCount(Map<String, String> paramMap) {
		return cr07Mapper.selectSellDcsnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSellDcsnList(Map<String, String> paramMap) {
		return cr07Mapper.selectSellDcsnList(paramMap);
	}
	
	@Override
	public Map<String, String> select_cr07_Info(Map<String, String> paramMap) {
		return cr07Mapper.select_cr07_Info(paramMap);
	}
	
	@Override
	public int addSellDscnCount(Map<String, String> paramMap) {
		return cr07Mapper.addSellDscnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> addSellDscnList(Map<String, String> paramMap) {
		return cr07Mapper.addSellDscnList(paramMap);
	}
	
	@Override
	public int insertSellDscn(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		
		//---------------------------------------------------------------
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
			//접근 권한 없으면 Exception 발생
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------
		
		int fileTrgtKey = cr07Mapper.select_cr07_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		int result = cr07Mapper.insertSellDscn(paramMap);

		//---------------------------------------------------------------
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------
		//첨부 화일 처리  끝
		//---------------------------------------------------------------
		return result;
		
	}
	
	@Override
	public int insertSellDscnDetail(Map<String, String> paramMap) {
		return cr07Mapper.insertSellDscnDetail(paramMap);
	}
	
}
