package com.dksys.biz.user.sm.sm03.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.sm.sm02.mapper.SM02Mapper;
import com.dksys.biz.user.sm.sm03.mapper.SM03Mapper;
import com.dksys.biz.user.sm.sm03.service.SM03Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM03SvcImpl implements SM03Svc {

	@Autowired
	SM03Mapper sm03Mapper;
	
	@Autowired
	SM02Mapper sm02Mapper;
	
	@Autowired
	SM03Svc sm03Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectWareHousingListCount(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWareHousingList(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingList(paramMap);
	}	
	
	@Override
	public List<Map<String, String>> selectDashBoardWareHousingList(Map<String, String> paramMap) {
		return sm03Mapper.selectDashBoardWareHousingList(paramMap);
	}	

	@Override
	public int selectMaxTrgtKey(Map<String, String> paramMap) {
		return sm03Mapper.selectMaxTrgtKey(paramMap);
	}	
	
	@Override
	public String selectMaxInNo(Map<String, String> paramMap) {
		return sm03Mapper.selectMaxInNo(paramMap);
	}		
		  
	/* 입고 등록시 발주 list */
	@Override
	public List<Map<String, String>> selectWareHousingDetailList(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingDetailList(paramMap);
	}		
	

	/* 입고정보 - 수정 팝업  */
	@Override
	public List<Map<String, String>> selectWareHousingDetailInfo(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingDetailInfo(paramMap);
	}	
	
	@Override
	public int insertWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
		if( paramMap.get("fileTrgtKey").equals("0") ) {
			int fileTrgtKey = sm03Svc.selectMaxTrgtKey(paramMap);	
		    paramMap.put("fileTrgtKey", String.valueOf(fileTrgtKey));
		}	    
		
	    //MAX ORDGR_NO - 최조 입력일 경우만
		String maxInNo = "";
		if( !paramMap.get("inNo").equals("") ) {
		    maxInNo = paramMap.get("inNo");
		} else {
		    maxInNo = sm03Svc.selectMaxInNo(paramMap);			
		}
	    paramMap.put("maxInNo", maxInNo);
	    if( paramMap.get("inNo").equals("") ) {
			//master 용		
			result += sm03Mapper.insertWareHousingMaster(paramMap);	        	
	    }	 
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxInNo", maxInNo);
			dtl.put("ioDiv", paramMap.get("ioDiv"));
			dtl.put("currCd", paramMap.get("currCd"));
			dtl.put("exrate", paramMap.get("exrate"));
    		result += sm03Mapper.insertWareHousingDetail(dtl);	
    		
    		//발주 상세 아이템별 수량과 입고수량이 같으면 상세내역은 자동으로 입고확인처리함.
    		result += sm02Mapper.detailArriveWareHousingStoreConfirm(dtl);

    		//발주 상세 아이템의 모든내역 입고확인면 마스터에 입고확인 처리함.
    		result += sm02Mapper.allArriveWareHousingStoreCheck(dtl);			
		}			
		  		
		
		//발주서 입고확인 및 입고확인 날자 설정 (발주수량과 입고수량이 같을때만 입고처리 됨)
//		result += sm02Mapper.arriveWareHousingStoreConfirm(paramMap);	
		
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
	
	//입고 master 수정 - 사용안함
	@Override
	public int updateWareHousing(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
  				
		int result = 0;	    	    
	    //upate
		result = sm03Mapper.updateWareHousingMaster(paramMap);	
		
		result += sm03Svc.updateWareHousingDetail(paramMap, mRequest);
		
		return result;
	}	
	
	//발주관리 구매 bom 수정
	@Override
	public int updateWareHousingDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("ioDiv", paramMap.get("ioDiv"));
			dtl.put("currCd", paramMap.get("currCd"));
			dtl.put("exrate", paramMap.get("exrate"));
			
    		result += sm03Mapper.updateWareHousingDetail(dtl);			
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
	
	@Override
	public int deleteWareHousingMaster(Map<String, String> param) {
		return sm03Mapper.deleteWareHousingMaster(param);
	}	
	
	
	@Override
	public int deleteWareHousingDetail(Map<String, String> param) {
		int result = 0;	   
		result = sm03Mapper.deleteWareHousingDetail(param);
		result += sm03Mapper.deleteWareHousingMaster(param);
		return result;
	}	
	
	@Override
	public int deleteWareHousingInno(Map<String, String> param) {
		int result = 0;	   
		result = sm03Mapper.deleteWareHousingDirectMaster(param);
		result += sm03Mapper.deleteWareHousingDirectDetail(param);
		return result;
	}		
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
		 return sm03Mapper.select_prjct_code(paramMap);
	  }
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_mngId_code(Map<String, String> paramMap) {
		 return sm03Mapper.select_mngId_code(paramMap);
	  }

	@Override
	public int updateMailStoreConfirm(Map<String, String> param) {
		int result = 0;
		result += sm03Mapper.updateMailStoreConfirm(param);
		return result;
	}		

	@Override
	public int selectWareHousingDetaiNewlListCount(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingDetaiNewlListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWareHousingDetaiNewlList(Map<String, String> paramMap) {
		return sm03Mapper.selectWareHousingDetaiNewlList(paramMap);
	}	

	
	@Override
	public Map<String, String> selectPurchaseconfirmed(Map<String, String> paramMap) {
		return sm03Mapper.selectPurchaseconfirmed(paramMap);
	}

	@Override
	public int updateDudtIntendDt(Map<String, String> param) {
		//Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		//데이터처리 시작
		int result = 0;
			
		//상세 납기일자 수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(param.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", param.get("userId"));
	    	dtl.put("pgmId", param.get("pgmId"));
			
			result = sm03Mapper.updateDudtIntendDt(dtl);
	    }
		//데이터 처리 끝
		return result;
	}	

	@Override
	public int select_sm03_ListCount(Map<String, String> paramMap) {
		return sm03Mapper.select_sm03_ListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> select_sm03_List(Map<String, String> paramMap) {
		return sm03Mapper.select_sm03_List(paramMap);
	}	
	
	@Override
	public List<Map<String, String>> selectMultiPrdtGrpCodeList(Map<String, String> paramMap) {
		return sm03Mapper.selectMultiPrdtGrpCodeList(paramMap);
	}	

	@Override
	public List<Map<String, String>> select_sm03_List_Pop(Map<String, String> paramMap) {
		return sm03Mapper.select_sm03_List_Pop(paramMap);
	}	
	
}