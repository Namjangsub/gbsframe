package com.dksys.biz.admin.cm.cm16.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM16Mapper {
    
    int selectItoaIssueCount(Map<String, String> paramMap);

    List<Map<String, String>> selectItoaIssueList(Map<String, String> paramMap);

    int selectConfirmCount(Map<String, String> paramMap);

    String selectItoaIssueSeqNext(Map<String, String> paramMap);

    int insertItoaIssue(Map<String, String> paramMap);

    int updateItoaIssue(Map<String, String> paramMap);

    int deleteItoaIssue(Map<String, String> paramMap);
}
