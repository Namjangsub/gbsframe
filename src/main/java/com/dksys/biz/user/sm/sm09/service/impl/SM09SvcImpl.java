package com.dksys.biz.user.sm.sm09.service.impl;

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

import com.dksys.biz.user.sm.sm09.mapper.SM09Mapper;
import com.dksys.biz.user.sm.sm09.service.SM09Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM09SvcImpl implements SM09Svc {
	
	@Autowired
	SM09Mapper sm09Mapper;
	
	@Autowired
	SM09Svc sm09Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	// 그리드 카운트
	@Override
	public int grid1_selectCount(Map<String, String> paramMap) {
		return sm09Mapper.grid1_selectCount(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
		return sm09Mapper.grid1_selectList(paramMap);
	}
	
	// 창고 코드 검색
	@Override
	public List<Map<String, Object>> selectWhCd(Map<String, String> paramMap) {
		return sm09Mapper.selectWhCd(paramMap);
	}

	// 팝업 재고 검색
	@Override
	public List<Map<String, String>> select_stock_modal(Map<String, String> paramMap) {
		return sm09Mapper.select_stock_modal(paramMap);
	}
	
	// 수정화면 정보
	@Override
	public Map<String, String> select_sm07_Info(Map<String, String> paramMap) {
		return sm09Mapper.select_sm07_Info(paramMap);
	}
	
	// 수정화면 상세정보
	@Override
	public List<Map<String, String>> select_sm07_Info_Dtl(Map<String, String> paramMap) {
		return sm09Mapper.select_sm07_Info_Dtl(paramMap);
	}
	
	//DATA INSERT
	@Override
	public int insert_sm07(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int fileTrgtKey = sm09Mapper.select_sm07_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		String newMNGM_NO = sm09Mapper.select_sm07_Next_MNGM_NO(paramMap);
		paramMap.put("ioNo", newMNGM_NO);
		
		//마스터입력
		int result = sm09Mapper.insert_sm07(paramMap);

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
				outInoutKey = sm09Mapper.select_sm07_Ioseq(paramMap);
				dtl.put("outInoutKey", Integer.toString(outInoutKey));
	    		dtl.put("ioSeq", Integer.toString(i));

				//데이터 처리
				sm09Mapper.insert_sm07_Dtl(dtl);
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
	public int update_sm07(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = sm09Mapper.update_sm07(paramMap);

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
//	    	if ("U".equals(dataChk)) {
				//데이터 처리
				sm09Mapper.update_sm07_Dtl(dtl);
//	    	} 
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
	public int delete_sm07(Map<String, String> paramMap) throws Exception {
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
			result = sm09Mapper.delete_sm07_Dtl_All(paramMap);
			result = sm09Mapper.delete_sm07(paramMap);
    	} else {
    		result = sm09Mapper.delete_sm07_Dtl(paramMap);
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
	
	//대체품목 팝업창에서 DATA INSERT
	@Override
	public int insert_sm091(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		
		//데이터 처리 시작
		int fileTrgtKey = sm09Mapper.select_sm09_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		String newMNGM_NO = sm09Mapper.select_sm09_Next_MNGM_NO(paramMap);
		paramMap.put("ioNo", newMNGM_NO);
		
		paramMap.put("salesCd", paramMap.get("selesCd"));
		
		//마스터입력
		int result = sm09Mapper.insert_sm09(paramMap);

		//상세입력
		int i = 1;
		int outInoutKey = 0;
		int outInoutKey1 = 0;
		
		outInoutKey = sm09Mapper.select_sm09_Ioseq(paramMap);
		paramMap.put("outInoutKey", Integer.toString(outInoutKey));
		paramMap.put("ioSeq", "1");


		int newPrice = sm09Mapper.select_bm02_price(paramMap);
		//왼쪽 가격 생성
		String sortQty = paramMap.get("stockQty");
		int sumAmt = Integer.parseInt(sortQty) * newPrice;
		paramMap.put("sumAmt", Integer.toString(sumAmt));
		
		//오른쪽 가격 생성
		paramMap.put("newPrice", Integer.toString(newPrice));
		
		//대체품목시 GOODS_DIV 채굴
		String goodsDiv = sm09Mapper.select_bm20_goodsdiv(paramMap);
		paramMap.put("goodsDiv", goodsDiv);
		
		//데이터 처리
		sm09Mapper.insert_sm09_Dtl(paramMap);
		sm09Mapper.update_bm20_item(paramMap);
		sm09Mapper.insert_bm30_item(paramMap);
		sm09Mapper.insert_bm20_itemright(paramMap);
		//outinoutkey 하나 더 생성
		outInoutKey1 = sm09Mapper.select_sm09_Ioseq(paramMap);
		paramMap.put("outInoutKey1", Integer.toString(outInoutKey1));
		
		sm09Mapper.insert_bm30_itemright(paramMap);

		//데이터 처리 끝
		
		return result;
	}
}
