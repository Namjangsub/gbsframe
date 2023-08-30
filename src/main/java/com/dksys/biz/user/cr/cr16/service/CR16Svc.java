package com.dksys.biz.user.cr.cr16.service;

import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;

public interface CR16Svc {
	
	int selectSalesYearPlanListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSalesYearPlanList(Map<String, String> paramMap);		
	
	int deleteSalesYearPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMC(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanMU(Map<String, String> paramMap);
	
	List<Map<String, String>> selectSalesYearPlanD(Map<String, String> paramMap);
}