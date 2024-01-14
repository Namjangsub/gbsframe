package com.dksys.biz.admin.bm.bm16.service.impl;

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

import com.dksys.biz.admin.bm.bm16.mapper.BM16Mapper;
import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM16SvcImpl implements BM16Svc {

  @Autowired
  BM16Mapper bm16Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Autowired
  QM01Mapper QM01Mapper;
  
  @Override
  public int selectPrjctCount(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectPrjctList(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectItemList(Map<String, String> paramMap) {
    return bm16Mapper.selectItemList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectPrdtList(Map<String, String> paramMap) {
    return bm16Mapper.selectPrdtList(paramMap);
  }

  @Override
  public Map<String, String> selectPrjctInfo(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctInfo(paramMap);
  }

  @Override
  public int selectConfirmCount(Map<String, String> paramMap) {
    return bm16Mapper.selectConfirmCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectFileList(Map<String, String> paramMap) {
    return bm16Mapper.selectFileList(paramMap);
  }

  @Override
  public int updatePrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//	Gson gson = new Gson();
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

	int result = bm16Mapper.updatePrjct(paramMap);
    	//  bm16Mapper.updatePrjct(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 업데이트하고 그 결과를 result 변수에 저장.


    List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
    for (Map<String, String> dtl : dtlPrdt) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행
    	
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
    	String dtaChk = dtl.get("prjctCrudChk").toString();
    	/* "dtaChk" 값을 확인하여
    	 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
    	 * "U"인 경우 bm16Mapper.updatePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
    	 * "D"인 경우 * bm16Mapper.deletePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
    	if ("I".equals(dtaChk)) {
    		dtl.put("prjctSeq", paramMap.get("prjctSeq"));
    		bm16Mapper.insertPrjctPrdt(dtl);
    	} else if ("U".equals(dtaChk)) {
    		bm16Mapper.updatePrjctPrdt(dtl);
    	} else if ("D".equals(dtaChk)) {
    		bm16Mapper.deletePrjctPrdt(dtl);
    		bm16Mapper.deletePrjctDtlPrdtAll(dtl);
    	}
    }
    
    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("prdtItemArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행

      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
      //      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
      String dtaChk = dtl.get("dtaChk").toString();
		/* "dtaChk" 값을 확인하여
		 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
		 * "U"인 경우 bm16Mapper.updatePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
		 * "D"인 경우 * bm16Mapper.deletePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
      if ("I".equals(dtaChk)) {
	        bm16Mapper.insertPrjctDtl(dtl);
	      } else if ("U".equals(dtaChk)) {
	        bm16Mapper.updatePrjctDtl(dtl);
	      } else if ("D".equals(dtaChk)) {
	        bm16Mapper.deletePrjctDtl(dtl);
	      }
    }
    
	//---------------------------------------------------------------  
	//첨부 화일 처리 시작 
	//---------------------------------------------------------------  
    if (uploadFileList.size() > 0) {
	    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
	    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
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
  public int insertPrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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
		String  newPrjctSeq = String.valueOf(bm16Mapper.selectPrjctSeqNext(paramMap));
		paramMap.put("prjctSeq", newPrjctSeq);
		int result = bm16Mapper.insertPrjct(paramMap);
	
	    List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
	    for (Map<String, String> dtl : dtlPrdt) {
	    	dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	bm16Mapper.insertPrjctPrdt(dtl);
	    }
	    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("prdtItemArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	dtl.put("prjctSeq", newPrjctSeq);
	    	dtl.put("userId", paramMap.get("userId"));
	    	dtl.put("pgmId", paramMap.get("pgmId"));
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
	    	String dtaChk = dtl.get("dtaChk").toString();
	    	/* "dtaChk" 값을 확인하여
	    	 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
	    	if ("I".equals(dtaChk)) {
	    		bm16Mapper.insertPrjctDtl(dtl);
	    	} 
	    }
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
	    
	    return result;
  }

  @Override
  public int deletePrjct(Map<String, String> paramMap) throws Exception {
	    //---------------------------------------------------------------  
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
	    HashMap<String, String> param = new HashMap<>();
	    param.put("jobType", "fileDelete");
	    param.put("userId", paramMap.get("userId"));
	    if (deleteFileList.size() > 0) {
		    for (Map<String, String> dtl : deleteFileList) {
					//접근 권한 없으면 Exception 발생
		            param.put("comonCd",  dtl.get("comonCd"));
			    	
					cm15Svc.selectFileAuthCheck(param);
			}
	    }
		//---------------------------------------------------------------  
		//첨부 화일 권한체크 끝 
		//---------------------------------------------------------------  
	  
	  int result = bm16Mapper.deletePrjctPrdtSeqAll(paramMap);
		  result = bm16Mapper.deletePrjctDtlSeqAll(paramMap);
		  result = bm16Mapper.deletePrjct(paramMap);
	  
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
		    }
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  	  
	    return result;
  }

  
  @Override
  public Map<String, String> selectPrjctIssueInfo(Map<String, String> paramMap) {
	  return bm16Mapper.selectPrjctIssueInfo(paramMap);
  }
 
  @Override
  public int insertPrjctIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	  int result = bm16Mapper.insertPrjctIssue(paramMap);
	  
	  Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
      Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
      
      Gson gson = new Gson();	
      
      String pgParam = "{\"actionType\":\""+ "U" +"\",";      
      pgParam += "\"prjctSeq\":\""+ paramMap.get("prjctSeq") +"\"}";
      
      String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("prjctNm") + "이슈 공유";
		      
      Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            		sharngMap.put("reqNo", paramMap.get("prjctSeq"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
	            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
	            	    sharngMap.put("userId", paramMap.get("userId"));
	            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap.put("pgParam", pgParam);
	            	    sharngMap.put("todoTitle", todoTitle1);
	                	QM01Mapper.insertWbsSharngList(sharngMap);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("prjctNm") + "이슈 결재";
		
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
	            		approvalMap.put("reqNo", paramMap.get("prjctSeq"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
		            	approvalMap.put("pgmId", paramMap.get("pgmId"));
		            	approvalMap.put("userId", paramMap.get("userId"));
		            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
		            	approvalMap.put("pgParam", pgParam);
		            	approvalMap.put("todoTitle", todoTitle2);
	                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
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
		    paramMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
		
	  return result;
  }
  
  @Override
  public int updatePrjctIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	    int result = bm16Mapper.updatePrjctIssue(paramMap);
	  
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
	    
	    

		Gson gson = new Gson();					

		String pgParam = "{\"actionType\":\""+ "U" +"\",";      
	    pgParam += "\"prjctSeq\":\""+ paramMap.get("prjctSeq") +"\"}";
	    
		paramMap.put("reqNo", paramMap.get("issNo"));
		paramMap.put("salesCd", paramMap.get("prjctSeq"));
		
		
		
		
		
		//List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
		//if (sharngChk.size() > 0) {
		//	QM01Mapper.deleteWbsSharngList(paramMap); 
		//}
		
		//List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk(paramMap); 
		//if (approvalgChk.size() > 0) {
		//	QM01Mapper.deleteWbsApprovalList(paramMap); 
		//}
		
		
		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
		
			String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("prjctNm") + "이슈 공유";
			
			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> sharngMap : sharngArr) {
		            try {	 
		            		sharngMap.put("reqNo", paramMap.get("prjctSeq"));
		            	    sharngMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
		            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
		            	    sharngMap.put("userId", paramMap.get("userId"));
		            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
		            	    sharngMap.put("pgParam", pgParam);
		            	    sharngMap.put("todoTitle", todoTitle1);
		                	QM01Mapper.insertWbsSharngList(sharngMap);       		
		            	i++;
		            } catch (Exception e) {
		                System.out.println("error2"+e.getMessage());
		            }
		        }
			}

			
			String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("prjctNm") + "이슈 결재";
			//결재
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			if (approvalArr != null && approvalArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> approvalMap : approvalArr) {
		            try {	 
		            		approvalMap.put("reqNo", paramMap.get("prjctSeq"));
			            	approvalMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
			            	approvalMap.put("pgmId", paramMap.get("pgmId"));
			            	approvalMap.put("userId", paramMap.get("userId"));
			            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
			            	approvalMap.put("pgParam", pgParam);
			            	approvalMap.put("todoTitle", todoTitle2);
		                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
		                	i++;
		            } catch (Exception e) {
		                System.out.println("error2"+e.getMessage());
		            }
		        }
			}
		
		
		}
		
		
		
		
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("prjctSeq"));
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
  public List<Map<String, String>> selectPrjctOrderBillChart(Map<String, String> paramMap) {
	  return bm16Mapper.selectPrjctOrderBillChart(paramMap);
  }
  
  @Override
  public int selectPrjctPlanCount(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctPlanCount(paramMap);
  }

  @Override
  public List<Map<String, Object>> selectPrjctPlanList(Map<String, String> paramMap) {
	  HashMap<String, Object> map = new HashMap<String, Object>();
    return bm16Mapper.selectPrjctPlanList(paramMap);
  }
  
  @Override
  public Map<String, String> selectTodoIssueInfo(Map<String, String> paramMap) {
	  return bm16Mapper.selectTodoIssueInfo(paramMap);
  }
  
  //그리드 검색
  @Override
  public List<Map<String, String>> select_wbs_code(Map<String, String> paramMap) {
	 return bm16Mapper.select_wbs_code(paramMap);
  }
  
  //그리드 검색
  @Override
  public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
	 return bm16Mapper.select_prjct_code(paramMap);
  }
  
}