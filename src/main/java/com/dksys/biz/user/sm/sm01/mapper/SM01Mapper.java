package com.dksys.biz.user.sm.sm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM01Mapper {
	// 수주리스트 조회
	int selectBomSalesCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap);
	// BOM내역상세 조회
	int selectBomDetailCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);
	// 매출확정등록 조회
	int addSellDscnCount(Map<String, String> paramMap);
	List<Map<String, String>> addSellDscnList(Map<String, String> paramMap);
	// 매출확정등록 입력
	int insertSellDscn(Map<String, String> paramMap);
	
	
}
