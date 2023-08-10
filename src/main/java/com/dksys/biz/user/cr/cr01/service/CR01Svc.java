package com.dksys.biz.user.cr.cr01.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR01Svc {

    String selectMaxEstNo(Map<String, String> paramMap);

    String selectMaxEstDeg(Map<String, String> paramMap);

    int selectEstCount(Map<String, String> param);

    int selectEstCountModal(Map<String, String> param);

    int selectEstDetailCount(Map<String, String> param);

    List<Map<String, Object>> selectEstList(Map<String, String> param);

    List<Map<String, Object>> selectEstListModal(Map<String, String> param);

    List<Map<String, Object>> selectEstListNotOrdrs(Map<String, String> param);

    Map<String, Object> selectEstInfo(Map<String, String> paramMap);

    Map<String, Object> insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    Map<String, Object> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    Map<String, Object> updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int updateEstConfirm(Map<String, String> paramMap);

    int deleteEst(Map<String, String> paramMap) throws Exception;
}