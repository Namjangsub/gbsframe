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
    public int selectConfirmCount(Map<String, String> paramMap) {
        return cm16Mapper.selectConfirmCount(paramMap);
    }

    @Override
    public int insertItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

        // Gson: JSON 데이터를 파싱하거나 생성하기 위한 라이브러리
        // disableHtmlEscaping(): HTML 문자를 이스케이프하지 않도록 설정
        // -TypeToken : 제너릭 타입의 정보를 유지하기 위해 사용
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

        String fileTrgtKey = cm16Mapper.selectItoaIssueSeqNext(paramMap);
        paramMap.put("fileTrgtKey", fileTrgtKey);

        //문제현황 등록
        int result = cm16Mapper.insertItoaIssue(paramMap);

        // 첨부파일 처리
        cm08Svc.uploadFile("PM1601M01", paramMap.get("fileTrgtKey"), mRequest);

        return result;
    }

    @Override
    public int updateItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

        int result = cm16Mapper.deleteItoaIssue(paramMap);

        // 첨부파일 처리
        cm08Svc.uploadFile("PM1601M01", paramMap.get("fileTrgtKey"), mRequest);
	    Gson gson = new Gson();
	    String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
	    List<String> deleteFileList = Arrays.asList(deleteFileArr);
        if (deleteFileList != null && !deleteFileList.isEmpty()) {
		    for(String fileKey : deleteFileList) {
	            if (fileKey != null && !fileKey.isEmpty()) {
	        	    cm08Svc.deleteFile(fileKey);
	            }
		    }
        }
        return result;
    }
    
    @Override
    public int deleteItoaIssue(Map<String, String> paramMap) throws Exception {
        
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

        int result = cm16Mapper.deleteItoaIssue(paramMap);

        if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey");
		        if (fileKey != null && !fileKey.isEmpty()) {	//경비내역이 있으면 처리함.
		        	cm08Svc.deleteFile( fileKey );
		        }
		    }
		}
        return result;
    }
}
