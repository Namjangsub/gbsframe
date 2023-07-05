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
import com.dksys.biz.user.wb.wb01.mapper.WB01Mapper;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
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
    WB01Mapper wb01Mapper;

    
    @Autowired
    WB02Svc wb02Svc;
    
    @Autowired
    CM08Svc cm08Svc;
	
    @Autowired
    CM15Svc cm15Svc;
    
    @Autowired
    ExceptionThrower thrower;

    @Override
    public int deleteWbsRsltslist(Map<String, String> paramMap) {
	    int result = 0;
	    Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {
	            	 wb02Mapper.deleteWbsRsltsDetailSub(arrMap);
	            	 wb02Mapper.deleteWbsSharngListSub(arrMap);
	            	 wb02Mapper.deleteWbsApprovalListSub(arrMap);
	        	 wb02Mapper.deleteWbsRsltslist(arrMap);
	        	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
    }

 @Override
     public int updateWbsRsltsCloseYn(Map<String, String> paramMap) {
		int result = 0;
		Gson gson = new Gson();
	    Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> arr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (arr != null && arr.size() > 0 ) {
			for (Map<String, String> arrMap : arr) {
	            try {                                    
	            	 wb02Mapper.updateWbsRsltsCloseYn(arrMap);
	            	 wb01Mapper.updateWbsPlanCloseYn(arrMap);
	            	 result++;
	            } catch (Exception e) {
	                 System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		return result;
	}


       @Override
	public List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap) {
		return wb02Mapper.selectRsltsMemberList(paramMap);
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
   	public Map<String, String> selectWbsRsltsInfo(Map<String, String> paramMap) {
   		return wb02Mapper.selectWbsRsltsInfo(paramMap);
   	}

	public int wbsLevel1RsltsInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb02Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
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
	            	//rsltsMap.put("wbsRsltsRmk", paramMap.get("wbsRsltsRmk"));
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
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
				
		return result;
	}

    @Override
	public int wbsPlanStsCodeUpdate(Map<String, String> paramMap) {
    	
		return wb02Mapper.wbsPlanStsCodeUpdate(paramMap);
	}



    @Override
    public int wbsLevel1RsltsUpdate(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
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
	            	//rsltsMap.put("wbsRsltsRmk", paramMap.get("wbsRsltsRmk"));
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

    @Override
	public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return wb02Mapper.selectApprovalList(paramMap);
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
}
    