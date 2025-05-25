package com.dksys.biz.user.sm.sm20.service.impl;

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
import com.dksys.biz.user.sm.sm20.mapper.SM20Mapper;
import com.dksys.biz.user.sm.sm20.service.SM20Svc;
import com.dksys.biz.user.sm.sm21.mapper.SM21Mapper;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM20SvcImpl implements SM20Svc {
	
	@Autowired
	SM20Mapper sm20Mapper;
	
	@Autowired
	SM21Mapper sm21Mapper;

	@Autowired
	SM20Svc sm20Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;
	
	@Autowired
	ExceptionThrower thrower;

	@Override
	public List<Map<String, String>> sm20_main_grid1_selectList(Map<String, String> paramMap) {		
		return sm20Mapper.sm20_main_grid1_selectList(paramMap);
	}

	@Override
	public List<Map<String, String>> sm20_main_grid2_selectList(Map<String, String> paramMap) {		
		return sm20Mapper.sm20_main_grid2_selectList(paramMap);
	}

	@Override
	public List<Map<String, String>> sm20_main_grid3_selectList(Map<String, String> paramMap) {
		return sm20Mapper.sm20_main_grid3_selectList(paramMap);
	}

	// 그리드 카운트
	@Override
	public int grid1_selectCount(Map<String, String> paramMap) {
		return sm20Mapper.grid1_selectCount(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
		return sm20Mapper.grid1_selectList(paramMap);
	}

	// 팝업 입력대상 검색
	@Override
	public List<Map<String, String>> select_sm20_insert_target_modal(Map<String, String> paramMap) {
		return sm20Mapper.select_sm20_insert_target_modal(paramMap);
	}

	// 수정화면 정보
	@Override
	public Map<String, String> select_sm20_Info(Map<String, String> paramMap) {
		return sm20Mapper.select_sm20_Info(paramMap);
	}

	// 수정화면 상세정보
	@Override
	public List<Map<String, String>> select_sm20_Info_Dtl(Map<String, String> paramMap) {
		return sm20Mapper.select_sm20_Info_Dtl(paramMap);
	}

	//세금계산서지급여부
	@Override
	public int update_sm20_payYn(Map<String, String> paramMap) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		
//		HashMap<String, String> param = new HashMap<>();
//		param.put("userId", paramMap.get("userId"));
//		param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
		String jobYn = paramMap.get("jobYn"); // Y:지급등록, N:지급취소, U:지급연결(대량데이터 연결 처리)

		int result = 0;

		// ---------------------------------------------------------------
		// 지급 처리 구분자
		// Y : 대금지급 개별 등록 (계산서 발행번호 필수)
		// N : 대금지금 취소처리 (계산서 발행번호 필수, 지급내겨 선택 필수)
		// U : 지금지급 연결처리 (계산서 발행번호 필수, 지급내겨 선택 필수)
		// D : 지급내역 삭제처리 (계산서 발행번호 필수, 지급내겨 선택 필수)
		// ---------------------------------------------------------------
//		result += sm20Mapper.update_sm20_payYn(paramMap);

		if (jobYn.equals("Y")) { // 대금지급
			paramMap.put("fileTrgtKey", sm21Mapper.select_sm21_SeqNext(paramMap));
			result += sm21Mapper.insert_sm21_payChk(paramMap);
		}

		//upate
		if (!jobYn.equals("Y")) { // 대금지급연결
//			String payType = paramMap.get("payType");
//			String payInterval = paramMap.get("payInterval");
//			String payDt = paramMap.get("payDt");
			List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap); // 대금 결제현황정보 리스트 정보
			for (Map<String, String> dtl : detailMap) {
    			if (jobYn.equals("U")) { // 대금지급연결
					dtl.put("billNo", paramMap.get("billNo"));
					dtl.put("ctrtNo", paramMap.get("ctrtNo"));
					dtl.put("userId", paramMap.get("userId"));
					dtl.put("pgmId", paramMap.get("pgmId"));
					result += sm21Mapper.update_sm21_payChk(dtl);
				} else if (jobYn.equals("N")) { // 취소처리
					dtl.put("billNo", "");
					dtl.put("ctrtNo", "");
					dtl.put("userId", paramMap.get("userId"));
					dtl.put("pgmId", paramMap.get("pgmId"));
					result += sm21Mapper.update_sm21_payChk(dtl);
				} else if (jobYn.equals("D")) { // 삭제처리
					result += sm21Mapper.delete_sm21_payChk(dtl);
					dtl.put("billNo", paramMap.get("billNo"));
					dtl.put("ctrtNo", paramMap.get("ctrtNo"));
    			}

			}
		}
		// 대금 지불 완료/미결 처리
		result += sm20Mapper.update_sm20_payCompleteChke(paramMap);

		return result;
	}
	
	//DATA INSERT
	@Override
	public int insert_sm20(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int fileTrgtKey = sm20Mapper.select_sm20_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		String newMNGM_NO = sm20Mapper.select_sm20_Next_MNGM_NO(paramMap);
		paramMap.put("billNo", newMNGM_NO);
		
		//마스터입력
		int result = sm20Mapper.insert_sm20(paramMap);

		//상세입력
		//int i = 1;
		//int outInoutKey = 0;

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	dtl.put("billNo", newMNGM_NO);
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));

	    	//마스터의 정보로 통화단위, 환율 변경
	    	dtl.put("currCdM", paramMap.get("currCd"));
	    	dtl.put("exrateM", paramMap.get("exrate"));
	    	//dtl["currCd"] = paramMap.get("currCd");
	    	//dtl["exrate"] = paramMap.get("exrate");
	    	
	    	String dataChk = dtl.get("dataChk").toString();
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("I".equals(dataChk)) {
				dtl.put("pchsNo", dtl.get("pchsNo").toString());
				dtl.put("pchsSeq", dtl.get("pchsSeq").toString());
				dtl.put("ordrgNo", dtl.get("ordrgNo").toString());
				dtl.put("ordrgSeq", dtl.get("ordrgSeq").toString());
				dtl.put("billDtlAmt", dtl.get("billDtlAmt").toString());
				dtl.put("billDtlVat", dtl.get("billDtlVat").toString());
				dtl.put("billDtlTot", dtl.get("billDtlTot").toString());
				
				//데이터 처리
				sm20Mapper.insert_sm20_Dtl(dtl);
				sm20Mapper.update_sm20_Conf(dtl);
				//i++;
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
	public int update_sm20(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = sm20Mapper.update_sm20(paramMap);

		// //상세수정
		// List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    // for (Map<String, String> dtl : dtlParam) {
	    // 	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
		// 	dtl.put("userId", paramMap.get("userId"));
	    // 	dtl.put("pgmId", paramMap.get("pgmId"));
			
		// 	String dataChk = dtl.get("dataChk").toString();	    	
		// 	//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    // 	if ("U".equals(dataChk)) {
		// 		//데이터 처리
		// 		sm20Mapper.update_sm20_Dtl(dtl);
	    // 	} 
	    // }
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
	public int delete_sm20(Map<String, String> paramMap) throws Exception {
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
		
		result = sm20Mapper.delete_sm20_Dtl(paramMap);
		result = sm20Mapper.delete_sm20(paramMap);
		result = sm20Mapper.update_sm20_Del(paramMap);
		
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
	
	//multi select 검색
	@Override
	public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
		return sm20Mapper.select_prjct_code(paramMap);
	}
	
	//multi select 검색
	@Override
	public List<Map<String, String>> select_mngId_code(Map<String, String> paramMap) {
		return sm20Mapper.select_mngId_code(paramMap);
	}
	
}