package com.dksys.biz.user.wb.wb02.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB02Svc {

   int deleteWbsRsltslist(Map<String, String> paramMap);

   int updateWbsRsltsCloseYn(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);

   List<Map<String, String>> selectWbsRsltsDetailList(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap);

   List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap);

   Map<String, String>  selectWbsRsltsInfo(Map<String, String> paramMap);

   int wbsLevel1RsltsInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
   int wbsPlanStsCodeUpdate(Map<String, String> paramMap);

   int wbsLevel1RsltsUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
   List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);

   List<Map<String, String>> deleteWbsRsltsDetailChk(Map<String, String> paramMap);

   int deleteWbsRsltsDetail(Map<String, String> paramMap);

   List<Map<String, String>>  deleteWbsSharngListChk(Map<String, String> paramMap);

   int deleteWbsSharngList(Map<String, String> paramMap);

   List<Map<String, String>>  deleteWbsApprovalListChk(Map<String, String> paramMap);

   int deleteWbsApprovalList(Map<String, String> paramMap);


}