package com.dksys.biz.user.cr.cr11.service.impl;

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

import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr10.mapper.CR10Mapper;
import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.user.cr.cr11.mapper.CR11Mapper;
import com.dksys.biz.user.cr.cr11.service.CR11Svc;
import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR11SvcImpl implements CR11Svc {
	
	@Autowired
    CR01Svc cr01Svc;
	
	@Autowired
    CR01Svc cr02Svc;
	
    @Autowired
    CR11Mapper cr11Mapper;

    @Autowired
    CM08Mapper cm08Mapper;

    @Autowired
    CM08Svc cm08Svc;
    
    // 그리드 카운트
 	@Override
 	public int grid1_selectCount(Map<String, String> paramMap) {
 		return cr11Mapper.grid1_selectCount(paramMap);
 	}

 	// 그리드 리스트
 	@Override
 	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
 		return cr11Mapper.grid1_selectList(paramMap);
 	}
 	
 	//모달 그리드 조회 -수정화면 정보
 	@Override
	public Map<String, String> select_cr11_Info(Map<String, String> paramMap) {
		return cr11Mapper.select_cr11_Info(paramMap);
	}

  

}