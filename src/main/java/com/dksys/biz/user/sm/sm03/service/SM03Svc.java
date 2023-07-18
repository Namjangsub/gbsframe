package com.dksys.biz.user.sm.sm03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM03Svc {

	// 발주 리스트 카운트	
	int selectWareHousingListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWareHousingList(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWareHousingExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWareHousingDetailList(Map<String, String> paramMap);			
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	String selectMaxInNo(Map<String, String> paramMap);	

	//발주마스터 등록, 수정, 삭제
	int insertWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
		
	int updateWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
	int updateWareHousingDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int deleteWareHousingMaster(Map<String, String> param);			
	
	int deleteWareHousingDetail(Map<String, String> param);	
}