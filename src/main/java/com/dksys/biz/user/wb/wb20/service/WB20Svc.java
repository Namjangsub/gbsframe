package com.dksys.biz.user.wb.wb20.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB20Svc {


	int selectToDoCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectToDoList(Map<String, String> paramMap);

	
}