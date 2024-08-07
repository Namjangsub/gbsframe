package com.dksys.biz.user.sm.sm15.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM15Svc {

	List<Map<String, String>> selectSM15MainList(Map<String, String> paramMap);

	List<Map<String, String>> sm15selectPurchaseListNew(Map<String, String> paramMap);

	List<Map<String, String>> sm15_2selectPurchaseListNew(Map<String, String> paramMap);

	int sm15_2selectPurchaseListNewCount(Map<String, String> paramMap);

	int selectSM15MainListCount(Map<String, String> paramMap);
}