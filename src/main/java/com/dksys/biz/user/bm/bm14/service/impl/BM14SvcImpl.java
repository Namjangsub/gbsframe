package com.dksys.biz.user.bm.bm14.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.bm.bm14.mapper.BM14Mapper;
import com.dksys.biz.user.bm.bm14.service.BM14Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM14SvcImpl implements BM14Svc {
	
	@Autowired
	BM14Mapper bm14Mapper;
	
	@Autowired
	BM14Svc bm14Svc;

	private List<Map<String, String>> allChildNodeInfos;
	
	@Override
	public List<Map<String, String>> selectBomTreeList(Map<String, String> paramMap) {
		return bm14Mapper.selectBomTreeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap) {
		return bm14Mapper.selectBomlevelList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectBomAllLevelList(Map<String, String> paramMap) {
		return bm14Mapper.selectBomAllLevelList(paramMap);
	}

	@Override
	public Map<String, String> selectBomTreInfo(Map<String, String> paramMap) {
		return bm14Mapper.selectBomTreInfo(paramMap);
	}

	@Override
	public int checkBomId(Map<String, String> paramMap) {
		return bm14Mapper.checkBomId(paramMap);
	}

	// 삭제체크
	@Override
	public Map<String, String> deleteMatrbomChk(Map<String, String> paramMap) {
		return bm14Mapper.deleteMatrbomChk(paramMap);
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
					dtl.put("ordrsNo", paramMap.get("ordrsNo"));
					//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
					String updChk = dtl.get("updChk").toString();
					/* "dtaChk" 값을 확인하여
					 * "I"인 경우 bm14Mapper.insertPrjctDtl(dtl)을 호출하여  세부정보를 삽입하고,
					 * "U"인 경우 bm14Mapper.updatePrjctDtl(dtl)을 호출하여  세부정보를 업데이트하고,
					 * "D"인 경우 * bm14Mapper.deletePrjctDtl(dtl)을 호출하여  세부정보를 삭제.		 */
					if ("I".equals(updChk)) {
						//신규등록 키값 할당
						fileTrgtKey = Integer.toString(bm14Mapper.selectBomSeqNext(paramMap));
						dtl.put("fileTrgtKey", fileTrgtKey);
						result += bm14Mapper.insertBomTree(dtl);
					} else if ("U".equals(updChk)) {
						if (bm14Mapper.selectPchsBomCheck(dtl) == 0) {
							result += bm14Mapper.updateBom(dtl);
						} else {
							//error 구매BOM 작업완료 수정 불가
							throw new IllegalArgumentException("구매BOM 작업완료 수정 불가");
						}
					} else if ("D".equals(updChk)) {
						if (bm14Mapper.selectPchsBomCheck(dtl) == 0) {
							result += bm14Mapper.deleteBom(dtl);
							
							// SPARE PART 관련 처리
							if (paramMap.get("salesCd").equals(dtl.get("upperCd"))) { // 최상단 (salesCd)에서 해당 9000 삭제시
								bm14Mapper.allCancelSpareBom(dtl);
							} else if ("9000".equals(dtl.get("upperCd"))) { // 상위가 9000번일 경우
								Map<String,String> param = new HashMap<>();
								param.put("salesCd", dtl.get("salesCd"));
								param.put("coCd", dtl.get("coCd"));
								param.put("etcField3", "");
								param.put("upperCd", dtl.get("lowerCd"));
								bm14Mapper.cancelSpareBom(param);
							} else {	// 제일 아래 단계 일경우
								// 삭제 후 추천 SPARE 취소
								Map<String, String> selectSparePart = bm14Mapper.selectSparePart(dtl);
								if (selectSparePart != null) {
									Map<String,String> param = new HashMap<>();
									param.put("salesCd", dtl.get("salesCd"));
									param.put("coCd", dtl.get("coCd"));
									param.put("etcField3", "");
									param.put("lowerKey", selectSparePart.get("lowerKey"));
									bm14Mapper.recommendBom(param);
								}
							}
						} else {
							//error 구매BOM 작업완료 삭제 불가
							throw new IllegalArgumentException("구매BOM 작업완료 삭제 불가");
						}
					}
				}
		}
		return result;
		
	}

	@Override
	public int moveBom(List<Map<String, String>> paramList) {
		int result = 0;
		for(Map<String, String> paramMap : paramList) {
			result += bm14Mapper.moveBom(paramMap);
		}
		return result;
	}
	
	@Override
	public int deleteBom(Map<String, String> paramMap) {
		return bm14Mapper.deleteBom(paramMap);
	}
	
	@Override
	public int copyBomTree(List<Map<String, String>> paramList) {
		int result = 0;
		String fileTrgtKey = "";
		allChildNodeInfos = new ArrayList<>();
		
		for(Map<String, String> paramMap : paramList) {
			//신규등록 키값 할당
			fileTrgtKey = Integer.toString(bm14Mapper.selectBomSeqNext(paramMap));
			paramMap.put("fileTrgtKey", fileTrgtKey);
			paramMap.put("salesCdTo", paramMap.get("salesCd"));
			//트리 복사
			result += bm14Mapper.copyBomTree(paramMap);
			
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
		List<Map<String, String>> topNodeInfo = bm14Mapper.selectBomlevelList(param);
	    if (topNodeInfo.size() > 0) {
		    for (Map<String, String> dtl : topNodeInfo) {
		    	
				fileTrgtKey = Integer.toString(bm14Mapper.selectBomSeqNext(param));
				dtl.put("FILE_TRGT_KEY",fileTrgtKey);
				dtl.put("UPPER_KEY", paramMap.get("fileTrgtKey") );
				dtl.put("UPPER_CD", paramMap.get("lowerCd") );
				dtl.put("SALES_CD_TO", paramMap.get("salesCdTo"));
				dtl.put("ORDRS_NO_TO", paramMap.get("ordrsNoTo"));
				dtl.put("CHANGE_YN", paramMap.get("changeYn"));
				dtl.put("USER_ID", paramMap.get("userId"));
				dtl.put("PGM_ID", paramMap.get("pgmId"));
				
				//lowerKey를 가지고 fileTrgtKey로 추가하는 작업
				result += bm14Mapper.copyBomTree(dtl);
				
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
		return bm14Mapper.checkBomInfo(paramMap);
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
		Map<String, String> rootInfo = bm14Mapper.selectBomRootSalesCdTree(paramMapTarget);
		paramMap.put("fileTrgtKey", rootInfo.get("fileTrgtKey") );
		paramMap.put("upperCd", rootInfo.get("lowerCd") );
		paramMap.put("upperKey", rootInfo.get("lowerKey") );
		
		//타겟 SalesCode의 관련 정보 DB에서 읽어서 파라미터에 저장
		rootInfo = bm14Mapper.selectBomRootSalesCdTree(paramMap);
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
	
	
	@Override
	public List<Map<String, String>> selectBomAllLevelTempList(Map<String, String> paramMap) {
		return bm14Mapper.selectBomAllLevelTempList(paramMap);
	}
	
	@Override
	public String insertTempBom(Map<String, String> paramMap) {
		String result = null;
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		
		List<Map<String, String>> bomList = gsonDtl.fromJson(paramMap.get("bomArr"), dtlMap);
	    for (Map<String, String> dtl : bomList) {
	    	//재조회시 salesCd 획득을 위한 것
	    	dtl.put("coCd", paramMap.get("coCd"));
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	
	    	if (result == null || result.isEmpty()) {
	    		result = dtl.get("salesCd");
	    		//넣기 전 기존 데이터 삭제
	    		bm14Mapper.deleteTempBom(dtl);
	    	}
	    	
	    	bm14Mapper.insertTempBom(dtl);
	    }
	    
	    paramMap.put("salesCd", result);
	    bm14Mapper.callCheckTempBom(paramMap);
	    
		return result;	
	}
	
	@Override
	public Map<String, String> callDraftTempBom(Map<String, String> paramMap) {
		bm14Mapper.callDraftTempBom(paramMap);
		return paramMap;
	}
	
	@Override
	public List<Map<String, String>> selectBomAllEnterList(Map<String, String> paramMap) {
		return bm14Mapper.selectBomAllEnterList(paramMap);
	}
	
	@Override
	public int selectBomAllEnterListCount(Map<String, String> paramMap) {
		return bm14Mapper.selectBomAllEnterListCount(paramMap);
	}
	
	@Override
	public int confirmBom(Map<String, String> paramMap) {
		return bm14Mapper.confirmBom(paramMap);
	}
	
	@Override
	public int recommendBom(Map<String, String> paramMap) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		int result = 0;
		String fileTrgtKey = "";

		List<Map<String, String>> listArr = gson.fromJson(paramMap.get("recommendArr"), mapList);
		if (listArr != null && listArr.size() > 0 ) {
			int i = 0;
			for (Map<String, String> listArrMap : listArr) {
				try {
					
					result = bm14Mapper.recommendBom(listArrMap);

					Map<String, String> updateRow = bm14Mapper.selectBomInfo(listArrMap);				// 추천 Spare Part가 된 row 정보
					Map<String, String> selectSpareInfo = bm14Mapper.selectSpareInfo(listArrMap);		// 해당 SalesCd의 기존 Spare Part(9000) 정보 조회

					if ("SPARE".equals(listArrMap.get("etcField3"))) {	// 추천 spare part 지정시
						if (selectSpareInfo != null) {
							Map<String,String> param = new HashMap<>();
							param.put("coCd", listArrMap.get("coCd"));
							param.put("salesCd", listArrMap.get("salesCd"));
							param.put("upperCd", selectSpareInfo.get("lowerCd"));
							param.put("lowerCd", updateRow.get("upperCd"));
							Map<String, String> selectSparePart = bm14Mapper.selectSparePart(param);

							if (selectSparePart == null) {
								fileTrgtKey = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
								String upperKey = selectSpareInfo.get("lowerKey");
								String dsgnNo = listArrMap.get("salesCd") + "-" + updateRow.get("upperCd");
								Map<String,String> param1 = new HashMap<>();
								param1.putAll(updateRow);
								param1.put("upperKey", upperKey);
								param1.put("userId", listArrMap.get("userId"));
								param1.put("pgmId", listArrMap.get("pgmId"));
								param1.put("upperKey", upperKey);
								param1.put("fileTrgtKey", fileTrgtKey);
								param1.put("upperCd", "9000");
								param1.put("lowerCd", updateRow.get("upperCd"));
								param1.put("dsgnNo", dsgnNo);
								param1.put("unitNo", "");
								param1.put("etcField3", "");
								param1.put("matrSpec", "");
								result += bm14Mapper.insertBomTree(param1);	// 9000 하위에 추천 spare part 추가

								Map<String, String> dtlMap = new HashMap<>();
								String fileTrgtKey2 = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
								dtlMap.putAll(updateRow);
								dtlMap.put("upperKey", fileTrgtKey);
								dtlMap.put("fileTrgtKey", fileTrgtKey2);
								dtlMap.put("userId", listArrMap.get("userId"));
								dtlMap.put("pgmId", listArrMap.get("pgmId"));
								dtlMap.put("etcField3", "");
								result += bm14Mapper.insertBomTree(dtlMap);	// 추천 spare part의 상세정보 추가
							} else {
								Map<String, String> dtlMap2 = new HashMap<>();
								String fileTrgtKey2 = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
								dtlMap2.putAll(updateRow);
								dtlMap2.put("upperKey", selectSparePart.get("lowerKey"));
								dtlMap2.put("fileTrgtKey", fileTrgtKey2);
								dtlMap2.put("userId", listArrMap.get("userId"));
								dtlMap2.put("pgmId", listArrMap.get("pgmId"));
								dtlMap2.put("etcField3", "");
								result += bm14Mapper.insertBomTree(dtlMap2);
							}
						} else {
							Map<String, String> insertMap = new HashMap<>();
							String fileTrgtKey3 = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
							insertMap.put("coCd", listArrMap.get("coCd"));
							insertMap.put("salesCd", listArrMap.get("salesCd"));
							insertMap.put("upperKey", listArrMap.get("parentId"));
							insertMap.put("fileTrgtKey", fileTrgtKey3);
							insertMap.put("upperCd", listArrMap.get("salesCd"));
							insertMap.put("lowerCd", "9000");
							insertMap.put("dsgnNo", listArrMap.get("salesCd") + "-9000");
							insertMap.put("partNo", listArrMap.get("salesCd"));
							insertMap.put("unitNo", "9000");
							insertMap.put("matrNm", "SPARE PART");
							insertMap.put("matrRmk", "SPARE PART");
							insertMap.put("avlbStrtDt", "");
							insertMap.put("useYn", "Y");
							insertMap.put("matr", "N");
							insertMap.put("userId", listArrMap.get("userId"));
							insertMap.put("pgmId", listArrMap.get("pgmId"));
							insertMap.put("ordrsNo", listArrMap.get("salesCd").split("-")[0]);
							result += bm14Mapper.insertBomTree(insertMap);	// salesCd 아래에 SPARE PART(9000) 추가

							Map<String, String> selectSpareInfo2 = bm14Mapper.selectSpareInfo(listArrMap);		// 해당 SalesCd의 기존 Spare Part(9000) 정보 조회
							fileTrgtKey = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
							String upperKey = selectSpareInfo2.get("lowerKey");
							String dsgnNo = listArrMap.get("salesCd") + "-" + updateRow.get("upperCd");
							Map<String,String> param1 = new HashMap<>();
							param1.putAll(updateRow);
							param1.put("upperKey", upperKey);
							param1.put("userId", listArrMap.get("userId"));
							param1.put("pgmId", listArrMap.get("pgmId"));
							param1.put("upperKey", upperKey);
							param1.put("fileTrgtKey", fileTrgtKey);
							param1.put("upperCd", "9000");
							param1.put("lowerCd", updateRow.get("upperCd"));
							param1.put("dsgnNo", dsgnNo);
							param1.put("unitNo", "");
							param1.put("etcField3", "");
							param1.put("matrSpec", "");
							result += bm14Mapper.insertBomTree(param1);

							Map<String, String> dtlMap = new HashMap<>();
							String fileTrgtKey2 = Integer.toString(bm14Mapper.selectBomSeqNext(listArrMap));
							dtlMap.putAll(updateRow);
							dtlMap.put("upperKey", fileTrgtKey);
							dtlMap.put("fileTrgtKey", fileTrgtKey2);
							dtlMap.put("userId", listArrMap.get("userId"));
							dtlMap.put("pgmId", listArrMap.get("pgmId"));
							dtlMap.put("etcField3", "");
							result += bm14Mapper.insertBomTree(dtlMap);
						}
					} else if ("".equals(listArrMap.get("etcField3"))) {		// 추천 spare part 취소시
						Map<String, String> partMap = new HashMap<>(listArrMap);
						partMap.put("upperCd", "9000");
						partMap.put("lowerCd", updateRow.get("upperCd"));
						Map<String, String> selectSparePart = bm14Mapper.selectSparePart(partMap);	// 9000폴더 정보 조회
						Map<String, String> deletMap = new HashMap<>();
						deletMap.put("coCd", listArrMap.get("coCd"));
						deletMap.put("salesCd", listArrMap.get("salesCd"));
						deletMap.put("upperKey", selectSparePart.get("lowerKey"));
						deletMap.put("upperCd", updateRow.get("upperCd"));
						deletMap.put("lowerCd", updateRow.get("lowerCd"));
						int deleteCancelSpareDtl = bm14Mapper.deleteCancelSpareDtl(deletMap);	// 9000폴더 하위의 추천 spare part 삭제

						Map<String, String> selectSparePartDtl = bm14Mapper.selectSparePartDtl(deletMap);	// 9000폴더 하위의 추천 spare part 상세정보 조회
						if (selectSparePartDtl == null || selectSparePartDtl.isEmpty()) {
							deletMap.put("upperKey", selectSpareInfo.get("lowerKey"));
							deletMap.put("upperCd", "9000");
							deletMap.put("lowerCd", updateRow.get("upperCd"));
							bm14Mapper.deleteCancelSpareDtl(deletMap);
						}
					}
					i++;
				} catch (Exception e) {
					System.out.println("error2"+e.getMessage());
				}
			}
		}
		
		return result;
	}

}