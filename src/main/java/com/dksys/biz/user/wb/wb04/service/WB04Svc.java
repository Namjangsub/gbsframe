package com.dksys.biz.user.wb.wb04.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB04Svc {
	
    List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanCloseYnNTreeList(Map<String, String> paramMap);
	
	int selectWbsPlanCloseYnNTreeListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanCloseYnNTreeListSub(Map<String, String> paramMap);
}