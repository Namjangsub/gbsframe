package com.dksys.biz.user.qm.qm01.service.impl;

import java.lang.reflect.Type;
import java.text.Format.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ocsp.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.qm.qm01.service.QM01Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class QM01SvcImpl implements QM01Svc {

  @Autowired
  QM01Mapper QM01Mapper;

  @Autowired
  CM15Svc cm15Svc;
  
  @Autowired
  CM08Svc cm08Svc;
  
  @Autowired
  WB20Svc wb20Svc;
  
  @Override
  public int selectQualityReqCount(Map<String, String> paramMap) {
    return QM01Mapper.selectQualityReqCount(paramMap);
  }
  
  @Override
  public int selectPurchaseListPopCount(Map<String, String> paramMap) {
    return QM01Mapper.selectPurchaseListPopCount(paramMap);
  }
  
  @Override
  public int selectShareUserCount(Map<String, String> paramMap) {
    return QM01Mapper.selectShareUserCount(paramMap);
  }
  
  @Override
  public int selectSignUserCount(Map<String, String> paramMap) {
    return QM01Mapper.selectSignUserCount(paramMap);
  }
  
    
 @Override
  public int selectShareUserResCount(Map<String, String> paramMap) {
    return QM01Mapper.selectShareUserResCount(paramMap);
  }

 @Override
 public int selectSignResCount(Map<String, String> paramMap) {
   return QM01Mapper.selectSignResCount(paramMap);
 }
 
  @Override
  public List<Map<String, String>> selectQualityReqList(Map<String, String> paramMap) {
    return QM01Mapper.selectQualityReqList(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectPurchaseListPop(Map<String, String> paramMap) {
    return QM01Mapper.selectPurchaseListPop(paramMap);
  }

  @Override
  public Map<String, String> selectQtyReqInfo(Map<String, String> paramMap) {
    return QM01Mapper.selectQtyReqInfo(paramMap);
  }
  
  @Override
  public Map<String, String> selectQtyReqRespInfo(Map<String, String> paramMap) {
    return QM01Mapper.selectQtyReqRespInfo(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectShareUserlst(Map<String, String> paramMap) {
    return QM01Mapper.selectShareUserlst(paramMap);
  }
    
  @Override
  public List<Map<String, String>> selectShareResUserlst(Map<String, String> paramMap) {
    return QM01Mapper.selectShareResUserlst(paramMap);
  }
  
  @Override
  public List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap) {
    return QM01Mapper.selectSignResUserlst(paramMap);
  }
  
//결재라인 부서명등 select 	  
  @Override
  public List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap) {
    return QM01Mapper.selectShareUserInfo(paramMap);
  }	
  
  /*
  @Override
  public List<Map<String, String>> selectSignUserInfo(Map<String, String> paramMap) {
    return QM01Mapper.selectSignUserInfo(paramMap);
  } */ 

  @Override
  public int selectConfirmCount(Map<String, String> paramMap) {
    return QM01Mapper.selectConfirmCount(paramMap);
  }

  @Override
  public int updateQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
    
    String midCode = paramMap.get("CODECOBGB");
	String midCd = midCode.substring(0, 7);
	paramMap.put("midCd", midCd);
    
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
	int result = QM01Mapper.updateQualityReq(paramMap);
	
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
    
    	//---------------------------------------------------------------  
  		// 결재라인 처리 시작 
  		//---------------------------------------------------------------		
  		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();		
  		if( paramMap.containsKey("approvalArr") ) {	
  			//결제라인 insert
  			result += wb20Svc.insertTodoMaster(paramMap);							
  		}		
  		//---------------------------------------------------------------  
  		// 결재라인 처리 end
  		//---------------------------------------------------------------
    
//    Gson gson = new Gson();	
	
//	List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
//	if (sharngChk.size() > 0) {
//		QM01Mapper.deleteWbsSharngList(paramMap); 
//
//	}
	
//	String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
//	pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
//	pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
//	pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
//	pgParam1 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
//
//	//공유				
//	Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
//	List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
//	if (sharngArr != null && sharngArr.size() > 0 ) {
//		int i = 0;
//        for (Map<String, String> sharngMap : sharngArr) {
//            try {	 
//            	    sharngMap.put("reqNo", paramMap.get("reqNo"));
//            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
//            	    sharngMap.put("userId", paramMap.get("userId"));
//            	    sharngMap.put("sanCtnSn", Integer.toString(i+1));
//            	    sharngMap.put("pgParam", pgParam1 );
//                	QM01Mapper.insertWbsSharngList(sharngMap);       		
//            	i++;
//            } catch (Exception e) {
//                System.out.println("error2"+e.getMessage());
//            }
//        }
//	}
//	
//	String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
//	pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
//	pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
//	pgParam2 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
//	pgParam2 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
//	
//	//결재
//	Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
//	List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
//	if (approvalArr != null && approvalArr.size() > 0 ) {
//		int i = 0;
//        for (Map<String, String> approvalMap : approvalArr) {
//            try {	 
//	            	approvalMap.put("reqNo", paramMap.get("reqNo"));
//	            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//	            	approvalMap.put("pgmId", paramMap.get("pgmId"));
//	            	approvalMap.put("userId", paramMap.get("userId"));
//	            	approvalMap.put("sanCtnSn", Integer.toString(i+1));
//	            	approvalMap.put("pgParam", pgParam2 );
//                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
//                	i++;
//            } catch (Exception e) {
//                System.out.println("error2"+e.getMessage());
//            }
//        }
//	}
	
	
     return result;
  }
  
  @Override
  public int updateQualityResp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//	Gson gson = new Gson();
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

	 paramMap.put("reqNo", paramMap.get("rsltNo")); //번호
	 //paramMap.put("coCd", paramMap.get("resCoCd")); //회사
	 paramMap.put("salesCd", paramMap.get("matrDrwNo")); //회사
	//---------------------------------------------------------------  
	//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
  	//   필수값 :  jobType, userId, comonCd
	//---------------------------------------------------------------  
    HashMap<String, String> param = new HashMap<>();
    param.put("userId", paramMap.get("userId"));
    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
    param.put("coCd", paramMap.get("coCd")); //회사
    
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
	int result = QM01Mapper.updateQualityResp(paramMap);

//	Gson gson = new Gson();	
//	
//	List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
//	if (sharngChk.size() > 0) {
//		QM01Mapper.deleteWbsSharngList(paramMap); 
//	}
//	
//	List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk(paramMap); 
//	if (approvalgChk.size() > 0) {
//		QM01Mapper.deleteWbsApprovalList(paramMap); 
//	}
	
//	String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
//	pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
//	pgParam1 += "\"coCd\":\""+ paramMap.get("resCoCd") +"\","; 
//	pgParam1 += "\"salesCd\":\""+ paramMap.get("matrDrwNo") +"\",";
//	pgParam1 += "\"reqNo\":\""+ paramMap.get("rsltNo") +"\"}";
//					
//	Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
//	List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
//	if (sharngArr != null && sharngArr.size() > 0 ) {
//		int i = 0;
//        for (Map<String, String> sharngMap : sharngArr) {
//            try {	 
//            	    sharngMap.put("reqNo", paramMap.get("reqNo"));
//            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
//            	    sharngMap.put("userId", paramMap.get("userId"));
//            	    sharngMap.put("todoCoCd", paramMap.get("coCd"));
//            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
//            	    sharngMap.put("pgParam", pgParam1);
//                	QM01Mapper.insertWbsSharngList(sharngMap);       		
//            	i++;
//            } catch (Exception e) {
//                System.out.println("error2"+e.getMessage());
//            }
//        }
//	}
//	
//	String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
//	pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
//	pgParam2 += "\"coCd\":\""+ paramMap.get("resCoCd") +"\","; 
//	pgParam2 += "\"salesCd\":\""+ paramMap.get("matrDrwNo") +"\",";
//	pgParam2 += "\"reqNo\":\""+ paramMap.get("rsltNo") +"\"}";
//	
//	Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
//	List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
//	if (approvalArr != null && approvalArr.size() > 0 ) {
//		int i = 0;
//        for (Map<String, String> approvalMap : approvalArr) {
//            try {	 
//	            	approvalMap.put("reqNo", paramMap.get("reqNo"));
//	            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//	            	approvalMap.put("pgmId", paramMap.get("pgmId"));
//	            	approvalMap.put("userId", paramMap.get("userId"));
//	            	approvalMap.put("todoCoCd", paramMap.get("coCd"));
//	            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
//	            	approvalMap.put("pgParam", pgParam2);
//                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
//                	i++;
//            } catch (Exception e) {
//                System.out.println("error2"+e.getMessage());
//            }
//        }
//	}
	
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
    
    	//---------------------------------------------------------------  
		// 결재라인 처리 시작 
		//---------------------------------------------------------------		
		Type dtl2Map = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();		
		if( paramMap.containsKey("approvalArr") ) {	
			//결제라인 insert
			result += wb20Svc.insertTodoMaster(paramMap);							
		}		
		//---------------------------------------------------------------  
		// 결재라인 처리 end
		//---------------------------------------------------------------
    
     return result;
  }

  @Override
  public Map<String, String> insertQualityReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

	  	Map rtnMap = new HashMap();
	  
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
		String midCode = paramMap.get("CODECOBGB");
		String midCd = midCode.substring(0, 7);
		paramMap.put("midCd", midCd);
		
		int fileTrgtKey = QM01Mapper.selectQualityReqSeqNext(paramMap);
		String reqNo = String.valueOf(QM01Mapper.selectQualityReqCalNext(paramMap));
		System.out.println("요청관리 번호==?"+reqNo);
		
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		paramMap.put("reqNo", reqNo);
		int result = QM01Mapper.insertQualityReq(paramMap);
		rtnMap.put("result", String.valueOf(result)); //문자열로 변환하여 rtnMap에 "result"키로 저장
		rtnMap.put("reqNo", reqNo);// rtnMap에 "reqNo"키로 저장
		
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
	    
		Gson gson = new Gson();	
		
		List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
		if (sharngChk.size() > 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
		}
		
		String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		pgParam1 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
		//공유
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("reqNo"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
	            	    sharngMap.put("userId", paramMap.get("userId"));
	            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap.put("pgParam", pgParam1);
	                	QM01Mapper.insertWbsSharngList(sharngMap);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
		pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		pgParam2 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		pgParam2 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("reqNo"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		            	approvalMap.put("pgmId", paramMap.get("pgmId"));
		            	approvalMap.put("userId", paramMap.get("userId"));
		            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
		            	approvalMap.put("pgParam", pgParam2);
	                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
	                	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}	
		
	    return rtnMap;
  }
  
  @Override
  public int insertQualityResp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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

		String reqNo = mRequest.getParameter("reqNo");
		System.out.println("요청관리 번호==========?"+reqNo);
		String rsltNoCopy = "RES" + reqNo.substring(3,10); 
		System.out.println(" 번호1==========?"+rsltNoCopy);
		paramMap.put("rsltNo", rsltNoCopy);
		//paramMap.put("reqNo", reqNo);
		int result = QM01Mapper.insertQualityResp(paramMap);		
		int result2 = QM01Mapper.updateReqRsltChg(paramMap);// 실적등록 여부 갱신 
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
		
		Gson gson = new Gson();	
		
		String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam1 += "\"coCd\":\""+ paramMap.get("resCoCd") +"\","; 
		pgParam1 += "\"salesCd\":\""+ paramMap.get("matrDrwNo") +"\",";
		pgParam1 += "\"rsltSubCd\":\""+ paramMap.get("copBgbDiv") +"\",";
		pgParam1 += "\"reqNo\":\""+ paramMap.get("rsltNo") +"\"}";
						
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("rsltNo"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
	            	    sharngMap.put("pgmId", paramMap.get("pgmId"));
	            	    sharngMap.put("userId", paramMap.get("userId"));
	            	    sharngMap.put("todoCoCd", paramMap.get("coCd"));
	            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap.put("pgParam", pgParam1);
	                	QM01Mapper.insertWbsSharngList(sharngMap);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
		pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam2 += "\"coCd\":\""+ paramMap.get("resCoCd") +"\","; 
		pgParam2 += "\"salesCd\":\""+ paramMap.get("matrDrwNo") +"\",";
		pgParam2 += "\"rsltSubCd\":\""+ paramMap.get("copBgbDiv") +"\",";
		pgParam2 += "\"reqNo\":\""+ paramMap.get("rsltNo") +"\"}";
		
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("rsltNo"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		            	approvalMap.put("pgmId", paramMap.get("pgmId"));
		            	approvalMap.put("userId", paramMap.get("userId"));
		            	approvalMap.put("todoCoCd", paramMap.get("coCd"));
		            	approvalMap.put("sanCtnSn",Integer.toString(i+1));
		            	approvalMap.put("pgParam", pgParam2);
	                	QM01Mapper.insertWbsApprovalList(approvalMap);       		
	                	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
	    return result;
  }

  @Override
  public int deleteQualityReq(Map<String, String> paramMap) throws Exception {
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


	  	int result = QM01Mapper.deleteQualityReq(paramMap);
	  	
		List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
			if (sharngChk.size() > 0) {
				QM01Mapper.deleteWbsSharngList(paramMap); 
			 }
	  
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

//	  List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
//		if (sharngChk.size() > 0) {
//			QM01Mapper.deleteWbsSharngList(paramMap); 
//		 }

		 	  
	    return result;
  }
  
  
  @Override
  public List<Map<String, String>> selectQualityReqExList(Map<String, String> paramMap) {
		return QM01Mapper.selectQualityReqExList(paramMap);
  }

  
  @Override
  public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return QM01Mapper.selectApprovalList(paramMap);
  }
  
  @Override
  public int selectCodeMaxCount(Map<String, String> paramMap) {
    return QM01Mapper.selectCodeMaxCount(paramMap);
  }
  
  @Override
	public List<Map<String, String>> selectMainCodeList(Map<String, String> param) {
		return QM01Mapper.selectMainCodeList(param);
	}
  
  @Override
  public List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap) {
		return QM01Mapper.selectApprovalChk(paramMap);
  }
  
  @Override
  public int updateReqStChk(Map<String, String> paramMap) {
	  String rsltNo = paramMap.get("todoNo");
	  if(rsltNo.substring(0, 3).equals("RES")) {
		  paramMap.put("reqSt", "REQST03");
	  }
	  else {
		  paramMap.put("reqSt", "REQST02");
	  }
	  
	  int result = QM01Mapper.updateReqStChk(paramMap);
	  
     return result;
  }
  
  @Override
  public int updateCheckDept(Map<String, String> paramMap) throws Exception {
      	Gson gson = new GsonBuilder().disableHtmlEscaping().create();
      	Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
      
	    int result = 0;
	    String chkDept1 = "";
	    String chkDept2 = "";
	    String chkDept3 = "";
	    String chkDept4 = "";
	    String deptId = paramMap.get("deptId").toString();
	    String sDeptId = deptId.substring(0,5);
	    
		if("GUN30".equals(sDeptId) || "TRN30".equals(sDeptId)){   //영업팀
			chkDept1 = "Y";
		}else if("GUN40".equals(sDeptId)){   //설계팀
			chkDept2 = "Y";
		}else if("TRN50".equals(sDeptId)){   //구매팀
			chkDept3 = "Y";
		}else if("GUN60".equals(sDeptId)){   //생산팀
			chkDept4 = "Y";
		}
		
        List<Map<String, String>> rowArr = gson.fromJson(paramMap.get("rowList"), mapList);
        for (Map<String, String> rowMap : rowArr) {
 
    	    rowMap.put("userId", paramMap.get("userId"));
    	    
    	    rowMap.put("chkDept1", chkDept1);
    	    rowMap.put("chkDept2", chkDept2);
    	    rowMap.put("chkDept3", chkDept3);
    	    rowMap.put("chkDept4", chkDept4);

    	    rowMap.put("reqNo",    rowMap.get("reqNo").toString());
    	    rowMap.put("saleCfYn", rowMap.get("chkdept1Yn").toString());
    	    rowMap.put("dsgnCfYn", rowMap.get("chkdept2Yn").toString());
    	    rowMap.put("matrCfYn", rowMap.get("chkdept3Yn").toString());
    	    rowMap.put("prodCfYn", rowMap.get("chkdept4Yn").toString());
    	    rowMap.put("woutCfYn", rowMap.get("chkdept5Yn").toString());

    	  	result = QM01Mapper.updateCheckDept(rowMap);
        }
	    return result;
  }
}