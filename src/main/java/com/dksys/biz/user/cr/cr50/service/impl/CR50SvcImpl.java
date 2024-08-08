package com.dksys.biz.user.cr.cr50.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr50.mapper.CR50Mapper;
import com.dksys.biz.user.cr.cr50.service.CR50Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR50SvcImpl implements CR50Svc {

	@Autowired
	CR50Mapper cr50Mapper;

	@Autowired
	CM15Svc cm15Svc;
	  
	@Autowired
	CM08Svc cm08Svc;
	
	@Override
	public List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap) {
		
		return cr50Mapper.selectPFUAreaItemList(paramMap);
	}
	
	
	@Override
	public Map<String, String> selectPfuInfo(Map<String, String> paramMap) {
	    return cr50Mapper.selectPfuInfo(paramMap);
	}
	
	
//	@Override
//	public Map<String, String> selectPfuClobInfo(Map<String, String> paramMap) {
//	    return cr50Mapper.selectPfuClobInfo(paramMap);
//	}
	
	@Override
	public int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
//		String  newPfuSeq = String.valueOf(cr50Mapper.selectPfuSeqNext(paramMap));
//		paramMap.put("fileTrgtKey", newPfuSeq);
		int result = cr50Mapper.insertPfu(paramMap);
		String  newPfuSeq = paramMap.get("fileTrgtKey");
		
//		List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
//		for (Map<String, String> dtl : dtlPrdt) {
//			dtl.put("fileTrgtKey", newPfuSeq);
//			dtl.put("userId", paramMap.get("userId"));
//			dtl.put("pgmId", paramMap.get("pgmId"));
//			//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
//			cr50Mapper.insertPfuPrdt(dtl);
//		}
//		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("prdtItemArr"), dtlMap);
//		for (Map<String, String> dtl : dtlParam) {
//			dtl.put("prjctSeq", newPfuSeq);
//			dtl.put("userId", paramMap.get("userId"));
//			dtl.put("pgmId", paramMap.get("pgmId"));
//			//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
//			String dtaChk = dtl.get("dtaChk").toString();
//			/* "dtaChk" 값을 확인하여
//			 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
//			if ("I".equals(dtaChk)) {
//				cr50Mapper.insertPfuDtl(dtl);
//			} 
//		}
		
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
	
}
