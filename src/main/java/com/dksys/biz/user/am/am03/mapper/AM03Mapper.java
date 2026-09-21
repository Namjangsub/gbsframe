package com.dksys.biz.user.am.am03.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AM03Mapper {

    List<Map<String, Object>> selectLinePresetList(Map<String, Object> paramMap);

    List<Map<String, Object>> selectLinePresetDetail(Map<String, Object> paramMap);

    int insertLinePreset(Map<String, Object> paramMap);

    int insertLinePresetDetail(Map<String, Object> paramMap);

    int deleteLinePreset(Map<String, Object> paramMap);

    int deleteLinePresetDetail(Map<String, Object> paramMap);

    List<Map<String, Object>> selectDelegateList(Map<String, Object> paramMap);

    int insertDelegate(Map<String, Object> paramMap);

    int updateDelegate(Map<String, Object> paramMap);
}
