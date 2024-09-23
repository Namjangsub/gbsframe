package com.dksys.biz.admin.cm.cm16.service.impl;

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
import com.dksys.biz.admin.cm.cm16.mapper.CM16Mapper;
import com.dksys.biz.admin.cm.cm16.service.CM16Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM16SvcImpl implements CM16Svc {

    @Autowired
    CM16Mapper cm16Mapper;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;
    
    @Override
    public int selectItoaIssueListCount(Map<String, String> paramMap) {
        return cm16Mapper.selectItoaIssueListCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectItoaIssueList(Map<String, String> paramMap) {
        return cm16Mapper.selectItoaIssueList(paramMap);
    }

    @Override
    public Map<String, String> selectItoaIssueInfo(Map<String, String> paramMap) {
    return cm16Mapper.selectItoaIssueInfo(paramMap);
    }

    @Override
    public int selectConfirmCount(Map<String, String> paramMap) {
        return cm16Mapper.selectConfirmCount(paramMap);
    }

    @Override
    public int itoaInsertIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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


        String fileTrgtKey = cm16Mapper.selectItoaIssueSeqNext(paramMap);
        paramMap.put("fileTrgtKey", fileTrgtKey);

        //문제현황 등록
        int result = cm16Mapper.itoaInsertIssue(paramMap);

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
    public int itoaUpdateIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

        int result = cm16Mapper.itoaUpdateIssue(paramMap);

        // 파일이 존재할 때만 파일 업로드 로직 실행
        if (mRequest != null && mRequest.getFileNames().hasNext()) {
            // 첨부파일 처리
            cm08Svc.uploadFile("CM1601M01", paramMap.get("fileTrgtKey"), mRequest);
        }

        // 삭제할 파일이 있을 때 삭제 처리
        Gson gson = new Gson();
        String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);
        if (deleteFileList != null && !deleteFileList.isEmpty()) {
            for (String fileKey : deleteFileList) {
                if (fileKey != null && !fileKey.isEmpty()) {
                    cm08Svc.deleteFile(fileKey);
                }
            }
        }

        return result;
    }
    
    @Override
    public int itoaDeleteIssue(Map<String, String> paramMap) throws Exception {
        //---------------------------------------------------------------  
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
        List<Map<String, String>> deleteFileList = cm08Svc.selectFileList(paramMap);
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

        int result = cm16Mapper.itoaDeleteIssue(paramMap);

        //---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------
        if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey");
		        if (fileKey != null && !fileKey.isEmpty()) {	//경비내역이 있으면 처리함.
		        	cm08Svc.deleteFile( fileKey );
		        }
		    }
		}
        //---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  

        return result;
    }

    @Override
    public List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap) {
        
        return cm08Svc.selectFileList(paramMap);
    }
}
