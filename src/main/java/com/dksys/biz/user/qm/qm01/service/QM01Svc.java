package com.dksys.biz.user.qm.qm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface QM01Svc {

  int selectQualityReqCount(Map<String, String> paramMap);
  
  int selectShareUserCount(Map<String, String> paramMap);  

  List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectQualityReqExList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);

  Map<String, String> selectQtyReqInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteQualityReq(Map<String, String> paramMap) throws Exception;

}