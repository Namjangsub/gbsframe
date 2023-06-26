package com.dksys.biz.user.sm.sm01.service.impl;

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

import com.dksys.biz.user.sm.sm01.mapper.SM01Mapper;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM01SvcImpl implements SM01Svc {

  @Autowired
  SM01Mapper sm01Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Override
  public int selectBomSalesCount(Map<String, String> paramMap) {
    return sm01Mapper.selectBomSalesCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap) {
    return sm01Mapper.selectBomSalesList(paramMap);
  }
	
  @Override
  public int selectBomDetailCount(Map<String, String> paramMap) {
	return sm01Mapper.selectBomDetailCount(paramMap);
  }
	
  @Override
  public List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap) {
    return sm01Mapper.selectBomDetailList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap) {
    return sm01Mapper.selectBuyBomList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap) {
    return sm01Mapper.selectBomMatrList(paramMap);
  }

  @Override
  public Map<String, String> selectPrjctInfo(Map<String, String> paramMap) {
    return sm01Mapper.selectPrjctInfo(paramMap);
  }

  @Override
  public int insertBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
		//String  newPrjctSeq = String.valueOf(sm01Mapper.selectPrjctSeqNext(paramMap));
		//paramMap.put("prjctSeq", newPrjctSeq);
		int result = sm01Mapper.insertBom(paramMap);
	
	    List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"), dtlMap);
	    for (Map<String, String> dtl : bomList) {
	    	//dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    }
	    List<Map<String, String>> matrList = gsonDtl.fromJson(paramMap.get("matrArr"), dtlMap);
	    for (Map<String, String> dtl : matrList) {
	    	//dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	/* "dtaChk" 값을 확인하여
	    	 * "I"인 경우 sm01Mapper.insertBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
	    	if ("I".equals(dtaChk)) {
	    		sm01Mapper.insertBomMatr(dtl);
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
  public int updateBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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

	int result = sm01Mapper.updateBom(paramMap);
    	//  sm01Mapper.updateBom(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 업데이트하고 그 결과를 result 변수에 저장.

    List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"), dtlMap);
    for (Map<String, String> dtl : bomList) {
    	//  matrList 리스트의 각 맵 요소에 대해 반복문을 실행
    	
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
    	String dtaChk = dtl.get("prjctCrudChk").toString();
    	/* "dtaChk" 값을 확인하여
    	 * "I"인 경우 sm01Mapper.insertBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
    	 * "U"인 경우 sm01Mapper.updateBomMatr(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
    	 * "D"인 경우 * sm01Mapper.deleteBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
    	if ("I".equals(dtaChk)) {
    		dtl.put("prjctSeq", paramMap.get("prjctSeq"));
    		sm01Mapper.insertBom(dtl);
    	} else if ("U".equals(dtaChk)) {
    		sm01Mapper.updateBom(dtl);
    	} else if ("D".equals(dtaChk)) {
    		sm01Mapper.deleteBomMatrAll(dtl);
    		sm01Mapper.deleteBom(dtl);
    	}
    }
    
    List<Map<String, String>> matrList = gsonDtl.fromJson(paramMap.get("matrArr"), dtlMap);
    for (Map<String, String> dtl : matrList) {
    	//  matrList 리스트의 각 맵 요소에 대해 반복문을 실행

      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
      //      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
      String dtaChk = dtl.get("dtaChk").toString();
		/* "dtaChk" 값을 확인하여
		 * "I"인 경우 sm01Mapper.insertBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
		 * "U"인 경우 sm01Mapper.updateBomMatr(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
		 * "D"인 경우 * sm01Mapper.deleteBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
      if ("I".equals(dtaChk)) {
	        sm01Mapper.insertBomMatr(dtl);
	      } else if ("U".equals(dtaChk)) {
	        sm01Mapper.updateBomMatr(dtl);
	      } else if ("D".equals(dtaChk)) {
	        sm01Mapper.deleteBomMatr(dtl);
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
  public int deleteBom(Map<String, String> paramMap) throws Exception {
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
	  
	  int result = sm01Mapper.deleteBomMatrAll(paramMap);
		  result = sm01Mapper.deleteBom(paramMap);
	  
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
  public Map<String, String> selectPrjctIssueInfo(Map<String, String> paramMap) {
	  return sm01Mapper.selectPrjctIssueInfo(paramMap);
  }
 
  @Override
  public int insertBomIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	  int result = sm01Mapper.insertBomIssue(paramMap);
	  return result;
  }
  
  @Override
  public int updateBomIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	  int result = sm01Mapper.updateBomIssue(paramMap);
	  return result;
  }
  
}