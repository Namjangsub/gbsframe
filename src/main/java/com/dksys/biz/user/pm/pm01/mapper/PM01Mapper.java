package com.dksys.biz.user.pm.pm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM01Mapper {

  int selectDailyWorkCount(Map<String, String> paramMap);

  List<Map<String, String>> selectDailyWorkList(Map<String, String> paramMap);

  Map<String, String> selectDailyWorkInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  int selectDailyWorkSeqNext(Map<String, String> paramMap);
  
  int insertDailyWork(Map<String, String> paramMap);

  int updateDailyWork(Map<String, String> paramMap);

  int deleteDailyWork(Map<String, String> paramMap);
  
  List<Map<String, String>> selectMonthlyWorkList(Map<String, String> paramMap);

	int selectWorkPrtCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWorkPrtList(Map<String, String> paramMap);

	int selectWorkOrdrsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWorkOrdrsList(Map<String, String> paramMap);
  
}