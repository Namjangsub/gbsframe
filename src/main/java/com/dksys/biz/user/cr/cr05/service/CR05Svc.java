package com.dksys.biz.user.cr.cr05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR05Svc {
	// 수금 조회
	int selectClmnListCount(Map<String, String> paramMap);
	List<Map<String, String>> selectClmnList(Map<String, String> paramMap);
	
	// INSERT
	int insert_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	// UPDATE
	int update_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	//DELETE
	int delete_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequset) throws Exception;
	
	// 수금정보 조회
	int selectClmnInfoCount(Map<String, String> paramMap);
	List<Map<String, String>> selectClmnInfo(Map<String, String> paramMap);
	// 수금번호 조회
	List<Map<String, String>> select_cr05_clmnNo(Map<String, String> paramMap);

}
