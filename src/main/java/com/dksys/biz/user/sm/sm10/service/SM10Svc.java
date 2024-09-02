package com.dksys.biz.user.sm.sm10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM10Svc {

  int selectPchsCostCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPchsCostList(Map<String, String> paramMap);

  Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertPchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deletePchsCost(Map<String, String> paramMap) throws Exception;

  int selectTurnKeySalesCodeCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTurnKeySalesCodeList(Map<String, String> paramMap);

  int etcPchsOrderMasterReport(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

  int updateMailEtcPchsOrderConfirm(Map<String, String> param);

  //매입확정자료가 존재하는제 체크하기
  int deletePchsCostCheck(Map<String, String> paramMap);

}