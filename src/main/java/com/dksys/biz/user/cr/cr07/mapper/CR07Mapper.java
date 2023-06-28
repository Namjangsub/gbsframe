package com.dksys.biz.user.cr.cr07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR07Mapper {
	// 수주리스트 조회
	int selectOrdrsDcsnCount(Map<String, String> paramMap);
	List<Map<String, String>> selectOrdrsDcsnList(Map<String, String> paramMap);
	// 매출확정리스트 조회
	int selectSellDcsnCount(Map<String, String> paramMap);
	List<Map<String, String>> selectSellDcsnList(Map<String, String> paramMap);
	// 매출확정등록 조회
	int addSellDscnCount(Map<String, String> paramMap);
	List<Map<String, String>> addSellDscnList(Map<String, String> paramMap);
	// 매출확정 입력
	int insertSellDscn(Map<String, String> paramMap);
	// 매출확정 상세 입력
	int insertSellDscnDetail(Map<String, String> paramMap);
	// 수정 시 정보 조회 
	Map<String, String> select_cr07_Info(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_cr07_SeqNext(Map<String, String> paramMap);
	
	// 매출확정 수정
	int updateSellDscn(Map<String, String> paramMap);
	// 계산서 번호 생성
	String select_cr07_sellBillNo(Map<String, String> paramMap);
	
	
}
