package com.dksys.biz.admin.cm.cm16.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM16Mapper {

    int selectItoaIssueListCount(Map<String, String> paramMap);

    List<Map<String, Object>> selectItoaIssueList(Map<String, String> paramMap);

    Map<String, String> selectItoaIssueInfo(Map<String, String> paramMap);

    int selectConfirmCount(Map<String, String> paramMap);

    String selectItoaIssueSeqNext(Map<String, String> paramMap);

    int itoaInsertIssue(Map<String, String> paramMap);

    int itoaUpdateIssue(Map<String, String> paramMap);

    int itoaDeleteIssue(Map<String, String> paramMap);

	
}
