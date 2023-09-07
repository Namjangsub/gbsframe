package com.dksys.biz.user.qm.qm02.service.impl;

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

import com.dksys.biz.user.qm.qm02.mapper.QM02Mapper;
import com.dksys.biz.user.qm.qm02.service.QM02Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM02SvcImpl implements QM02Svc {
	
	@Autowired
	QM02Mapper qm02Mapper;
	
	@Autowired
	QM02Svc qm02Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	// 그리드 카운트
	@Override
	public int select_grid_Count(Map<String, String> paramMap) {
		return qm02Mapper.select_grid_Count(paramMap);
	}
	
	// 그리드 카운트
	@Override
	public int select_gochal_count(Map<String, String> paramMap) {
		return qm02Mapper.select_gochal_count(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> selectMainGridList(Map<String, String> paramMap) {
		return qm02Mapper.selectMainGridList(paramMap);
	}

	//그리드 검색
	@Override
	public List<Map<String, String>> select_stock_modal(Map<String, String> paramMap) {
		String roleStr = "";
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String[] roleArray = paramMap.get("cobtp").split(",");
		String test = "";
		for (int i = 0; i < roleArray.length; i++) {
			if ((roleArray.length -1) == i ) {
				test += "'" + roleArray[i] +"' AS COBTP"+ (i+1);
			}
			else {
				test += "'" + roleArray[i] +"' AS COBTP"+ (i+1) +  ",";
			}
		}

		//map.put("userName", Arrays.toString(roleArray));
		map.put("test", test);
		map.put("userName", roleArray);
		map.put("statym", paramMap.get("statym"));
		map.put("actiontype", paramMap.get("actionType"));
		map.put("userId", paramMap.get("userId"));
		map.put("coCd", paramMap.get("coCd"));
		
		return qm02Mapper.select_stock_modal(map);
	}
	
	//그리드 검색
	@Override
	public List<Map<String, String>> select_soojung_modal(Map<String, String> paramMap) {
		return qm02Mapper.select_soojung_modal(paramMap);
	}
	
	//그리드 검색
	@Override
	public List<Map<String, String>> select_cobtp_modal(Map<String, String> paramMap) {
		return qm02Mapper.select_cobtp_modal(paramMap);
	}
	
	//그리드 검색
	@Override
	public List<Map<String, String>> select_all_modal(Map<String, String> paramMap) {
		return qm02Mapper.select_all_modal(paramMap);
	}
	
	//그리드 검색
	@Override
	public List<Map<String, Object>> select_zupiter_modal(Map<String, String> paramMap) {
		String roleStr = "";
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String[] roleArray = paramMap.get("userName").split(",");
		String test = "";
		for (int i = 0; i < roleArray.length; i++) {
			if ((roleArray.length -1) == i ) {
				test += "'" + roleArray[i] +"' AS user"+ (i+1);
			}
			else {
				test += "'" + roleArray[i] +"' AS user"+ (i+1) +  ",";
			}
		}

		//map.put("userName", Arrays.toString(roleArray));
		map.put("test", test);
		map.put("userName", roleArray);
		map.put("userId", paramMap.get("userId"));
		map.put("coCd", paramMap.get("coCd"));
		map.put("strDate", paramMap.get("strDate"));
		map.put("endDate",paramMap.get("endDate"));
		map.put("statyy",paramMap.get("statyy"));

		return qm02Mapper.select_zupiter_modal(map);
	}
	
	// 수정화면 정보
	@Override
	public Map<String, String> select_qm02_Info(Map<String, String> paramMap) {
		return qm02Mapper.select_qm02_Info(paramMap);
	}
	
	//DATA INSERT
	@Override
	public int insert_qm02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = 0;
		//해당월의 고찰데이터가 있는지 없는지 확인
		int gochalIn = qm02Mapper.select_gochal_count(paramMap);
				
		if (gochalIn == 0) {
			int fileTrgtKey = qm02Mapper.select_qm02_SeqNext(paramMap);
			paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

			String newMNGM_NO = qm02Mapper.select_qm02_Next_MNGM_NO(paramMap);
			paramMap.put("resNo", newMNGM_NO);
			
			//마스터입력
			result = qm02Mapper.insert_qm02(paramMap);
			if (result > 0) {
				qm02Mapper.update_statyy_qm01(paramMap);
			}
		}
		else if (gochalIn == 1) {
			result = 7;
		}
		

		//상세입력
//		int i = 1;
//		int outInoutKey = 0;

//		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
//	    for (Map<String, String> dtl : dtlParam) {
//	    	dtl.put("ioNo", newMNGM_NO);
//	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
//			dtl.put("userId", paramMap.get("userId"));
//	    	dtl.put("pgmId", paramMap.get("pgmId"));
//			
//			String dataChk = dtl.get("dataChk").toString();	    	
//			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
//	    	if ("I".equals(dataChk)) {
//				outInoutKey = qm02Mapper.select_pm02_Ioseq(paramMap);
//				dtl.put("outInoutKey", Integer.toString(outInoutKey));
//	    		dtl.put("ioSeq", Integer.toString(i));
//
//				//데이터 처리
//				qm02Mapper.insert_pm02_Dtl(dtl);
//				i++;
//	    	}
//	    }
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
	public int update_qm02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = qm02Mapper.update_qm02(paramMap);

//		//상세수정
//		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
//	    for (Map<String, String> dtl : dtlParam) {
//	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
//			dtl.put("userId", paramMap.get("userId"));
//	    	dtl.put("pgmId", paramMap.get("pgmId"));
//			
//			String dataChk = dtl.get("dataChk").toString();	    	
//			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
//	    	if ("U".equals(dataChk)) {
//				//데이터 처리
//				qm02Mapper.update_pm02_Dtl(dtl);
//	    	} 
//	    }
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
	public int delete_qm02(Map<String, String> paramMap) throws Exception {
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
		int sop_result = 0;
		if (paramMap.get("resDiv").equals("개인")) {
			sop_result = qm02Mapper.selec_delete_result_count(paramMap);
		}
		
		if (sop_result == 0) {
			result = qm02Mapper.delete_qm02(paramMap);
			if (result == 1) {
				 if (paramMap.get("resDiv").equals("개인")) {
					 qm02Mapper.update_delete_qm01(paramMap);
				 }
			}
		}
		else {
			result = 7;
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
	
	//DATA INSERT
		@Override
		public int insert_qm02_p02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
			int result = 0;
			//해당월의 고찰데이터가 있는지 없는지 확인
			int resultIn = qm02Mapper.select_result_count(paramMap);
					
			if (resultIn == 0) {
				int fileTrgtKey = qm02Mapper.select_qm02_SeqNext(paramMap);
				paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

				String newMNGM_NO = qm02Mapper.select_qm02_Next_MNGM_NO(paramMap);
				paramMap.put("resNo", newMNGM_NO);
				
				//마스터입력
				result = qm02Mapper.insert_qm02(paramMap);
			}
			else if (resultIn == 1) {
				result = 7;
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
		public int update_qm02_p02(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
			int result = qm02Mapper.update_qm02(paramMap);

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
}
