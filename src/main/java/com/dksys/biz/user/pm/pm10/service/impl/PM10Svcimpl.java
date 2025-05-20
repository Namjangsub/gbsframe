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
	public int pm10_main_insert(Map<String, String> param) throws Exception {
		System.out.println("pm10Mapper = " + pm10Mapper);
		return pm10Mapper.pm10_main_insert(param);
	}

	@Override
	public int pm10_d03_insert(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_d03_insert(param);
	}

	@Override
	public int pm10_d03_update(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_d03_update(param);
	}

	@Override
	public int insertMn(Map<String, String> param) throws Exception {
		return pm10Mapper.insertMn(param);
	}

	@Override
	public int updateMn(Map<String, String> param) throws Exception {
		return pm10Mapper.updateMn(param);
	}

	@Override
	public int deleteMn(Map<String, String> paramMap) throws Exception {
		return pm10Mapper.deleteMn(paramMap);
	}

	@Override
	public int pm10_d01_insert(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_d01_insert(param);
	}

	@Override
	public int pm10_d01_update(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_d01_update(param);
	}

	

	

	

  
}