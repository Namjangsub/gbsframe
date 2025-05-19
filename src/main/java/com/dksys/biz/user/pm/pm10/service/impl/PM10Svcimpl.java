package com.dksys.biz.user.pm.pm10.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.pm.pm10.mapper.PM10Mapper;
import com.dksys.biz.user.pm.pm10.service.PM10Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM10Svcimpl implements PM10Svc {

	@Autowired
	PM10Mapper pm10Mapper;

	@Override
	public List<Map<String, String>> selectMnList(Map<String, String> paramMap) {
		return pm10Mapper.selectMnList(paramMap);
	}

	@Override
	public int updateMn(Map<String, String> param) {
		return pm10Mapper.updateMn(param);
	}

	@Override
	public int deleteMn(Map<String, String> paramMap) {
		return pm10Mapper.deleteMn(paramMap);
	}

  
}