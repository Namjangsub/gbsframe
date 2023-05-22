package com.dksys.biz.user.bm.bm05.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.bm.bm05.mapper.BM05Mapper;
import com.dksys.biz.user.bm.bm05.service.BM05Svc;
import com.dksys.biz.util.ExceptionThrower;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM05SvcImpl implements BM05Svc {
	
	@Autowired
	BM05Mapper bm05Mapper;
	
	@Autowired
	BM05Svc bm05Svc;
	
	@Autowired
	ExceptionThrower thrower;
	
	@Override
	public int selectBmMstrCount(Map<String, String> paramMap) {
		return bm05Mapper.selectBmMstrCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap) {
		return bm05Mapper.selectBmMstrList(paramMap);
	}

	@Override
	public String insertBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		return bm05Mapper.insertBmMstr(paramMap, mRequest);
	}

	@Override
	public int updateBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		return bm05Mapper.updateBmMstr(paramMap, mRequest);
		
	}

}
