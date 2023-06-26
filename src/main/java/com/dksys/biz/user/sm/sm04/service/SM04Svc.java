package com.dksys.biz.user.sm.sm04.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface SM04Svc {


    public int selectIoCount(Map<String, String> param);

    public int selectIoDetailCount(Map<String, String> param);

    public List<Map<String, Object>> selectIoList(Map<String, String> param);
    
    public List<Map<String, Object>> selectIoDetail(Map<String, String> param);

    public List<Map<String, Object>> selectStInfo(Map<String, String> param);
    
    public List<Map<String, Object>> selectWhCd(Map<String, String> param);

    // 수정화면 정보
    Map<String, String> select_sm04_info(Map<String, String> paramMap);

	//기본정보 & 불출정보 등록
    int insert_sm04(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	//출고창고 재고정보 등록
    int insert_sm04_Info(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);


}