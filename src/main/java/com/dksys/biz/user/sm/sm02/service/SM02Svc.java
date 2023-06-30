package com.dksys.biz.user.sm.sm02.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM02Svc {

	// 발주 리스트 카운트	
	int selectOrderListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderDetailList(Map<String, String> paramMap);		
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	String selectMaxOrdrgNo(Map<String, String> paramMap);	

	//발주마스터 등록, 수정, 삭제
	int insertOrderMaster(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
		
	int updateOrderMaster(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int deleteOrderMaster(Map<String, String> paramMap) throws Exception;
	
	int deleteOrderDetail(Map<String, String> param);	
}