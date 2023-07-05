package com.dksys.biz.user.wb.wb01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB01Svc {

   List<Map<String, String>> selectWbsLeftSalesCodeList(Map<String, String> paramMap);	

   List<Map<String, String>> selectNewWbsPlanTreeList(Map<String, String> paramMap);	

   List<Map<String, String>> selectNewWbsPlanExcelList(Map<String, String> paramMap);

   int deleteWbsPlanlist(Map<String, String> paramMap);

   int updateWbsPlanLockYn(Map<String, String> paramMap);

   List<Map<String, String>> selectPlanSharngList(Map<String, String> paramMap);
	
   List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	 
   int selectWbsPlanChk(Map<String, String> paramMap);

   int insertWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

   int updateWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
   int updateWbsRsltsCloseYn(Map<String, String> paramMap);

   int deleteWbsSharngList(Map<String, String> paramMap);
   
   List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);
   
   List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
   
   List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
   
   int selectWbsLeftSalesCodeListCount(Map<String, String> paramMap);
   
}