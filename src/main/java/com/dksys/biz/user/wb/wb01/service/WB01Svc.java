package com.dksys.biz.user.wb.wb01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB01Svc {

	
	int selectWbsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsList(Map<String, String> paramMap);
	List<Map<String, String>> selectWbsList01(Map<String, String> paramMap);

}