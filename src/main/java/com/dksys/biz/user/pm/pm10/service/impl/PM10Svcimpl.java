package com.dksys.biz.user.pm.pm10.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.pm.pm10.mapper.PM10Mapper;
import com.dksys.biz.user.pm.pm10.service.PM10Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM10Svcimpl implements PM10Svc {

	@Autowired
	PM10Mapper pm10Mapper;

	@Override
	public List<Map<String, String>> selectMnList(Map<String, String> paramMap) {
		return pm10Mapper.selectMnList(paramMap);
	}

	@Override
	public List<Map<String, String>> select_p10_d02_List(Map<String, String> paramMap) {
		return pm10Mapper.select_p10_d02_List(paramMap);
	}

	@Override
	public int pm10_main_update(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_main_update(param);
	}

	@Override
	public int pm10_d03_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param); // 일자별 마스터테이블 추가
		pm10Mapper.pm10_d01_update(param); // 주제 추가
		return pm10Mapper.pm10_d03_update(param);
	}

	@Override
	public int deleteMn(Map<String, String> paramMap) throws Exception {
		// D01 삭제

		Integer result = 0;

		Object mnSubSeq = paramMap.get("mnSubSeq");
		if (mnSubSeq != null && !"".equals(mnSubSeq)) {
			result += pm10Mapper.deleteMnD01(paramMap);
			// D03 삭제
			result += pm10Mapper.deleteMnD03(paramMap);
		}

		// if ("해당 날짜에 해당하는 주제 및 내용이 없다면 메인 삭제, 참석자 테이블도 삭제") {
		// 	pm10Mapper.deleteMnM01(paramMap);
		// }

		return result;
	}

	@Override
	public int pm10_d01_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param);
		return pm10Mapper.pm10_d01_update(param);
	}

	@Override
	public int pm10_d01_sortNo_update(Map<String,Object> paramMap) throws Exception {
		int result = 0;
		List<Map<String,Object>> list = (List<Map<String,Object>>) paramMap.get("list");
		
		for (Map<String,Object> dtl : list) {
			dtl.put("mnDate", paramMap.get("mnDate"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId",  paramMap.get("pgmId"));
			result += pm10Mapper.pm10_d01_sortNo_update(dtl);
		}
		return result;
	}

	@Override
	public int pm10_d02_update(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		List<Map<String,Object>> attedList = (List<Map<String,Object>>) paramMap.get("attendList");
		
		for (Map<String,Object> dtl : attedList) {
			dtl.put("mnDate", paramMap.get("mnDate"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId",  paramMap.get("pgmId"));
			result += pm10Mapper.pm10_d02_update(dtl);
		}
		return result;
	}

	@Override
	public int pm10_d02_delete(Map<String,Object> paramMap) throws Exception {
		int result = 0;
		List<String> ids = (List<String>) paramMap.get("attendIds");
		for (String id : ids) {
			Map<String,Object> p = new HashMap<>();
			p.put("mnDate", paramMap.get("mnDate"));
			p.put("userId", id);
			result += pm10Mapper.pm10_d02_delete(p);
		}
		return result;
	}

	

}