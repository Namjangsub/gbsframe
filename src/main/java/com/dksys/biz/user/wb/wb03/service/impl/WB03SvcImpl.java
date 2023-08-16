package com.dksys.biz.user.wb.wb03.service.impl;

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

import com.dksys.biz.user.wb.wb03.mapper.WB03Mapper;
import com.dksys.biz.user.wb.wb03.service.WB03Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB03SvcImpl implements WB03Svc {

  @Autowired
  WB03Mapper wb03Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Override
  public List<Map<String, String>> selectWbsPlanTreeIssueList(Map<String, String> paramMap) {
    return wb03Mapper.selectWbsPlanTreeIssueList(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectWbsPlanTreeIssueExcelList(Map<String, String> paramMap) {
    return wb03Mapper.selectWbsPlanTreeIssueExcelList(paramMap);
  }
  

  @Override
  public List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap) {
		return wb03Mapper.selectMaxWbsIssueNo(paramMap);
  }
  
  @Override
  public int insertWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb03Mapper.selectWbsPlanIssueSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
		Gson gson = new Gson();
		
	    String pgParam = "{\"actionType\":\""+ "T" +"\",";
	    pgParam += "\"wbsPlanFileTrgtKey\":\""+ paramMap.get("wbsPlanFileTrgtKey") +"\","; 
	    pgParam += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
	    pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
	    pgParam += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
	    pgParam += "\"idx\":\""+ paramMap.get("idx") +"\",";
	    pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
	    pgParam += "\"issueIdx\":\""+ paramMap.get("issueIdx") +"\",";
	    pgParam += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
	    pgParam += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";
	    
	    
		// 공유테이블 등록  
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
		            	sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		            	sharngMap.put("todoDiv1CodeId", paramMap.get("S_todoDiv1CodeId"));
		            	sharngMap.put("todoDiv2CodeId", paramMap.get("S_todoDiv2CodeId"));	
		            	sharngMap.put("creatId", paramMap.get("creatId"));	   
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));
		            	sharngMap.put("sanctnSn", Integer.toString(i+1)); 
		            	sharngMap.put("pgParam", pgParam);
	            	    wb03Mapper.insertIssueSharngList(sharngMap);
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
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

	    int result = wb03Mapper.insertWbsPlanIssue(paramMap);	
	    return result;
  }
  
  @Override
  public List<Map<String, String>> deleteIssueSharngListChk(Map<String, String> paramMap) {
		return wb03Mapper.deleteIssueSharngListChk(paramMap);
  }
  
  @Override
  public int deleteIssueSharngList(Map<String, String> paramMap) {
		int result = wb03Mapper.deleteIssueSharngList(paramMap);
		return result;
  }
  
  @Override
  public int updateWbsPlanIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
	
	    Gson gson = new Gson();
	    
	    String pgParam = "{\"actionType\":\""+ "T" +"\",";
	    pgParam += "\"wbsPlanFileTrgtKey\":\""+ paramMap.get("wbsPlanFileTrgtKey") +"\","; 
	    pgParam += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
	    pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
	    pgParam += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
	    pgParam += "\"idx\":\""+ paramMap.get("idx") +"\",";
	    pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
	    pgParam += "\"issueIdx\":\""+ paramMap.get("issueIdx") +"\",";
	    pgParam += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
	    pgParam += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";
	    
	    
		// 공유테이블 등록  
	    Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
	            	sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	            	sharngMap.put("todoDiv1CodeId", paramMap.get("S_todoDiv1CodeId"));
	            	sharngMap.put("todoDiv2CodeId", paramMap.get("S_todoDiv2CodeId"));	
	            	sharngMap.put("creatId", paramMap.get("creatId"));	   
	            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));
	            	sharngMap.put("sanctnSn", Integer.toString(i+1)); 
	            	sharngMap.put("pgParam", pgParam);
            	    wb03Mapper.insertIssueSharngList(sharngMap);
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
	    
		int result = wb03Mapper.updateWbsPlanIssue(paramMap);
	
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
  public int deleteWbsPlanIssue(Map<String, String> paramMap) {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {                                    
	            	 wb03Mapper.deleteIssueSharngList(arrMap);
	            	 wb03Mapper.deleteWbsPlanIssue(arrMap);
	            	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
  }
  
  @Override
  public List<Map<String, String>> selectIssueSharngList(Map<String, String> paramMap) {
		return wb03Mapper.selectIssueSharngList(paramMap);
  }
}