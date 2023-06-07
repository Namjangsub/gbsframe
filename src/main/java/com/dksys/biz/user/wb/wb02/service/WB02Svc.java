package com.dksys.biz.user.wb.wb02.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB02Svc {

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
	
	int selectMaxTrgtKey(Map<String, String> paramMap);
	
	int selectWbsPlanChk(Map<String, String> paramMap);
	
	int wbsLevel1RsltsInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
	int wbsLevel1RsltsUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
    int selectWbsRsltsResultCount(Map<String, String> paramMap);
	
    
    // 실적 상세테이블 삭제전 확인
    List<Map<String, String>> deleteWbsRsltsDetailChk(Map<String, String> paramMap);
	
    // 실적 상세테이블 삭제
    int deleteWbsRsltsDetail(Map<String, String> paramMap);
    
    
    // 공유테이블 삭제전 확인
    List<Map<String, String>>  deleteWbsSharngListChk(Map<String, String> paramMap);
    
    // 공유 테이블 삭제
    int deleteWbsSharngList(Map<String, String> paramMap);
    
    // 결재테이블 삭제전 확인
    List<Map<String, String>>  deleteWbsApprovalListChk(Map<String, String> paramMap);
    
    // 결재 테이블 삭제
    int deleteWbsApprovalList(Map<String, String> paramMap);
    
    
    
	List<Map<String, String>> selectWbsRsltsResultList(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap);
	
	List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap);
	
	
	//  <!-- WBS 실적 상위공정 유무체크  -->
    List<Map<String, String>>  selectWbsPlanConfirmCount(Map<String, String> paramMap);
}