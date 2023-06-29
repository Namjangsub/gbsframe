package com.dksys.biz.user.cr.cr07.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR07Svc {
	// 수주리스트
	int selectOrdrsDcsnCount(Map<String, String> paramMap);
	List<Map<String, String>> selectOrdrsDcsnList(Map<String, String> paramMap);

	// 매출확정리스트 조회
	int selectSellDcsnCount(Map<String, String> paramMap);
	List<Map<String, String>> selectSellDcsnList(Map<String, String> paramMap);
	
	// 매출확정등록 조회
	int addSellDscnCount(Map<String, String> paramMap);
	List<Map<String, String>> addSellDscnList(Map<String, String> paramMap);
	
	// 매출확정 입력
	int insertSellDscn(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	// 매출확정 상세입력
	int insertSellDscnDetail(Map<String, String> paramMap);

	// 수정 시 정보 조회
	Map<String, String> select_cr07_Info(Map<String, String> paramMap);
	// 매출확정 수정
	int updateSellDscn(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	// 매출확정번호 조회
	List<Map<String, String>> select_cr07_sellDcsnNo(Map<String, String> paramMap);
	
	// 매출확정 삭제
	int delete_cr07(Map<String, String> paramMap) throws Exception;

}
