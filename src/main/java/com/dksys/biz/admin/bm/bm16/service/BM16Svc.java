package com.dksys.biz.admin.bm.bm16.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM16Svc {

  int selectPrjctCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPrjctList(Map<String, String> paramMap);

  List<Map<String, String>> selectItemList(Map<String, String> paramMap);

  List<Map<String, String>> selectPrdtList(Map<String, String> paramMap);

  Map<String, String> selectPrjctInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  List<Map<String, String>> selectFileList(Map<String, String> paramMap);

  int insertPrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updatePrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deletePrjct(Map<String, String> paramMap) throws Exception;
  
  Map<String, String> selectPrjctIssueInfo(Map<String, String> paramMap);
  
  int insertPrjctIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  int updatePrjctIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  List<Map<String, String>> selectPrjctOrderBillChart(Map<String, String> paramMap);

  int selectPrjctPlanCount(Map<String, String> paramMap);

  List<Map<String, Object>> selectPrjctPlanList(Map<String, String> paramMap);

}