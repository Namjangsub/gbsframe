package com.dksys.biz.user.gm.gm01.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.gm.gm01.mapper.GM01Mapper;
import com.dksys.biz.user.gm.gm01.service.GM01Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class GM01SvcImpl implements GM01Svc {

	@Autowired
	GM01Mapper gm01Mapper;

	@Override
	public int selectSecUsbCount(Map<String, String> paramMap) {
		return gm01Mapper.selectSecUsbCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSecUsbList(Map<String, String> paramMap) {
		return gm01Mapper.selectSecUsbList(paramMap);
	}

	@Override
	public int insertSecUsbOut(Map<String, String> paramMap) {
		String nextMngNo = gm01Mapper.selectSecUsbNextMngNo(paramMap);
		paramMap.put("mngNo", nextMngNo);
		return gm01Mapper.insertSecUsbOut(paramMap);
	}

	@Override
	public int updateSecUsbOut(Map<String, String> paramMap) {
		return gm01Mapper.updateSecUsbOut(paramMap);
	}

	@Override
	public int updateSecUsbIn(Map<String, String> paramMap) {
		return gm01Mapper.updateSecUsbIn(paramMap);
	}

	@Override
	public int cancelSecUsbIn(Map<String, String> paramMap) {
		return gm01Mapper.updateSecUsbInCancel(paramMap);
	}

	@Override
	public int deleteSecUsb(Map<String, String> paramMap) {
		return gm01Mapper.deleteSecUsb(paramMap);
	}

}
