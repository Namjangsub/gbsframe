package com.dksys.biz.user.bm.bm10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM10Svc {
  // 그리드 카운트
  int grid1_selectCount(Map<String, String> paramMap);

  // 그리드 리스트
  List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

  // 팝업 그리드 카운트
  int ProdModal_selectCount(Map<String, String> paramMap);

  // 팝업 그리드 리스트
  List<Map<String, String>> ProdModal_selectList(Map<String, String> paramMap);
  
  // 수정화면 정보
  Map<String, String> select_bm10_Info(Map<String, String> paramMap);

  //DATA INSERT
  int insert_bm10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  //DATA UPDATE
  int update_bm10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  //DATA DELETE
  int delete_bm10(Map<String, String> paramMap) throws Exception;

}