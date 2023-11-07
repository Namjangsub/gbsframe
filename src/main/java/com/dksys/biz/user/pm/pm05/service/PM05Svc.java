package com.dksys.biz.user.pm.pm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM05Svc {
	int pm05_grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> pm05_grid1_selectList(Map<String, String> paramMap);
	
	List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);
	
	List<Map<String, String>> pm05_grid1_selectList_m(Map<String, String> paramMap);
	
	
}