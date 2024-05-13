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



  String selectContractBilCostNoCreation(Map<String, String> paramMap);

  int insertTurnKeyClmnPlan(Map<String, String> detailMap);

  int updateTurnKeyClmnPlan(Map<String, String> paramMap);

  int deleteTurnKeyClmnPlan(Map<String, String> paramMap);
  
  int deleteTurnKeyClmnPlanEx(Map<String, String> paramMap);
  
  

  int selectContractLPayCount(Map<String, String> paramMap);
  
  List<Map<String, String>> selectContractLPayList(Map<String, String> paramMap);
  
  //외주관리에서 기타매출 생성 및 삭제처리
  int createContractBillMaster(Map<String, String> paramMap);
  int createContractBillDetail(Map<String, String> paramMap);
  int deleteContractBillMaster(Map<String, String> paramMap);
  int deleteContractBillDetail(Map<String, String> paramMap);
  int deleteContractBillReport(Map<String, String> paramMap);
  

  int selectClntCrtrListCount(Map<String, String> paramMap);
  
  List<Map<String, String>> selectClntCrtrList(Map<String, String> paramMap);
  
}