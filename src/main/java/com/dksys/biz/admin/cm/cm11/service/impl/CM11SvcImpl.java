package com.dksys.biz.admin.cm.cm11.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm11.mapper.CM11Mapper;
import com.dksys.biz.admin.cm.cm11.service.CM11Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM11SvcImpl implements CM11Svc {
	
    @Autowired
    CM11Mapper cm11Mapper;
    
	@Override
	public String selectSearchDttm() {
		return cm11Mapper.selectSearchDttm();
	}
    

	@Override
	public List<Map<String, String>> selectPrjectDashList(Map<String, String> paramMap) {
		return cm11Mapper.selectPrjectDashList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectClientTaxDashList(Map<String, String> paramMap) {
		return cm11Mapper.selectClientTaxDashList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectClientPchsDashList(Map<String, String> paramMap) {
		return cm11Mapper.selectClientPchsDashList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectFacList(Map<String, String> paramMap) {
		return cm11Mapper.selectFacList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectMonthlyStat(Map<String, String> paramMap) {
		return cm11Mapper.selectMonthlyStat(paramMap);
	}
}