package com.dksys.biz.user.sm.sm02.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm02.mapper.SM02Mapper;
import com.dksys.biz.user.sm.sm02.service.SM02Svc;
import com.dksys.biz.user.wb.wb20.mapper.WB20Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM02SvcImpl implements SM02Svc {

	@Autowired
	SM02Mapper sm02Mapper;
	
	@Autowired
	SM02Svc sm02Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	
	
    @Autowired
    WB20Svc wb20Svc;
    
    @Autowired
    WB20Mapper wb20Mapper;

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectOrderListCount(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrderList(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderList(paramMap);
	}	

	@Override
	public int selectMaxTrgtKey(Map<String, String> paramMap) {
		return sm02Mapper.selectMaxTrgtKey(paramMap);
	}	
	
	@Override
	public String selectMaxOrdrgNo(Map<String, String> paramMap) {
		return sm02Mapper.selectMaxOrdrgNo(paramMap);
	}		
	
	/* 발주등록시 bom list */
	@Override
	public List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap) {
		return sm02Mapper.selectBomDetailList(paramMap);
	}		
	  
	/* 발주등록수정시 bom list */
	@Override
	public List<Map<String, String>> selectOrderDetailList(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderDetailList(paramMap);
	}	
	
	/* 발주상세 view  */
	@Override
	public List<Map<String, String>> selectOrderDetailView(Map<String, String> paramMap) {
		return sm02Mapper.selectOrderDetailView(paramMap);
	}			
	
	@Override
	public int insertOrderMaster(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
				param.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(param);
		}
		String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
	    for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			    Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
			    param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			    param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  		
		
		int result = 0;	    
		//FILE TARGET KEY
		int fileTrgtKey = sm02Svc.selectMaxTrgtKey(paramMap);	
	    paramMap.put("fileTrgtKey", String.valueOf(fileTrgtKey));
	    
	    //MAX ORDGR_NO
	    String maxOrdrgNo = sm02Svc.selectMaxOrdrgNo(paramMap);	    
	    paramMap.put("maxOrdrgNo", maxOrdrgNo);
	    
	    //insert orderMaster
		result += sm02Mapper.insertOrderMaster(paramMap);
	    
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxOrdrgNo", maxOrdrgNo);
			
    		result += sm02Mapper.insertOrderDetail(dtl);			
		}			
		  		
		
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
	    }
	    
	    for(String fileKey : deleteFileList) {
	    	cm08Svc.deleteFile(fileKey);
	    }
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
		return result;
	}
	
	//발주관리 구매 bom 수정
	@Override
	public int updateOrder(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
  				
		int result = 0;	    	    
	    //upate
		result = sm02Mapper.updateOrderMaster(paramMap);	
		
		result += sm02Svc.updateOrderDetail(paramMap, mRequest);
		
		return result;
	}	
	
	//발주관리 구매 bom 수정
	@Override
	public int updateOrderDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    	


		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
				param.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(param);
		}
		String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
	    for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			    Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
			    param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			    param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  		
		
		int result = 0;	  
		//발주 세부 내역 변경 DB Update
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);   
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("ordrgNo", paramMap.get("ordrgNo"));
			
    		result += sm02Mapper.updateOrderDetail(dtl);		
		}			

		result = 0;	 
		//발주 세부 내역 삭제 DB delete   	 		
		List<Map<String, String>> detailDeleteArrMap = gsonDtl.fromJson(paramMap.get("makeDeleteArr"), dtlMap);   
		// detailDeleteArrMap에 삭제할 자료가 있는지 확인후 처리
		if (detailDeleteArrMap != null && !detailDeleteArrMap.isEmpty()) {
			//delete
			for(Map<String, String> dtl : detailDeleteArrMap) {
				dtl.put("coCd", paramMap.get("coCd"));
				dtl.put("ordrgNo", paramMap.get("ordrgNo"));
				
	    		result += sm02Mapper.deleteOrderDetail(dtl);		
			}	
		}
		  		
		//---------------------------------------------------------------  
		// 결재라인 처리 시작 
		//---------------------------------------------------------------		
		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();		
		if( paramMap.containsKey("approvalArr") ) {	
			//결제라인 insert
			result += wb20Svc.insertTodoMaster(paramMap);							
		}		
		//---------------------------------------------------------------  
		// 결재라인 처리 end
		//---------------------------------------------------------------		
		
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
	    }
	    
	    for(String fileKey : deleteFileList) {
	    	cm08Svc.deleteFile(fileKey);
	    }
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
		return result;
	}	
		
	@Override
	public int deleteOrderDetail(Map<String, String> param) {
		return sm02Mapper.deleteOrderDetail(param);
	}	
	
	/* 오늘자 환율 select  */
	@Override
	public List<Map<String, String>> selectCurrToday(Map<String, String> paramMap) {
		return sm02Mapper.selectCurrToday(paramMap);
	}
	
	/* 오늘자 구매단가관리  */
	@Override
	public String selectCurrMatrUpr(Map<String, String> paramMap) {
		return sm02Mapper.selectCurrMatrUpr(paramMap);
	}		

	/* 발주삭제시 입고, 매입여부 체크  */
	@Override
	public List<Map<String, String>> selectInPurchaseChk(Map<String, String> paramMap) {
		return sm02Mapper.selectInPurchaseChk(paramMap);
	}			
	
	/* 발주 master detail 삭제 */
	public int deleteOrder(Map<String, String> param) {
		int result = 0;
		result += sm02Mapper.deleteOrderDetailAll(param);
		result += sm02Mapper.deleteOrderMaster(param);
		return result;
	}
	
	/* 메일발송 yn*/
	public int updateMailConfirm(Map<String, String> param) {
		int result = 0;
		result += sm02Mapper.updateMailConfirm(param);
		return result;
	}		
	
	//발주 일괄 결재 요청
	@Override
	public int updateOrderApprovalRequest(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);	
		
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
			result = sm02Mapper.updateOrderMaster(dtl);	
			
			//---------------------------------------------------------------  
			// 결재라인 처리 시작 
			//---------------------------------------------------------------		
			if( paramMap.containsKey("approvalArr") ) {	

				List<Map<String, String>> approvalMap = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
					paramMap.put("todoNo", dtl.get("ordrgNo"));
					paramMap.put("todoDiv2CodeId", "TODODIV2050");
					paramMap.put("todoFileTrgtKey", dtl.get("fileTrgtKey"));
					paramMap.put("todoCoCd", dtl.get("coCd"));
					paramMap.put("salesCd", dtl.get("salesCd"));
					paramMap.put("sanctnSttus", "N");
					
					//수정시 삭제된 부분만 처리가 불가하여 전체 삭제후 저장
					wb20Mapper.deleteAllTodoMaster(paramMap);
					
					//결제라인 insert			
					String maxTodoKey = "";		
					for(Map<String, String> dtlApp : approvalMap) {
						//입력, 수정 
						
						dtlApp.put("todoNo", dtl.get("ordrgNo"));
						dtlApp.put("todoDiv2CodeId", "TODODIV2050");
						dtlApp.put("todoFileTrgtKey", dtl.get("fileTrgtKey"));
						dtlApp.put("todoCoCd", dtl.get("coCd"));
						dtlApp.put("salesCd", dtl.get("salesCd"));
						dtlApp.put("sanctnSttus", "N");
						dtlApp.put("pgParam", dtl.get("pgParam"));
						dtlApp.put("pgPath", paramMap.get("pgPath"));
						dtlApp.put("userId", paramMap.get("userId"));
						dtlApp.put("pgmId", paramMap.get("pgmId"));
						
			            //TITLE 추가
						dtlApp.put("todoTitl", dtl.get("pchsClntNm") + " " + dtl.get("eqpNm"));
						
						maxTodoKey =  wb20Mapper.selectmaxTodoKey(dtlApp);
						dtlApp.put("todoKey", maxTodoKey);
						result += wb20Mapper.insertTodoMaster(dtlApp);				
					}		
				}				
			}		
			//---------------------------------------------------------------  
			// 결재라인 처리 end
			//---------------------------------------------------------------	
		
		return result;
	}	
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
		 return sm02Mapper.select_prjct_code(paramMap);
	  }
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_mngId_code(Map<String, String> paramMap) {
		 return sm02Mapper.select_mngId_code(paramMap);
	  }
	
	//발주서 발행 이력 남기기
	@Override
	public int OrderMasterReport(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("reportArr"), dtlMap);	
		
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
			result = sm02Mapper.OrderMasterReport(dtl);
		}
		
		return result;
	}

	//발주 물품 당사 도착 확인 처리
	@Override
	public int arriveWareHousing(Map<String, String> paramMap)  throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);	
		
		int result = 0;	    	    
		String jobType = paramMap.get("jobType");	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("userId", paramMap.get("userId"));
			
			if ("Y".equals(jobType)) {
				result += sm02Mapper.arriveWareHousingConfirm(dtl);
			} else {
				result += sm02Mapper.arriveWareHousingUnconfirm(dtl);
			}
		}
		return result;
	}

	//발주 물품 당사 도착 상세 확인 처리
	@Override
	public int detailArriveWareHousing(Map<String, String> paramMap)  throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);	
		
		int result = 0;	    	    
		String jobType = paramMap.get("jobType");	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("userId", paramMap.get("userId"));
			
			if ("Y".equals(jobType)) {
				result += sm02Mapper.detailArriveWareHousingConfirm(dtl);
				result += sm02Mapper.allArriveWareHousingStoreCheck(dtl);
			} else {
				result += sm02Mapper.detailArriveWareHousingUnconfirm(dtl);
			}
		}
		return result;
	}
}