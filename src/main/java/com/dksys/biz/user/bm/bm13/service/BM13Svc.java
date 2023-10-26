package com.dksys.biz.user.bm.bm13.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM13Svc {

	// 결재선목록 리스트 카운트	
	int selectApprovalListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalExcelList(Map<String, String> paramMap);
		
	//결재선 등록, 수정, 삭제
	int insertApproval(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
		
	int updateApproval(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int deleteApproval(Map<String, String> paramMap) throws Exception;
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);

	
}