package com.dksys.biz.user.bm.bm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM05Svc {
	int selectBmMstrCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap);
	// 품번 조회
	List<Map<String, String>> selectMatrCd(Map<String, String> paramMap);
	
//	int insertBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	int insertBmMstr(Map<String, String> paramMap);

//	int updateBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	int updateBmMstr(Map<String, String> paramMap);
	// 자재마스터 품번 중복 체크 조회
	String selectMatrCdChk(Map<String, String> paramMap);
	// 자재마스터 팝업 품번 그리드
	List<Map<String, String>> selectMatList(Map<String, String> paramMap);


}
