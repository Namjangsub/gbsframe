package com.dksys.biz.user.sm.sm07.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm07.mapper.SM07Mapper;
import com.dksys.biz.user.sm.sm07.service.SM07Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM07SvcImpl implements SM07Svc {

	@Autowired
	SM07Mapper sm07Mapper;

	@Autowired
	SM07Svc sm07Svc;

	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
	ExceptionThrower thrower;
	
	@Override
	public int updateOrderDetail(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = sm07Mapper.updateOrderDetail(paramMap);

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
	
	/*
	 * 마스터 페이지 조회
	 * 
	 * @Override public int master_grid_selectCount(Map<String, String> paramMap) {
	 * return sm07Mapper.master_grid_selectCount(paramMap); }
	 * 
	 * @Override public List<Map<String, String>> master_grid_selectList(Map<String,
	 * String> paramMap) { return sm07Mapper.master_grid_selectList(paramMap); } 마스터
	 * 페이지 조회
	 * 
	 * //모달 그리드 조회 -수정화면 정보
	 * 
	 * @Override public Map<String, String> select_sm07_Info(Map<String, String>
	 * paramMap) { return sm07Mapper.select_sm07_Info(paramMap); }
	 * 
	 * //모달창 안에 그리드 grid-modal
	 * 
	 * @Override public List<Map<String, String>> grid_Modal_selectList(Map<String,
	 * String> paramMap) { return sm07Mapper.grid_Modal_selectList(paramMap); }
	 */

}