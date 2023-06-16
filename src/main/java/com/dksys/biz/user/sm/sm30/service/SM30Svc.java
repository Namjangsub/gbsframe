package com.dksys.biz.user.sm.sm30.service;

import java.util.List;
import java.util.Map;

public interface SM30Svc {
    public int selectBomCount(Map<String, String> param);
    public int selectBomDetailCount(Map<String, String> param);
    public List<Map<String, Object>> selectBomList(Map<String, String> param);
    public List<Map<String, Object>> selectBomDetailList(Map<String, String> param);
    Map<String, Object> insertBomDetailList(Map<String, String> param);
    Map<String, Object> updateBom(Map<String, String> param);
    int deleteBom(Map<String, String> paramMap);

    Map<String, Object> selectBomInfo(Map<String, String> param);

    List<Map<String, Object>> selectBomListMatr(Map<String, String> param);
    //Map<String, Object> updateBom(Map<String, String> paramMap);
}
