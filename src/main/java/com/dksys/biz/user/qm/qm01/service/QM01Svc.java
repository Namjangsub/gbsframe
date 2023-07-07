package com.dksys.biz.user.qm.qm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface QM01Svc {

  int selectPchsCostCount(Map<String, String> paramMap);

  List<Map<String, String>> selectPchsCostList(Map<String, String> paramMap);

  Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertPchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deletePchsCost(Map<String, String> paramMap) throws Exception;

}