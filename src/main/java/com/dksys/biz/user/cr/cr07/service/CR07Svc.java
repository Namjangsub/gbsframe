package com.dksys.biz.user.cr.cr07.service;

import java.util.List;
import java.util.Map;

public interface CR07Svc {
	// 수주리스트
	public int selectOrdrsDcsnCount(Map<String, String> paramMap);
	public List<Map<String, String>> selectOrdrsDcsnList(Map<String, String> paramMap);

	// 매출확정리스트 조회
	public int selectSellDcsnCount(Map<String, String> paramMap);
	public List<Map<String, String>> selectSellDcsnList(Map<String, String> paramMap);
	
	// 매출확정등록 조회
	public int addSellDscnCount(Map<String, String> paramMap);
	public List<Map<String, String>> addSellDscnList(Map<String, String> paramMap);
	
	// 매출확정등록 입력
	public int insertSellDscn(Map<String, String> paramMap);
	
	

}
