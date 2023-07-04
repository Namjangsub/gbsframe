package com.dksys.biz.user.sm.sm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM05Mapper {

	int selectIoCount(Map<String, String> paramMap);
	List<Map<String, String>> selectIoList(Map<String, String> paramMap);

	int selectIoDetailCount(Map<String, String> paramMap);
	List<Map<String, String>> selectIoDetailList(Map<String, String> paramMap);

//	int selectIoOutWhCount(Map<String, String> paramMap);
//	List<Map<String, String>> selectIoOutWhList(Map<String, String> paramMap);
	
	// 수정화면 정보
	Map<String, String> select_sm05_IoInfo(Map<String, String> paramMap);	
	List<Map<String, Object>> selectOutWhNm(Map<String, String> paramMap);
	
	// 폐기창고재고정보 - 모달창 
	int selectIoModalCount(Map<String, String> paramMap);
	List<Map<String, String>> selectIoModalList(Map<String, String> paramMap);

	// fileTrgtKey 생성
    int select_sm05_SeqNext(Map<String, String> paramMap);
    
    //폐기창고 insert
	int insert_sm05(Map<String, String> paramMap);
	
	//페기창고 재고정보 insert
	int insert_sm05_IoInfo(Map<String, String> paramMap);
	
	//수정
	int update_sm05(Map<String, String> paramMap);
    
	//Map<String, String> selectIoInfo(Map<String, String> paramMap);

//	int selectPchsDetailCount(Map<String, String> paramMap);  

//	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

//	int selectConfirmCount(Map<String, String> paramMap);
//
//	int selectPchsCostSeqNext(Map<String, String> paramMap);
//

//
//	int updatePchsCost(Map<String, String> paramMap);
//
//	int deleteIoCost(Map<String, String> paramMap);

}