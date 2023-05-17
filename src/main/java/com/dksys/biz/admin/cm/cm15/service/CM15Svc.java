package com.dksys.biz.admin.cm.cm15.service;

import java.util.List;
import java.util.Map;

public interface CM15Svc {

	int updateFileAuth(Map<String, Object> param);
	
	public int selectTreeAuthCount(Map<String, String> paramMap);
	
	public List<Map<String, String>> selectTreeAuthList(Map<String, String> paramMap);
//
//	int selectConfirmCount(Map<String, String> paramMap);
//	
//	int moveFile(Map<String, String> paramMap);
//
//	int deleteFileCall(Map<String, String> paramMap);


}