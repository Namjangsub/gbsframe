package com.dksys.biz.user.pm.pm01.service.impl;

import java.lang.reflect.Type;
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

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.pm.pm01.mapper.PM01Mapper;
import com.dksys.biz.user.pm.pm01.service.PM01Svc;
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

	int result = pm01Mapper.updateDailyWork(paramMap);


	//---------------------------------------------------------------  
	//첨부 화일 처리 시작 
	//---------------------------------------------------------------
	cm08Svc.uploadFile("PM0101M01", paramMap.get("fileTrgtKey"), mRequest);
	Gson gson = new Gson();
	String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
	List<String> deleteFileList = Arrays.asList(deleteFileArr);
    if (deleteFileList != null && !deleteFileList.isEmpty()) {
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
    }
	//---------------------------------------------------------------  
	//첨부 화일 처리  끝 
	//---------------------------------------------------------------  


	//경비중 삭제된 내역  처리
	List<Map<String, String>> tripRptRowDeleteArr = gsonDtl.fromJson(paramMap.get("tripRptRowDeleteArr"), dtlMap);
	if (tripRptRowDeleteArr != null && !tripRptRowDeleteArr.isEmpty()) {	//경비내역이 있으면 처리함.
	    for (Map<String, String> tripMap : tripRptRowDeleteArr) {
		    //경비관련 첨부파일은 아래 함수를 활용함
		    // fileTrgtKey : PM0101M01_M 으로 저장함.  fileTrgtKey=PM0101M01은 일반 첨부 파일임
		    String tripfileTrgtTyp = "PM0101M01_M";
		    //세부내역별 키값에 대한부분은 작업일보 번호 + 경비인련번호로 구성함
		    String tripfileTrgtKey = tripMap.get("workRptNo") +  "-" + tripMap.get("workRptNoSeq");
        	String updCheck = tripMap.get("updCheck"); //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
        	if ("D".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
        	    //출장경비 상세 내역 삭제 처리
        		result += pm01Mapper.deleteTripRpt(tripMap);
        		//첨부파일 상세내역 연계자료 삭제 처리 필요함
		    	String fileKey = tripMap.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
        	}
	    }
	}  
	
	//경비 Insert 처리
	List<Map<String, String>> tripArr = gsonDtl.fromJson(paramMap.get("tripRptS"), dtlMap);
	if (tripArr != null && !tripArr.isEmpty()) {	//경비내역이 있으면 처리함.
	    for (Map<String, String> tripMap : tripArr) {
		    //경비관련 첨부파일은 아래 함수를 활용함
		    // fileTrgtKey : PM0101M01_M 으로 저장함.  fileTrgtKey=PM0101M01은 일반 첨부 파일임
		    String tripfileTrgtTyp = "PM0101M01_M";
		    //세부내역별 키값에 대한부분은 작업일보 번호 + 경비인련번호로 구성함
		    String tripfileTrgtKey = tripMap.get("workRptNo") +  "-" + tripMap.get("workRptNoSeq");

        	String updCheck = tripMap.get("updCheck"); //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
        	if ("C".equals(updCheck)) { //처음 등록시에는 C인 것만 처리하면 됨.
		        pm01Mapper.insertTripRpt(tripMap);
		        
		        //경비용 첨부파일 처리
			    cm08Svc.uploadFile(tripfileTrgtTyp, tripfileTrgtKey, mRequest) ;
        	} else if ("U".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
		        pm01Mapper.updateTripRpt(tripMap);
        		
        	} else if ("D".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.

        	    //출장경비 상세 내역 삭제 처리
        		result += pm01Mapper.deleteTripRpt(tripMap);
        		//첨부파일 상세내역 연계자료 삭제 처리 필요함
		    	String fileKey = tripMap.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
        	}
	    }
	}
  
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
//		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
//		Boolean fileFlag = false;
//		if (uploadFileList == null || uploadFileList.isEmpty()) {
//			
//		} else {
//			if (uploadFileList.size() > 0) {
//				fileFlag = true;
//				//접근 권한 없으면 Exception 발생
//				paramMap.put("jobType", "fileUp");
//				cm15Svc.selectFileAuthCheck(paramMap);
//			}
//		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  

		
		String fileTrgtKey = pm01Mapper.selectDailyWorkSeqNext(paramMap);
		paramMap.put("fileTrgtKey", fileTrgtKey);
		
		//작업일보 등록
		int result = pm01Mapper.insertDailyWork(paramMap);
		//작업일보는 작업권한 체크없으 무조건 등록가능 첨부파일 처리
		cm08Svc.uploadFile("PM0101M01", paramMap.get("fileTrgtKey"), mRequest);
		
		
		//경비 Insert 처리
        List<Map<String, String>> tripArr = gsonDtl.fromJson(paramMap.get("tripRptS"), dtlMap);
        if (tripArr != null && !tripArr.isEmpty()) {	//경비내역이 있으면 처리함.
	        for (Map<String, String> tripMap : tripArr) {
	        	String updCheck = tripMap.get("updCheck"); //구분코드 C, D  : 처음 등록시에는 C, D만 있음. Delete 는 무시하면 됨
	        	if ("C".equals(updCheck)) { //처음 등록시에는 C인것만 처리하면 됨.
		        	//출장비용 상세 내역별 일련번호 생성후 사용
		        	tripMap.put("fileTrgtKey", pm01Mapper.selectTripRptSeqNext(paramMap));	//상세내역 fileTrgtKey 번호 생성 필요함.
		        	tripMap.put("workRptNo", paramMap.get("workRptNo"));					//백엔드에서 신규 등록시 생성된 번호로 대체함
		            pm01Mapper.insertTripRpt(tripMap);
	    	        
	    	        //경비용 첨부파일 처리
	    		    //경비관련 첨부파일은 아래 함수를 활용함
	    		    // fileTrgtKey : PM0101M01_M 으로 저장함.  fileTrgtKey=PM0101M01은 일반 첨부 파일임
	    		    String tripfileTrgtTyp = "PM0101M01_M";
	    		    //세부내역별 키값에 대한부분은 작업일보 번호 + 경비일련번호로 구성함
	    		    String tripfileTrgtKey = tripMap.get("workRptNo") +  "-" + tripMap.get("workRptNoSeq");
	    		    cm08Svc.uploadFile(tripfileTrgtTyp, tripfileTrgtKey, mRequest) ;
	        	}
	        }
        }
        
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
//		if (fileFlag) {
//			if (uploadFileList.size() > 0) {
//				paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
//				paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//				cm08Svc.uploadFile(paramMap, mRequest);
//			}
//		}
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
		
		//출장경비용 삭제
		List<Map<String, String>> deleteTripRptFileList = pm01Mapper.selectDailyWorkTripRows(paramMap); 
		if (deleteTripRptFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteTripRptFileList) {
		    	String fileKey = deleteDtl.get("fileKey").toString();
				//첨부파일 상세내역 연계자료 삭제 처리
			    cm08Svc.deleteFile( fileKey );

				//출장경비 상세 내역 삭제 처리
				result += pm01Mapper.deleteTripRpt(deleteDtl);
		    }
		}
		
	    return result;
  }

	@Override
	public List<Map<String, String>> selectMonthlyWorkList(Map<String, String> paramMap) {
	    return pm01Mapper.selectMonthlyWorkList(paramMap);
	}
	
	@Override
	public int selectWorkPrtCount(Map<String, String> paramMap) {
		return pm01Mapper.selectWorkPrtCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWorkPrtList(Map<String, String> paramMap) {
		return pm01Mapper.selectWorkPrtList(paramMap);
	}
	
	@Override
	public int selectWorkOrdrsCount(Map<String, String> paramMap) {
		return pm01Mapper.selectWorkOrdrsCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWorkOrdrsList(Map<String, String> paramMap) {
		return pm01Mapper.selectWorkOrdrsList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap) {
	  //return pm01Mapper.selectUploadFileList(paramMap);
		return cm08Svc.selectFileList(paramMap);
	//  Map<String, Object> returnMap = new HashMap<String, Object>();
	//	Map<String, String> fileMap = new HashMap<String, String>();
	//	fileMap.put("fileTrgtTyp", "TB_PM01M01");
	//	fileMap.put("userId", paramMap.get("userId"));
	//	fileMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	//	returnMap.put("fileList", cm08Svc.selectFileList(fileMap));
	//	returnMap.put("workInfo", pm01Mapper.selectDailyWorkInfo(paramMap));
	//	return returnMap; 
	}
	
	@Override
	public int selectIssueWorkCount(Map<String, String> paramMap) {
		return pm01Mapper.selectIssueWorkCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectIssueWorkList(Map<String, String> paramMap) {
		return pm01Mapper.selectIssueWorkList(paramMap);
	}
	
	@Override
	public int selectAllIssueWorkListCount(Map<String, String> paramMap) {
		return pm01Mapper.selectAllIssueWorkListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectAllIssueWorkList(Map<String, String> paramMap) {
		return pm01Mapper.selectAllIssueWorkList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDailyWorkPrductList(Map<String, String> paramMap) {
		return pm01Mapper.selectDailyWorkPrductList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectDailyWorkTripRows(Map<String, String> paramMap) {
		return pm01Mapper.selectDailyWorkTripRows(paramMap);
	}
  
}