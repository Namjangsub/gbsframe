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
			paramMap.put("salesCdTo", paramMap.get("salesCd"));
			//트리 복사
			result += zz01Mapper.copyBomTree(paramMap);
			
			allChildNodeInfos = getAllChildNodeInfos(paramMap);
//			for(Map<String, String> NodeInfo : allChildNodeInfos) {
//			}
			
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
	
	@Override
	public List<Map<String, String>> getAllChildNodeInfos(Map<String, String> paramMap) {
    	
		Map<String, String> param = new HashMap<>();
		List<Map<String, String>> nodeInfos = new ArrayList<>();
		
		int result = 0;
		String fileTrgtKey = "";
		/*
		 * 파라메테 체크 필요
		 * {upperCd=0050, coCd=GUN, salesCdTo=23010-99TVFTE, salesCd=23010-99TVFTE, upperKey=244}
		 */
		param.put("upperKey", paramMap.get("lowerKey"));
		param.put("upperCd", paramMap.get("lowerCd"));
		param.put("coCd", paramMap.get("coCd"));
		param.put("salesCd", paramMap.get("salesCd"));
		param.put("salesCdTo", paramMap.get("salesCdTo"));
		 System.out.println("변수 salesCdTo의 값: " + paramMap.get("salesCdTo"));
		List<Map<String, String>> topNodeInfo = zz01Mapper.selectBomlevelList(param);
	    if (topNodeInfo.size() > 0) {
		    for (Map<String, String> dtl : topNodeInfo) {
		    	
				fileTrgtKey = Integer.toString(zz01Mapper.selectBomSeqNext(param));
				dtl.put("FILE_TRGT_KEY",fileTrgtKey);
				dtl.put("UPPER_KEY", paramMap.get("fileTrgtKey") );
				dtl.put("UPPER_CD", paramMap.get("lowerCd") );
				dtl.put("SALES_CD_TO", paramMap.get("salesCdTo"));
				dtl.put("CHANGE_YN", paramMap.get("changeYn"));
				dtl.put("USER_ID", paramMap.get("userId"));
				dtl.put("PGM_ID", paramMap.get("pgmId"));
				
				//lowerKey를 가지고 fileTrgtKey로 추가하는 작업
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

	@Override
	public int checkBomInfo(Map<String, String> paramMap) {
		return zz01Mapper.checkBomInfo(paramMap);
	}
	
	
	@Override
	public int copyBomRootSalesCdTree(Map<String, String> paramMap) {
		int result = 1;
		String fileTrgtKey = "";
		allChildNodeInfos = new ArrayList<>();

		//프론트엔드에서 넘어오는 값
		// 회사코드          : coCd 
		// SalseCd    : salesCd  <--원본 SalseCd
		// 복사 SalseCd : salesCdTo
		//-----------------------------------------------------------------
		//타겟 SalesCode의 관련 정보 DB에서 fileTrgtKey 키값을 읽어서 파라미터에 저장
		Map<String, String> paramMapTarget = new HashMap<>(paramMap);
		paramMapTarget.put("salesCd", paramMap.get("salesCdTo") );
		Map<String, String> rootInfo = zz01Mapper.selectBomRootSalesCdTree(paramMapTarget);
		paramMap.put("fileTrgtKey", rootInfo.get("fileTrgtKey") );
		paramMap.put("upperCd", rootInfo.get("lowerCd") );
		paramMap.put("upperKey", rootInfo.get("lowerKey") );
		
		//타겟 SalesCode의 관련 정보 DB에서 읽어서 파라미터에 저장
		rootInfo = zz01Mapper.selectBomRootSalesCdTree(paramMap);
		paramMap.put("lowerCd", rootInfo.get("lowerCd") );
		paramMap.put("lowerKey", rootInfo.get("lowerKey") );

		//자식노드 재귀호출을 통한 복사작업 수행함
		allChildNodeInfos = getAllChildNodeInfos(paramMap);
//		for(Map<String, String> NodeInfo : allChildNodeInfos) {
//			allChildNodeInfos 함수내에서 트랜잭션 처리하므로 여기서는 패스함
//		}
			
		/*
		 * {coCd=GUN, salesCd=23010-99TVFTE, parentId=114, upperCd=2000, upperKey=114, childId=129, lowerCd=4200, lowerKey=129, fileTrgtKey=216}
		 * 
		 */
		return result;	
	}
		
}