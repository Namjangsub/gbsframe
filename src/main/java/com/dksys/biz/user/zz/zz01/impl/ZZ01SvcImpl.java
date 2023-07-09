package com.dksys.biz.user.zz.zz01.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.zz.zz01.mapper.ZZ01Mapper;
import com.dksys.biz.user.zz.zz01.service.ZZ01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class ZZ01SvcImpl implements ZZ01Svc {
    
    private static final int Map = 0;


	@Autowired
    ZZ01Mapper zz01Mapper;

    
    @Autowired
    ZZ01Svc zz01Svc;


	private List<Map<String, String>> allChildNodeInfos;
    

	@Override
	public List<Map<String, String>> selectBomTreeList(Map<String, String> paramMap) {
		return zz01Mapper.selectBomTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap) {
		return zz01Mapper.selectBomlevelList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectBomAllLevelList(Map<String, String> paramMap) {
		return zz01Mapper.selectBomAllLevelList(paramMap);
	}

	@Override
	public Map<String, String> selectBomTreInfo(Map<String, String> paramMap) {
		return zz01Mapper.selectBomTreInfo(paramMap);
	}

	@Override
	public int checkBomId(Map<String, String> paramMap) {
		return zz01Mapper.checkBomId(paramMap);
	}

	@Override
	public int insertBomTree(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		int result = 0;
		String fileTrgtKey = "";
		List<Map<String, String>> updateRowList = gsonDtl.fromJson(paramMap.get("updateRowArr"), dtlMap);
		if (updateRowList.size() > 0) {
				for (Map<String, String> dtl : updateRowList) {
					dtl.put("userId", paramMap.get("userId"));
					dtl.put("pgmId", paramMap.get("pgmId"));
					dtl.put("salesCd", paramMap.get("salesCd"));
					dtl.put("upperKey", paramMap.get("upperKey"));
					dtl.put("upperCd", paramMap.get("upperCd"));
					//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
					String updChk = dtl.get("updChk").toString();
					/* "dtaChk" 값을 확인하여
					 * "I"인 경우 zz01Mapper.insertPrjctDtl(dtl)을 호출하여  세부정보를 삽입하고,
					 * "U"인 경우 zz01Mapper.updatePrjctDtl(dtl)을 호출하여  세부정보를 업데이트하고,
					 * "D"인 경우 * zz01Mapper.deletePrjctDtl(dtl)을 호출하여  세부정보를 삭제.		 */
					if ("I".equals(updChk)) {
						//신규등록 키값 할당
						fileTrgtKey = Integer.toString(zz01Mapper.selectBomSeqNext(paramMap));
						dtl.put("fileTrgtKey", fileTrgtKey);
						result += zz01Mapper.insertBomTree(dtl);
					} else if ("U".equals(updChk)) {
						result += zz01Mapper.updateBom(dtl);
					} else if ("D".equals(updChk)) {
						result += zz01Mapper.deleteBom(dtl);
					}
				}
		}
		return result;
		
	}

	@Override
	public int moveBom(List<Map<String, String>> paramList) {
		int result = 0;
		for(Map<String, String> paramMap : paramList) {
			result += zz01Mapper.moveBom(paramMap);
		}
		return result;
	}
	
	@Override
	public int deleteBom(Map<String, String> paramMap) {
		return zz01Mapper.deleteBom(paramMap);
	}
	
	@Override
	public int copyBomTree(List<Map<String, String>> paramList) {
		int result = 0;
		String fileTrgtKey = "";
		allChildNodeInfos = new ArrayList<>();
		
		for(Map<String, String> paramMap : paramList) {
			//신규등록 키값 할당
			fileTrgtKey = Integer.toString(zz01Mapper.selectBomSeqNext(paramMap));
			paramMap.put("fileTrgtKey", fileTrgtKey);
			//트리 복사
			result += zz01Mapper.copyBomTree(paramMap);
			
			allChildNodeInfos = getAllChildNodeInfos(paramMap);
//			for(Map<String, String> NodeInfo : allChildNodeInfos) {
//			}
			
			/*
			 * {coCd=GUN, salesCd=23010-99TVFTE, parentId=114, upperCd=2000, upperKey=114, childId=129, lowerCd=4200, lowerKey=129, fileTrgtKey=216}
			 * 
			 */
		}
		return result;	
	}
	
	@Override
	public List<Map<String, String>> getAllChildNodeInfos(Map<String, String> paramMap) {
    	
		Map<String, String> param = new HashMap<>();
		List<Map<String, String>> nodeInfos = new ArrayList<>();
		
		int result = 0;
		String fileTrgtKey = "";
		
		param.put("upperKey", paramMap.get("lowerKey"));
		param.put("upperCd", paramMap.get("lowerCd"));
		param.put("coCd", paramMap.get("coCd"));
		param.put("salesCd", paramMap.get("salesCd"));
		List<Map<String, String>> topNodeInfo = zz01Mapper.selectBomlevelList(param);
	    if (topNodeInfo.size() > 0) {
		    for (Map<String, String> dtl : topNodeInfo) {
		    	
				fileTrgtKey = Integer.toString(zz01Mapper.selectBomSeqNext(param));
				dtl.put("FILE_TRGT_KEY",fileTrgtKey);
				dtl.put("UPPER_KEY", paramMap.get("fileTrgtKey") );
				dtl.put("UPPER_CD", paramMap.get("lowerCd") );
				result += zz01Mapper.copyBomTree(dtl);
				
//		    	nodeInfos.add(dtl);
		    	// 재귀적으로 자식 노드들의 정보 가져오기
		    	dtl.put("parentId",  fileTrgtKey);
		    	List<Map<String, String>> grandChildNodeInfos = getAllChildNodeInfos(dtl);
//		    	nodeInfos.addAll(grandChildNodeInfos);
			}
	    }

        return nodeInfos;
    }	
	
}