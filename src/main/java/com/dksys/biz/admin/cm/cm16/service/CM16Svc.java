package com.dksys.biz.admin.cm.cm16.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CM16Svc {

	int selectItoaIssueListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectItoaIssueList(Map<String, String> paramMap);

    Map<String, String> selectItoaIssueInfo(Map<String, String> paramMap);

    int selectConfirmCount(Map<String, String> paramMap);

    List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap);
    
    int itoaInsertIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int itoaUpdateIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int itoaDeleteIssue(Map<String, String> paramMap) throws Exception;

}