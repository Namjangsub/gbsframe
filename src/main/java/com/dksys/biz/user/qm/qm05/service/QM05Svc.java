package com.dksys.biz.user.qm.qm05.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface QM05Svc {

	// 그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);
}
