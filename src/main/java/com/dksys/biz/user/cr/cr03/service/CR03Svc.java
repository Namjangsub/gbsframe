package com.dksys.biz.user.cr.cr03.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR03Svc {
    String selectMaxEstNo(Map<String, String> paramMap);
    String selectMaxEstDeg(Map<String, String> paramMap);





    public int selectEstCount(Map<String, String> param);
    public int selectEstDetailCount(Map<String, String> param);
    public List<Map<String, Object>> selectEstList(Map<String, String> param);

    public List<Map<String, Object>> selectEstListNotOrdrs(Map<String, String> param);



    Map<String, Object> selectEstInfo(Map<String, String> paramMap);
    

    
    Map<String, Object> insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    Map<String, Object> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

    
    Map<String, Object> updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
    int updateEstConfirm(Map<String, String> paramMap);
    int deleteEst(Map<String, String> paramMap);
}
