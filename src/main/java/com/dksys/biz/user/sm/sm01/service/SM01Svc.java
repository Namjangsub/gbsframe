package com.dksys.biz.user.sm.sm01.service;

import java.util.List;
import java.util.Map;

public interface SM01Svc {
	// 수주리스트
	int selectBomSalesCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap);

	// BOM내역상세 조회
	int selectBomMakerCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBomMakerList(Map<String, String> paramMap);
	
	// 매출확정등록 조회
	int addSellDscnCount(Map<String, String> paramMap);
	List<Map<String, String>> addSellDscnList(Map<String, String> paramMap);
	
	// 매출확정등록 입력
	int insertSellDscn(Map<String, String> paramMap);
	

}
