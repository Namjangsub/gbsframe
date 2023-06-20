package com.dksys.biz.user.bm.bm10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM10Svc {

  // int selectPchsCostCount(Map<String, String> paramMap);
  int grid1_selectCount(Map<String, String> paramMap);

  // List<Map<String, String>> selectPchsCostList(Map<String, String> paramMap);
  List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

  Map<String, String> select_bm10_Info(Map<String, String> paramMap);

  // int selectConfirmCount(Map<String, String> paramMap);

  int insert_bm10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int update_bm10(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int delete_bm10(Map<String, String> paramMap) throws Exception;

}