package com.dksys.biz.user.wb.wb03.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB03Svc {

  List<Map<String, String>> selectWbsPlanTreeIssueList(Map<String, String> paramMap);
  List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap);
}