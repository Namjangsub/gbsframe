package com.dksys.biz.user.pm.pm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM01Mapper {

  int selectDailyWorkCount(Map<String, String> paramMap);

  List<Map<String, String>> selectDailyWorkList(Map<String, String> paramMap);

  List<Map<String, String>> selectItemList(Map<String, String> paramMap);

  List<Map<String, String>> selectPrdtList(Map<String, String> paramMap);
  
  Map<String, String> selectDailyWorkInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  List<Map<String, String>> selectFileList(Map<String, String> paramMap);

  int selectDailyWorkSeqNext(Map<String, String> paramMap);
  
  int insertDailyWork(Map<String, String> paramMap);

  int insertDailyWorkPrdt(Map<String, String> paramMap);

  int insertDailyWorkDtl(Map<String, String> paramMap);
  
  int updateDailyWork(Map<String, String> paramMap);

  int updateDailyWorkPrdt(Map<String, String> paramMap);
  
  int updateDailyWorkDtl(Map<String, String> paramMap);

  int deleteDailyWork(Map<String, String> paramMap);
  
  int deleteDailyWorkPrdtSeqAll(Map<String, String> paramMap);
  
  int deleteDailyWorkDtlSeqAll(Map<String, String> paramMap);

  int deleteDailyWorkPrdt(Map<String, String> paramMap);
  
  int deleteDailyWorkDtlPrdtAll(Map<String, String> paramMap);

  int deleteDailyWorkDtl(Map<String, String> paramMap);
  
}