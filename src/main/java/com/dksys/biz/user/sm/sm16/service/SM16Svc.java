package com.dksys.biz.user.sm.sm16.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM16Svc {

	List<Map<String, String>> selectSM16MainList(Map<String, String> paramMap);

	List<Map<String, String>> sm16selectPurchaseListNew(Map<String, String> paramMap);

}