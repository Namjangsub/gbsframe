package com.dksys.biz.user.bm.bm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM05Svc {
	int selectBmMstrCount(Map<String, String> paramMap);

	List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap);

	int insertBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	int updateBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

}
