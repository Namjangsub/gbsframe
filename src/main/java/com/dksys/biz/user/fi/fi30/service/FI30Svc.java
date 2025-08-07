package com.dksys.biz.user.fi.fi30.service;

import java.util.List;
import java.util.Map;

public interface FI30Svc {
	
	// 그리드 카운트
	int select_fi30_Count(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> select_fi30_List(Map<String, String> paramMap);

	// 상세 정보 조회
	Map<String, String> select_fi30_detail_info(Map<String, String> paramMap);
}
