package com.dksys.biz.admin.cm.cm12.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm12.mapper.CM12Mapper;
import com.dksys.biz.admin.cm.cm12.service.CM12Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM12SvcImpl implements CM12Svc {
	
    @Autowired
    CM12Mapper cm12Mapper;
    

	@Override
    public List<Map<String, String>> selectSolarLunarEventHolidaysList(Map<String, String> paramMap) {
        return cm12Mapper.selectSolarLunarEventHolidaysList(paramMap);
	}

}