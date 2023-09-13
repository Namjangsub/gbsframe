package com.dksys.biz.user.wb.wb22.service.impl;

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

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.dksys.biz.user.wb.wb22.service.WB22Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB22SvcImpl implements WB22Svc {
    
    @Autowired
    WB22Mapper wb22Mapper;

    
    @Autowired
    WB22Svc wb22Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
    @Override
	public List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsLeftSalesCodeList(paramMap);
	}
    
    @Override
	public Map<String, String> selectSjInfo(Map<String, String> paramMap) {
		return wb22Mapper.selectSjInfo(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS1Level(paramMap);
	}
    
    @Override
	public int wbsLevel1Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
    	List<Map<String, String>> plan = wb22Mapper.selectMaxWbsPlanNo(paramMap);
    	String wbsplanno = plan.get(0).get("wbsPlanNo");
    	
    	int result = 0;
		Gson gson = new Gson(); 		
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>(){}.getType(); 
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList); 
		if (sharngArr != null && sharngArr.size() > 0 ) { 
			int i = 0; 
			for (Map<String, String> sharngMap :sharngArr) { 
				try {
					sharngMap.put("coCd", paramMap.get("coCd"));
					sharngMap.put("wbsPlanNo", wbsplanno);					
					int fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));					
					result = wb22Mapper.wbsLevel1Insert(sharngMap);
					i++; 
					} catch (Exception e) { 
						System.out.println("error2"+e.getMessage()); 
					} 
				} 
			}
	    return result;
	}
    
    @Override
	public int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
    	int result = 0;
		Gson gson = new Gson(); 		
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>(){}.getType(); 
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList); 
		if (sharngArr != null && sharngArr.size() > 0 ) { 
			int i = 0; 
			for (Map<String, String> sharngMap :sharngArr) { 
				try {
					result = wb22Mapper.wbsLevel1Update(sharngMap);
					i++; 
					} catch (Exception e) { 
						System.out.println("error2"+e.getMessage()); 
					} 
				} 
			}
	    return result;
	}
    
    @Override
	public List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS2Level(paramMap);
	}
    
    
    @Override
	public int wbsLevel2Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
    	
    	List<Map<String, String>> listChk = wb22Mapper.wbsPlanListChk(paramMap);
		if (listChk.size() > 0) {
			wb22Mapper.deleteWbsPlanlist(paramMap);        	
		} 
		
    	List<Map<String, String>> plan = wb22Mapper.selectMaxWbsPlanNo(paramMap);
    	String wbsplanno = plan.get(0).get("wbsPlanNo");
    	
    	int result = 0;
		Gson gson = new Gson(); 		
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>(){}.getType(); 
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList); 
		if (sharngArr != null && sharngArr.size() > 0 ) { 
			int i = 0; 
			for (Map<String, String> sharngMap :sharngArr) { 
				try {
					sharngMap.put("coCd", paramMap.get("coCd"));
					sharngMap.put("wbsPlanNo", wbsplanno);
					sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
					String codeId = "";
					if ((i+1) < 10) {
						codeId = "0" + String.valueOf(i+1);
					}
					else {
						codeId = String.valueOf(i+1);
					}
					sharngMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind")+codeId);
					sharngMap.put("seq", String.valueOf(i+1));
					int fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));	
					
					result = wb22Mapper.wbsLevel2Insert(sharngMap);
					i++; 
					} catch (Exception e) { 
						System.out.println("error2"+e.getMessage()); 
					} 
				} 
			}
	    return result;
	}
    
    
    
}