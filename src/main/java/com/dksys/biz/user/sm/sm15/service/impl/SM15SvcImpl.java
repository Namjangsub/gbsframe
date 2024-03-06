package com.dksys.biz.user.sm.sm15.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm15.mapper.SM15Mapper;
import com.dksys.biz.user.sm.sm15.service.SM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM15SvcImpl implements SM15Svc {

	@Autowired
	SM15Mapper sm15Mapper;	
	
	@Autowired
	SM15Svc sm15Svc;	
	
	@Autowired
	ExceptionThrower thrower;

	
	@Override
	public List<Map<String, String>> selectSM15MainList(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm15Mapper.selectSM15MainList(paramMap);
		return result;		
	}
	
	@Override
	public List<Map<String, String>> sm15selectPurchaseListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm15Mapper.sm15selectPurchaseListNew(paramMap);
		return result;		
	}		
	
}