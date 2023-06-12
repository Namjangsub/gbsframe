package com.dksys.biz.user.bm.bm13.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM13Svc {

	// 결재선목록 리스트 카운트	
	int selectApprovalListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);
		
}