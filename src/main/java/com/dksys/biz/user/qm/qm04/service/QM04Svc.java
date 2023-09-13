package com.dksys.biz.user.qm.qm04.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface QM04Svc {

	// 그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> selectSecondGridList(Map<String, String> paramMap);
}
