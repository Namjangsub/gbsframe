package com.dksys.biz.user.sm.sm01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM01Mapper {
  int selectBomSalesCount(Map<String, String> paramMap);

  List<Map<String, String>> selectBomSalesTreeList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectBomMatrTreeList(Map<String, String> paramMap);
  
  Map<String, String> selectBomMatrInfo(Map<String, String> paramMap);
  
  int selectPrjctSeqNext(Map<String, String> paramMap);

  // LOWER_CD생성
	int select_bm14_Key(Map<String, String> paramMap);
  
  int insertBom(Map<String, String> paramMap);

  int updateBom(Map<String, String> paramMap);

  int deleteBomAll(Map<String, String> paramMap);

  int deleteBom(Map<String, String> paramMap);
  
  int deleteBomAllMatrAll(Map<String, String> paramMap);
  
  int deleteBomMatrAll(Map<String, String> paramMap);

  int deleteBomMatr(Map<String, String> paramMap);
  
  int updateBomMatr(Map<String, String> paramMap);
  
  int insertBomMatr(Map<String, String> paramMap);
  
  List<Map<String, String>> bomTreeList(Map<String, String> paramMap);

  void callCopyBom(Map<String, String> paramMap);
  
  List<Map<String, String>> nextPrcsnNmList(Map<String, String> paramMap);
  
  List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

  // int syncBom(Map<String, String> paramMap);

  void syncBom(Map<String, String> param);
  
  // Tree Node Copy
  int copyMatrBomTree(Map<String, String> paramMap);

  // Tree Node Move
  int moveMatrBom(Map<String, String> paramMap);

  List<Map<String, String>> selectMatrBomlevelList(Map<String, String> paramMap);
  
  int selectMatrBomSeqNext(Map<String, String> paramMap);
  
}