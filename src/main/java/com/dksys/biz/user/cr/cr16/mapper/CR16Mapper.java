package com.dksys.biz.user.cr.cr16.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR16Mapper {
	
	int selectSalesYearPlanListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanList(Map<String, String> paramMap);
	
	int deleteSalesYearPlanM(Map<String, String> paramMap);
	
	int deleteSalesYearPlanD(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMC(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMU(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanD(Map<String, String> paramMap);
	
	int selectSalesYearPlanSeqNext(Map<String, String> paramMap);
	
	int salesPlanYearInsert(Map<String, String> paramMap);
	
	int salesPlanYearUpdate(Map<String, String> paramMap);
	
	List<Map<String, String>> salesYearPlanCloseChk(Map<String, String> paramMap);
	
	int salesYearPlanCloseInsert(Map<String, String> paramMap);
	
	int salesYearPlanCloseUpdate(Map<String, String> paramMap);
	
	int salesYearPlanCloseDelete(Map<String, String> paramMap);
	
	int salesYearPlanCloseChkUpdateY(Map<String, String> paramMap);
	
	int salesYearPlanCloseChkUpdateN(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanListHist(Map<String, String> paramMap);
	
	
	List<Map<String, String>> selectSalesYearPlanMC2(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanD2(Map<String, String> paramMap);
	
}










