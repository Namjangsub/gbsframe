package com.dksys.biz.user.cr.cr05.service.impl;

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
import com.dksys.biz.user.cr.cr05.mapper.CR05Mapper;
import com.dksys.biz.user.cr.cr05.service.CR05Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR05SvcImpl implements CR05Svc {
	@Autowired
	CR05Mapper cr05Mapper;
	
	@Autowired
	CR05Svc cr05Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;
	
	
	@Override
	public int selectClmnListCount(Map<String, String> paramMap) {
		return cr05Mapper.selectClmnListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectClmnList(Map<String, String> paramMap) {
		return cr05Mapper.selectClmnList(paramMap);
	}
	
	@Override
	public Map<String, String> select_cr05_Info(Map<String, String> paramMap) {
		return cr05Mapper.select_cr05_Info(paramMap);
	}
	
	@Override
	public List<Map<String, String>> select_cr05_Info_Dtl(Map<String, String> paramMap) {
		return cr05Mapper.select_cr05_Info_Dtl(paramMap);
	}
	
	@Override
	public List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap) {
		return cr05Mapper.select_insert_target_modal(paramMap);
	}
	
	
	@Override
	public List<Map<String, String>> select_cr05_clmnNo(Map<String, String> paramMap) {
		return cr05Mapper.select_cr05_clmnNo(paramMap);
	}
	
	//DATA INSERT
	@Override
	public int insert_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		
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
		
		//데이터 처리 시작
		int fileTrgtKey = cr05Mapper.select_cr05_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		//마스터입력
		int result = cr05Mapper.insert_cr05(paramMap);

		//상세입력 ---------수정 중 
		
		//데이터 처리 끝

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
	
	//DATA UPDATE
	@Override
	public int update_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		//Gson gson = new Gson();
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
		
		for(String fileKey : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			//접근 권한 없으면 Exception 발생
			param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------
		
		//데이터처리 시작
		//마스터 수정
		int result = cr05Mapper.update_cr05(paramMap, mRequest);
		
		//상세수정  ---------수정 중 
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dtaChk = dtl.get("dtaChk").toString();	    	
			//"dtaChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("U".equals(dtaChk)) {
				//데이터 처리
				cr05Mapper.update_cr05_Dtl(dtl);
	    	} 
	    }
	    //데이터 처리 끝
		
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
	public int delete_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequset) throws Exception {
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
		
		int result = 0;
		
		String pid = paramMap.get("pid").toString();		
		
		if ("top".equals(pid)) {
			//데이터 처리
			result = cr05Mapper.delete_cr05_Dtl_All(paramMap);
			result = cr05Mapper.delete_cr05(paramMap);
    	} else {
    		result = cr05Mapper.delete_cr05_Dtl(paramMap);
    	}
		
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
	
	
}
