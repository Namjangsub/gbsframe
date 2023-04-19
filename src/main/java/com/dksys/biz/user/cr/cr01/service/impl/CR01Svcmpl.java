package com.dksys.biz.user.cr.cr01.service.impl;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr01.mapper.CR01Mapper;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR01Svcmpl implements CR01Svc {
	
    @Autowired
	CR01Mapper cr01Mapper;
    
    @Autowired
    CM08Svc cm08Svc; 
    
    @Override
    public String selectMaxEstNo() {
        return cr01Mapper.selectMaxEstNo();
    }
    
	@Override
	public int selectEstCount(Map<String, String> param) {
		return cr01Mapper.selectEstCount(param);
	}

	@Override
	public List<Map<String, Object>> selectEstList(Map<String, String> param) {

		return cr01Mapper.selectEstList(param);
	}
	
	@Override
	public Map<String, Object> selectEstInfo(Map<String, String> paramMap) {

		Map<String, Object> estInfo = cr01Mapper.selectEstInfo(paramMap);



		return estInfo;

	}

	@Override
	public void insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		System.out.println(paramMap+"여기1");
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		try {
			
			
		// 견적서 insert
		cr01Mapper.insertEst(paramMap);

		List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);
		
		System.out.println(detailArr+"여기2");
		
	
			for (Map<String, String> detailMap : detailArr) {
			    System.out.println("여기실행");
			    detailMap.put("coCd", paramMap.get("coCd"));
			    detailMap.put("estNo", paramMap.get("estNo"));
			    detailMap.put("userId", paramMap.get("userId"));
			    detailMap.put("pgmId", paramMap.get("pgmId"));
		
			    cr01Mapper.insertEstDetail(detailMap);
			}
			cm08Svc.uploadFile("TB_SD03M01", paramMap.get("estNo"), mRequest);
			List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"),stringList);

			for(String fileKey :
			  deleteFileList) {cm08Svc.deleteFile(fileKey);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage()+"에러명");
		}
	
	}
	public static String removeEmptyObjects(String jsonArrayString) {
		String nullAndEmptyObjectPattern = "(\\{\\s*\\}|null),?";
		    String resultString = jsonArrayString.replaceAll(nullAndEmptyObjectPattern, "");
		    // In case the last empty object doesn't have a comma
		    if (resultString.endsWith(",}")) {
		        resultString = resultString.substring(0, resultString.length() - 2) + "}";
		    }
		    return resultString;
	}
	
	@Override
	public int updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		
		// 견적 update
		int result = cr01Mapper.updateEst(paramMap);
		
		// 견적상세 delete
		cr01Mapper.deleteEstDetail(paramMap);
		// 견적상세 insert
		List<Map<String, String>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);
		for(Map<String, String> estMap : detailList) {
			estMap.put("estNo", paramMap.get("estNo"));
			estMap.put("userId", paramMap.get("userId"));
			estMap.put("pgmId", paramMap.get("pgmId"));
			cr01Mapper.insertEstDetail(estMap);
		}
		
		/*
		 * // 파일 업로드 cm08Svc.uploadFile("TB_SD03M01", paramMap.get("estNo"), mRequest);
		 * // 파일 삭제 List<String> deleteFileList =
		 * gson.fromJson(paramMap.get("deleteFileArr"),stringList); for(String fileKey :
		 * deleteFileList) { cm08Svc.deleteFile(fileKey); }
		 */
		return result;
	}
	
	@Override
	public int deleteEst(Map<String, String> paramMap) {
		int result = cr01Mapper.deleteEst(paramMap);
		result += cr01Mapper.deleteEstDetail(paramMap);
		return result;
	}
}