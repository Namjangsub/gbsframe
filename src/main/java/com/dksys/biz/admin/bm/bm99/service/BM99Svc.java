package com.dksys.biz.admin.bm.bm99.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM99Svc {

	Map<String, Object> selectManualInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectManualFileInfo(Map<String, String> paramMap);
	
	void updateManualInfo(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

}