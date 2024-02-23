package com.dksys.biz.user.sm.sm14.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM14Svc {
	
	int selectPurchaseListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectClntPurchaseList(Map<String, String> paramMap);

	List<Map<String, String>> selectClntPurchaseDetailList(Map<String, String> paramMap);
		
	List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap);			
		
	String selectMaxPchsNo(Map<String, String> paramMap);	

	//매입 디테일 마스터 등록, 수정, 삭제
	int insertPurchaseBillDetail(Map<String, String> paramMap) throws Exception;	
	
	int insertinsertPurchaseSel(Map<String, String> paramMap) throws Exception;	
		
	int updatePurchaseBillDetail(Map<String, String> paramMap) throws Exception;			
	
	int deletePurchaseDetail(Map<String, String> param);	
	
	int updateBillYn(Map<String, String> paramMap) throws Exception;

	int updateBillSeqYn(Map<String, String> paramMap) throws Exception;
	
	List<Map<String, String>> selectOrdrgMatList(Map<String, String> paramMap);	
	
	int selectPurchaseListCountNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPurchaseListNew(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderDetailListNew(Map<String, String> paramMap);		
	
	List<Map<String, String>> selectPchsDetailListNew(Map<String, String> paramMap);
	
	int insertPurchaseBillDetailNew(Map<String, String> paramMap) throws Exception;		

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);
}