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

  List<Map<String, String>> selectFileList(Map<String, String> paramMap);

  Map<String, String> selectPrjctInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertPrjct(Map<String, String> paramMap);

  int insertPrjctDtl(Map<String, String> paramMap);

  int updatePrjct(Map<String, String> paramMap);

  int updatePrjctDtl(Map<String, String> param);

  int deletePrjct(Map<String, String> param);

  int deletePrjctDtlAll(Map<String, String> param);

  int deletePrjctDtl(Map<String, String> paramMap);

//이하 PRDT 추가

  int selectPrdtCount(Map<String, String> param);

  int selectOneMasterCount(Map<String, String> param);

  List<Map<String, String>> selectPrdtClntList(Map<String, String> param);

  int insertOneMasterClnt(Map<String, String> param);

  int selectPrdtClntCount(Map<String, String> param);

  int selectOneMasterClntCount(Map<String, String> param);

  Map<String, String> selectPrdtInfo(Map<String, String> param);

  Map<String, String> seletOneMasterClnt(Map<String, String> param);

  int selectDetail01Count(Map<String, String> param);

  Map<String, String> seletOneDetail01(Map<String, String> param);

  int updateOneDetail01(Map<String, String> param);

  Map<String, String> seletOneMaster(Map<String, String> param);

  List<Map<String, String>> selectDetail01List(Map<String, String> param);

  int insertOneMaster(Map<String, String> param);

  int insertOneDetail01(Map<String, String> param);

}