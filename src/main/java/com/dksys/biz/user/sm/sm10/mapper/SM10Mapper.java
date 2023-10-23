package com.dksys.biz.user.sm.sm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM10Mapper {

  int selectPchsCostCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPchsCostList(Map<String, String> paramMap);

  Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  int selectPchsCostSeqNext(Map<String, String> paramMap);
  
  int insertPchsCost(Map<String, String> paramMap);

  int updatePchsCost(Map<String, String> paramMap);

  int deletePchsCost(Map<String, String> paramMap);

  int selectTurnKeySalesCodeCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTurnKeySalesCodeList(Map<String, String> paramMap);

  void deleteTurnKeyDetail(Map<String, String> paramMap);

  void insertTurnKeyDetail(Map<String, String> detailMap);
  
}