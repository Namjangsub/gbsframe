package com.dksys.biz.user.cr.cr50.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR50Svc {
	
	List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPFUAreaRetriveList(Map<String, String> paramMap);

	Map<String, String> selectSalesCdInfo(Map<String, String> paramMap);

	Map<String, String> selectPfuInfo(Map<String, String> paramMap);

//	Map<String, String> selectPfuClobInfo(Map<String, String> paramMap);

	int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int updatePfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int deletePfu(Map<String, String> paramMap) throws Exception;

	int selectPfuListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPfuList(Map<String, String> paramMap);

	List<Map<String, String>> selectStdPfuClobInfo(Map<String, String> paramMap);

}