package com.dksys.biz.user.sm.sm12.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM12Mapper {
	

	int selectPurchaseListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap);	
	
	int selectMaxTrgtKey(Map<String, String> paramMap);	
	
	String selectMaxInNo(Map<String, String> paramMap);		
	
	//입고 등록, 수정, 삭제
	int insertPurchaseMaster(Map<String, String> paramMap);
	
	int insertPurchaseDetail(Map<String, String> paramMap);	
	
	int updatePurchaseMaster(Map<String, String> paramMap);
	
	int updatePurchaseDetail(Map<String, String> paramMap);
	
	int deletePurchaseMaster(Map<String, String> paramMap);
	
	int deletePurchaseDetail(Map<String, String> param);	
}




