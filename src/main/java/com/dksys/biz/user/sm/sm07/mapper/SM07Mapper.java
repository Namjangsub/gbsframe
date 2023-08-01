package com.dksys.biz.user.sm.sm07.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface SM07Mapper {

	int selectOrderListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectOrderList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectOrderDetailList(Map<String, String> paramMap);	
	
	int updateOrderDetail(Map<String, String> paramMap);
	/*
	 * 마스터 페이지 조회 int master_grid_selectCount(Map<String, String> paramMap);
	 * List<Map<String, String>> master_grid_selectList(Map<String, String>
	 * paramMap); 마스터 페이지 조회
	 * 
	 * //모달 그리드 조회 -수정화면 정보 Map<String, String> select_sm07_Info(Map<String, String>
	 * paramMap);
	 * 
	 * //모달창 안에 그리드 grid-modal List<Map<String, String>>
	 * grid_Modal_selectList(Map<String, String> paramMap);
	 */
}
