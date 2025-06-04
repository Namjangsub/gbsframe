package com.dksys.biz.user.pm.pm10.service.impl;

import java.io.File;
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
		Gson gson = new Gson();

		// 회의록 리스트 조회
		List<Map<String, String>> mnList = pm10Mapper.selectMnList(paramMap);

		// 결과를 담을 새로운 리스트에 회의록 복사
		List<Map<String, String>> resultList = new ArrayList<>(mnList);

		// 첫 번째 회의록의 날짜(rawDate)만 사용
		String rawDate = mnList.get(0).get("mnDate").replace("-", "");

		// 모든 부서코드로 파일 조회하여 allFiles 에 모으기
		List<Map<String, String>> allFiles = new ArrayList<>();
		for (String deptCode : deptCodes) {
			String fileTrgtKey = rawDate + "-" + deptCode;
			Map<String, String> fileMap = new HashMap<>(paramMap);
			fileMap.put("comonCd",     "FITR9901");
			fileMap.put("fileTrgtTyp", "PM1002M01");
			fileMap.put("fileTrgtKey", fileTrgtKey);
			fileMap.put("jobType",     "fileList");

			List<Map<String, String>> files = cm08Svc.selectFileList(fileMap);
			if (files != null && !files.isEmpty()) {
				allFiles.addAll(files);
			}
		}

		// 조회된 파일이 하나라도 있으면, 맨 뒤에 파일 전용 row 추가
		if (!allFiles.isEmpty()) {
			Map<String, String> fileRow = new HashMap<>();
			// fileRow에는 원본 회의록과 구분하기 위해 "files"키에만 JSON 문자열을 담음
			fileRow.put("files", gson.toJson(allFiles));
			resultList.add(fileRow);
		}

		return resultList;
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
			// D01 삭제 (주제)
			result += pm10Mapper.deleteMnD01(paramMap);
			// D02 삭제 (참석자)
			result += pm10Mapper.deleteMnD02(paramMap);
			// D03 삭제 (회의 내용)
			result += pm10Mapper.deleteMnD03(paramMap);
		}
		int deleteM01Count = pm10Mapper.deleteMnM01(paramMap);  

		String fileInfoJson = paramMap.get("fileInfoJson");

		if (deleteM01Count > 0 && fileInfoJson != null && !fileInfoJson.isEmpty()) {
			Map<String, List<Map<String, Object>>> fileInfoMap = new Gson().fromJson(fileInfoJson, new TypeToken<Map<String, List<Map<String, Object>>>>(){}.getType());

			for (Map.Entry<String, List<Map<String, Object>>> entry : fileInfoMap.entrySet()) {
				List<Map<String, Object>> fileList = entry.getValue();
				for (Map<String, Object> file : fileList) {
					String fileKey = String.valueOf(file.get("fileKey"));
					if (fileKey != null && !fileKey.isEmpty()) {

						Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
						String filePath = fileInfo.get("filePath") + fileKey + "_" + fileInfo.get("fileName");

						Map<String, String> deleteParam = new HashMap<>();
						deleteParam.put("fileKey", fileKey);
						deleteParam.put("mnDate", paramMap.get("mnDate"));
						result = pm10Mapper.deleteMnFile(deleteParam);	// 임팀장 회의록 파일첨부 삭제 코드
						try {
							File f = new File(filePath);
							f.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

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