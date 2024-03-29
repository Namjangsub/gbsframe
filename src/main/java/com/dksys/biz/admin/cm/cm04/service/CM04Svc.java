package com.dksys.biz.admin.cm.cm04.service;

import java.util.List;
import java.util.Map;

public interface CM04Svc {
	
	List<Map<String, String>> selectDeptTree(Map<String, String> paramMap);

	Map<String, String> selectDeptInfo(Map<String, String> paramMap);

	int selectDeptCount(Map<String, String> paramMap);

	void updateDept(Map<String, String> paramMap) throws Exception;

	void moveDept(List<Map<String, String>> paramList) throws Exception;
	
	List<Map<String, String>> select_deptId_code(Map<String, String> paramMap);
	
}