package com.dksys.biz.user.pm.pm40.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.pm.pm40.mapper.PM40Mapper;
import com.dksys.biz.user.pm.pm40.service.PM40Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;



@Service
@Transactional(rollbackFor = Exception.class)
public class PM40SvcImpl implements PM40Svc{

	
	 @Autowired
	  PM40Mapper pm40Mapper;
	
	 
	 
	 	
	 
		@Override
		public int selectMainGridListCount(Map<String, String> paramMap) {
			return pm40Mapper.selectMainGridListCount(paramMap);
		}
		
		@Override
		public List<Map<String, String>> selectMainGridList(Map<String, String> paramMap) {
			return pm40Mapper.selectMainGridList(paramMap);
		}
		
		@Override
		public int insert_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {	 
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			int result = 0;
			//해당월의 고찰데이터가 있는지 없는지 확인
			int gochalIn = pm40Mapper.select_gochal_count(paramMap);
					
			if (gochalIn == 0) {
				//int fileTrgtKey = pm402Mapper.select_qm02_SeqNext(paramMap);
				//paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

				String newMNGM_NO = pm40Mapper.select_pm40_Next_MNGM_NO(paramMap);
				paramMap.put("workNo", newMNGM_NO);
				
				//마스터입력
				result = pm40Mapper.insert_pm40(paramMap);

			}
			else if (gochalIn == 1) {
				result = 7;
			}
		return result;
		}
		
		@Override
		public int update_pm40(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {	 
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
			
			int result = pm40Mapper.update_pm40(paramMap);
			
		return result;
		}
		
		@Override
		public int delete_pm40(Map<String, String> paramMap) throws Exception {	 
		int result = 0;
		result = pm40Mapper.delete_pm40(paramMap);
		return result;
		}

		@Override
		public List<Map<String, String>> selectYearWorkMainList(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.selectYearWorkMainList(paramMap);
		}

		@Override
		public String select_pm40_Next_MNGM_NO(Map<String, String> paramMap) {
			
			
			// TODO Auto-generated method stub
			return pm40Mapper.select_pm40_Next_MNGM_NO(paramMap);
		}

		@Override
		public int select_gochal_count(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.select_gochal_count(paramMap);
		}

		@Override
		public Map<String, String> select_pm40_Info(Map<String, String> paramMap) {
			// TODO Auto-generated method stub
			return pm40Mapper.select_pm40_Info(paramMap);
		}
}
