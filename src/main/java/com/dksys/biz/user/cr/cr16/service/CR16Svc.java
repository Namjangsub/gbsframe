package com.dksys.biz.user.cr.cr16.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CR16Svc {
	
	int selectSalesYearPlanListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSalesYearPlanList(Map<String, String> paramMap);		
	
	int selectSalesYearPlanListHistCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanListHist(Map<String, String> paramMap);
	
	int deleteSalesYearPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMC(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMU(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanD(Map<String, String> paramMap);
	
	int salesPlanYearInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int salesPlanYearUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int salesYearPlanCloseY(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int salesYearPlanCloseN(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	List<Map<String, String>> selectSalesYearPlanMC2(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanD2(Map<String, String> paramMap);

	List<Map<String, String>> selectSalesDeptList(Map<String, String> paramMap);

	void callPlanClose(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesPlanHistList(Map<String, String> paramMap);
}