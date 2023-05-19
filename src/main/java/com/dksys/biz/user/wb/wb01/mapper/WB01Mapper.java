package com.dksys.biz.user.wb.wb01.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB01Mapper {
	int selectWbsPlanCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap);

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
	
	
	int deleteWbsSharngUser(Map<String, String> paramMap);
	
	int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap);

	int deleteWbsPlanlist(Map<String, String> paramMap);
	
	int selectMaxWbsPlanNo(Map<String, String> paramMap);

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
	
	int selectWbsPlanChk(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap);
}