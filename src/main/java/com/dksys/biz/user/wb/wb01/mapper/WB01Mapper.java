package com.dksys.biz.user.wb.wb01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB01Mapper {
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
	
	int insertWbsSharngList(Map<String, String> paramMap);
	
	
	
	int deleteWbsSharngList(Map<String, String> paramMap);
	
	int deleteWbsSharngUser(Map<String, String> paramMap);
	
	int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap);

	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	int insertWbsSharngUser(Map<String, String> paramMap);
	
	int insertToDoList(Map<String, String> paramMap);
	
	int wbsLevel1PlanInsert(Map<String, String> paramMap);

	int wbsLevel1PlanUpdate(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsInfoList(Map<String, String> paramMap);
	
	int wbsLevel2PlanInsert(Map<String, String> paramMap);

	int wbsLevel2PlanUpdate(Map<String, String> paramMap);
	
	int wbsLevel3PlanInsert(Map<String, String> paramMap);

	int wbsLevel3PlanUpdate(Map<String, String> paramMap);	
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	
	List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap);
	
	int selectWbsPlanConfirmCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectFileCodeSelect(Map<String, String> paramMap);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap);
	int selectWbsPlanTreeListCount(Map<String, String> paramMap);
	Map<String, String> selectWbsPlanInfo(Map<String, String> paramMap);
	int selectWbsPlanSeqNext(Map<String, String> paramMap);
	int insertWbsPlan(Map<String, String> paramMap);
	int deleteWbsPlanlist(Map<String, String> paramMap);
	int deleteWbsSharngListSub(Map<String, String> paramMap);
	int selectWbsPlanChk(Map<String, String> paramMap);
	int updateWbsPlan(Map<String, String> paramMap);
	Map<String, String> selectWbsPlanFileTrgtKeyInfo(Map<String, String> paramMap);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	List<Map<String, String>> selectNewWbsPlanTreeList(Map<String, String> paramMap);
	List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);
	
	
	
	
	
	
	
	
	
	
}