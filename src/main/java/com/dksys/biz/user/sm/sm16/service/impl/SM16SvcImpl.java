package com.dksys.biz.user.sm.sm16.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm16.mapper.SM16Mapper;
import com.dksys.biz.user.sm.sm16.service.SM16Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM16SvcImpl implements SM16Svc {

	@Autowired
	SM16Mapper sm16Mapper;	
	
	@Autowired
	SM16Svc sm16Svc;	
	
	@Autowired
	ExceptionThrower thrower;

	
	@Override
	public List<Map<String, String>> selectSM16MainList(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm16Mapper.selectSM16MainList(paramMap);
		return result;		
	}
	
	@Override
	public List<Map<String, String>> sm16selectPurchaseListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm16Mapper.sm16selectPurchaseListNew(paramMap);
		return result;		
	}		
	
	
}