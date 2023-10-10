package com.dksys.biz.user.wb.wb04.service.impl;

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
import com.dksys.biz.user.wb.wb04.mapper.WB04Mapper;
import com.dksys.biz.user.wb.wb04.service.WB04Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB04SvcImpl implements WB04Svc {

	@Autowired
	WB04Mapper wb04Mapper;
	
	@Autowired
	WB04Svc wb04Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public List<Map<String, String>> selectWbsLeftSalesCodeTreeList(Map<String, String> paramMap) {
		return wb04Mapper.selectWbsLeftSalesCodeTreeList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap) {
		return wb04Mapper.selectWbsPlanTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectGanttList(Map<String, String> paramMap) {
		return wb04Mapper.selectGanttList(paramMap);
	}
	
    @Override
    public int updateWbsPlanDate(Map<String, String> paramMap) {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {                                    
	            	 wb04Mapper.updateWbsPlanDate(arrMap);
	            	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
	}
    
    @Override
	public List<Map<String, String>> selectGanttAllList(Map<String, String> paramMap) {
		return wb04Mapper.selectGanttAllList(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectGanttInfo(Map<String, String> paramMap) {
		return wb04Mapper.selectGanttInfo(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectGanttAllList2(Map<String, String> paramMap) {
		return wb04Mapper.selectGanttAllList2(paramMap);
	}
    
	
}