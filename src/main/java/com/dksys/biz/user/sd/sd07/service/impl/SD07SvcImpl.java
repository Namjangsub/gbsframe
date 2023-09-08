package com.dksys.biz.user.sd.sd07.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.sd.sd07.mapper.SD07Mapper;
import com.dksys.biz.user.sd.sd07.service.SD07Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SD07SvcImpl implements SD07Svc {

    @Autowired
    SD07Mapper sd07Mapper;
    
    @Override
    public List<Map<String, String>> selectCloseYmList(Map<String, String> paramMap) {
    	return sd07Mapper.selectCloseYmList(paramMap);
    }
    
	@Override
	public void saveClose(Map<String, String> paramMap) {
		sd07Mapper.saveClose(paramMap);
	}

	@Override
	public List<Map<String, String>> selectCloseLastYm(Map<String, String> paramMap) {
		return sd07Mapper.selectCloseLastYm(paramMap);
	}
	
}