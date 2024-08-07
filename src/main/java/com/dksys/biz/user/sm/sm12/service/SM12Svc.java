package com.dksys.biz.user.sm.sm12.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM12Svc {

	// 발주 리스트 카운트	
	int selectPurchaseListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap);			
		
	String selectMaxPchsNo(Map<String, String> paramMap);	

	//발주마스터 등록, 수정, 삭제
	int insertPurchaseDetail(Map<String, String> paramMap) throws Exception;	
		
	int updatePurchaseDetail(Map<String, String> paramMap) throws Exception;			
	
	int deletePurchaseDetail(Map<String, String> param);	
}