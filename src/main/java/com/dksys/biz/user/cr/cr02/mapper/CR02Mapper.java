package com.dksys.biz.user.cr.cr02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CR02Mapper {

    int selectOrdrsCount(Map<String, String> param);

    int selectOrdrsPlanHisCount(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsList(Map<String, String> param);
    
    List<Map<String, Object>> selectOrdrsListPop(Map<String, String> param);


    Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap);

    Map<String, Object> selectOrdrsWithEst(Map<String, String> paramMap);

    List<Map<String, Object>> selectOrdrsDetails(Map<String, String> paramMap);


    List<Map<String, Object>> selectPmntPlan(Map<String, String> paramMap);

    String selectMaxOrdrsNo(Map<String, String> param);
    
    String selectAsMaxOrdrsNo(Map<String, String> param);
    
    String selectItemDivEtc(Map<String, String> param);

    int insertOrdrs(Map<String, String> param);

    void updateOrdrs(Map<String, String> param);

    int insertOrdrsDetail(Map<String, String> param);

    void updateOrdrsDetail(Map<String, String> param);

    int insertClmnPlan(Map<String, String> param);

    void updateClmnPlan(Map<String, String> param);

    int insertClmnPlanHis(Map<String, String> param);
    
    int selectDegKey(Map<String, String> param);
    
    int insertUpdatePlanHis(Map<String, String> param);

    int deleteOrdrs(Map<String, String> param);

    int deleteOrdrsPlan(Map<String, String> param);
    
    int deleteOrdrsPlanHis(Map<String, String> param);

    int deleteOrdrsDetail(Map<String, String> param);

    int updateEstDeleteConfirm(Map<String, String> param);

    List<Map<String, Object>> selectOrdrsPlanHis(Map<String, String> param);
    
    List<Map<String, Object>> selectWbsLeftSalesCodeTreeList(Map<String, String> param);
    
    List<Map<String, Object>> selectItemSalesCodeTreeList(Map<String, String> param);

	void callUpdateOrdrsNo(Map<String, String> param);
	
	void callUpdateProjectMaster(Map<String, String> param);

	void callCopyOrdrs(Map<String, String> paramMap);

    int orderNoExistValidationCheck(Map<String, String> paramMap);
    
    int salesCdExistValidationCheck(Map<String, String> paramMap);
    
    Map<String, String> salesCdSearchOrderInfo(Map<String, String> paramMap);
    
    Map<String, String> ordrsCdSearchOrderInfo(Map<String, String> paramMap);
}