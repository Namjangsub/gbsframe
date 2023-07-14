package com.dksys.biz.user.wb.wb04.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB04Svc {
	
	List<Map<String, String>> selectWbsLeftSalesCodeTreeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap);
	
    List<Map<String, String>> selectGanttList(Map<String, String> paramMap);

}