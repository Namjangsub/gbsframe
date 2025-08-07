package com.dksys.biz.user.fi.fi30.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.fi.fi30.mapper.FI30Mapper;
import com.dksys.biz.user.fi.fi30.service.FI30Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class FI30SvcImpl implements FI30Svc {
	
	@Autowired
	FI30Mapper fi30Mapper;
	
	@Autowired
	FI30Svc fi30Svc;

	@Override
	public int select_fi30_Count(Map<String, String> paramMap) {
		return fi30Mapper.select_fi30_Count(paramMap);
	}

	@Override
	public List<Map<String, String>> select_fi30_List(Map<String, String> paramMap) {
		return fi30Mapper.select_fi30_List(paramMap);
	}

	@Override
	public Map<String, String> select_fi30_detail_info(Map<String, String> paramMap) {
		return fi30Mapper.select_fi30_detail_info(paramMap);
	}
}
