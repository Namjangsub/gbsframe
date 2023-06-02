package com.dksys.biz.user.bm.bm10.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.bm.bm10.mapper.BM10Mapper;
import com.dksys.biz.user.bm.bm10.service.BM10Svc;
import com.dksys.biz.util.ExceptionThrower;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM10SvcImpl implements BM10Svc {

	@Autowired
	BM10Mapper bm10Mapper;

	@Autowired
	BM10Svc bm10Svc;

	@Autowired
	ExceptionThrower thrower;

//	@Override
//	public int selectBmPrdtMstrCount(Map<String, String> paramMap) {
//		return bm10Mapper.selectBmPrdtMstrCount(paramMap);
//	} 

	@Override
	public int selectBmPrdtMstrCount(Map<String, String> paramMap) {
		// TODO Auto-generated method stub
		return bm10Mapper.selectBmPrdtMstrCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBmPrdtMstrList(Map<String, String> paramMap) {
		return bm10Mapper.selectBmPrdtMstrList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBmProdSearchList(Map<String, String> paramMap) {
		return bm10Mapper.selectBmProdSearchList(paramMap);
	}

	@Override
	public String insertBmPrdtMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		return bm10Mapper.insertBmPrdtMstr(paramMap, mRequest);
	}

	@Override
	public int updateBmPrdtMstr(Map<String, String> paramMap) {
		return bm10Mapper.updateBmPrdtMstr(paramMap);

	}

}
