package com.dksys.biz.admin.cm.cm25.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CM25Svc {

    int selectExpendListCount(Map<String, String> paramMap);

    List<Map<String, Object>> selectExpendList(Map<String, String> paramMap);

    Map<String, String> selectExpendInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap);

    Map<String, String> selectExpendPdfImageInfo(Map<String, String> paramMap);

    int insertExpend(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int updateExpend(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int deleteExpend(Map<String, String> paramMap) throws Exception;

    int updateExpendSts(Map<String, String> paramMap);
}
