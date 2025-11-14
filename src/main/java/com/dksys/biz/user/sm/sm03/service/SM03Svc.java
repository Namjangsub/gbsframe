package com.dksys.biz.user.sm.sm03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM03Svc {

	// 발주 리스트 카운트	
	int selectWareHousingListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWareHousingList(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWareHousingDetailList(Map<String, String> paramMap);		
	
	List<Map<String, String>> selectWareHousingDetailInfo(Map<String, String> paramMap);
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	String selectMaxInNo(Map<String, String> paramMap);	

	//발주마스터 등록, 수정, 삭제
	int insertWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;	
		
	int updateWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
		
	int updateWareHousingDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int deleteWareHousingMaster(Map<String, String> param);			
	
	int deleteWareHousingDetail(Map<String, String> param);			
	
	int deleteWareHousingInno(Map<String, String> param) throws Exception;	

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int updateMailStoreConfirm(Map<String, String> param);


	// 입고 상세 리스트 카운트	
	int selectWareHousingDetaiNewlListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> selectWareHousingDetaiNewlList(Map<String, String> paramMap);

	Map<String, String> selectPurchaseconfirmed(Map<String, String> paramMap);

	List<Map<String, String>> selectDashBoardWareHousingList(Map<String, String> paramMap);

	int updateDudtIntendDt(Map<String, String> param);
	
	// 장납기 파트 현황	
	int select_sm03_ListCount(Map<String, String> paramMap);
		
	List<Map<String, String>> select_sm03_List(Map<String, String> paramMap);

	List<Map<String, String>> selectMultiPrdtGrpCodeList(Map<String, String> paramMap);

	List<Map<String, String>> select_sm03_List_Pop(Map<String, String> paramMap);
}