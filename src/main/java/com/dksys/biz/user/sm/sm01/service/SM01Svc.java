package com.dksys.biz.user.sm.sm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM01Svc {

  int selectBomSalesCount(Map<String, String> paramMap);

  List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);

  Map<String, String> selectBomMatrInfo(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrTreeList(Map<String, String> paramMap);

  int insertBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteBomAll(Map<String, String> paramMap) throws Exception;

  int insertBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteBomMatr(Map<String, String> paramMap) throws Exception;

  int insertCrudMatrAndBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int insertUploadBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;




  List<Map<String, String>> bomTreeList(Map<String, String> paramMap);

  void callCopyBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);


}