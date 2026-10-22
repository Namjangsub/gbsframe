package com.dksys.biz.user.wb.wb30.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB30Mapper {

	// WBS 기준 템플릿 마스터 목록 조회
	List<Map<String, Object>> selectWbsTmplatList(Map<String, String> paramMap);

	// WBS 기준 템플릿 단계 상세 목록 조회
	List<Map<String, Object>> selectWbsTmplatDetailList(Map<String, String> paramMap);

	// 템플릿 마스터 등록
	int insertWbsTmplatMaster(Map<String, Object> paramMap);

	// 템플릿 마스터 수정
	int updateWbsTmplatMaster(Map<String, Object> paramMap);

	// 템플릿 마스터 삭제
	int deleteWbsTmplatMaster(Map<String, Object> paramMap);

	// 템플릿 단계 상세 등록
	int insertWbsTmplatDetail(Map<String, Object> paramMap);

	// 템플릿 단계 상세 일괄 삭제
	int deleteWbsTmplatDetail(Map<String, Object> paramMap);

}
