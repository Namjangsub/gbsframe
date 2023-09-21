package com.dksys.biz.admin.bm.bm99.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.bm.bm99.mapper.BM99Mapper;
import com.dksys.biz.admin.bm.bm99.service.BM99Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM99SvcImpl implements BM99Svc {
	
    @Autowired
    BM99Mapper bm99Mapper;
    
    @Autowired
    CM08Svc cm08Svc;

	@Override
	public Map<String, Object> selectManualInfo(Map<String, String> paramMap) {
		// 조회 시 기존에 없으면 만듬
		int manualCnt = bm99Mapper.selectManualCount(paramMap);
		if(manualCnt < 1) {
			bm99Mapper.insertManual(paramMap);
		}
		return bm99Mapper.selectManualInfo(paramMap);
	}
	
	@Override
	public void updateManualInfo(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
 		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		
		// 거래처 update
		bm99Mapper.updateManual(paramMap);
		
		// 파일 업로드
		cm08Svc.uploadFile("TB_BM99P01", paramMap.get("fileTrgtKey"), mRequest);
		// 파일 삭제
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		if (deleteFileList != null) {
			for(String fileKey : deleteFileList) {
				cm08Svc.deleteFile(fileKey);
			}
		}
	}

}