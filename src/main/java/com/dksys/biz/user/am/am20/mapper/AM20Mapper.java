package com.dksys.biz.user.am.am20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AM20Mapper {

    int selectInboxCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectInboxList(Map<String, Object> paramMap);

    int selectProgressCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectProgressList(Map<String, Object> paramMap);

    int selectDraftedCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectDraftedList(Map<String, Object> paramMap);

    int selectCompletedCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectCompletedList(Map<String, Object> paramMap);

    int selectRejectedCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectRejectedList(Map<String, Object> paramMap);

    int selectReferencedCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectReferencedList(Map<String, Object> paramMap);

    int selectTempCount(Map<String, Object> paramMap);
    List<Map<String, Object>> selectTempList(Map<String, Object> paramMap);
}
