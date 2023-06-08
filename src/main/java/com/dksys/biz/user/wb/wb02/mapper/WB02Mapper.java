package com.dksys.biz.user.wb.wb02.mapper;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB02Mapper {

	int selectWbsRsltsPlanListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsRsltsPlanList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsRsltsPlanExcelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsRsltsResultExcelList1(Map<String, String> paramMap);
	

    List<Map<String, String>> selectWbsRsltsMasterList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsRsltsDetailList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap);
		
	List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteWbsRsltsDetailChk(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);
	
	List<Map<String, String>> deleteWbsApprovalListChk(Map<String, String> paramMap);
	
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	int selectWbsPlanChk(Map<String, String> paramMap);
		
	int wbsRsltsMasterInsert(Map<String, String> paramMap);
	
	int wbsRsltsDetailInsert(Map<String, String> paramMap);
	
    int wbsRsltsMasterUpdate(Map<String, String> paramMap);
	
	int wbsRsltsDetailUpdate(Map<String, String> paramMap);

	
	
	int deleteWbsRsltsDetail(Map<String, String> paramMap);
		
	int deleteWbsSharngList(Map<String, String> paramMap);
	 
	int deleteWbsApprovalList(Map<String, String> paramMap);
	
	
	
	int insertWbsSharngList(Map<String, String> paramMap);
	
	int insertWbsApprovalList(Map<String, String> paramMap);
	
    int selectWbsRsltsResultCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsRsltsResultList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanConfirmCount(Map<String, String> paramMap);
	
	int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap);
	
	int deleteWbsPlanlist(Map<String, String> paramMap);
	
	
	
}