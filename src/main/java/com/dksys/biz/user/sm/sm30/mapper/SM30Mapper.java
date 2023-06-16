package com.dksys.biz.user.sm.sm30.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SM30Mapper {
    int selectBomCount(Map<String, String> param);
    int selectBomDetailCount(Map<String, String> param);
    List<Map<String, Object>> selectBomList(Map<String, String> param);
    List<Map<String, Object>> selectBomDetailList(Map<String, String> param);
    Map<String, Object> insertBomDetailList(Map<String, String> param);
    int updateBom(Map<String, String> param);
    int deleteBom(Map<String, String> paramMap);
    int deleteAllBomDetails(Map<String, String> paramMap);

    Map<String, Object> selectBomInfo(Map<String, String> param);

    List<Map<String, Object>> selectBomListMatr(Map<String, String> param);
}
