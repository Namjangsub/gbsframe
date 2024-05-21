package com.dksys.biz.user.sm.sm02.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM02Mapper {
	

	int selectOrderListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectOrderList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

	List<Map<String, String>> selectOrderDetailList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectOrderDetailView(Map<String, String> paramMap);	
	
	int selectMaxTrgtKey(Map<String, String> paramMap);	
	
	String selectMaxOrdrgNo(Map<String, String> paramMap);		
	
	//발주 등록, 수정, 삭제
	int insertOrderMaster(Map<String, String> paramMap);
	
	int insertOrderDetail(Map<String, String> paramMap);	
	
	int updateOrderMaster(Map<String, String> paramMap);
	
	int updateOrderDetail(Map<String, String> paramMap);
	
	int deleteOrderMaster(Map<String, String> paramMap);
	
	int deleteOrderDetail(Map<String, String> param);
	
	int deleteOrderDetailAll(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectCurrToday(Map<String, String> paramMap);	
	
	String selectCurrMatrUpr(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectInPurchaseChk(Map<String, String> paramMap);
	
	int updateMailConfirm(Map<String, String> paramMap);	

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int OrderMasterReport(Map<String, String> paramMap);

	int arriveWareHousingConfirm(Map<String, String> paramMap);
	
	int arriveWareHousingStoreConfirm(Map<String, String> paramMap);
	
	int arriveWareHousingUnconfirm(Map<String, String> paramMap);

	int detailArriveWareHousingConfirm(Map<String, String> dtl);

	int detailArriveWareHousingUnconfirm(Map<String, String> dtl);
	
	int detailArriveWareHousingStoreConfirm(Map<String, String> dtl);

	int allArriveWareHousingStoreCheck(Map<String, String> dtl);
}




