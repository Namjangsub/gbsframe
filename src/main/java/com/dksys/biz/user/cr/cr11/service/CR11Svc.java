package com.dksys.biz.user.cr.cr11.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR11Svc {
	
	// 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
		
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
  
	//모달 그리드 조회 -수정화면 정보
	Map<String, String> select_cr11_Info(Map<String, String> paramMap);
	
	//모달창 안에 그리드 grid-modal
	List<Map<String, String>> grid_Modal_selectList(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_cr11_Modal(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

}