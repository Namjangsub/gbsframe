package com.dksys.biz.user.sm.sm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM05Svc {

	int select_sm05_ioCount(Map<String, String> paramMap);
	List<Map<String, String>> select_sm05_ioList(Map<String, String> paramMap);

	int select_sm05_ioDetailCount(Map<String, String> paramMap);
	List<Map<String, String>> select_sm05_ioDetailList(Map<String, String> paramMap);

//	int selectIoOutWhCount(Map<String, String> paramMap);
//	List<Map<String, String>> selectIoOutWhList(Map<String, String> paramMap);

	Map<String, String> select_sm05_ioInfo(Map<String, String> paramMap);
	
	public List<Map<String, Object>> selectOutWhNm(Map<String, String> param);
	
	// 폐기창고재고정보 - 모달창 
	int select_sm05_ioModalCount(Map<String, String> paramMap);
	List<Map<String, String>> select_sm05_ioModalList(Map<String, String> paramMap);
	
	//폐기창고 재고정보 insert
	int insert_sm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	//폐기창고 insert
	int insert_sm05_IoInfo(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	//수정 
	int update_sm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int update_sm05_IoInfo(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	//삭제
	int delete_sm05M_io(Map<String, String> paramMap) throws Exception;
	int delete_sm05D_io(Map<String, String> paramMap);
	//Map<String, String> selectIoInfo(Map<String, String> paramMap);

//	int selectPchsDetailCount(Map<String, String> paramMap);

//	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

//	int selectConfirmCount(Map<String, String> paramMap);
//
	
//
//	int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
//
	

}