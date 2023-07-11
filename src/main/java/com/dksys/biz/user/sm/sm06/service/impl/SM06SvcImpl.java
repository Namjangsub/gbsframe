package com.dksys.biz.user.sm.sm06.service.impl;

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

import com.dksys.biz.user.sm.sm06.mapper.SM06Mapper;
import com.dksys.biz.user.sm.sm06.service.SM06Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM06SvcImpl implements SM06Svc {

	@Autowired
	SM06Mapper sm06Mapper;
	
	@Autowired
	SM06Svc sm06Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	// 그리드 카운트
	@Override
	public int grid1_selectCount(Map<String, String> paramMap) {
		return sm06Mapper.grid1_selectCount(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
		return sm06Mapper.grid1_selectList(paramMap);
	}
//	
//	// 창고 코드 검색
//	@Override
//	public List<Map<String, Object>> selectWhCd(Map<String, String> paramMap) {
//		return sm04Mapper.selectWhCd(paramMap);
//	}

	// 팝업 재고 검색
	@Override
	public List<Map<String, String>> select_whin_modal(Map<String, String> paramMap) {
		return sm06Mapper.select_whin_modal(paramMap);
	}
	
	// 수정화면 정보
	@Override
	public Map<String, String> select_sm06_Info(Map<String, String> paramMap) {
		return sm06Mapper.select_sm06_Info(paramMap);
	}
	
	// 수정화면 상세정보
	@Override
	public List<Map<String, String>> select_sm06_Info_Dtl(Map<String, String> paramMap) {
		return sm06Mapper.select_sm06_Info_Dtl(paramMap);
	}
	
	//DATA INSERT
	@Override
	public int insert_sm06(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		System.out.println(paramMap.get("returnSum")+": 해당위치");
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
		int fileTrgtKey = sm06Mapper.select_sm06_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		String newMNGM_NO = sm06Mapper.select_sm06_Next_MNGM_NO(paramMap);
		paramMap.put("retNo", newMNGM_NO);
		
		//상세입력
		int i = 1;
		int outInoutKey = 0;
		int sumReturnSum = 0;  // 'returnSum' 값들의 합계를 저장할 변수

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : dtlParam) {

		    dtl.put("ioNo", newMNGM_NO);
		    dtl.put("retNo", paramMap.get("retNo"));
		    
		    // 반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
		    dtl.put("userId", paramMap.get("userId"));
		    dtl.put("pgmId", paramMap.get("pgmId"));

		    String dtaChk = dtl.get("dtaChk").toString();        
		    //"dtaChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
		    if ("I".equals(dtaChk)) {
		        outInoutKey = sm06Mapper.select_sm06_Ioseq(paramMap);
		        //dtl.put("outInoutKey", Integer.toString(outInoutKey));
		        dtl.put("retSeq", Integer.toString(i));

		        //데이터 처리
		        sm06Mapper.insert_sm06_Dtl(dtl);

		        // dtl로부터 'returnSum' 값을 가져와 'paramMap'에 설정
		        sumReturnSum += Integer.parseInt(dtl.get("returnSum"));

		        i++;
		    }
		}
		//데이터 처리 끝

		//마스터입력
		paramMap.put("returnSum", Integer.toString(sumReturnSum));
		int result = sm06Mapper.insert_sm06(paramMap);

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
	public int update_sm06(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int sumReturnSum = 0; 

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dtaChk = dtl.get("dtachk").toString();
			String Chk = dtl.get("chk").toString();
			//수정처리한거만 업데이트 U 받으려면 dtChk 이 변수 써야 돼요
			if ("U".equals(dtaChk)) {
				//데이터 처리
				sm06Mapper.update_sm06_Dtl(dtl); //여기서 디테일 테이블 먼저 업데이트하니깐 변경된 값이 적용 됏을테니
			}

//			//"dtaChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
//	    	if ("U".equals(dtaChk)) {
//				// dtl로부터 'returnSum' 값을 가져와 'paramMap'에 설정
//		        sumReturnSum += Integer.parseInt(dtl.get("returnSum"));
//	    	} 
	    }
		//데이터 처리 끝
	    int sumQty = sm06Mapper.select_sm06_Sum(paramMap);
	  //마스터입력
	  paramMap.put("returnSum", Integer.toString(sumQty));
	 //마스터 수정
	  int result = sm06Mapper.update_sm06(paramMap);
		
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
	public int delete_sm06(Map<String, String> paramMap) throws Exception {
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
		
		String lvl = paramMap.get("lvl").toString();
		
		if ("1".equals(lvl)) {
			//데이터 처리
			result = sm06Mapper.delete_sm06_Dtl_All(paramMap);
			result = sm06Mapper.delete_sm06(paramMap);
    	} else {
    		result = sm06Mapper.delete_sm06_Dtl(paramMap);
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
