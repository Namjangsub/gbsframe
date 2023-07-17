package com.dksys.biz.user.wb.wb01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB01Mapper {

	List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);
	
    List<Map<String, String>> selectNewWbsPlanTreeList(Map<String, String> paramMap);
	
    List<Map<String, String>> selectNewWbsPlanExcelList(Map<String, String> paramMap);
	
    int deleteWbsPlanlist(Map<String, String> paramMap);
	
    int deleteWbsSharngListSub(Map<String, String> paramMap);

    
    int updateWbsPlanCloseYn(Map<String, String> paramMap);

	List<Map<String, String>> selectPlanSharngList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);

    int selectWbsPlanChk(Map<String, String> paramMap);

    
    int selectWbsPlanSeqNext(Map<String, String> paramMap);

    List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);
	
    int deleteWbsSharngList(Map<String, String> paramMap);
	
    int insertWbsSharngList(Map<String, String> paramMap);
    
    int insertWbsPlan(Map<String, String> paramMap);

    int updateWbsPlanLockYn(Map<String, String> paramMap);

    int updateWbsRsltsCloseYn(Map<String, String> paramMap);

    int updateWbsPlan(Map<String, String> paramMap);
    
    List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
    
    List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
    
    int selectWbsLeftSalesCodeListCount(Map<String, String> paramMap);
    
    int deleteWbsRsltslist(Map<String, String> paramMap);
    
    int deleteWbsRsltsDetailSub(Map<String, String> paramMap);

    int deleteWbsApprovalListSub(Map<String, String> paramMap);
}










