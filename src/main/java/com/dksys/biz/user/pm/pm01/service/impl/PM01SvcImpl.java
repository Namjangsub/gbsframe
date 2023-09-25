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
  public Map<String, String> selectDailyWorkInfo(Map<String, String> paramMap) {
    return pm01Mapper.selectDailyWorkInfo(paramMap);
  }

  @Override
  public int selectConfirmCount(Map<String, String> paramMap) {
    return pm01Mapper.selectConfirmCount(paramMap);
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
	String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
	List<String> deleteFileList = Arrays.asList(deleteFileArr);
	
	Boolean fileFlag = false; //파일영역이 추가되어 있는지 확인하는 플래그
	if (uploadFileList == null || uploadFileList.isEmpty()) {
		
	} else {
		fileFlag = true;
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
				param.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(param);
		}
		for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
				Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
				param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
				param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
	}
	//---------------------------------------------------------------  
	//첨부 화일 권한체크  끝 
	//---------------------------------------------------------------  

	int result = pm01Mapper.updateDailyWork(paramMap);

	//---------------------------------------------------------------  
	//첨부 화일 처리 시작 
	//---------------------------------------------------------------  
	
	if (fileFlag) {
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
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
		Boolean fileFlag = false;
		if (uploadFileList == null || uploadFileList.isEmpty()) {
			
		} else {
			if (uploadFileList.size() > 0) {
				fileFlag = true;
				//접근 권한 없으면 Exception 발생
				paramMap.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(paramMap);
			}
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  

		
		int fileTrgtKey = pm01Mapper.selectDailyWorkSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		int result = pm01Mapper.insertDailyWork(paramMap);
	
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (fileFlag) {
			if (uploadFileList.size() > 0) {
				paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
				paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
				cm08Svc.uploadFile(paramMap, mRequest);
			}
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
	  
	  int result = pm01Mapper.deleteDailyWork(paramMap);
	  
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
  public List<Map<String, String>> selectMonthlyWorkList(Map<String, String> paramMap) {
    return pm01Mapper.selectMonthlyWorkList(paramMap);
  }
  
}