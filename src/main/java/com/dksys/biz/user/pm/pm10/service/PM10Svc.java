package com.dksys.biz.user.pm.pm10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM10Svc {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> select_p10_d02_List(Map<String, String> paramMap);

	int pm10_main_update(Map<String, String> param) throws Exception;

	int pm10_d01_update(Map<String, String> param) throws Exception;

	int pm10_d03_update(Map<String, String> param) throws Exception;

	int deleteMn(Map<String, String> paramMap) throws Exception;

	int pm10_d01_sortNo_update(Map<String, Object> paramMap) throws Exception;

	int pm10_d02_update(Map<String, Object> paramMap) throws Exception;

	int pm10_d02_delete(Map<String, Object> paramMap) throws Exception;

	public int mnUploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int lockD01Cell(Map<String, String> param) throws Exception;

	int lockD03Cell(Map<String, String> param) throws Exception;

	int unlockD01Cell(Map<String, String> param) throws Exception;

	int unlockD03Cell(Map<String, String> param) throws Exception;

	int unlockUserLocks(Map<String, String> param) throws Exception;

	Map<String, String> selectD01Cell(Map<String, String> param) throws Exception;

	Map<String, String> selectD03Cell(Map<String, String> param) throws Exception;
}
