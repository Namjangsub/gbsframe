package com.dksys.biz.user.bm.bm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM05Svc {
	
	// 자자재마스터 조회
	int selectBmMstrCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap);
	// 품번 조회
	List<Map<String, String>> selectMatrCd(Map<String, String> paramMap);
	// 상세 입력
	int insertBmMstr(Map<String, String> paramMap);
	// 상세 수정
	int updateBmMstr(Map<String, String> paramMap);
	// 자재마스터 품번 중복 체크 조회
	String selectMatrCdChk(Map<String, String> paramMap);
	// 자재마스터 품번 팝업 조회
	int selectMatListCount(Map<String, String> paramMap);
	List<Map<String, String>> selectMatList(Map<String, String> paramMap);

}
