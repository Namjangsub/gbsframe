package com.dksys.biz.user.sm.sm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM05Mapper {

	int selectPchsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPchsList(Map<String, String> paramMap);

	int selectPchsDetailCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

	Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);

	int selectConfirmCount(Map<String, String> paramMap);

	int selectPchsCostSeqNext(Map<String, String> paramMap);

	int insertPchsCost(Map<String, String> paramMap);

	int updatePchsCost(Map<String, String> paramMap);

	int deletePchsCost(Map<String, String> paramMap);

}