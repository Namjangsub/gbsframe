package com.dksys.biz.user.qm.qm05.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface QM05Svc {

	  int selectMainGridListCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);

	List<Map<String, String>> select_dept_code(Map<String, String> paramMap);
}
