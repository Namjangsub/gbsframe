package com.dksys.biz.admin.bm.bm99.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.bm.bm99.mapper.BM99Mapper;
import com.dksys.biz.admin.bm.bm99.service.BM99Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM99SvcImpl implements BM99Svc {
	
    @Autowired
    BM99Mapper bm99Mapper;
    
    @Autowired
    CM08Svc cm08Svc;
    
	@Autowired
	CM15Svc cm15Svc;

	@Override
	public Map<String, Object> selectManualInfo(Map<String, String> paramMap) {
		// 조회 시 기존에 없으면 만듬
		int manualCnt = bm99Mapper.selectManualCount(paramMap);
		if(manualCnt < 1) {
			bm99Mapper.insertManual(paramMap);
		}
		
		return bm99Mapper.selectManualInfo(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectManualFileInfo(Map<String, String> paramMap) {

		Map<String, String> fileMap = new HashMap<String, String>();
		
		fileMap.putAll(paramMap);
		//"FITR9999"은 공통코드에서 공지사항 첨부 디렉토리임
		fileMap.put("comonCd", 		"FITR9999");
		fileMap.put("fileTrgtTyp", 	"TB_BM99P01");
		fileMap.put("coCd", 		"GUN");
		fileMap.put("fileTrgtKey", 	paramMap.get("fileTrgtKey"));
		fileMap.put("jobType", 		"fileList");
		
		return cm08Svc.selectFileList(fileMap);
	}
	
	@Override
	public void updateManualInfo(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		
		// 거래처 update
		bm99Mapper.updateManual(paramMap);
		
	    List<MultipartFile> fileList = mRequest.getFiles("files");
		if (fileList.size() > 0) {
			//"FITR9999"은 공통코드에서 도움말 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
			paramMap.put("comonCd", "FITR9999");
			paramMap.put("coCd", 	"GUN");
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		
		paramMap.put("fileTrgtTyp", "TB_BM99P01");
		paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		cm08Svc.uploadFile(paramMap, mRequest);
		
		Gson gson = new Gson();
		String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
		// deleteFileArr가 null이 아닌 경우의 동작
		if (deleteFileArr != null) {
			List<String> deleteFileList = Arrays.asList(deleteFileArr);
			
			if (deleteFileList.size() > 0) {
				//"FITR9901"은 공통코드에서 공지사항 첨부 디렉토리임
				//접근 권한이 없으면 Exception 발생
				paramMap.put("comonCd", "FITR9999");
				paramMap.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(paramMap);
			}
			
			for(String fileKey : deleteFileList) {
				cm08Svc.deleteFile(fileKey);
			}
		}
		
	}

}