package com.dksys.biz.user.sm.sm11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM11Mapper {

  int selectContractCount(Map<String, String> paramMap);

  List<Map<String, String>> selectContractList(Map<String, String> paramMap);

  Map<String, String> selectContractInfo(Map<String, String> paramMap);
  

  int selectContractSeqNext(Map<String, String> paramMap);
  
  int insertContract(Map<String, String> paramMap);

  int updateContract(Map<String, String> paramMap);

  int deleteContract(Map<String, String> paramMap);

  List<Map<String, String>> selectTurnKeySalesCodeList(Map<String, String> paramMap);

  int insertTurnKeyDetail(Map<String, String> detailMap);

  int deleteTurnKeyDetail(Map<String, String> paramMap);



  int insertTurnKeyClmnPlan(Map<String, String> detailMap);

  int updateTurnKeyClmnPlan(Map<String, String> paramMap);

  int deleteTurnKeyClmnPlan(Map<String, String> paramMap);
  
  int deleteTurnKeyClmnPlanEx(Map<String, String> paramMap);
  
  
}