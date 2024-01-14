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
	
	//발주+기타비용 추가
	List<Map<String, String>> selectPurchaseListUnion(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap);
		
	String selectMaxPchsNo(Map<String, String> paramMap);		
	
	//매입	
	int insertPurchaseBillDetail(Map<String, String> paramMap);	
	
	//매입 팝업 insert(리스트에서 사용)
	int insertPurchaseBillDetailOrdrg(Map<String, String> paramMap);
	
	//매입 팝업수정	
	int updatePurchaseBillDetail(Map<String, String> paramMap);
	
	//리스트에서 등록시 사용
	int updatePurchaseBillDetailOrdrg(Map<String, String> paramMap);	
	
	int updatePurchaseMaster(Map<String, String> paramMap);	
	
	int deletePurchaseDetail(Map<String, String> param);
	
	int deletePurchaseMaster(Map<String, String> param);	
	
	int updateBillYn(Map<String, String> param);	

	List<Map<String, String>> selectOrdrgMatList(Map<String, String> paramMap);	
	
	int selectPurchaseListCountNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseListNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderDetailListNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPchsDetailListNew(Map<String, String> paramMap);
	
	int insertPurchaseBillDetailNew(Map<String, String> paramMap);	
	
	int insertPurchaseMaster(Map<String, String> param);
	
	String selectPurchaseDetailCount(Map<String, String> paramMap);
	
	String selectMaxPchsNoNew(Map<String, String> paramMap);

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);
}




