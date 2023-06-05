package com.dksys.biz.admin.bm.bm16.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BM16Mapper {

  int selectPrjctCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPrjctList(Map<String, String> paramMap);

  List<Map<String, String>> selectItemList(Map<String, String> paramMap);

  List<Map<String, String>> selectPrdtList(Map<String, String> paramMap);
  
  Map<String, String> selectPrjctInfo(Map<String, String> paramMap);
  
  int selectConfirmCount(Map<String, String> paramMap);

  List<Map<String, String>> selectFileList(Map<String, String> paramMap);

  int insertPrjct(Map<String, String> paramMap);

  int insertPrjctPrdt(Map<String, String> paramMap);

  int insertPrjctDtl(Map<String, String> paramMap);
  
  int updatePrjct(Map<String, String> paramMap);

  int updatePrjctPrdt(Map<String, String> param);
  
  int updatePrjctDtl(Map<String, String> param);

  int deletePrjct(Map<String, String> param);
  
  int deletePrjctPrdtSeqAll(Map<String, String> param);
  
  int deletePrjctDtlSeqAll(Map<String, String> param);

  int deletePrjctPrdt(Map<String, String> paramMap);
  
  int deletePrjctDtlPrdtAll(Map<String, String> paramMap);

  int deletePrjctDtl(Map<String, String> paramMap);
  
}