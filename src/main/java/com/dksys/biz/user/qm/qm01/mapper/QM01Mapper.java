package com.dksys.biz.user.qm.qm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM01Mapper {

  int selectPchsCostCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPchsCostList(Map<String, String> paramMap);

  Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  int selectPchsCostSeqNext(Map<String, String> paramMap);
  
  int insertPchsCost(Map<String, String> paramMap);

  int updatePchsCost(Map<String, String> paramMap);

  int deletePchsCost(Map<String, String> paramMap);
  
}