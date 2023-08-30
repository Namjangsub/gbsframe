package com.dksys.biz.user.cr.cr16.service.impl;

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
import com.dksys.biz.user.cr.cr16.mapper.CR16Mapper;
import com.dksys.biz.user.cr.cr16.service.CR16Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR16SvcImpl implements CR16Svc {

	@Autowired
	CR16Mapper cr16Mapper;
	
	@Autowired
    CM08Svc cm08Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CR16Svc cr16Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectSalesYearPlanListCount(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanList(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanList(paramMap);
	}
	
	@Override
	public int deleteSalesYearPlanList(Map<String, String> paramMap) {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {                                    
	            	 cr16Mapper.deleteSalesYearPlanM(arrMap);
	            	 cr16Mapper.deleteSalesYearPlanD(arrMap);
	            	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanMC(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanMC(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanMU(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanMU(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanD(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanD(paramMap);
	}
}
