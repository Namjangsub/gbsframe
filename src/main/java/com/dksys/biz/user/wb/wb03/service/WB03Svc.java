package com.dksys.biz.user.wb.wb03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB03Svc {

  int selectWbsIssuePagingCount(Map<String, String> paramMap);

  List<Map<String, String>> selectWbsIssuePageList(Map<String, String> paramMap);
  
  List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap);

  List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap);

  List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap);

  Map<String, String> selectBomMatrInfo(Map<String, String> paramMap);

  int insertBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteBomAll(Map<String, String> paramMap) throws Exception;
  
  int insertBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  
  int deleteBomMatr(Map<String, String> paramMap) throws Exception;
  
  int insertCopyBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

}