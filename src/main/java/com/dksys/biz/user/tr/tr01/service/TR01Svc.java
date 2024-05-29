package com.dksys.biz.user.tr.tr01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface TR01Svc {

  int selectTransCount(Map<String, String> paramMap);

  List<Map<String, String>> selectTransList(Map<String, String> paramMap);

  Map<String, String> selectTransInfo(Map<String, String> paramMap);

  int insertTrans(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateTrans(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteTrans(Map<String, String> paramMap) throws Exception;

}