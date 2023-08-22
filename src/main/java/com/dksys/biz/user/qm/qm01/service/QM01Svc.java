package com.dksys.biz.user.qm.qm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface QM01Svc {

  int selectQualityReqCount(Map<String, String> paramMap);
  
  int selectShareUserCount(Map<String, String> paramMap);  
  
  int selectSignUserCount(Map<String, String> paramMap); 
  
  int selectShareUserResCount(Map<String, String> paramMap); 
  
  int selectSignResCount(Map<String, String> paramMap); 

  List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectQualityReqExList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareResUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);
  
  //List<Map<String, String>> selectSignUserInfo(Map<String, String> paramMap);  

  Map<String, String> selectQtyReqInfo(Map<String, String> paramMap);
  
  Map<String, String> selectQtyReqRespInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  int insertQualityResp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  int updateQualityResp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteQualityReq(Map<String, String> paramMap) throws Exception;
  
  List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);
  
  int selectCodeMaxCount(Map<String, String> paramMap);
  
  public List<Map<String, String>> selectMainCodeList(Map<String, String> param);
  
  List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);
  
}