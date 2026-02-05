package com.dksys.biz.user.mm.mm01.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.mm.mm01.mapper.MM01Mapper;
import com.dksys.biz.user.mm.mm01.service.MM01Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class MM01SvcImpl implements MM01Svc {

	@Autowired
	private MM01Mapper mm01Mapper;

	@Override
	public List<Map<String, String>> selectMindMapList(Map<String, String> paramMap) {
		return mm01Mapper.selectMindMapList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectAgendaList(Map<String, String> paramMap) {
		return mm01Mapper.selectAgendaList(paramMap);
	}

}
