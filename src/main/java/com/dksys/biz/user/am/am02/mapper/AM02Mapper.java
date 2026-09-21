package com.dksys.biz.user.am.am02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AM02Mapper {

    int selectFormCount(Map<String, Object> paramMap);

    List<Map<String, Object>> selectFormList(Map<String, Object> paramMap);

    Map<String, Object> selectFormDetail(Map<String, Object> paramMap);

    int insertForm(Map<String, Object> paramMap);

    int insertFormVersion(Map<String, Object> paramMap);

    int updateForm(Map<String, Object> paramMap);
}
