package com.dksys.biz.user.cr.cr50.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR50Svc {
	
	List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap);

	Map<String, String> selectPfuInfo(Map<String, String> paramMap);

//	Map<String, String> selectPfuClobInfo(Map<String, String> paramMap);

	int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

}