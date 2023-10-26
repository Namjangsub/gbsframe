package com.dksys.biz.user.bm.bm17.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM17Svc {

	// 결재선목록 리스트 카운트	
	int selectMessageTemplListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMessageTemplList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMessageTemplExcelList(Map<String, String> paramMap);
		
	//결재선 등록, 수정, 삭제
	int insertMessageTempl(Map<String, String> paramMap) throws Exception;	
		
	int updateMessageTempl(Map<String, String> paramMap) throws Exception;
	
	int deleteMessageTempl(Map<String, String> paramMap) throws Exception;
	
	//테스트 버튼 등록
	int insertKakaoMessage(Map<String, String> paramMap) throws Exception;	
	
	Map<String, Object> selectMessageTemplInfo(Map<String, String> paramMap) throws Exception;	
}