package com.dksys.biz.user.wb.wb02.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.DateUtil;
import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.wb.wb02.mapper.WB02Mapper;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


@Service
@Transactional(rollbackFor = Exception.class)
public class WB02SvcImpl implements WB02Svc {
    
    @Autowired
    WB02Mapper wb02Mapper;

    
    @Autowired
    WB02Svc wb02Svc;
    
    @Autowired
    CM08Svc cm08Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
    
    @Override
	public int selectWbsRsltsPlanListCount(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsPlanListCount(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsPlanList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsPlanList(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsPlanExcelList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsPlanExcelList(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsResultExcelList1(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultExcelList1(paramMap);
	}
    
    
    @Override
	public List<Map<String, String>> selectWbsRsltsMasterList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsMasterList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsRsltsDetailList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsDetailList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap) {
		return wb02Mapper.selectRsltsSharngList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap) {
		return wb02Mapper.selectRsltsApprovalList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap) {
		return wb02Mapper.selectRsltsMemberList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return wb02Mapper.selectApprovalList(paramMap);
	}
	
	@Override
	public int selectMaxTrgtKey(Map<String, String> paramMap) {
		return wb02Mapper.selectMaxTrgtKey(paramMap);
	}
	
	@Override
	public int selectWbsPlanChk(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsPlanChk(paramMap);
	}
	
	// 실적 상세 테이블 삭제전 확인
	@Override
	public List<Map<String, String>> deleteWbsRsltsDetailChk(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsRsltsDetailChk(paramMap);
	}
	
	
	// 실적 상세 테이블 삭제
	@Override
	public int deleteWbsRsltsDetail(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsRsltsDetail(paramMap);
	}
	
	
	// 공유 테이블 삭제전 확인
	@Override
	public List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsSharngListChk(paramMap);
	}
		
		
	// 공유 테이블 삭제
	@Override
	public int deleteWbsSharngList(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsSharngList(paramMap);
	}
	
	// 결재 테이블 삭제전 확인
	@Override
	public List<Map<String, String>> deleteWbsApprovalListChk(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsApprovalListChk(paramMap);
	}
			
			
	// 결재 테이블 삭제
	@Override
	public int deleteWbsApprovalList(Map<String, String> paramMap) {
		return wb02Mapper.deleteWbsApprovalList(paramMap);
	}
	
	
	
	
	public int wbsLevel1RsltsInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) {
		int result = wb02Mapper.wbsRsltsMasterInsert(paramMap);	
		Gson gson = new Gson();		
		Type stringList1 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> rsltsArr = gson.fromJson(paramMap.get("rowRsltsListArr"), stringList1);
		if (rsltsArr != null && rsltsArr.size() > 0 ) {
			
			int i = 0;
	        for (Map<String, String> rsltsMap : rsltsArr) {
	            try {
	            	rsltsMap.put("coCd", paramMap.get("coCd"));
	            	rsltsMap.put("wbsRsltsNo", paramMap.get("wbsRsltsNo"));
	            	rsltsMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	rsltsMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeId"));
	            	rsltsMap.put("wbsRsltsSeq", Integer.toString(i));
	            	rsltsMap.put("wbsRsltsRmk", paramMap.get("wbsRsltsRmk"));
	            	rsltsMap.put("creatId", paramMap.get("creatId"));
	            	rsltsMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.wbsRsltsDetailInsert(rsltsMap);          		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
         
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
	            	
	            	sharngMap.put("coCd", paramMap.get("coCd"));
	            	sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	            	sharngMap.put("todoDiv1CodeId", paramMap.get("S_todoDiv1CodeId"));
	            	sharngMap.put("todoDiv2CodeId", paramMap.get("S_todoDiv2CodeId"));	
	            	sharngMap.put("wbsRsltsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	sharngMap.put("creatId", paramMap.get("creatId"));
	            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {
	            	approvalMap.put("coCd", paramMap.get("coCd"));
	            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));   
	            	approvalMap.put("todoDiv1CodeId", paramMap.get("A_todoDiv1CodeId"));
	            	approvalMap.put("todoDiv2CodeId", paramMap.get("A_todoDiv2CodeId"));	
	            	approvalMap.put("wbsRsltsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	approvalMap.put("creatId", paramMap.get("creatId"));
	            	approvalMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.insertWbsApprovalList(approvalMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		/*
		 * Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		 * List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"),
		 * stringList); for(String fileKey : deleteFileList) {
		 * cm08Svc.deleteFile(fileKey); } for (int i = 0; i <
		 * mRequest.getFiles("files").size(); i++) { try {
		 * cm08Svc.uploadTreeFile("TB_WB02M01", paramMap, mRequest); } catch(Exception
		 * e){ System.out.println("error4"+e.getMessage()); } }
		 */

		
		return result;
	}
	
	@Override
	public int selectWbsRsltsResultCount(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultCount(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsResultList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultList(paramMap);
	}
 
    
    @Override
	public List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsPlanInfoSelect(paramMap);
	}
    
    
    @Override
	public List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap) {
		return wb02Mapper.selectMaxWbsPlanNo(paramMap);
	}
    
    
    
    public int wbsLevel1RsltsUpdate(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) {
		int result = wb02Mapper.wbsRsltsMasterUpdate(paramMap);	
		Gson gson = new Gson();		
		Type stringList1 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> rsltsArr = gson.fromJson(paramMap.get("rowRsltsListArr"), stringList1);
		if (rsltsArr != null && rsltsArr.size() > 0 ) {
			
			int i = 0;
	        for (Map<String, String> rsltsMap : rsltsArr) {
	            try {
	            	rsltsMap.put("coCd", paramMap.get("coCd"));
	            	rsltsMap.put("wbsRsltsNo", paramMap.get("wbsRsltsNo"));
	            	rsltsMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	rsltsMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeId"));
	            	rsltsMap.put("wbsRsltsSeq", Integer.toString(i));
	            	rsltsMap.put("wbsRsltsRmk", paramMap.get("wbsRsltsRmk"));
	            	rsltsMap.put("creatId", paramMap.get("creatId"));
	            	rsltsMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.wbsRsltsDetailInsert(rsltsMap);          		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
         
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
	            	
	            	sharngMap.put("coCd", paramMap.get("coCd"));
	            	sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	            	sharngMap.put("todoDiv1CodeId", paramMap.get("S_todoDiv1CodeId"));
	            	sharngMap.put("todoDiv2CodeId", paramMap.get("S_todoDiv2CodeId"));	
	            	sharngMap.put("wbsRsltsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	sharngMap.put("creatId", paramMap.get("creatId"));
	            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {
	            	approvalMap.put("coCd", paramMap.get("coCd"));
	            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));   
	            	approvalMap.put("todoDiv1CodeId", paramMap.get("A_todoDiv1CodeId"));
	            	approvalMap.put("todoDiv2CodeId", paramMap.get("A_todoDiv2CodeId"));	
	            	approvalMap.put("wbsRsltsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
	            	approvalMap.put("creatId", paramMap.get("creatId"));
	            	approvalMap.put("creatPgm", paramMap.get("creatPgm"));
	            	wb02Mapper.insertWbsApprovalList(approvalMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		/*
		 * Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		 * List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"),
		 * stringList); for(String fileKey : deleteFileList) {
		 * cm08Svc.deleteFile(fileKey); } for (int i = 0; i <
		 * mRequest.getFiles("files").size(); i++) { try {
		 * cm08Svc.uploadTreeFile("TB_WB02M01", paramMap, mRequest); } catch(Exception
		 * e){ System.out.println("error4"+e.getMessage()); } }
		 */

		
		return result;
	}
    
    @Override
	public List<Map<String, String>> selectWbsPlanConfirmCount(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsPlanConfirmCount(paramMap);
	}
    
    @Override
	public int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsPlanDeleteConfirmCount(paramMap);
	}
    
    
    @Override
	public int deleteWbsPlanlist(Map<String, String> paramMap) {
    	wb02Mapper.deleteWbsRsltsDetailSub(paramMap);
    	wb02Mapper.deleteWbsSharngListSub(paramMap);
    	wb02Mapper.deleteWbsApprovalListSub(paramMap);
		return wb02Mapper.deleteWbsPlanlist(paramMap);
	}
    
    @Override
	public int wbsPlanStsCodeUpdate(Map<String, String> paramMap) {
    	
		return wb02Mapper.wbsPlanStsCodeUpdate(paramMap);
	}
    
    
    
    
    @Override
	public int updateWbsPlanLockYnLvl1(Map<String, String> paramMap) {
		return wb02Mapper.updateWbsPlanLockYnLvl1(paramMap);
	}
    
    @Override
	public int updateWbsPlanLockYnLvl2(Map<String, String> paramMap) {
		return wb02Mapper.updateWbsPlanLockYnLvl2(paramMap);
	}
    
    @Override
	public int updateWbsPlanLockYnLvl3(Map<String, String> paramMap) {
		return wb02Mapper.updateWbsPlanLockYnLvl3(paramMap);
	}
    
    
    
    @Override
	public int updateWbsPlanCloseYn(Map<String, String> paramMap) {
    	wb02Mapper.updateWbsRsltsMasterCloseYn(paramMap);
    	wb02Mapper.updateWbsRsltsDetailCloseYn(paramMap);
		return wb02Mapper.updateWbsPlanCloseYn(paramMap);
	}
    
    
    
    
    /* WBS 실적메인화면 실적조회 부분 수정 추가 */
    @Override
	public int selectWbsRsltsResultCountM(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultCountM(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsResultListM(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultListM(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsResultExcelListM(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsResultExcelListM(paramMap);
	}
    /* WBS 실적메인화면 실적조회 부분 수정 추가 END */
    
    
    
    
    
    @Override
	public int selectWbsRsltsListCount(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsListCount(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectWbsRsltsList(Map<String, String> paramMap) {
		return wb02Mapper.selectWbsRsltsList(paramMap);
	}
    
    
    
}