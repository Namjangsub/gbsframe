package com.dksys.biz.user.bm.bm11.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.bm.bm11.mapper.BM11Mapper;
import com.dksys.biz.user.bm.bm11.service.BM11Svc;
import com.dksys.biz.util.ExceptionThrower;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM11SvcImpl implements BM11Svc {
	
	@Autowired
	BM11Mapper bm11Mapper;
	
	@Autowired
	BM11Svc bm11Svc;
	
	@Autowired
	ExceptionThrower thrower;
	
	@Override
	public int selectBmUprMstrCount(Map<String, String> paramMap) {
		return bm11Mapper.selectBmUprMstrCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBmUprMstrList(Map<String, String> paramMap) {
		return bm11Mapper.selectBmUprMstrList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectUprHsMstrList(Map<String, String> paramMap) {
		return bm11Mapper.selectUprHsMstrList(paramMap);
	}

	@Override
	public int insertBmUprMstr(Map<String, String> paramMap) {
		return bm11Mapper.insertBmUprMstr(paramMap);
	}

	@Override
	public int UpdateBmUprMstr(Map<String, String> paramMap) {
		return bm11Mapper.UpdateBmUprMstr(paramMap);
		
	}

}
