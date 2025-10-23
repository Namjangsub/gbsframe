package com.dksys.biz.user.pm.pm03.service.impl;

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
import com.dksys.biz.user.pm.pm03.mapper.PM03Mapper;
import com.dksys.biz.user.pm.pm03.service.PM03Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM03SvcImpl implements PM03Svc {
	@Autowired
	PM03Mapper pm03Mapper;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;
	
	@Override
	public int selectDeliveryPageCount(Map<String, String> paramMap) {
		return pm03Mapper.selectDeliveryPageCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectDeliveryPageList(Map<String, String> paramMap) {
		return pm03Mapper.selectDeliveryPageList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectDeliveryList(Map<String, String> paramMap) {
		return pm03Mapper.selectDeliveryList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSelesCdList(Map<String, String> paramMap) {
		return pm03Mapper.selectSelesCdList(paramMap);
	}
	
	@Override
	public Map<String, String> selectDeliveryMastInfo(Map<String, String> paramMap) {
		return pm03Mapper.selectDeliveryMastInfo(paramMap);
	}
	
	@Override
	public int insertDeliveryMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		String outNo = String.valueOf(pm03Mapper.selectOutNoNext(paramMap));
		paramMap.put("outNo", outNo);
		int result = pm03Mapper.insertDeliveryMast(paramMap);
		//pm03Mapper.deleteLgistSalesCdAll(paramMap);
		
		// List<Map<String, String>> salesCdList = gsonDtl.fromJson(paramMap.get("salesCdArr"), dtlMap);
		List<Map<String, String>> salesCdList = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : salesCdList) {
	    	dtl.put("outNo", outNo);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
			pm03Mapper.insertLgistSalesCd(dtl);
			
	    	// pm03Mapper.insertLgistSalesCd(dtl);
	    	// String dtaChk = dtl.get("dtaChk").toString();
	    	// if ("I".equals(dtaChk)) {
	    	// 	pm03Mapper.insertLgistSalesCd(dtl);
	    	// } else if ("U".equals(dtaChk)) {
	    	// 	pm03Mapper.updateLgistSalesCd(dtl);
	    	// } else if ("D".equals(dtaChk)) {
	    	// 	pm03Mapper.deleteLgistSalesCd(dtl);
	    	// }
	    }

		//---------------------------------------------------------------
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------
		//첨부 화일 처리  끝
		//---------------------------------------------------------------

	    return result;
	}
	
	@Override
	public int updateDeliveryMast(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		// Gson gson = new Gson();
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
		for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
				Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
				param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
				param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------

		int result = pm03Mapper.updateDeliveryMast(paramMap);
			//pm03Mapper.deleteLgistSalesCdAll(paramMap);


		List<Map<String, String>> salesCdList = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : salesCdList) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));

			paramMap.put("salesCd", dtl.get("salesCd"));
			
			pm03Mapper.updateLgistSalesCd(dtl);
			//String dtaChk = dtl.get("dtaChk").toString();
			/* "dtaChk" 값을 확인하여
			* "I"인 경우 pm03Mapper.insertLgistSalesCd(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
			* "U"인 경우 pm03Mapper.updateLgistSalesCd(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
			* "D"인 경우 * pm03Mapper.deleteLgistSalesCd(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
			/*
			if ("I".equals(dtaChk)) {
				pm03Mapper.insertLgistSalesCd(dtl);
			} else if ("U".equals(dtaChk)) {
				pm03Mapper.updateLgistSalesCd(dtl);
			} else if ("D".equals(dtaChk)) {
				pm03Mapper.deleteLgistSalesCd(dtl);
			}
			*/
		}

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
	public int deleteDeliveryMast(Map<String, String> paramMap) throws Exception {
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
		int result = pm03Mapper.deleteLgistSalesCdAll(paramMap);
		result = pm03Mapper.deleteDeliveryMast(paramMap);

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