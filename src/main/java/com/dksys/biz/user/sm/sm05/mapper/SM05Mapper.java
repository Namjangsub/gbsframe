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

	int selectIoOutWhCount(Map<String, String> paramMap);

	List<Map<String, String>> selectIoOutWhList(Map<String, String> paramMap);

//	int selectPchsDetailCount(Map<String, String> paramMap);  

//	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

//	int selectConfirmCount(Map<String, String> paramMap);
//
//	int selectPchsCostSeqNext(Map<String, String> paramMap);
//
//	int insertPchsCost(Map<String, String> paramMap);
//
//	int updatePchsCost(Map<String, String> paramMap);
//
//	int deleteIoCost(Map<String, String> paramMap);

}