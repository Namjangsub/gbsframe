package com.dksys.biz.user.dw.dw03.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dksys.biz.user.dw.dw03.mapper.DW03Mapper;
import com.dksys.biz.user.dw.dw03.service.DW03Svc;

@Service
public class DW03Svcmpl implements DW03Svc {

	@Autowired
    DW03Mapper dw03Mapper;

	@Override
	public List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap) {
		return dw03Mapper.selectDrawDocTreeList(paramMap);
	}

	@Override
	public int selectDrawTreeFileListCount(Map<String, String> paramMap) {
		return dw03Mapper.selectDrawTreeFileListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap) {
		return dw03Mapper.selectDrawTreeFileList(paramMap);
	}

	@Override
	public List<Map<String, String>> select_dw03_detailList(Map<String, String> paramMap) {
		return dw03Mapper.select_dw03_detailList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSalesCdDrawDocTreeList(Map<String, String> paramMap) {
		return dw03Mapper.selectSalesCdDrawDocTreeList(paramMap);
	}

	@Override
	public int selectSalesCdDrawFileListCount(Map<String, String> paramMap) {
		return dw03Mapper.selectSalesCdDrawFileListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSalesCdDrawFileList(Map<String, String> paramMap) {
		return dw03Mapper.selectSalesCdDrawFileList(paramMap);
	}

	@Override
    public Map<String, String> dwgFileDownInfo(Map<String, String> paramMap) throws Exception {
        return dw03Mapper.dwgFileDownInfo(paramMap);
    }

	@Override
	public int selectFileDownAuthChk(Map<String, String> paramMap) {
		return dw03Mapper.selectFileDownAuthChk(paramMap);
	}

}
