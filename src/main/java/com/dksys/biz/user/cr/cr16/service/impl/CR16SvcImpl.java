package com.dksys.biz.user.cr.cr16.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr16.mapper.CR16Mapper;
import com.dksys.biz.user.cr.cr16.service.CR16Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR16SvcImpl implements CR16Svc {

	@Autowired
	CR16Mapper cr16Mapper;
	
	@Autowired
    CM08Svc cm08Svc;
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CR16Svc cr16Svc;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectSalesYearPlanListCount(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanList(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanList(paramMap);
	}
	
	@Override
	public int deleteSalesYearPlanList(Map<String, String> paramMap) {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {                                    
	            	 cr16Mapper.deleteSalesYearPlanM(arrMap);
	            	 cr16Mapper.deleteSalesYearPlanD(arrMap);
	            	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanMC(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanMC(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanMU(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanMU(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectSalesYearPlanD(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanD(paramMap);
	}
	
    @Override
    public int salesPlanYearInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		int fileTrgtKey = cr16Mapper.selectSalesYearPlanSeqNext(paramMap);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {   	            	    
	            	arrMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
	            	
	            	//수주와 매출을 동시에 입력해야 하므로 한줄당 2번의 업데이트 발생
            		//수주
	            	arrMap.put("salesPlanAmt", arrMap.get("salesPlanAmt10").toString());
	            	arrMap.put("salesPlanDiv", "SALESPLANDIV10");
	            	cr16Mapper.salesPlanYearInsert(arrMap);
	            	
	            	//매출
	            	arrMap.put("salesPlanAmt", arrMap.get("salesPlanAmt20").toString());
	            	arrMap.put("salesPlanDiv", "SALESPLANDIV20");
	            	cr16Mapper.salesPlanYearInsert(arrMap);
	            	
	            	result++;	            	 
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
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

		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//--------------------------------------------------------------- 
			    
		return result;
	}
    
    
    @Override
    public int salesPlanYearUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
    	int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {   
	            	
            		//수주와 매출을 동시에 입력해야 하므로 한줄당 2번의 업데이트 발생
            		//수주
	            	arrMap.put("salesPlanAmt", arrMap.get("salesPlanAmt10").toString());
	            	arrMap.put("salesPlanDiv", "SALESPLANDIV10");
	            	cr16Mapper.salesPlanYearUpdate(arrMap);
	            	
	            	//매출
	            	arrMap.put("salesPlanAmt", arrMap.get("salesPlanAmt20").toString());
	            	arrMap.put("salesPlanDiv", "SALESPLANDIV20");
	            	cr16Mapper.salesPlanYearUpdate(arrMap);
	            	
	            	result++;	            	 
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
    	
		
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
    	
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
	    }
	    String fKey = "";
	    for(String fileKey : deleteFileList) {
	    	if (!fKey.equals(fileKey)) {
	    		cm08Svc.deleteFile(fileKey);		    	
	    	}
	    	fKey = fileKey;
	    }
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
			    
		return result;
	}
    
    @Override
    public int salesYearPlanCloseY(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {   	            	
		            	List<Map<String, String>> listChk = cr16Mapper.salesYearPlanCloseChk(arrMap);
		    			if (listChk.size() == 0) {
		    				cr16Mapper.salesYearPlanCloseInsert(arrMap);
			            	result++;
		    			}	  
		    			else {
		    				cr16Mapper.salesYearPlanCloseUpdate(arrMap);
			            	result++;
		    			}
		    			cr16Mapper.salesYearPlanCloseChkUpdateY(arrMap);
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}		
		return result;
	}
    
    @Override
    public int salesYearPlanCloseN(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {   	            	
	            		cr16Mapper.salesYearPlanCloseDelete(arrMap);
	            		cr16Mapper.salesYearPlanCloseChkUpdateN(arrMap);
	            		result++;           	 
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}		
		return result;
	}
    
    @Override
	public List<Map<String, String>> selectSalesYearPlanListHist(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanListHist(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectSalesYearPlanMC2(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanMC2(paramMap);
	}
    
    @Override
	public List<Map<String, String>> selectSalesYearPlanD2(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesYearPlanD2(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSalesDeptList(Map<String, String> paramMap) {
		//회사가 건양ITT인가 아닌가에 따른 부서 조회
		if(paramMap.get("coCd").equals("GUN")) {//건양ITT일때
			return cr16Mapper.selectSalesGunDeptList(paramMap);
		}else if(paramMap.get("coCd").equals("TRN")){//건양트루넷일때
			return cr16Mapper.selectSalesTrnDeptList(paramMap);
		}else{//전체일 경우
			return null;
		}
	}

	@Override
	public void callPlanClose(Map<String, String> paramMap) {
		cr16Mapper.callPlanClose(paramMap);
		
	}
	
	@Override
	public List<Map<String, String>> selectSalesPlanHistList(Map<String, String> paramMap) {
		return cr16Mapper.selectSalesPlanHistList(paramMap);
	}
}
