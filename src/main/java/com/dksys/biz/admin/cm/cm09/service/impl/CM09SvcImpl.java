package com.dksys.biz.admin.cm.cm09.service.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm09.mapper.CM09Mapper;
import com.dksys.biz.admin.cm.cm09.service.CM09Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;

@Service
public class CM09SvcImpl implements CM09Svc {
	
    @Autowired
    CM09Mapper cm09Mapper;
    
    @Autowired
    CM08Svc cm08Svc;
    
    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    ExceptionThrower thrower;

	@Override
	public List<Map<String, String>> selectNotiList(Map<String, String> paramMap) {
		return cm09Mapper.selectNotiList(paramMap);
	}

	@Override
	public int selectNotiCount(Map<String, String> paramMap) {
		return cm09Mapper.selectNotiCount(paramMap);
	}

	@Override
	public int insertNoti(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result;
		
		result = cm09Mapper.insertNoti(paramMap);
		List<MultipartFile> fileList = mRequest.getFiles("files");
	    if (fileList.size() > 0) {
			//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
	    	paramMap.put("fileTrgtTyp", "TB_CM09M01");
	    	paramMap.put("fileTrgtKey", paramMap.get("notiKey"));
	    	paramMap.put("comonCd", "FITR9901");
	    	paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
//			cm08Svc.uploadFile("TB_CM09M01", paramMap.get("notiKey"), mRequest);
			cm08Svc.uploadFile(paramMap, mRequest);
	    }
	    
		return result;
	}

	@Override
	public Map<String, Object> selectNotiInfo(Map<String, String> paramMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, String> fileMap = new HashMap<String, String>();
		fileMap.putAll(paramMap);
		//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
		fileMap.put("comonCd", "FITR9901");
		fileMap.put("fileTrgtTyp", "TB_CM09M01");
		fileMap.put("fileTrgtKey", paramMap.get("notiKey"));
		fileMap.put("jobType", "fileList");
		returnMap.put("fileList", cm08Svc.selectFileList(fileMap));
		returnMap.put("notiInfo", cm09Mapper.selectNotiInfo(paramMap));
		return returnMap; 
	}

	@Override
	public int updateNoti(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result;
		
		List<MultipartFile> fileList = mRequest.getFiles("files");
		if (fileList.size() > 0) {
			//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
			paramMap.put("comonCd", "FITR9901");
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		
		Gson gson = new Gson();
		String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
		
		if (deleteFileList.size() > 0) {
			//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
			paramMap.put("comonCd", "FITR9901");
			paramMap.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(paramMap);
		}

		result = cm09Mapper.updateNoti(paramMap);
 
		paramMap.put("fileTrgtTyp", "TB_CM09M01");
		paramMap.put("fileTrgtKey", paramMap.get("notiKey"));
		cm08Svc.uploadFile(paramMap, mRequest);

		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}

		return result;
	}

	@Override
	public int uploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		List<MultipartFile> fileList = mRequest.getFiles("files");
	    if (fileList.size() > 0) {
			//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
			paramMap.put("comonCd", "FITR9901");
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
			
	    	paramMap.put("fileTrgtTyp", "TB_CM09M01");
	    	paramMap.put("fileTrgtKey", paramMap.get("notiKey"));
	 
	    	cm08Svc.uploadFile(paramMap, mRequest);
	    }
		return 1;
	}

	@Override
	public List<String> selectNotiPopList() {
//		List<String> keyList = cm09Mapper.selectNotiPopList();
//		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
//		for (int i = 0; i < keyList.size(); i++) {
//			Map<String, String> param = new HashMap<String, String>();
//			Map<String, Object> returnMap = new HashMap<String, Object>();
//			param.put("notiKey", keyList.get(i));
//			returnMap.put("fileList", cm08Svc.selectFileList(keyList.get(i)));
//			returnMap.put("notiInfo", cm09Mapper.selectNotiInfo(param));
//			resultList.add(returnMap);
//		}
		return cm09Mapper.selectNotiPopList();
	}

}