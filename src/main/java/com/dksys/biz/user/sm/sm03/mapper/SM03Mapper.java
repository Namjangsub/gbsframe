package com.dksys.biz.user.sm.sm03.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM03Mapper {
	

	int selectWareHousingListCount(Map<String, String> paramMap);
	
	int selectOrderListCount(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectWareHousingList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

	List<Map<String, String>> selectWareHousingDetailList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectWareHousingDetailInfo(Map<String, String> paramMap);
	
	int selectMaxTrgtKey(Map<String, String> paramMap);	
	
	String selectMaxInNo(Map<String, String> paramMap);		
	
	//입고 등록, 수정, 삭제
	int insertWareHousingMaster(Map<String, String> paramMap);
	
	int insertWareHousingDetail(Map<String, String> paramMap);	
	
	int updateWareHousingMaster(Map<String, String> paramMap);
	
	int updateWareHousingDetail(Map<String, String> paramMap);
	
	int deleteWareHousingMaster(Map<String, String> paramMap);
	
	int deleteWareHousingDetail(Map<String, String> param);	
	
	int deleteWareHousingDirectMaster(Map<String, String> paramMap);
	
	int deleteWareHousingDirectDetail(Map<String, String> param);	

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);
}




