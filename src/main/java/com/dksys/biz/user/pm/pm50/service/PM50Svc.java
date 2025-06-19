package com.dksys.biz.user.pm.pm50.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM50Svc {

	int select_pm50_ListCount(Map<String, String> paramMap);

    List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap);
	
	Map<String, Object> select_pm50_Info(Map<String, String> paramMap);

	int upLoadFiles(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int delete_pm50(Map<String, String> paramMap);
}
