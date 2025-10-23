package com.dksys.biz.user.sm.sm05.service.impl;

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
import com.dksys.biz.user.sm.sm05.mapper.SM05Mapper;
import com.dksys.biz.user.sm.sm05.service.SM05Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM05SvcImpl implements SM05Svc {
	
	@Autowired
	SM05Mapper sm05Mapper;
	
	@Autowired
	SM05Svc sm05Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	// 그리드 카운트
	@Override
	public int grid1_selectCount(Map<String, String> paramMap) {
		return sm05Mapper.grid1_selectCount(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
		return sm05Mapper.grid1_selectList(paramMap);
	}
	
	// 창고 코드 검색
	@Override
	public List<Map<String, Object>> selectWhCd(Map<String, String> paramMap) {
		return sm05Mapper.selectWhCd(paramMap);
	}

	// 팝업 재고 검색
	@Override
	public List<Map<String, String>> select_stock_modal(Map<String, String> paramMap) {
		return sm05Mapper.select_stock_modal(paramMap);
	}
	
	// 수정화면 정보
	@Override
	public Map<String, String> select_sm05_Info(Map<String, String> paramMap) {
		return sm05Mapper.select_sm05_Info(paramMap);
	}
	
	// 수정화면 상세정보
	@Override
	public List<Map<String, String>> select_sm05_Info_Dtl(Map<String, String> paramMap) {
		return sm05Mapper.select_sm05_Info_Dtl(paramMap);
	}
	
	//DATA INSERT
	@Override
	public int insert_sm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int fileTrgtKey = sm05Mapper.select_sm05_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		String newMNGM_NO = sm05Mapper.select_sm05_Next_MNGM_NO(paramMap);
		paramMap.put("ioNo", newMNGM_NO);
		
		//마스터입력
		int result = sm05Mapper.insert_sm05(paramMap);

		//상세입력
		int i = 1;
		int outInoutKey = 0;

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	dtl.put("ioNo", newMNGM_NO);
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("I".equals(dataChk)) {
				outInoutKey = sm05Mapper.select_sm05_Ioseq(paramMap);
				dtl.put("outInoutKey", Integer.toString(outInoutKey));
	    		dtl.put("ioSeq", Integer.toString(i));

				//데이터 처리
				sm05Mapper.insert_sm05_Dtl(dtl);
				i++;
	    	}
	    }
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
	public int update_sm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		//Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		//---------------------------------------------------------------
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("coCd", paramMap.get("coCd"));
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
		int result = sm05Mapper.update_sm05(paramMap);

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("U".equals(dataChk)) {
				//데이터 처리
				sm05Mapper.update_sm05_Dtl(dtl);
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
	
	//DATA DELETE
	@Override
	public int delete_sm05(Map<String, String> paramMap) throws Exception {
		//---------------------------------------------------------------
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
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
		
		int result = 0;
		
		String lvl = paramMap.get("lvl").toString();		
		
		if ("1".equals(lvl)) {
			//데이터 처리
			result = sm05Mapper.delete_sm05_Dtl_All(paramMap);
			result = sm05Mapper.delete_sm05(paramMap);
    	} else {
    		result = sm05Mapper.delete_sm05_Dtl(paramMap);

    		//마스터 아래 디테일 없으면
    		int chk = sm05Mapper.check_sm05_Master(paramMap);
    		if(chk == 0) {
    			result = sm05Mapper.delete_sm05(paramMap);	
    		}
    	}
		
		//int result = sm05Mapper.delete_sm05(paramMap);
		
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
