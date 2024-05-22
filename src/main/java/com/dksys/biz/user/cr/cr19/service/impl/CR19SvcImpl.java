package com.dksys.biz.user.cr.cr19.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.cr.cr19.mapper.CR19Mapper;
import com.dksys.biz.user.cr.cr19.service.CR19Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR19SvcImpl implements CR19Svc {
	
	@Autowired
	CR19Mapper cr19Mapper;
	
	@Autowired
	CR19Svc cr19Svc;

	// 그리드 카운트
	@Override
	public int select_cr19_Count(Map<String, String> paramMap) {
		return cr19Mapper.select_cr19_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr19_List(Map<String, String> paramMap) {
		return cr19Mapper.select_cr19_List(paramMap);
	}

	// 그리드 카운트
	@Override
	public int select_cr19m02_Count(Map<String, String> paramMap) {
		return cr19Mapper.select_cr19m02_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr19m02_List(Map<String, String> paramMap) {
		return cr19Mapper.select_cr19m02_List(paramMap);
	}

	// 수주버전
	@Override
	public List<Map<String, Object>> select_ordrsHistNo_List(Map<String, String> paramMap) {
		return cr19Mapper.select_ordrsHistNo_List(paramMap);
	}

	@Override
    public void save_cr19(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		String OrderSeq = "";

        // 데이터 처리 시작
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
			dtl.put("userId", paramMap.get("userId").toString());
			dtl.put("pgmId", paramMap.get("pgmId").toString());
			
			//데이터 처리
			cr19Mapper.call_save_cr19(dtl);
	    }
    }

	//@Override
    //public void save_cr19_create_cr10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	//	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	//	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	//	String OrderSeq = "";
    //    // 데이터 처리 시작
	//	List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	//    for (Map<String, String> dtl : dtlParam) {
	//		dtl.put("userId", paramMap.get("userId").toString());
	//		dtl.put("pgmId", paramMap.get("pgmId").toString());
	//		//데이터 처리
	//		cr19Mapper.call_save_cr19_create_cr10(dtl);
	//    }
    //}
	
	//@Override
	//public int save_cr19_create_cr10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	//	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	//	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	//	//생성
	//	int result = cr19Mapper.call_save_cr19_create_cr10(paramMap);
	//	return result;
	//}
	
	//@Override
	//public void save_cr19_create_cr10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
	//	cr19Mapper.call_save_cr19_create_cr10(paramMap);
	//}
	
	@Override
	public Map<String, String> save_cr19_create_cr10(Map<String, String> paramMap) {
		cr19Mapper.save_cr19_create_cr10(paramMap);
		return paramMap;
	}
}
