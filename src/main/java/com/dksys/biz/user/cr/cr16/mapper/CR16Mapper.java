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
	
	
}










