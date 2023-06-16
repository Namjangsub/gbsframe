package com.dksys.biz.user.cr.cr07.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.cr.cr07.mapper.CR07Mapper;
import com.dksys.biz.user.cr.cr07.service.CR07Svc;
import com.dksys.biz.util.ExceptionThrower;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR07SvcImpl implements CR07Svc {
	
	@Autowired
	CR07Mapper cr07Mapper;
	
	@Autowired
	CR07Svc cr07Svc;
	
	@Autowired
	ExceptionThrower thrower;
	
	@Override
	public int selectSellDcsnCount(Map<String, String> paramMap) {
		return cr07Mapper.selectSellDcsnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSellDcsnList(Map<String, String> paramMap) {
		return cr07Mapper.selectSellDcsnList(paramMap);
	}
	
	@Override
	public int selectOrdrsDcsnCount(Map<String, String> paramMap) {
		return cr07Mapper.selectOrdrsDcsnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectOrdrsDcsnList(Map<String, String> paramMap) {
		return cr07Mapper.selectOrdrsDcsnList(paramMap);
	}
	
	@Override
	public int addSellDscnCount(Map<String, String> paramMap) {
		return cr07Mapper.addSellDscnCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> addSellDscnList(Map<String, String> paramMap) {
		return cr07Mapper.addSellDscnList(paramMap);
	}
	
	@Override
	public int insertSellDscn(Map<String, String> paramMap) {
		int result = cr07Mapper.insertSellDscn(paramMap);
		return result;
		
	}
	
}
