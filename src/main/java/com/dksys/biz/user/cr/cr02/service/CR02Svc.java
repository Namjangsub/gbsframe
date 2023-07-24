package com.dksys.biz.user.cr.cr02.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface CR02Svc {

    int selectOrdrsCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsList(Map<String, String> param);
    
    List<Map<String, Object>> selectOrdrsListPop(Map<String, String> param);

    Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap);


    Map<String, Object> selectOrdrsWithEst(Map<String, String> params);

    String selectMaxOrdrsNo(Map<String, String> param);
    
    String selectAsMaxOrdrsNo(Map<String, String> param);
    
    String selectItemDivEtc(Map<String, String> param);

    void insertOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;

    void updateOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;


    int deleteOrdrs(Map<String, String> paramMap) throws Exception;

    int updateEstDeleteConfirm(Map<String, String> paramMap);
    
    int selectOrdrsPlanHisCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsPlanHis(Map<String, String> param);
    
    List<Map<String, Object>> selectWbsLeftSalesCodeTreeList(Map<String, String> param);
    
    List<Map<String, Object>> selectItemSalesCodeTreeList(Map<String, String> param);
}