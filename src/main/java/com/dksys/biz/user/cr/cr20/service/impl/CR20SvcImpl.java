package com.dksys.biz.user.cr.cr20.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.cr.cr20.mapper.CR20Mapper;
import com.dksys.biz.user.cr.cr20.service.CR20Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR20SvcImpl implements CR20Svc {
	
	@Autowired
	CR20Mapper cr20Mapper;
	
	@Autowired
	CR20Svc cr20Svc;

	// 그리드 카운트
	@Override
	public int select_cr20m01_List_Count(Map<String, String> paramMap) {
		return cr20Mapper.select_cr20m01_List_Count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> select_cr20m01_List(Map<String, String> paramMap) {
		return cr20Mapper.select_cr20m01_List(paramMap);
	}

}
