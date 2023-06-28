package com.dksys.biz.user.cr.cr08.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr08.mapper.CR08Mapper;
import com.dksys.biz.user.cr.cr08.service.CR08Svc;
//import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR08SvcImpl implements CR08Svc{
	
	  @Autowired
	  CR08Mapper CR08Mapper;

	  @Autowired
	  CM15Svc cm15Svc;
	  
	  @Autowired
	  CM08Svc cm08Svc;
	  
	  @Override
	  public int selectPchsCostCount(Map<String, String> paramMap) {
	    return CR08Mapper.selectPchsCostCount(paramMap);
	  }
	  
	  @Override
	  public int selectSalesStmtConCount(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtConCount(paramMap);
	  }
	  
	  
	  @Override
	  public int selectSalesStmtCalCount(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtCalCount(paramMap);
	  }
	  

	  @Override
	  public List<Map<String, String>> selectSalesStmtList(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtList(paramMap);
	  }
	  
	  @Override
	  public List<Map<String, String>> selectSalesStmtCalList(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtCalList(paramMap);
	  }
	  
	  @Override
	  public List<Map<String, String>> selectSalesStmtConList(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtConList(paramMap);
	  }

	  @Override
	  public Map<String, String> selectPchsCostInfo(Map<String, String> paramMap) {
	    return CR08Mapper.selectPchsCostInfo(paramMap);
	  }
	  
	  @Override
	  public Map<String, String> selectSalesStmtInfo(Map<String, String> paramMap) {
	    return CR08Mapper.selectSalesStmtInfo(paramMap);
	  }
	  

	  @Override
	  public int selectConfirmCount(Map<String, String> paramMap) {
	    return CR08Mapper.selectConfirmCount(paramMap);
	  }

	  @Override
	  public int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//		Gson gson = new Gson();
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

		int result = CR08Mapper.updatePchsCost(paramMap);

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
	  public int updateSalesStmt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//		Gson gson = new Gson();
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

		int result = CR08Mapper.updateSalesStmt(paramMap);

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
	  public int insertSalesStmt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		    
			System.out.println("insertSalesStmt_Svcimpl");
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
			int fileTrgtKey = CR08Mapper.selectSalesStmtSeqNext(paramMap);
			String sellDcsnNo = "CONF23-0003" ; //SELL_DCSN_NO//test code!!!
			
			paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
			paramMap.put("sellDcsnNo", sellDcsnNo);
			
			int result = CR08Mapper.insertSalesStmt(paramMap);
			
			System.out.println("fileTrgtKey===?"+fileTrgtKey);
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
	  public int deletePchsCost(Map<String, String> paramMap) throws Exception {
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
		  
		  int result = CR08Mapper.deletePchsCost(paramMap);
		  
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
	  public List<Map<String, String>> selectSalesStmtExcelList(Map<String, String> paramMap) {
			return CR08Mapper.selectSalesStmtExcelList(paramMap);
	  }
	  
	  @Override
	  public List<Map<String, String>> selectSalesStmtCalExcelList(Map<String, String> paramMap) {
			return CR08Mapper.selectSalesStmtCalExcelList(paramMap);
	  }

}
