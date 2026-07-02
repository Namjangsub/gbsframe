package com.dksys.biz.admin.cm.cm30.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.admin.cm.cm30.mapper.CM30Mapper;
import com.dksys.biz.admin.cm.cm30.service.CM30Svc;

@Service
public class CM30SvcImpl implements CM30Svc {

    @Autowired
    CM30Mapper cm30Mapper;

	@Override
	public String selectGridConfig(Map<String, String> param) {
		return cm30Mapper.selectGridConfig(param);
	}

	@Override
	public int saveGridConfig(Map<String, String> param) {
		return cm30Mapper.saveGridConfig(param);
	}

	@Override
	public int deleteGridConfig(Map<String, String> param) {
		return cm30Mapper.deleteGridConfig(param);
	}

}
