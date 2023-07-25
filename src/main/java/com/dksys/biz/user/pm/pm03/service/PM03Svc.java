package com.dksys.biz.user.pm.pm03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM03Svc {

  int selectDeliveryPageCount(Map<String, String> paramMap);

  List<Map<String, String>> selectDeliveryPageList(Map<String, String> paramMap);

  List<Map<String, String>> selectDeliveryList(Map<String, String> paramMap);

  List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap);

  Map<String, String> selectDeliveryMastInfo(Map<String, String> paramMap);

  int insertDeliveryMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateDeliveryMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteDeliveryMast(Map<String, String> paramMap) throws Exception;

}