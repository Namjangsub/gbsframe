package com.dksys.biz.user.qm.qm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM01Mapper {

  int selectQualityReqCount(Map<String, String> paramMap);
  
  int selectShareUserCount(Map<String, String> paramMap);
  
  int selectSignUserCount(Map<String, String> paramMap);
  
  int selectShareUserResCount(Map<String, String> paramMap);

  List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectQualityReqExList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareResUserlst(Map<String, String> paramMap);
  
  List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap);
  
 // List<Map<String, String>> selectSignUserInfo(Map<String, String> paramMap);

  Map<String, String> selectQtyReqInfo(Map<String, String> paramMap);
  
  Map<String, String> selectQtyReqRespInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  int selectQualityReqSeqNext(Map<String, String> paramMap);
  
  String selectQualityReqCalNext(Map<String, String> paramMap);
  
  int insertQualityReq(Map<String, String> paramMap);
  
  int insertQualityResp(Map<String, String> paramMap);
  
  int updateReqRsltChg(Map<String, String> paramMap);

  int updateQualityReq(Map<String, String> paramMap);
  
  int updateQualityResp(Map<String, String> paramMap);

  int deleteQualityReq(Map<String, String> paramMap);
  
  int deleteQualityResp(Map<String, String> paramMap);
  
}