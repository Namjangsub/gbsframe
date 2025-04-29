package com.dksys.biz.user.sm.sm30.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM30Svc {
	
	Map<String, String> selectPjtInfo(Map<String, String> paramMap);

	List<Map<String, String>> selectPjtInfoDetail(Map<String, String> paramMap);

	// 엑셀업로드 전 체크
	List<Map<String, String>> select_sm21_chk( Map<String, Object> paramMap);

	// 엑셀업로드 insert
	int insert_sm21_excelUpload(Map<String, String> paramMap);

	int insert_sm30(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
}
