package com.dksys.biz.user.cr.cr21.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.cr.cr21.mapper.CR21Mapper;
import com.dksys.biz.user.cr.cr21.service.CR21Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR21SvcImpl implements CR21Svc {

	@Autowired
	CR21Mapper cr21Mapper;
	
	@Override
	public List<Map<String, String>> selectPrjctVSResultChart(Map<String, String> paramMap) {
		
		return cr21Mapper.selectPrjctVSResultChart(paramMap);
	}
}
