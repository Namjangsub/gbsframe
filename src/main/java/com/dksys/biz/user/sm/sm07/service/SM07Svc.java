package com.dksys.biz.user.sm.sm07.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM07Svc {

	/*마스터 페이지 조회*/
	// 발주 리스트 카운트	
	int master_grid_selectCount(Map<String, String> paramMap);
	// 발주 리스트
	List<Map<String, String>> master_grid_selectList(Map<String, String> paramMap);
	/*마스터 페이지 조회*/
	
	//모달 그리드 조회 -수정화면 정보
	Map<String, String> select_sm07_Info(Map<String, String> paramMap);
	
	
}