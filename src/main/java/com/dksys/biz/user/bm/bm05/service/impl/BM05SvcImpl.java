package com.dksys.biz.user.bm.bm05.service.impl;

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
import com.dksys.biz.user.bm.bm05.mapper.BM05Mapper;
import com.dksys.biz.user.bm.bm05.service.BM05Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM05SvcImpl implements BM05Svc {
	
	@Autowired
	BM05Mapper bm05Mapper;
	
	@Autowired
	BM05Svc bm05Svc;
	
	@Autowired
	CM15Svc cm15Svc;

	@Autowired
	CM08Svc cm08Svc;
	
	// 그리드 카운트
	@Override
	public int grid1_selectCount(Map<String, String> paramMap) {
		return bm05Mapper.grid1_selectCount(paramMap);
	}

	// 그리드 리스트
	@Override
	public List<Map<String, String>> grid1_selectList(Map<String, String> paramMap) {
		return bm05Mapper.grid1_selectList(paramMap);
	}

	// 팝업(설계BOM) 그리드 카운트
	@Override
	public int MatModal_selectCount(Map<String, String> paramMap) {
		int result = bm05Mapper.MatModal_selectCount(paramMap);
		return result;
	}

	// 팝업(설계BOM) 그리드 리스트
	@Override
	public List<Map<String, String>> MatModal_selectList(Map<String, String> paramMap) {
		return bm05Mapper.MatModal_selectList(paramMap);
	}

	// 팝업(구매BOM) 그리드 카운트
	@Override
	public int MatModal_pchs_selectCount(Map<String, String> paramMap) {
		int result = bm05Mapper.MatModal_pchs_selectCount(paramMap);
		return result;
	}

	// 팝업(구매BOM) 그리드 리스트
	@Override
	public List<Map<String, String>> MatModal_pchs_selectList(Map<String, String> paramMap) {
		return bm05Mapper.MatModal_pchs_selectList(paramMap);
	}

	// 수정화면 정보
	@Override
	public Map<String, String> select_bm05_Info(Map<String, String> paramMap) {
		return bm05Mapper.select_bm05_Info(paramMap);
	}

	// 중복체크
	@Override
	public Map<String, String> selectMatrCdChk(Map<String, String> paramMap) {
		return bm05Mapper.selectMatrCdChk(paramMap);
	}

	// 삭제체크
	@Override
	public Map<String, String> deleteMatrCdChk(Map<String, String> paramMap) {
		return bm05Mapper.deleteMatrCdChk(paramMap);
	}
	
	//DATA UPDATE
	@Override
	public int update_bm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		//Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		//---------------------------------------------------------------
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		// param.put("coCd", paramMap.get("coCd"));
		param.put("coCd", "TRN");
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
		
		int result = bm05Mapper.update_bm05(paramMap);
		
		//---------------------------------------------------------------
		//첨부 화일 처리 시작
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			paramMap.put("coCd", "TRN");
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
	
	//DATA INSERT
	@Override
	public int insert_bm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		
		//---------------------------------------------------------------
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
			HashMap<String, String> param = new HashMap<>();
			//접근 권한 없으면 Exception 발생
			param.putAll(paramMap);
			param.put("jobType", "fileUp");
			param.put("coCd", "TRN");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------
		
		int fileTrgtKey = bm05Mapper.select_bm05_SeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		int result = bm05Mapper.insert_bm05(paramMap);
		//---------------------------------------------------------------
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
			paramMap.put("coCd", "TRN");
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------
		//첨부 화일 처리  끝
		//---------------------------------------------------------------
		return result;
	}
	
	//DATA DELETE
	@Override
	public int delete_bm05(Map<String, String> paramMap) throws Exception {
		//---------------------------------------------------------------
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
		//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------
		List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
		HashMap<String, String> param = new HashMap<>();
		param.put("jobType", "fileDelete");
		param.put("coCd", "TRN");
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
		
		int resultLog = bm05Mapper.update_bm05_userLog(paramMap);
		int result = bm05Mapper.delete_bm05(paramMap);
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

	// 자재마스터 설계 BOM에서 형번/규격 검색용
	@Override
	public List<Map<String, String>> BOM_selectMatrMnoList(Map<String, String> paramMap) {
		return bm05Mapper.BOM_selectMatrMnoList(paramMap);
	}
	
	// 자재마스터 등록시 제조사+형번/규격 검색용
	@Override
	public List<Map<String, String>> selectMatrMatSpecToDuplicateList(Map<String, String> paramMap) {
		return bm05Mapper.selectMatrMatSpecToDuplicateList(paramMap);
	}
	
	//DATA UPDATE
	@Override
	public int bm05_dlvrRqmDay_update(Map<String, String> paramMap) {
		return bm05Mapper.bm05_dlvrRqmDay_update(paramMap);
	}
	
}