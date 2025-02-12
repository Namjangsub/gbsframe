package com.dksys.biz.user.cr.cr50.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR50Svc {

    List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap);

    List<Map<String, String>> selectPFUAreaRetriveList(Map<String, String> paramMap);

    List<Map<String, String>> selectPFUChangedList(Map<String, String> paramMap);

    Map<String, String> selectSalesCdInfo(Map<String, String> paramMap);

    Map<String, String> selectPfuInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuInfoSalesCdList(Map<String, String> paramMap);

//  Map<String, String> selectPfuClobInfo(Map<String, String> paramMap);

    int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int updatePfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int deletePfuNo(Map<String, String> paramMap) throws Exception;

    int selectPfuListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuList(Map<String, String> paramMap);

    List<Map<String, String>> selectStdPfuClobInfo(Map<String, String> paramMap);

    // PFU복사부분 시작
    int copy_cr50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int selectPfuCopyTargetListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuCopyTargetList(Map<String, String> paramMap);
    // PFU복사부분 끝

    int selectPfuIsThereListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectTagetSalesCodeList(Map<String, String> paramMap);

    List<Map<String, String>> selectPfuReferenceTargetList(Map<String, String> paramMap);

    // 그리드 카운트
    int selectPfuReferenceTargetListCount(Map<String, String> paramMap);

    List<Map<String, String>> selectIssueReferenceList(Map<String, String> paramMap);

    List<Map<String, String>> selectImprovementReferenceList(Map<String, String> paramMap);

    int updatePfuVersionUpProcess(Map<String, String> paramMap) throws Exception;

}