package com.dksys.biz.user.wb.wb03.service.impl;

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

import com.dksys.biz.user.wb.wb03.mapper.WB03Mapper;
import com.dksys.biz.user.wb.wb03.service.WB03Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB03SvcImpl implements WB03Svc {

  @Autowired
  WB03Mapper wb03Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Override
  public List<Map<String, String>> selectWbsPlanTreeIssueList(Map<String, String> paramMap) {
    return wb03Mapper.selectWbsPlanTreeIssueList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap) {
		return wb03Mapper.selectMaxWbsIssueNo(paramMap);
  }
  
  @Override
  public int insertWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb03Mapper.selectWbsPlanIssueSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
	    int result = wb03Mapper.insertWbsPlanIssue(paramMap);	
	    return result;
  }
  
  @Override
  public int updateWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
	    int result = wb03Mapper.updateWbsPlanIssue(paramMap);	
	    return result;
  }
  
  
}