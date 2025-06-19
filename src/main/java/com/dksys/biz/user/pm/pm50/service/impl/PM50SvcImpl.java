package com.dksys.biz.user.pm.pm50.service.impl;

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
import com.dksys.biz.user.pm.pm50.mapper.PM50Mapper;
import com.dksys.biz.user.pm.pm50.service.PM50Svc;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM50SvcImpl implements PM50Svc{

	@Autowired
	PM50Mapper pm50Mapper;

	@Autowired
    CM08Svc cm08Svc;

	@Autowired
	WB22Mapper wb22Mapper;

	@Autowired
    CM15Svc cm15Svc;

	@Override
	public int select_pm50_ListCount(Map<String, String> paramMap) {
		return pm50Mapper.select_pm50_ListCount(paramMap);
	}

	@Override
	public List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap) {
		return pm50Mapper.select_pm50_List(paramMap);
	}

	@Override
	public Map<String, Object> select_pm50_Info(Map<String, String> paramMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, String> fileMap = new HashMap<String, String>();
		fileMap.put("fileTrgtTyp", "PM5001P01_M");
		fileMap.put("fileTrgtKey", paramMap.get("salesCd"));
		fileMap.put("userId", paramMap.get("userId"));
		returnMap.put("fileList", pm50Mapper.selectFileList(fileMap));
		returnMap.put("select_pm50_Info", wb22Mapper.selectSjInfo(paramMap));
		return returnMap; 
	}

	@Override
	public int upLoadFiles(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
			paramMap.put("fileTrgtTyp",    "PM5001P01_M");
			paramMap.put("fileTrgtKey",    paramMap.get("salesCd"));
			paramMap.put("userId",		   paramMap.get("userId"));
			cm15Svc.selectFileAuthCheck(paramMap);

			// 업로드
			result = cm08Svc.uploadFile(paramMap, mRequest);
		}

		return result;
	}

	@Override
	public int delete_pm50(Map<String, String> paramMap) {
		return pm50Mapper.delete_pm50(paramMap);
	}
}
