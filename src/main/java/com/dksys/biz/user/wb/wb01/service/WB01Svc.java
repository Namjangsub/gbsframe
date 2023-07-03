package com.dksys.biz.user.wb.wb01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB01Svc {

	
	int selectWbsPlanCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanDivList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsLevel1List(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsDsgnDifList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPrdctnDifList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsLevel2List(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsLevel3List(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanStsList(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPopupLevel2PlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPopupLevel3PlanList(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPopupSharngList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPlanSharngList(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);
	
	
	

	
	int deleteWbsSharngList(Map<String, String> paramMap);
	
	int deleteWbsSharngUser(Map<String, String> paramMap);
	
	int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	int insertWbsSharngUser(Map<String, String> paramMap);

	int insertToDoList(Map<String, String> paramMap);
	
	int wbsLevel1PlanInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int wbsLevel1PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	List<Map<String, String>> selectWbsInfoList(Map<String, String> paramMap);
	
    int wbsLevel2PlanInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int wbsLevel2PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
    int wbsLevel3PlanInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int wbsLevel3PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);	
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	int selectWbsPlanChk(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap);
	
	int selectWbsPlanConfirmCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectFileCodeSelect(Map<String, String> paramMap);
	
	
	
	
	
	
	
	
	
	
	
	
	
	List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap);
	int selectWbsPlanTreeListCount(Map<String, String> paramMap);
	Map<String, String> selectWbsPlanInfo(Map<String, String> paramMap);
	Map<String, String> selectWbsLevelPlanInfo(Map<String, String> paramMap);
	Map<String, String> selectWbsRsltsInfo(Map<String, String> paramMap);
	
	int insertWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	int updateWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	int deleteWbsPlanlist(Map<String, String> paramMap);
	
	
	
	
	
	
	
	List<Map<String, String>> selectNewWbsPlanTreeList(Map<String, String> paramMap);	
	List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);	
	Map<String, String> selectWbsPlanFileTrgtKeyInfo(Map<String, String> paramMap);
	
	List<Map<String, String>> selectNewWbsPlanExcelList(Map<String, String> paramMap);	
	int updateWbsPlanLockYn(Map<String, String> paramMap);
	
}