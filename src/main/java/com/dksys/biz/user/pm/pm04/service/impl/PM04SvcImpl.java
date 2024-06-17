package com.dksys.biz.user.pm.pm04.service.impl;

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

import com.dksys.biz.user.pm.pm04.mapper.PM04Mapper;
import com.dksys.biz.user.pm.pm04.service.PM04Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM04SvcImpl implements PM04Svc{
	  @Autowired
	  PM04Mapper pm04Mapper;
	  
	@Override
	  public List<Map<String, String>> selectDailyWorkMainList(Map<String, String> paramMap) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String[] roleArray = paramMap.get("userName").split(",");
		String test = "";
		for (int i = 0; i < roleArray.length; i++) {
			if ((roleArray.length -1) == i ) {
				test += "'" + roleArray[i] +"' AS user"+ (i+1);
			}
			else {
				test += "'" + roleArray[i] +"' AS user"+ (i+1) +  ",";
			}
		}

		map.put("test", test);
		map.put("userId", paramMap.get("userId"));
		map.put("coCd", paramMap.get("coCd"));
		map.put("reqDtFrom", paramMap.get("reqDtFrom"));
		map.put("reqDtTo",paramMap.get("reqDtTo"));
		map.put("salesCd",paramMap.get("salesCd"));
		map.put("workRptm",paramMap.get("workRptm"));
		map.put("workRpts",paramMap.get("workRpts"));
		
	    return pm04Mapper.selectDailyWorkMainList(map);
	  }
	
	@Override
	public List<Map<String, String>> select_all_name(Map<String, String> paramMap) {
		return pm04Mapper.select_all_name(paramMap);
	}
}
