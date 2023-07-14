package com.dksys.biz.user.wb.wb03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB03Svc {

  List<Map<String, String>> selectWbsPlanTreeIssueList(Map<String, String> paramMap);
  List<Map<String, String>> selectWbsPlanTreeIssueExcelList(Map<String, String> paramMap);
  List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap);
  int insertWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  int updateWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
  int deleteWbsPlanIssue(Map<String, String> paramMap);
  List<Map<String, String>> deleteIssueSharngListChk(Map<String, String> paramMap);
  int deleteIssueSharngList(Map<String, String> paramMap);
  List<Map<String, String>> selectIssueSharngList(Map<String, String> paramMap);
  
}