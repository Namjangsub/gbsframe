package com.dksys.biz.user.dw.dw02.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.dw.dw02.mapper.DW02Mapper;
import com.dksys.biz.user.dw.dw02.service.DW02Svc;


@Service
@Transactional(rollbackFor = Exception.class)
public class DW02SvcImpl implements DW02Svc {


	@Autowired
	DW02Mapper dw02Mapper;
	
	@Override
	public int searchAuditsCount(Map<String, String> paramMap) {
		return dw02Mapper.searchAuditsCount(paramMap);
	}

	@Override
	public List<Map<String, String>> searchAuditsList(Map<String, String> paramMap) {
		return  dw02Mapper.searchAuditsList(paramMap);
	}

}
