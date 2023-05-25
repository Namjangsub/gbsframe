package com.dksys.biz.user.wb.wb03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB03Svc {

	
	int selectWbsPlanCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap);
	

}