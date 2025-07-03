package com.dksys.biz.user.pm.pm50.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM50Svc {

	int select_pm50_ListCount(Map<String, String> paramMap);

    List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap);

	int selectSendFileCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSendFileList(Map<String, String> paramMap);

	int insert_pm50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int update_pm50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int update_issue_pm50_d01(Map<String, Object> paramMap) throws Exception;
	
	int update_issue_reset_pm50_d01(Map<String, String> paramMap) throws Exception;
	
	Map<String, String> select_pm50_Info(Map<String, String> paramMap);

	Map<String, String> select_salesCd_info(Map<String, String> paramMap);

	List<Map<String, String>> selectBfuAllFileRows(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBfuFileRows(Map<String, String> paramMap);

	int delete_pm50(Map<String, String> paramMap) throws Exception;
}
