package com.dksys.biz.user.sm.sm14.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM14Mapper {
	

	int selectPurchaseListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap);	
	
	String selectMaxPchsNo(Map<String, String> paramMap);		
	
	//입고 등록, 수정, 삭제	
	int insertPurchaseBillDetail(Map<String, String> paramMap);	
	
	int updatePurchaseBillDetail(Map<String, String> paramMap);
	
	int updatePurchaseMaster(Map<String, String> paramMap);	
	
	int deletePurchaseDetail(Map<String, String> param);
	
	int deletePurchaseMaster(Map<String, String> param);	
	
	int updateBillYn(Map<String, String> param);	

}




