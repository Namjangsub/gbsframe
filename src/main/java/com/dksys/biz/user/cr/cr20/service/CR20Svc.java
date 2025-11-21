package com.dksys.biz.user.cr.cr20.service;

import java.util.List;
import java.util.Map;

public interface CR20Svc {

	// 그리드 리스트
	int select_cr20m01_List_Count(Map<String, String> paramMap);
	
	List<Map<String, String>> select_cr20m01_List(Map<String, String> paramMap);

}