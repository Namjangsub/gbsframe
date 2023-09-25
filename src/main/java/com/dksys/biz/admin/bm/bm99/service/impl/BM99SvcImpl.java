package com.dksys.biz.admin.bm.bm99.service.impl;

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

import com.dksys.biz.admin.bm.bm99.mapper.BM99Mapper;
import com.dksys.biz.admin.bm.bm99.service.BM99Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	public void updateManualInfo(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
 		Gson gson = new Gson();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
		
		// 거래처 update
		bm99Mapper.updateManual(param);
		
        //---------------------------------------------------------------
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put("userId", param.get("userId"));
		paramMap.put("comonCd", param.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
		paramMap.put("uploadFileArr", param.get("uploadFileArr"));
		
		List<Map<String, String>> uploadFileList = gson.fromJson(paramMap.get("uploadFileArr"), mapList);
		if (uploadFileList.size() > 0) {
			//접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		
		String[] deleteFileArr = gson.fromJson(param.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
		
		for(String fileKey : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			//접근 권한 없으면 Exception 발생
			paramMap.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			paramMap.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------
		
        //---------------------------------------------------------------
		//첨부 화일 처리 시작
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", param.get("pgmId"));
			paramMap.put("fileTrgtKey", param.get("fileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		//---------------------------------------------------------------
		//첨부 화일 처리  끝
		//---------------------------------------------------------------
		
		
	}

}