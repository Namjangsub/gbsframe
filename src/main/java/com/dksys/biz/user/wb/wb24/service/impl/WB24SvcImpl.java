package com.dksys.biz.user.wb.wb24.service.impl;

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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB24SvcImpl implements WB24Svc {

	@Autowired
	WB24Mapper wb24Mapper;

	@Autowired
	WB24Svc wb24Svc;

    @Autowired
    CM08Svc cm08Svc;
	
    @Autowired
    CM15Svc cm15Svc;
    
    @Autowired
    QM01Mapper QM01Mapper;
    
	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsIssueListCount(Map<String, String> paramMap) {;		
		return wb24Mapper.selectWbsIssueListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsIssueList(Map<String, String> paramMap) {
		return wb24Mapper.selectWbsIssueList(paramMap);
	}
	
	@Override
	  public List<Map<String, String>> selectMaxWbsIssueNo(Map<String, String> paramMap) {
			return wb24Mapper.selectMaxWbsIssueNo(paramMap);
	}
	
	@Override
	public int wbsIssueInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb24Mapper.selectWbsIssueSeqNext(paramMap);
		paramMap.put("issFileTrgtKey", Integer.toString(fileTrgtKey));
				
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
	    	   	   
		int result = wb24Mapper.wbsIssueInsert(paramMap);
		
		//int result1 = wb24Mapper.wbsActInsert(paramMap);
		
		Gson gson = new Gson();		
		
		String pgParam = "{\"fileTrgtKey\":\""+ paramMap.get("issFileTrgtKey") +"\"}";
		
		//String todoTitle = "이슈번호 : " + paramMap.get("issNo") + ",   이슈제목 : " + paramMap.get("issSj");
		
		String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 공유"; 
		
		//String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		//pgParam1 += "\"fileTrgtKey\":\""+ fileTrgtKey +"\","; 
		//pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		//pgParam1 += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
		//pgParam1 += "\"idx\":\""+ paramMap.get("idx") +"\",";
		//pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		//pgParam1 += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\",";
		//pgParam1 += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
		//pgParam1 += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";
	    
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("issNo"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
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

		
		String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 결재"; 
		
		
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("issNo"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
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
		
		
		// 조치정보 공유 결재 처리 프로세스
		
		String todoTitle3 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 조치 공유"; 
		
		Type stringList4 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr2 = gson.fromJson(paramMap.get("rowSharngListArr2"), stringList4);
		if (sharngArr2 != null && sharngArr2.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap2 : sharngArr2) {
	            try {	 
	            	    sharngMap2.put("reqNo", paramMap.get("issNo"));
	            	    sharngMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
	            	    sharngMap2.put("pgmId", paramMap.get("pgmId"));
	            	    sharngMap2.put("userId", paramMap.get("userId"));
	            	    sharngMap2.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap2.put("pgParam", pgParam);
	            	    sharngMap2.put("todoTitle", todoTitle3);
	                	QM01Mapper.insertWbsSharngList(sharngMap2);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		
		//결재
		String todoTitle4 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 조치 결재";
		
		Type stringList5 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr2 = gson.fromJson(paramMap.get("rowApprovalListArr2"), stringList5);
		if (approvalArr2 != null && approvalArr2.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap2 : approvalArr2) {
	            try {	 
		            	approvalMap2.put("reqNo", paramMap.get("issNo"));
		            	approvalMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
		            	approvalMap2.put("pgmId", paramMap.get("pgmId"));
		            	approvalMap2.put("userId", paramMap.get("userId"));
		            	approvalMap2.put("sanCtnSn",Integer.toString(i+1));
		            	approvalMap2.put("pgParam", pgParam);
		            	approvalMap2.put("todoTitle", todoTitle4);
	                	QM01Mapper.insertWbsApprovalList(approvalMap2);       		
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
    public int wbsIssueUpdate(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
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
	  	    
	    int result = wb24Mapper.wbsIssueUpdate(paramMap);	
	    
	    List<Map<String, String>> actchk = wb24Mapper.actChk(paramMap); 
		if (actchk.size() > 0) {
			int result1 = wb24Mapper.wbsActUpdate(paramMap);
		}
		else {
			int result1 = wb24Mapper.wbsActInsert(paramMap);
		}
	    
		Gson gson = new Gson();					

		String pgParam = "{\"fileTrgtKey\":\""+ paramMap.get("issFileTrgtKey") +"\"}";
		
		//String todoTitle = "이슈번호 : " + paramMap.get("issNo") + ",   이슈제목 : " + paramMap.get("issSj");
		
		
		paramMap.put("reqNo", paramMap.get("issNo"));
		paramMap.put("salesCd", paramMap.get("salesCd"));
		
		

		//List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk1(paramMap); 
		//if (sharngChk.size() > 0) {
		//	QM01Mapper.deleteWbsSharngList1(paramMap); 
		//}
		
		//List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk1(paramMap); 
		//if (approvalgChk.size() > 0) {
		//	QM01Mapper.deleteWbsApprovalList1(paramMap); 
		//}
		
		
	
		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
			
			
			String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 공유";
			
			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			
			if (sharngArr != null && sharngArr.size() > 0 ) {
				
				QM01Mapper.deleteWbsSharngList1(paramMap); 
				
				int i = 0;
		        for (Map<String, String> sharngMap : sharngArr) {
		            try {	 
		            	    sharngMap.put("reqNo", paramMap.get("issNo"));
		            	    sharngMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
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

			
			String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 결재";
			
			//결재
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			
			if (approvalArr != null && approvalArr.size() > 0 ) {
				
				QM01Mapper.deleteWbsApprovalList1(paramMap);
				
				
				int i = 0;
		        for (Map<String, String> approvalMap : approvalArr) {
		            try {	 
			            	approvalMap.put("reqNo", paramMap.get("issNo"));
			            	approvalMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
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
		
		
		
		
		if (Integer.parseInt(paramMap.get("approvalYnCnt2")) == 0) {
			
			
			// 조치정보 공유 결재 처리 프로세스
			
			String todoTitle3 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 조치 공유";
			
			Type stringList4 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr2 = gson.fromJson(paramMap.get("rowSharngListArr2"), stringList4);
			if (sharngArr2 != null && sharngArr2.size() > 0 ) {
				
				QM01Mapper.deleteWbsSharngList2(paramMap); 
				
				int i = 0;
				
				
		        for (Map<String, String> sharngMap2 : sharngArr2) {
		            try {	 
		            	    sharngMap2.put("reqNo", paramMap.get("issNo"));
		            	    sharngMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
		            	    sharngMap2.put("pgmId", paramMap.get("pgmId"));
		            	    sharngMap2.put("userId", paramMap.get("userId"));
		            	    sharngMap2.put("sanCtnSn",Integer.toString(i+1));
		            	    sharngMap2.put("pgParam", pgParam);
		            	    sharngMap2.put("todoTitle", todoTitle3);
		                	QM01Mapper.insertWbsSharngList(sharngMap2);       		
		            	i++;
		            } catch (Exception e) {
		                System.out.println("error2"+e.getMessage());
		            }
		        }
			}

			
			//결재
			String todoTitle4 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 조치 결재";
			
			Type stringList5 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> approvalArr2 = gson.fromJson(paramMap.get("rowApprovalListArr2"), stringList5);
			if (approvalArr2 != null && approvalArr2.size() > 0 ) {
				
				QM01Mapper.deleteWbsApprovalList2(paramMap);
								
				int i = 0;
		        for (Map<String, String> approvalMap2 : approvalArr2) {
		            try {	 
			            	approvalMap2.put("reqNo", paramMap.get("issNo"));
			            	approvalMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
			            	approvalMap2.put("pgmId", paramMap.get("pgmId"));
			            	approvalMap2.put("userId", paramMap.get("userId"));
			            	approvalMap2.put("sanCtnSn",Integer.toString(i+1));
			            	approvalMap2.put("pgParam", pgParam);
			            	approvalMap2.put("todoTitle", todoTitle4);
		                	QM01Mapper.insertWbsApprovalList(approvalMap2);       		
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
		    paramMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
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
	public int wbsIssCloseYnConfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					if (sharngMap.containsKey("issFileTrgtKey")) {
						result = wb24Mapper.wbsIssCloseYnConfirm(sharngMap);
						result++;
					}					
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap) {
		return wb24Mapper.selectTodoRsltsView(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectRsltsMemberList(Map<String, String> paramMap) {
		return wb24Mapper.selectRsltsMemberList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectMemberTelNo(Map<String, String> paramMap) {
		return wb24Mapper.selectMemberTelNo(paramMap);
	}
}