package com.dksys.biz.user.dw.dw02.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.dw.dw02.mapper.DW02Mapper;
import com.dksys.biz.user.dw.dw02.service.DW02Svc;


@Service
@Transactional(rollbackFor = Exception.class)
public class DW02SvcImpl implements DW02Svc {


	@Autowired
	DW02Mapper dw02Mapper;
	
	@Override
	public int drawingAuditsListCount(Map<String, String> paramMap) {
		return dw02Mapper.drawingAuditsListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> drawingAuditsList(Map<String, String> paramMap) {
		return  dw02Mapper.drawingAuditsList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDrawDocTreeList(Map<String, String> paramMap) {
		return dw02Mapper.selectDrawDocTreeList(paramMap);
	}

	@Override
	public int selectDrawTreeFileListCount(Map<String, String> paramMap) {
		return dw02Mapper.selectDrawTreeFileListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDrawTreeFileList(Map<String, String> paramMap) {
		return dw02Mapper.selectDrawTreeFileList(paramMap);
	}

	@Override
	public Map<String, Object> selectDrawDocItemInfo(Map<String, String> paramMap) {
		return dw02Mapper.selectDrawDocItemInfo(paramMap);
	}

	@Override
	public int insertDrawItem(Map<String, String> paramMap) {
		return dw02Mapper.insertDrawItem(paramMap);
	}

	@Override
	public int updateDrawItem(Map<String, String> paramMap) {
		return dw02Mapper.updateDrawItem(paramMap);
	}
	
    @Override
    public int deleteDrawDocItem(Map<String, String> paramMap) throws Exception {
        return dw02Mapper.deleteDrawDocItem(paramMap);
    }

}
