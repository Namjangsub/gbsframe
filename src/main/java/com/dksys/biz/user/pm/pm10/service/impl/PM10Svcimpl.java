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
	public int pm10_main_update(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_main_update(param);
	}

	@Override
	public int pm10_d03_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param); // 일자별 마스터테이블 추가
		pm10Mapper.pm10_d01_update(param); // 주제 추가
		return pm10Mapper.pm10_d03_update(param);
	}

	@Override
	public int deleteMn(Map<String, String> paramMap) throws Exception {
		// D01 삭제
		Integer result = pm10Mapper.deleteMnD01(paramMap);
		// D03 삭제
		result += pm10Mapper.deleteMnD03(paramMap);

		pm10Mapper.deleteMnM01(paramMap);

		return result;
	}

	@Override
	public int pm10_d01_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param);
		return pm10Mapper.pm10_d01_update(param);
	}

	

	

	

  
}