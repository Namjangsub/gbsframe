package com.dksys.biz.user.cr.cr02.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR02Svc {

    int selectOrdrsCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsList(Map<String, String> param);

    int selectOrdrsListPopCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsListPop(Map<String, String> param);

    Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap);


    Map<String, Object> selectOrdrsWithEst(Map<String, String> params);

    String selectMaxOrdrsNo(Map<String, String> param);

    String selectAsMaxOrdrsNo(Map<String, String> param);

    String selectItemDivEtc(Map<String, String> param);

    Map<String, String> insertOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;

    void updateOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;

    void updateOrdrs_OLD(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;

    int deleteOrdrs(Map<String, String> paramMap) throws Exception;

    int updateEstDeleteConfirm(Map<String, String> paramMap);

    int selectOrdrsPlanHisCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsPlanHis(Map<String, String> param);

    List<Map<String, Object>> selectWbsLeftSalesCodeTreeList(Map<String, String> param);

    List<Map<String, Object>> selectItemSalesCodeTreeList(Map<String, String> param);

    List<Map<String, Object>> selectItemSalesCodeTreeList2(Map<String, String> param);

	void callCopyOrdrs(Map<String, String> paramMap);

	int selectOrdrsKey(Map<String, String> paramMap) throws Exception;

	int selectNoSalesCdOrdrsListPopCount(Map<String, String> param);

	List<Map<String, Object>> selectNoSalesCdOrdrsListPop(Map<String, String> param);

	int selectJunmooApproval(Map<String, String> param);

	int deleteOrdrsDetail(Map<String, String> param);

	List<Map<String, Object>> selectOrdrsDetails(Map<String, String> param);

    void updateOrdrsPmntPlanProcess(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception;

	Map<String, String> salesCdSearchOrderInfo(Map<String, String> paramMap);

    List<Map<String, String>> selectOrderChangeTitle(Map<String, String> paramMap);

	int clmnPlanRmkUpdate(Map<String, String> paramMap) throws Exception;

    Map<String, String> ordrsDeleteChk(Map<String, String> paramMap);

    Map<String, String> deleteDetailChk(Map<String, String> paramMap);

    Map<String, String> ordrsDivChangeChk(Map<String, String> paramMap);
}