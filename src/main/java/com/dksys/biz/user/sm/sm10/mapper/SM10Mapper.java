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

  int etcPchsOrderMasterReport(Map<String, String> dtl);

  int updateMailEtcPchsOrderConfirm(Map<String, String> param);

  //매입확정자료가 존재하는제 체크하기
  int deletePchsCostCheck(Map<String, String> paramMap);

    Map<String, String> selectPchsCostInfoByCostNo(Map<String, String> paramMap);
  
}