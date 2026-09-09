package com.dksys.biz.admin.cm.cm25.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM25Mapper {

    int selectExpendListCount(Map<String, String> paramMap);

    List<Map<String, Object>> selectExpendList(Map<String, String> paramMap);

    Map<String, String> selectExpendInfo(Map<String, String> paramMap);

    String selectExpendNoNext(Map<String, String> paramMap);

    int insertExpend(Map<String, String> paramMap);

    int updateExpend(Map<String, String> paramMap);

    int deleteExpend(Map<String, String> paramMap);

    int selectApprovedCount(Map<String, String> paramMap);

    int deleteExpendApprovalList(Map<String, String> paramMap);

    int updateExpendSts(Map<String, String> paramMap);

    List<Map<String, String>> selectExpendPdfImageList(Map<String, String> paramMap);

    Map<String, String> selectExpendPdfImageInfo(Map<String, String> paramMap);

    int insertExpendPdfImage(Map<String, String> paramMap);

    int deleteExpendPdfImages(Map<String, String> paramMap);
}
