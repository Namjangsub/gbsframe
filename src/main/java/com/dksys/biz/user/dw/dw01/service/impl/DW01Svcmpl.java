package com.dksys.biz.user.dw.dw01.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.user.dw.dw01.mapper.DW01Mapper;
import com.dksys.biz.user.dw.dw01.service.DW01Svc;

@Service
public class DW01Svcmpl implements DW01Svc {

	@Autowired
    DW01Mapper dw01Mapper;

	@Override
	public List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap) {
		return dw01Mapper.selectDrawDocTreeList(paramMap);
	}

	@Override
	public int selectDrawTreeFileListCount(Map<String, String> paramMap) {
		return dw01Mapper.selectDrawTreeFileListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap) {
		return dw01Mapper.selectDrawTreeFileList(paramMap);
	}
	
}
