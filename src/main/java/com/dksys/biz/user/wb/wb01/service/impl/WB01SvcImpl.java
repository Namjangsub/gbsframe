package com.dksys.biz.user.wb.wb01.service.impl;

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
import com.dksys.biz.user.wb.wb01.mapper.WB01Mapper;
import com.dksys.biz.user.wb.wb01.service.WB01Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB01SvcImpl implements WB01Svc {

	@Autowired
	WB01Mapper wb01Mapper;
	
	@Autowired
    CM08Svc cm08Svc;
	
	@Autowired
	  CM15Svc cm15Svc;
	
	@Autowired
	WB01Svc wb01Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsPlanCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPlanList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsPlanExcelList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanExcelList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanNoList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectPlanSharngList(Map<String, String> paramMap) {
		return wb01Mapper.selectPlanSharngList(paramMap);
	}

	@Override
	public List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap) {
		return wb01Mapper.deleteWbsSharngListChk(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsSalesCodeList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPlanDivList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanDivList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsLevel1List(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsLevel1List(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsDsgnDifList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsDsgnDifList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPrdctnDifList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPrdctnDifList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsLevel2List(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsLevel2List(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsLevel3List(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsLevel3List(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPlanStsList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanStsList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPopupLevel2PlanList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPopupLevel2PlanList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPopupLevel3PlanList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPopupLevel3PlanList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsPopupSharngList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPopupSharngList(paramMap);
	}

	public int insertWbsSharngUser(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = wb01Mapper.insertWbsSharngUser(paramMap);
		return result;
	}

	@Override
	public int deleteWbsSharngUser(Map<String, String> paramMap) {
		int result = wb01Mapper.deleteWbsSharngUser(paramMap);
		return result;
	}

	@Override
	public int selectWbsPlanDeleteConfirmCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanDeleteConfirmCount(paramMap);
	}

	@Override
	public int deleteWbsSharngList(Map<String, String> paramMap) {
		int result = wb01Mapper.deleteWbsSharngList(paramMap);
		return result;
	}
	

	@Override
	public List<Map<String, String>> selectMaxWbsPlanNo(Map<String, String> paramMap) {
		return wb01Mapper.selectMaxWbsPlanNo(paramMap);
	}
	

	public int insertWbsSharngUser(Map<String, String> paramMap) {
		int result = wb01Mapper.insertWbsSharngUser(paramMap);
		return result;
	}

	public int insertToDoList(Map<String, String> paramMap) {
		int result = wb01Mapper.insertToDoList(paramMap);
		return result;
	}

	public int wbsLevel1PlanInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                      
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb01Mapper.wbsLevel1PlanInsert(paramMap);

		return result;
	}

	public int wbsLevel1PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                       
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		int result = wb01Mapper.wbsLevel1PlanUpdate(paramMap);
		return result;
	}

	@Override
	public List<Map<String, String>> selectWbsInfoList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsInfoList(paramMap);
	}

	public int wbsLevel2PlanInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                       
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
	            	    
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		int result = wb01Mapper.wbsLevel2PlanInsert(paramMap);
		return result;
	}

	public int wbsLevel2PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                       
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		int result = wb01Mapper.wbsLevel2PlanUpdate(paramMap);
		return result;
	}

	public int wbsLevel3PlanInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                      
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		int result = wb01Mapper.wbsLevel3PlanInsert(paramMap);

		return result;
	}

	public int wbsLevel3PlanUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> deleteFileList = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
		for(String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
			try {
				cm08Svc.uploadTreeFile("TB_WB01M01", paramMap, mRequest);
			}
			catch(Exception e){
				System.out.println("error4"+e.getMessage());
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("creatId"));
		            	sharngMap.put("creatPgm", paramMap.get("creatPgm"));                                      
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		int result = wb01Mapper.wbsLevel3PlanUpdate(paramMap);
		return result;
	}

	@Override
	public int selectWbsPlanConfirmCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanConfirmCount(paramMap);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public int selectMaxTrgtKey(Map<String, String> paramMap) {
		return wb01Mapper.selectMaxTrgtKey(paramMap);
	}


	@Override
	public List<Map<String, String>> selectWbsPlanInfoSelect(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanInfoSelect(paramMap);
	}


	@Override
	public List<Map<String, String>> selectFileCodeSelect(Map<String, String> paramMap) {
		return wb01Mapper.selectFileCodeSelect(paramMap);
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanTreeList(paramMap);
	}

	@Override
	public int selectWbsPlanTreeListCount(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanTreeListCount(paramMap);
	}
	
	@Override
	public Map<String, String> selectWbsPlanInfo(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanInfo(paramMap);
	}
	
	
	@Override
	public int insertWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb01Mapper.selectWbsPlanSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
	    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
	    
		
	    // 공유테이블 기존 데이터 삭제
	    List<Map<String, String>> sharngChk = wb01Mapper.deleteWbsSharngListChk(paramMap);
		if (sharngChk.size() > 0) {
			wb01Mapper.deleteWbsSharngList(paramMap);	
		}
		
		Gson gson = new Gson();
		// 공유테이블 등록  
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("userId"));
		            	sharngMap.put("creatPgm", paramMap.get("pgmId"));                                      
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생
				paramMap.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(paramMap);
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  

		
		int result = wb01Mapper.insertWbsPlan(paramMap);
	
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
	    return result;
	}
	
	@Override
	public int deleteWbsPlanlist(Map<String, String> paramMap) {
		wb01Mapper.deleteWbsSharngListSub(paramMap);
		int result = wb01Mapper.deleteWbsPlanlist(paramMap);
		return result;
	}
	

	@Override
	public int selectWbsPlanChk(Map<String, String> paramMap) {
		return wb01Mapper.selectWbsPlanChk(paramMap);
	}
	
    @Override
    public int updateWbsPlan(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
    	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
	
		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
   	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
				//접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
				param.put("jobType", "fileUp");
				cm15Svc.selectFileAuthCheck(param);
		}
		String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
		List<String> deleteFileList = Arrays.asList(deleteFileArr);
	    for(String fileKey : deleteFileList) {  // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			    Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
				//접근 권한 없으면 Exception 발생
			    param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			    param.put("jobType", "fileDelete");
				cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------  
		//첨부 화일 권한체크  끝 
		//---------------------------------------------------------------  
	
	    Gson gson = new Gson();
		// 공유테이블 등록  
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
		            	sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
		            	sharngMap.put("creatId", paramMap.get("userId"));
		            	sharngMap.put("creatPgm", paramMap.get("pgmId"));                                      
	            	    wb01Mapper.insertWbsSharngList(sharngMap);
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
	    
		int result = wb01Mapper.updateWbsPlan(paramMap);
	
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}