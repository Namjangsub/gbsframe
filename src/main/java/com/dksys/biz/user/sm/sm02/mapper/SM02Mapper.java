package com.dksys.biz.user.sm.sm02.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SM02Mapper {
	

	int selectOrderListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectOrderList(Map<String, String> paramMap);	
	
	List<Map<String, String>> selectOrderExcelList(Map<String, String> paramMap);	
	
	//결재선 등록, 수정, 삭제
	int insertOrderMaster(Map<String, String> paramMap);
	
	int updateOrderMaster(Map<String, String> paramMap);
	
	int deleteOrderMaster(Map<String, String> paramMap);
}




