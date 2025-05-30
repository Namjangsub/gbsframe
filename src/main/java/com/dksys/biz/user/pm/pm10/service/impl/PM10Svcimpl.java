package com.dksys.biz.user.pm.pm10.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.pm.pm10.mapper.PM10Mapper;
import com.dksys.biz.user.pm.pm10.service.PM10Svc;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM10Svcimpl implements PM10Svc {

	@Autowired
	PM10Mapper pm10Mapper;

	@Autowired
	CM08Svc cm08Svc;

	@Autowired
    CM15Svc cm15Svc;

	@Override
	public List<Map<String, String>> selectMnList(Map<String, String> paramMap) throws Exception {

    	String[] deptCodes = new Gson().fromJson(paramMap.get("deptCodes"), String[].class);
		List<Map<String, String>> mnList = pm10Mapper.selectMnList(paramMap);
		Gson gson = new Gson();

		for (Map<String,String> row : mnList) {
			String rawDate = row.get("mnDate").replace("-", "");
			List<Map<String,String>> allFiles = new ArrayList<>();

			for (String deptCode : deptCodes) {
				String fileTrgtKey = rawDate + "-" + deptCode;
				Map<String,String> fileMap = new HashMap<>(paramMap);
				fileMap.put("comonCd",     "FITR9901");
				fileMap.put("fileTrgtTyp", "PM1002M01");
				fileMap.put("fileTrgtKey", fileTrgtKey);
				fileMap.put("jobType",     "fileList");

				// 해당 부서코드로 무조건 파일 조회
				List<Map<String,String>> files = cm08Svc.selectFileList(fileMap);
				allFiles.addAll(files);
			}
			row.put("files", gson.toJson(allFiles));
		}

		return mnList;
	}

	@Override
	public List<Map<String, String>> select_p10_d02_List(Map<String, String> paramMap) {
		return pm10Mapper.select_p10_d02_List(paramMap);
	}

	@Override
	public int pm10_main_update(Map<String, String> param) throws Exception {
		return pm10Mapper.pm10_main_update(param);
	}

	@Override
	public int pm10_d03_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param); // 일자별 마스터테이블 추가
		pm10Mapper.pm10_d01_update(param); // 주제 추가
		return pm10Mapper.pm10_d03_update(param);
	}

	@Override
	public int deleteMn(Map<String, String> paramMap) throws Exception {
		// D01 삭제

		Integer result = 0;

		Object mnSubSeq = paramMap.get("mnSubSeq");
		if (mnSubSeq != null && !"".equals(mnSubSeq)) {
			result += pm10Mapper.deleteMnD01(paramMap);
			// D03 삭제
			result += pm10Mapper.deleteMnD03(paramMap);
		}

		// if ("해당 날짜에 해당하는 주제 및 내용이 없다면 메인 삭제, 참석자 테이블도 삭제") {
		// 	pm10Mapper.deleteMnM01(paramMap);
		// }

		return result;
	}

	@Override
	public int pm10_d01_update(Map<String, String> param) throws Exception {
		pm10Mapper.pm10_main_update(param);
		return pm10Mapper.pm10_d01_update(param);
	}

	@Override
	public int pm10_d01_sortNo_update(Map<String,Object> paramMap) throws Exception {
		int result = 0;
		List<Map<String,Object>> list = (List<Map<String,Object>>) paramMap.get("list");
		
		for (Map<String,Object> dtl : list) {
			dtl.put("mnDate", paramMap.get("mnDate"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId",  paramMap.get("pgmId"));
			result += pm10Mapper.pm10_d01_sortNo_update(dtl);
		}
		return result;
	}

	@Override
	public int pm10_d02_update(Map<String, Object> paramMap) throws Exception {
		int result = 0;
		List<Map<String,Object>> attedList = (List<Map<String,Object>>) paramMap.get("attendList");
		
		for (Map<String,Object> dtl : attedList) {
			dtl.put("mnDate", paramMap.get("mnDate"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId",  paramMap.get("pgmId"));
			result += pm10Mapper.pm10_d02_update(dtl);
		}
		return result;
	}

	@Override
	public int pm10_d02_delete(Map<String,Object> paramMap) throws Exception {
		int result = 0;
		List<String> ids = (List<String>) paramMap.get("attendIds");
		for (String id : ids) {
			Map<String,Object> p = new HashMap<>();
			p.put("mnDate", paramMap.get("mnDate"));
			p.put("userId", id);
			result += pm10Mapper.pm10_d02_delete(p);
		}
		return result;
	}

	@Override
	public int mnUploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;

		// 삭제할 키부터 삭제
		String deleteJson = paramMap.get("deleteFileArr");
		if (deleteJson != null && !deleteJson.isEmpty()) {
			List<Map<String, String>> deleteList = new Gson().fromJson(
				deleteJson,
				new TypeToken<List<Map<String, String>>>(){}.getType()
			);
			for (Map<String, String> entry : deleteList) {
				String fileKey = entry.get("fileKey");
				cm08Svc.deleteFile(fileKey);
			}
		}

		// 업로드할 파일이 있으면 업로드
		List<MultipartFile> fileList = mRequest.getFiles("files");
		if (!fileList.isEmpty()) {
			//"FITR05"은 공통코드에서 사진,미디어 첨부 디렉토리임
			paramMap.put("comonCd",        "FITR05");
			paramMap.put("jobType",        "fileUp");
			paramMap.put("fileTrgtTyp",    "PM1002M01");
			paramMap.put("fileTrgtKey",    mRequest.getParameter("fileTrgtKey"));
			cm15Svc.selectFileAuthCheck(paramMap);

			// 업로드
			result = cm08Svc.uploadFile(paramMap, mRequest);
		}

		return result;
	}


}