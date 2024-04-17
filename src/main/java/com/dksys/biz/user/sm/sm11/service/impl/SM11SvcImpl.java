package com.dksys.biz.user.sm.sm11.service.impl;

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

import com.dksys.biz.user.sm.sm11.mapper.SM11Mapper;
import com.dksys.biz.user.sm.sm11.service.SM11Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM11SvcImpl implements SM11Svc {

  @Autowired
  SM11Mapper sm11Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Override
  public int selectContractCount(Map<String, String> paramMap) {
    return sm11Mapper.selectContractCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectContractList(Map<String, String> paramMap) {
    return sm11Mapper.selectContractList(paramMap);
  }

  @Override
  public Map<String, String> selectContractInfo(Map<String, String> paramMap) {
    return sm11Mapper.selectContractInfo(paramMap);
  }


  @Override
  public int updateContract(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//	Gson gson = new Gson();
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

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

	int result = sm11Mapper.updateContract(paramMap);

	
	List<Map<String, String>> detailArr = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	//일단 날리고
	sm11Mapper.deleteTurnKeyDetail(paramMap);
	//턴키 저장
	if (detailArr != null && !detailArr.isEmpty()) {
		for (Map<String, String> detailMap : detailArr) {
				detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
				detailMap.put("ctrtNo", paramMap.get("ctrtNo"));
				detailMap.put("ordrsCoCd", paramMap.get("ordrsCoCd"));
				detailMap.put("ordrsNo", paramMap.get("ordrsNo"));
				detailMap.put("userId", paramMap.get("userId"));
				detailMap.put("pgmId", paramMap.get("pgmId"));
				
				result += sm11Mapper.insertTurnKeyDetail(detailMap);
		}
	}
	
	List<Map<String, String>> clmnPlanArr = gsonDtl.fromJson(paramMap.get("clmnPlanArr"), dtlMap);
	//일단 날리고
	sm11Mapper.deleteTurnKeyClmnPlan(paramMap);
	//턴키 저장
	if (clmnPlanArr != null && !clmnPlanArr.isEmpty()) {
		for (Map<String, String> detailMap : clmnPlanArr) {
				detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
				detailMap.put("ctrtNo", paramMap.get("ctrtNo"));
				detailMap.put("ordrsCoCd", paramMap.get("ordrsCoCd"));
				detailMap.put("ordrsNo", paramMap.get("ordrsNo"));
				detailMap.put("userId", paramMap.get("userId"));
				detailMap.put("pgmId", paramMap.get("pgmId"));
				
				result += sm11Mapper.insertTurnKeyClmnPlan(detailMap);
		}
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
  public int insertContract(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

	    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생
				paramMap.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  

		
		int fileTrgtKey = sm11Mapper.selectContractSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		int result = sm11Mapper.insertContract(paramMap);
	
		List<Map<String, String>> detailArr = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		//일단 날리고
		sm11Mapper.deleteTurnKeyDetail(paramMap);
		//턴키 저장
		if (detailArr != null && !detailArr.isEmpty()) {
			for (Map<String, String> detailMap : detailArr) {
					detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					detailMap.put("ctrtNo", paramMap.get("ctrtNo"));
					detailMap.put("ordrsCoCd", paramMap.get("ordrsCoCd"));
					detailMap.put("ordrsNo", paramMap.get("ordrsNo"));
					detailMap.put("userId", paramMap.get("userId"));
					detailMap.put("pgmId", paramMap.get("pgmId"));
					
					result += sm11Mapper.insertTurnKeyDetail(detailMap);
			}
		}
		
		List<Map<String, String>> clmnPlanArr = gsonDtl.fromJson(paramMap.get("clmnPlanArr"), dtlMap);
		//일단 날리고
		sm11Mapper.deleteTurnKeyClmnPlan(paramMap);
		//턴키 저장
		if (clmnPlanArr != null && !clmnPlanArr.isEmpty()) {
			for (Map<String, String> detailMap : clmnPlanArr) {
					detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					detailMap.put("ctrtNo", paramMap.get("ctrtNo"));
					detailMap.put("ordrsCoCd", paramMap.get("ordrsCoCd"));
					detailMap.put("ordrsNo", paramMap.get("ordrsNo"));
					detailMap.put("userId", paramMap.get("userId"));
					detailMap.put("pgmId", paramMap.get("pgmId"));
					
					result += sm11Mapper.insertTurnKeyClmnPlan(detailMap);
			}
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
	    return result;
  }

  @Override
  public int deleteContract(Map<String, String> paramMap) throws Exception {
	    //---------------------------------------------------------------  
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
	    HashMap<String, String> param = new HashMap<>();
	    param.put("jobType", "fileDelete");
	    param.put("userId", paramMap.get("userId"));
	    if (deleteFileList.size() > 0) {
		    for (Map<String, String> dtl : deleteFileList) {
					//접근 권한 없으면 Exception 발생
		            param.put("comonCd",  dtl.get("comonCd"));
			    	
					cm15Svc.selectFileAuthCheck(param);
			}
	    }
		//---------------------------------------------------------------  
		//첨부 화일 권한체크 끝 
		//---------------------------------------------------------------  
	    int result = sm11Mapper.deleteContract(paramMap);

	    //상세내역 삭제
	    result += sm11Mapper.deleteTurnKeyDetail(paramMap);
	    
	    //지급계획 삭제
	    result += sm11Mapper.deleteTurnKeyClmnPlan(paramMap);
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
		    }
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  	  
	    return result;
  }
	
	@Override
	public List<Map<String, String>> selectTurnKeySalesCodeList(Map<String, String> paramMap) {
		return sm11Mapper.selectTurnKeySalesCodeList(paramMap);
	}

	
	/* 메일발송 yn*/
	public int updateMailEtcPchsOrderConfirm(Map<String, String> param) {
		int result = 0;
//		result += sm11Mapper.updateMailEtcPchsOrderConfirm(param);
		return result;
	}		
	
	//발주서 발행 이력 남기기
	@Override
	public int etcPchsOrderMasterReport(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("reportArr"), dtlMap);	
		
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
//			result = sm11Mapper.etcPchsOrderMasterReport(dtl);
		}
		
		return result;
	}

	  
	  @Override
	  public int selectContractLPayCount(Map<String, String> paramMap) {
	    return sm11Mapper.selectContractLPayCount(paramMap);
	  }

	  @Override
	  public List<Map<String, String>> selectContractLPayList(Map<String, String> paramMap) {
	    return sm11Mapper.selectContractLPayList(paramMap);
	  }
}