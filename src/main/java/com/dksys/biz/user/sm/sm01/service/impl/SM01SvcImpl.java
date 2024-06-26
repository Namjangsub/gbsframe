package com.dksys.biz.user.sm.sm01.service.impl;

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
import com.dksys.biz.user.sm.sm01.mapper.SM01Mapper;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM01SvcImpl implements SM01Svc {
	@Autowired
	SM01Mapper sm01Mapper;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;

	@Override
	public int selectBomSalesCount(Map<String, String> paramMap) {
		return sm01Mapper.selectBomSalesCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectBomSalesTreeList(Map<String, String> paramMap) {
		return sm01Mapper.selectBomSalesTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomSalesList(Map<String, String> paramMap) {
	return sm01Mapper.selectBomSalesList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomDetailList(Map<String, String> paramMap) {
	return sm01Mapper.selectBomDetailList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBuyBomList(Map<String, String> paramMap) {
	return sm01Mapper.selectBuyBomList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomMatrList(Map<String, String> paramMap) {
	return sm01Mapper.selectBomMatrList(paramMap);
	}

	@Override
	public Map<String, String> selectBomMatrInfo(Map<String, String> paramMap) {
	return sm01Mapper.selectBomMatrInfo(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomMatrTreeList(Map<String, String> paramMap) {
	return sm01Mapper.selectBomMatrTreeList(paramMap);
	}

	@Override
	public int insertBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		//String  newPrjctSeq = String.valueOf(sm01Mapper.selectPrjctSeqNext(paramMap));
		//paramMap.put("prjctSeq", newPrjctSeq);
		//int result = sm01Mapper.insertBom(paramMap);
		int result = 200;

		List<Map<String, String>> matrList = gsonDtl.fromJson(paramMap.get("matrArr"), dtlMap);
	    for (Map<String, String> dtl : matrList) {
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	/* "dtaChk" 값을 확인하여
	    	 * "I"인 경우 sm01Mapper.insertBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
	    	if ("I".equals(dtaChk)) {
				int lowerCd = sm01Mapper.select_bm14_Key(paramMap);
				dtl.put("lowerCd", Integer.toString(lowerCd));
	    		sm01Mapper.insertBomMatr(dtl);
	    	}
	    }

	    List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"), dtlMap);
	    for (Map<String, String> dtl : bomList) {
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	if ("I".equals(dtaChk)) {
	    		sm01Mapper.insertBom(dtl);
	    	} else if ("U".equals(dtaChk)) {
	    		sm01Mapper.updateBom(dtl);
	    	} else if ("D".equals(dtaChk)) {
	    		sm01Mapper.deleteBomMatrAll(dtl);
	    		sm01Mapper.deleteBom(dtl);
	    	}
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
	public int updateBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();

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

		int result = 200;//sm01Mapper.updateBom(paramMap);
			//  sm01Mapper.updateBom(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 업데이트하고 그 결과를 result 변수에 저장.
		
		List<Map<String, String>> matrList = gsonDtl.fromJson(paramMap.get("matrArr"), dtlMap);

		for (Map<String, String> dtl : matrList) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			String dtaChk = dtl.get("dtaChk").toString();
			if ("I".equals(dtaChk)) {
				int lowerCd = sm01Mapper.select_bm14_Key(paramMap);
				dtl.put("lowerCd", Integer.toString(lowerCd));
				sm01Mapper.insertBomMatr(dtl);
			} else if ("U".equals(dtaChk)) {
				dtl.put("oldLowerCd", dtl.get("lowerCd"));
				dtl.put("oldUpperCd", dtl.get("upperCd"));
				sm01Mapper.updateBomMatr(dtl);
			} else if ("D".equals(dtaChk)) {
				sm01Mapper.deleteBomMatr(dtl);
			}
		}

			/*
			* List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"),
			* dtlMap); for (Map<String, String> dtl : bomList) {
			* 
			* dtl.put("userId", paramMap.get("userId")); dtl.put("pgmId",
			* paramMap.get("pgmId")); String dtaChk = dtl.get("dtaChk").toString(); if
			* ("I".equals(dtaChk)) { sm01Mapper.insertBom(dtl); } else if
			* ("U".equals(dtaChk)) { sm01Mapper.updateBom(dtl); } else if
			* ("D".equals(dtaChk)) { sm01Mapper.deleteBomMatrAll(dtl);
			* sm01Mapper.deleteBom(dtl); } }
			*/

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
	public int deleteBomAll(Map<String, String> paramMap) throws Exception {
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
		
		int result = 200;
		sm01Mapper.deleteBomAllMatrAll(paramMap);
		sm01Mapper.deleteBomAll(paramMap);

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
	
	@Override
	public int insertBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = sm01Mapper.insertBomMatr(paramMap);
		return result;
	}
	
	@Override
	public int updateBomMatr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = sm01Mapper.updateBomMatr(paramMap);
		return result;
	}
	
	@Override
	public int deleteBomMatr(Map<String, String> paramMap) throws Exception {
		int result = sm01Mapper.deleteBomMatr(paramMap);
		return result;
	}
	
	@Override
	public int insertCrudMatrAndBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		int result = 200;

	    List<Map<String, String>> matrList = gsonDtl.fromJson(paramMap.get("matrArr"), dtlMap);
	    for (Map<String, String> dtl : matrList) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));

	    	String dtaChk = "";
	    	if(dtl.get("dtaChk") != null) dtaChk = dtl.get("dtaChk").toString();
	    	/* "dtaChk" 값을 확인하여
	    	 * "I"인 경우 sm01Mapper.insertBomMatr(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
	    	if ("I".equals(dtaChk)) {
	    		sm01Mapper.insertBomMatr(dtl);
	    	}
	    	else if ("U".equals(dtaChk)) {
		        sm01Mapper.updateBomMatr(dtl);
		    }
	    	else if ("D".equals(dtaChk)) {
		        sm01Mapper.deleteBomMatr(dtl);
		    }
		}
		
		List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"), dtlMap);
	    for (Map<String, String> dtl : bomList) {
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));

	    	String dtaChk = "";
	    	if(dtl.get("dtaChk") != null) dtaChk = dtl.get("dtaChk").toString();
	    	if ("I".equals(dtaChk)) {
	    		sm01Mapper.insertBom(dtl);
	    	} else if ("U".equals(dtaChk)) {
	    		sm01Mapper.updateBom(dtl);
	    	} else if ("D".equals(dtaChk)) {
	    		sm01Mapper.deleteBom(dtl);
	    	}
	    }

	    return result;
	}
	
	@Override
	public int insertUploadBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		//fileTrgtTyp,fileTrgtKey,userId
	    String fileTrgtTyp = paramMap.get("fileTrgtTyp");
	    String fileTrgtKey = paramMap.get("fileTrgtKey");
	    String userId = paramMap.get("userId");
		deleteBomAll(paramMap);
		return insertCrudMatrAndBom(paramMap, mRequest);
	}
	
	@Override
	public List<Map<String, String>> bomTreeList(Map<String, String> paramMap) {
		return sm01Mapper.bomTreeList(paramMap);
	}
	
	@Override
	public void callCopyBom(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		sm01Mapper.callCopyBom(paramMap);
	}
	
	@Override
	public List<Map<String, String>> nextPrcsnNmList(Map<String, String> paramMap) {
		return sm01Mapper.nextPrcsnNmList(paramMap);
	}
	
	//multi select 검색
	@Override
	public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
		return sm01Mapper.select_prjct_code(paramMap);
	}

	// @Override
	// public int syncBom(Map<String, String> paramMap) {
	// 	return sm01Mapper.syncBom(paramMap);
	// }

	@Override
	public Map<String, String> syncBom(Map<String, String> paramMap) {
		sm01Mapper.syncBom(paramMap);
		return paramMap;
	}

	// BOM tree Node Move
	@Override
	public int moveMatrBom(List<Map<String, String>> paramList) {
		int result = 0;
		for(Map<String, String> paramMap : paramList) {
			result += sm01Mapper.moveMatrBom(paramMap);
		}
		return result;
	}

	// BOM tree Node Copy
	@Override
	public int copyMatrBomTree(List<Map<String, String>> paramList) {
		int result = 0;
		String lowerCd = "";
		
		for(Map<String, String> paramMap : paramList) {
			//신규등록 키값 할당
			lowerCd = Integer.toString(sm01Mapper.select_bm14_Key(paramMap));
			paramMap.put("lowerCdTo", lowerCd);
			//트리 복사
			result += sm01Mapper.copyMatrBomTree(paramMap);
			
			/*{coCd=GUN, salesCd=23010-99TVFTE, salesCdTo=23010-99TVFTE, 
			 * 복사하고자하는 대상 부모코드 : parentId=114, upperCd=2000, upperKey=114, 
			 * 붙여넣기할 대상코드 하위코드 : childId=244, lowerCd=0050, lowerKey=244, 
			 * 새로 생성할 키값 : fileTrgtKey=1630} --> 신규생성된 lowerKey값도 동일하게 처리함
			 * 
			 * {coCd=GUN, salesCd=23010-99TVFTE, salesCdTo=23010-99TVFTE,
			 *  parentId=114, upperCd=2000, upperKey=114,
			 *  childId=244, lowerCd=0050, lowerKey=244, 
			 *  fileTrgtKey=1633}
			 * 
			 * {coCd=GUN, salesCd=23010-99TVFTE, salesCdTo=23010-99TVFTE,
			 *  parentId=114, upperCd=2000, upperKey=114, 
			 *  childId=244, lowerCd=0050, lowerKey=244, 
			 *  userId=jangsub.nam, pgmId=ZZ0101M01, 
			 *  fileTrgtKey=1634}
			 */
		}
		return result;	
	}

}