package com.dksys.biz.admin.bm.bm16.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.bm.bm16.mapper.BM16Mapper;
import com.dksys.biz.admin.bm.bm16.service.BM16Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM16SvcImpl implements BM16Svc {
	
    @Autowired
    BM16Mapper bm16Mapper;

	@Override
	public int selectPrjctCount(Map<String, String> paramMap) {
		return bm16Mapper.selectPrjctCount(paramMap);
	}

	@Override 
	public List<Map<String, String>> selectPrjctList(Map<String, String> paramMap) {
		return bm16Mapper.selectPrjctList(paramMap);
	}

}