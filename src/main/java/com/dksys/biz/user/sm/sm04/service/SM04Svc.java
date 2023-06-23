package com.dksys.biz.user.sm.sm04.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface SM04Svc {

    String selectMaxEstNo(Map<String, String> paramMap);

    String selectMaxEstDeg(Map<String, String> paramMap);

    public int selectIoCount(Map<String, String> param);

    public int selectIoDetailCount(Map<String, String> param);

    public List<Map<String, Object>> selectIoList(Map<String, String> param);
    
    public List<Map<String, Object>> selectIoDetail(Map<String, String> param);

    public List<Map<String, Object>> selectStInfo(Map<String, String> param);
    
    public List<Map<String, Object>> selectWhCd(Map<String, String> param);
    

    //DATA INSERT
    int insert_sm04(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

    int insert_sm04_Info(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    Map<String, Object> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    int updateEstConfirm(Map<String, String> paramMap);

}