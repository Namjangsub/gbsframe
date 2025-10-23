package com.dksys.biz.admin.cm.cm14.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm14.mapper.CM14Mapper;
import com.dksys.biz.admin.cm.cm14.service.CM14Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;

@Service
public class CM14SvcImpl implements CM14Svc {
	
    @Autowired
    CM14Mapper cm14Mapper;
    
    @Autowired
    CM08Svc cm08Svc;
    
    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    ExceptionThrower thrower;

	@Override
	public List<Map<String, String>> selectBoardList(Map<String, String> paramMap) {
		return cm14Mapper.selectBoardList(paramMap);
	}

	@Override
	public int selectBoardCount(Map<String, String> paramMap) {
		return cm14Mapper.selectBoardCount(paramMap);
	}

	@Override
	public int insertBoard(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result;
		
		result = cm14Mapper.insertBoard(paramMap);
		List<MultipartFile> fileList = mRequest.getFiles("files");
	    if (fileList.size() > 0) {
            // "FITR9903"은 공통코드에서 자료실 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
	    	paramMap.put("fileTrgtTyp", "TB_CM14M01");
	    	paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            paramMap.put("comonCd", "FITR9903");
	    	paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
//			cm08Svc.uploadFile("TB_CM14M01", paramMap.get("fileTrgtKey"), mRequest);
			cm08Svc.uploadFile(paramMap, mRequest);
	    }
	    
		return result;
	}

	@Override
	public Map<String, Object> selectBoardInfo(Map<String, String> paramMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, String> fileMap = new HashMap<String, String>();
		fileMap.putAll(paramMap);
        // "FITR9903"은 공통코드에서 자료실 첨부 디렉토리임
        fileMap.put("comonCd", "FITR9903");
		fileMap.put("fileTrgtTyp", "TB_CM14M01");
		fileMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		fileMap.put("jobType", "fileList");
		returnMap.put("fileList", cm08Svc.selectFileList(fileMap));
		returnMap.put("boardInfo", cm14Mapper.selectBoardInfo(paramMap));
		return returnMap; 
	}

	@Override
	public int updateBoard(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result;
		
		List<MultipartFile> fileList = mRequest.getFiles("files");
		if (fileList.size() > 0) {
            // "FITR9901"은 공통코드에서 자료실 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
            paramMap.put("comonCd", "FITR9903");
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		
		Gson gson = new Gson();
		String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
		
		if (deleteFileList.size() > 0) {
            // "FITR9901"은 공통코드에서 자료실 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
            paramMap.put("comonCd", "FITR9903");
			paramMap.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(paramMap);
		}

		result = cm14Mapper.updateBoard(paramMap);
 
		paramMap.put("fileTrgtTyp", "TB_CM14M01");
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
            // "FITR9901"은 공통코드에서 자료실 첨부 디렉토리임
	    	//접근 권한이 없으면 Exception 발생
            paramMap.put("comonCd", "FITR9903");
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
			
	    	paramMap.put("fileTrgtTyp", "TB_CM14M01");
	 
	    	cm08Svc.uploadFile(paramMap, mRequest);
	    }
		return 1;
	}

	@Override
	public List<String> selectBoardPopList() {
//		List<String> keyList = cm14Mapper.selectBoardPopList();
//		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
//		for (int i = 0; i < keyList.size(); i++) {
//			Map<String, String> param = new HashMap<String, String>();
//			Map<String, Object> returnMap = new HashMap<String, Object>();
//			param.put("fileTrgtKey", fileTrgtKey.get(i));
//			returnMap.put("fileList", cm08Svc.selectFileList(keyList.get(i)));
//			returnMap.put("BoardInfo", cm14Mapper.selectBoardInfo(param));
//			resultList.add(returnMap);
//		}
		return cm14Mapper.selectBoardPopList();
	}


	  @Override
	  public int deleteBoard(Map<String, String> paramMap) throws Exception {
	    	//---------------------------------------------------------------
			//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
			//   필수값 :  jobType, userId, comonCd
			//---------------------------------------------------------------
		    paramMap.put("fileTrgtTyp", "TB_CM14M01");
			List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
			HashMap<String, String> param = new HashMap<>();
			param.put("jobType", "fileDelete");
			param.put("coCd", paramMap.get("coCd"));
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
			
			
		  //자료실 레코드 삭제
		  int result = cm14Mapper.deleteBoard(paramMap);

	        
	        //---------------------------------------------------------------
			//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
			//---------------------------------------------------------------ma
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