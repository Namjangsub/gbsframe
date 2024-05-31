package com.dksys.biz.user.tr.tr01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TR01Mapper {

  int selectTransCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTransList(Map<String, String> paramMap);

  Map<String, String> selectTransInfo(Map<String, String> paramMap);
  

  String selectTransSeqNext(Map<String, String> paramMap);
  
  int insertTrans(Map<String, String> paramMap);

  int updateTrans(Map<String, String> paramMap);

  int deleteTrans(Map<String, String> paramMap);

  List<Map<String, String>> selectTransTargetSalesCodeList(Map<String, String> paramMap);

  int deleteTransTargetDetail(Map<String, String> paramMap);

  int insertTransTargetDetail(Map<String, String> detailMap);

  
}