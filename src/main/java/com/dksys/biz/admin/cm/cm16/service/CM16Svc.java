package com.dksys.biz.admin.cm.cm16.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CM16Svc {

    int selectItoaIssueCount(Map<String, String> paramMap);

    List<Map<String, String>> selectItoaIssueList(Map<String, String> paramMap);

    int selectConfirmCount(Map<String, String> paramMap);
    
    int insertItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int updateItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int deleteItoaIssue(Map<String, String> paramMap) throws Exception;
}