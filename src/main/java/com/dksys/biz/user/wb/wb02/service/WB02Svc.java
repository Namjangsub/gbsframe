package com.dksys.biz.user.wb.wb02.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB02Svc {

	int selectWbsRsltsPlanListCount(Map<String, String> paramMap);
	List<Map<String, String>> selectWbsRsltsPlanList(Map<String, String> paramMap);

	
}