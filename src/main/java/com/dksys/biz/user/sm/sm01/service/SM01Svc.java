package com.dksys.biz.user.sm.sm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM01Svc {

  int selectBomSalesCount(Map<String, String> paramMap);

  List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap);
  
  int selectBomDetailCount(Map<String, String> paramMap);
  
  List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);

  Map<String, String> selectPrjctInfo(Map<String, String> paramMap);

  int insertBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteBom(Map<String, String> paramMap) throws Exception;
  
  Map<String, String> selectPrjctIssueInfo(Map<String, String> paramMap);
  
  int insertBomIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  int updateBomIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

}