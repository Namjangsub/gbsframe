package com.dksys.biz.user.bm.bm10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM10Svc {
	int selectBmPrdtMstrCount(Map<String, String> paramMap);

	List<Map<String, String>> selectBmPrdtMstrList(Map<String, String> paramMap);

	List<Map<String, String>> selectBmProdSearchList(Map<String, String> paramMap);

	String insertBmPrdtMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	int updateBmPrdtMstr(Map<String, String> paramMap);

	int selectBmProdCount(Map<String, String> paramMap);

}
