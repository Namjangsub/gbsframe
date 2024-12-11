package com.dksys.biz.user.im.im01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface IM01Svc {

    int selectImprovementListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectImprovementList(Map<String, String> paramMap);

    Map<String, String> selectImprovement(Map<String, String> paramMap);

    int insertImprovement(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int updateImprovement(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int deleteImprovement(Map<String, String> paramMap) throws Exception;

    List<Map<String, String>> selectImprovementShareUserlst(Map<String, String> paramMap);

}