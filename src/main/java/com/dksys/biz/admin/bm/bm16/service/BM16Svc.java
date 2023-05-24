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

  int insertPrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

  int updatePrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

  int deletePrjct(Map<String, String> param);

  void deletePrjctDtl(List<Map<String, String>> paramList);

//  이하 PRDT 추가

  int selectPrdtCount(Map<String, String> param);

  public Map<String, String> seletOneMaster(Map<String, String> param);
//

//
//  int selectOneMasterCount(Map<String, String> param);

  public int insertOneMaster(Map<String, String> param);

  public int selectOneMasterCount(Map<String, String> param);

  public int insertOneDetail01(Map<String, String> param);

  public int updateOneDetail01(Map<String, String> param);

  public int selectDetail01Count(Map<String, String> param);

  public List<Map<String, String>> selectDetail01List(Map<String, String> param);

  public Map<String, String> seletOneDetail01(Map<String, String> param);

  Map<String, String> selectPrdtInfo(Map<String, String> param);

  int selectPrdtClntCount(Map<String, String> param);

  Map<String, String> seletOneMasterClnt(Map<String, String> param);

  int insertOneMasterClnt(Map<String, String> param);

  List<Map<String, String>> selectPrdtClntList(Map<String, String> param);

  int selectOneMasterClntCount(Map<String, String> param);

}