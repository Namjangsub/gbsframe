package com.dksys.biz.user.sm.sm01.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.sm.sm01.mapper.SM01Mapper;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.dksys.biz.util.ExceptionThrower;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM01SvcImpl implements SM01Svc {
	
	@Autowired
	SM01Mapper sm01Mapper;
	
	@Autowired
	SM01Svc sm01Svc;
	
	@Autowired
	ExceptionThrower thrower;
	
	@Override
	public int selectBomMakerCount(Map<String, String> paramMap) {
		return sm01Mapper.selectBomMakerCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectBomMakerList(Map<String, String> paramMap) {
		return sm01Mapper.selectBomMakerList(paramMap);
	}
	
	@Override
	public int selectBomSalesCount(Map<String, String> paramMap) {
		return sm01Mapper.selectBomSalesCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap) {
		return sm01Mapper.selectBomSalesList(paramMap);
	}
	
	@Override
	public int addSellDscnCount(Map<String, String> paramMap) {
		return sm01Mapper.addSellDscnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> addSellDscnList(Map<String, String> paramMap) {
		return sm01Mapper.addSellDscnList(paramMap);
	}
	
	@Override
	public int insertSellDscn(Map<String, String> paramMap) {
		int result = sm01Mapper.insertSellDscn(paramMap);
		return result;
		
	}
	
}
