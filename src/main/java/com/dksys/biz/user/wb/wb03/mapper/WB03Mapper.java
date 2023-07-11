package com.dksys.biz.user.wb.wb03.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB03Mapper {

 
  List<Map<String, String>> selectWbsPlanTreeIssueList(Map<String, String> paramMap);
  List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap);
  int selectWbsPlanIssueSeqNext(Map<String, String> paramMap);
  int insertWbsPlanIssue(Map<String, String> paramMap);
  int updateWbsPlanIssue(Map<String, String> paramMap);
  
}