package com.dksys.biz.user.pm.pm01.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.pm.pm01.mapper.PM01Mapper;
import com.dksys.biz.user.pm.pm01.service.PM01Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM01SvcImpl implements PM01Svc {

  @Autowired
  PM01Mapper pm01Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Override
  public int selectDailyWorkCount(Map<String, String> paramMap) {
    return pm01Mapper.selectDailyWorkCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectDailyWorkList(Map<String, String> paramMap) {
    return pm01Mapper.selectDailyWorkList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectItemList(Map<String, String> paramMap) {
    return pm01Mapper.selectItemList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectPrdtList(Map<String, String> paramMap) {
    return pm01Mapper.selectPrdtList(paramMap);
  }

  @Override
  public Map<String, String> selectDailyWorkInfo(Map<String, String> paramMap) {
    return pm01Mapper.selectDailyWorkInfo(paramMap);
  }

  @Override
  public int selectConfirmCount(Map<String, String> paramMap) {
    return pm01Mapper.selectConfirmCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectFileList(Map<String, String> paramMap) {
    return pm01Mapper.selectFileList(paramMap);
  }

  @Override
  public int updateDailyWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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

	int result = pm01Mapper.updateDailyWork(paramMap);
    	//  pm01Mapper.updateDailyWork(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 업데이트하고 그 결과를 result 변수에 저장.


    List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
    for (Map<String, String> dtl : dtlPrdt) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행
    	
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
    	String dtaChk = dtl.get("prjctCrudChk").toString();
    	/* "dtaChk" 값을 확인하여
    	 * "I"인 경우 pm01Mapper.insertDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
    	 * "U"인 경우 pm01Mapper.updateDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
    	 * "D"인 경우 * pm01Mapper.deleteDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
    	if ("I".equals(dtaChk)) {
    		dtl.put("prjctSeq", paramMap.get("prjctSeq"));
    		pm01Mapper.insertDailyWorkPrdt(dtl);
    	} else if ("U".equals(dtaChk)) {
    		pm01Mapper.updateDailyWorkPrdt(dtl);
    	} else if ("D".equals(dtaChk)) {
    		pm01Mapper.deleteDailyWorkPrdt(dtl);
    		pm01Mapper.deleteDailyWorkDtlPrdtAll(dtl);
    	}
    }
    
    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("prdtItemArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행

      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
      //      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
      String dtaChk = dtl.get("dtaChk").toString();
		/* "dtaChk" 값을 확인하여
		 * "I"인 경우 pm01Mapper.insertDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
		 * "U"인 경우 pm01Mapper.updateDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
		 * "D"인 경우 * pm01Mapper.deleteDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
      if ("I".equals(dtaChk)) {
	        pm01Mapper.insertDailyWorkDtl(dtl);
	      } else if ("U".equals(dtaChk)) {
	        pm01Mapper.updateDailyWorkDtl(dtl);
	      } else if ("D".equals(dtaChk)) {
	        pm01Mapper.deleteDailyWorkDtl(dtl);
	      }
    }
    
	//---------------------------------------------------------------  
	//첨부 화일 처리 시작 
	//---------------------------------------------------------------  
    if (uploadFileList.size() > 0) {
	    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
	    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
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
  public int insertDailyWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
		String  newPrjctSeq = String.valueOf(pm01Mapper.selectDailyWorkSeqNext(paramMap));
		paramMap.put("prjctSeq", newPrjctSeq);
		int result = pm01Mapper.insertDailyWork(paramMap);
	
	    List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
	    for (Map<String, String> dtl : dtlPrdt) {
	    	dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	pm01Mapper.insertDailyWorkPrdt(dtl);
	    }
	    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("prdtItemArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	/* "dtaChk" 값을 확인하여
	    	 * "I"인 경우 pm01Mapper.insertDailyWorkDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
	    	if ("I".equals(dtaChk)) {
	    		pm01Mapper.insertDailyWorkDtl(dtl);
	    	} 
	    }
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
	    return result;
  }

  @Override
  public int deleteDailyWork(Map<String, String> paramMap) throws Exception {
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
	  
	  int result = pm01Mapper.deleteDailyWorkPrdtSeqAll(paramMap);
		  result = pm01Mapper.deleteDailyWorkDtlSeqAll(paramMap);
		  result = pm01Mapper.deleteDailyWork(paramMap);
	  
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

}