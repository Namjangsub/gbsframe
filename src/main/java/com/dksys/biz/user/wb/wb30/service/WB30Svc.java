package com.dksys.biz.user.wb.wb30.service;

import java.util.List;
import java.util.Map;

public interface WB30Svc {

	// WBS 기준 템플릿 마스터 목록 조회
	List<Map<String, Object>> selectWbsTmplatList(Map<String, String> paramMap);

	// WBS 기준 템플릿 단계 상세 목록 조회
	List<Map<String, Object>> selectWbsTmplatDetailList(Map<String, String> paramMap);

	// 템플릿 마스터 및 단계 목록 일괄 저장 (CUD 단일 트랜잭션)
	int saveWbsTmplat(Map<String, Object> paramMap) throws Exception;

	// 템플릿 삭제
	int deleteWbsTmplat(Map<String, Object> paramMap) throws Exception;

}
