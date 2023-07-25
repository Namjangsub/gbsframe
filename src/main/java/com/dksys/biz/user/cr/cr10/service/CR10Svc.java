package com.dksys.biz.user.cr.cr10.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR10Svc {

  int selectLgistReqPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqPageList(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistReqList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdViewList(Map<String, String> paramMap);

  Map<String, String> selectLgistMastInfo(Map<String, String> paramMap);

  List<Map<String, String>> selectLgistAppList(Map<String, String> paramMap);

  int insertLgistMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateLgistMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteLgistMast(Map<String, String> paramMap) throws Exception;

  int updateTodoAppConfirm(Map<String, String> paramMap) throws Exception;

}