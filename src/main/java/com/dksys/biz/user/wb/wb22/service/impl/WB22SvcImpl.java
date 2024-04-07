package com.dksys.biz.user.wb.wb22.service.impl;

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
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.dksys.biz.user.wb.wb22.service.WB22Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB22SvcImpl implements WB22Svc {

	@Autowired
	WB22Mapper wb22Mapper;

	@Autowired
	WB22Svc wb22Svc;

    @Autowired
    CM08Svc cm08Svc;
	
    @Autowired
    CM15Svc cm15Svc;
    
    @Autowired
    QM01Mapper QM01Mapper;
    
	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsSjListCount(Map<String, String> paramMap) {;		
		return wb22Mapper.selectWbsSjListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsSjList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsSjList(paramMap);
	}

	@Override
	public Map<String, String> selectSjInfo(Map<String, String> paramMap) {
		return wb22Mapper.selectSjInfo(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWBS1Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS1Level(paramMap);
	}

	@Override
	public int wbsLevel1Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					sharngMap.put("coCd", paramMap.get("coCd"));
					
					if(!sharngMap.containsKey("fileTrgtKey")) {
						int wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
						sharngMap.put("wbsPlanNo", Integer.toString(wbsPlanNo));
						
						int fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
						sharngMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
						
						sharngMap.put("seq", String.valueOf(i + 1));
						
						result = wb22Mapper.wbsLevel1Insert(sharngMap);
					}else {
						result = wb22Mapper.wbsLevel1Update(sharngMap);
					}
					
					i++;
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		return result;
	}

	@Override
	public int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					result = wb22Mapper.wbsLevel1Update(sharngMap);
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		return result;
	}

	@Override
	public List<Map<String, String>> selectWBS2Level(Map<String, String> paramMap) {
		return wb22Mapper.selectWBS2Level(paramMap);
	}

	@Override
	public int wbsLevel2Insert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gson = new Gson();
							
		int result = 0;

		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					//System.out.println(sharngMap.get("fileTrgtKey"));
					if (sharngMap.get("fileTrgtKey").length() > 0) {
						sharngMap.put("seq", String.valueOf(i + 1));
						result = wb22Mapper.wbsLevel2Update(sharngMap);
					}
					else {
						sharngMap.put("coCd", paramMap.get("coCd"));					
						int wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
						sharngMap.put("wbsPlanNo", Integer.toString(wbsPlanNo));
						sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
						int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);
																	
					    String codeId = ""; 
					    if (wbsPlanCodeId < 10) { 
							  codeId = "0" + String.valueOf(wbsPlanCodeId);
						  } 
						  else { 
							  codeId = String.valueOf(wbsPlanCodeId); 
						}						 					
						sharngMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);
						
						sharngMap.put("seq", String.valueOf(i + 1));
						int fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
						sharngMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
						result = wb22Mapper.wbsLevel2Insert(sharngMap);
					}									
					i++;
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		
		Type stringList1 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> deleteRowArr = gson.fromJson(paramMap.get("deleteRowArr"), stringList1);
		if (deleteRowArr != null && deleteRowArr.size() > 0) {
			for (Map<String, String> sharngMap : deleteRowArr) {
				try {
					//실적먼저 지워져야함
					wb22Mapper.wbsRsltsDelete(sharngMap);
					result = wb22Mapper.wbsLevel2Delete(sharngMap);
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
				
		return result;
	}

	@Override
	public int wbsVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {	
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					sharngMap.put("seq", String.valueOf(i + 1));
					wb22Mapper.wbsVerUpInsert(sharngMap);
					result++;
					i++;
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
			wb22Mapper.wbsVerUpUpdate(paramMap);
		}
		return result;
	}
	
	@Override
    public List<Map<String, String>> selectVerNoNext(Map<String, String> paramMap) {
  		return wb22Mapper.selectVerNoNext(paramMap);
    }
	
	@Override
	public int wbsLevel1confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					result = wb22Mapper.wbsLevel1confirm(sharngMap);
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		return result;
	}
	
	@Override
	public int wbsLevel2confirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					result = wb22Mapper.wbsLevel2confirm(sharngMap);
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		return result;
	}
    @Override
	public List<Map<String, String>> selectRsltsSharngList(Map<String, String> paramMap) {
		return wb22Mapper.selectRsltsSharngList(paramMap);
	}


    @Override
	public List<Map<String, String>> selectRsltsApprovalList(Map<String, String> paramMap) {
		return wb22Mapper.selectRsltsApprovalList(paramMap);
	}
	
    @Override
	public int wbsRsltsInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("rsltsFileTrgtKey", Integer.toString(fileTrgtKey));
				
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
		int result = wb22Mapper.wbsRsltsInsert(paramMap);	
		Gson gson = new Gson();		

		//String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		//pgParam1 += "\"fileTrgtKey\":\""+ fileTrgtKey +"\","; 
		//pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		//pgParam1 += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
		//pgParam1 += "\"idx\":\""+ paramMap.get("idx") +"\",";
		//pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		//pgParam1 += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\",";
		//pgParam1 += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
		//pgParam1 += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";
	    
		String pgParam = "{\"fileTrgtKey\":\""+ fileTrgtKey +"\"}";
		
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_등록]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");
		
		//String todoTitle = "TASK명 : " + paramMap.get("wbsPlanCodeNm2_P") + ",    SALES CODE : " + paramMap.get("salesCd2_P")  + "    실적일자 : " + paramMap.get("wbsRsltssDt") + "  ~  " + paramMap.get("wbsRsltseDt");
		
		
		
		
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
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

		
//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 결재";
		String todoTitle2 = "[WBS실적_등록]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");
		
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
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
		    paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
				
		return result;
	}
    
    
    @Override
    public int wbsRsltsUpdate(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
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
	  	    
	    int result = wb22Mapper.wbsRsltsUpdate(paramMap);	
		Gson gson = new Gson();					

		String pgParam = "{\"fileTrgtKey\":\""+ paramMap.get("rsltsFileTrgtKey") +"\"}";
		
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_변경]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");
		
		
		paramMap.put("reqNo", paramMap.get("wbsRsltsNo"));
		paramMap.put("salesCd", paramMap.get("salesCd2_P"));
		
		
		
		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
			
			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> sharngMap : sharngArr) {
		            try {	 
		            	    sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
		            	    sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
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

//			String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
			String todoTitle2 = "[WBS실적_변경]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");
			
			//결재
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			if (approvalArr != null && approvalArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> approvalMap : approvalArr) {
		            try {	 
			            	approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
			            	approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
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
		
		//List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
		//if (sharngChk.size() == 0) {
		//	QM01Mapper.deleteWbsSharngList(paramMap); 
		//}
		
		//List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk(paramMap); 
		//if (approvalgChk.size() > 0) {
		//	QM01Mapper.deleteWbsApprovalList(paramMap); 
		//}
		
		
		
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
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
	public int wbsRsltsconfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		try {
			result = wb22Mapper.wbsRsltsconfirm(paramMap);
		} catch (Exception e) {
			System.out.println("error2" + e.getMessage());
		}
		return result;
	}
    
    @Override
	public List<Map<String, String>> selectTodoRsltsView(Map<String, String> paramMap) {
		return wb22Mapper.selectTodoRsltsView(paramMap);
	}

	@Override
	public List<Map<String, String>> selectIncompleteJob(Map<String, String> paramMap) {
		return wb22Mapper.selectIncompleteJob(paramMap);
	}

	@Override
	public void callCopyWbsPlan(Map<String, String> paramMap) {
		wb22Mapper.callCopyWbsPlan(paramMap);
	}

	@Override
	public int selectWbsTaskTempletCount(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsTaskTempletCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsTaskTempletList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsTaskTempletList(paramMap);
	}

	@Override
	public int saveWbsTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					//상위 코드
					sharngMap.put("codeKind", paramMap.get("wbsPlanCodeId"));
					//codeId 값이 없으면 insert
					if(sharngMap.get("codeId").isEmpty()) {
						String wbsPlanNo = wb22Mapper.selectNewWbsTaskTempletCd(sharngMap);
						sharngMap.put("codeId", wbsPlanNo);
						
						result = wb22Mapper.wbsTaskTempletInsert(sharngMap);
					}else {
						result = wb22Mapper.wbsTaskTempletUpdate(sharngMap);
					}
					
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		
		List<Map<String, String>> delArr = gson.fromJson(paramMap.get("taskDeleteRowArr"), stringList);
		if (delArr != null && delArr.size() > 0) {
			for (Map<String, String> sharngMap : delArr) {
				try {

					result = wb22Mapper.wbsTaskTempletDelete(sharngMap);
					
				} catch (Exception e) {
					System.out.println("error2" + e.getMessage());
				}
			}
		}
		
		return result;
	}

	@Override
	public List<Map<String, String>> selectHistWBS1Level(Map<String, String> paramMap) {
		return wb22Mapper.selectHistWBS1Level(paramMap);
	}
	
	// 계획일괄복사부분
	@Override
	public int ModalwbsPlanconfirmListCount(Map<String, String> paramMap) {
		return wb22Mapper.ModalwbsPlanconfirmListCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> ModalwbsPlanconfirmList(Map<String, String> paramMap) {
		return wb22Mapper.ModalwbsPlanconfirmList(paramMap);
	}
	
	@Override
	public int confirm_copy(Map<String, String> paramMap) {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		
		//데이터처리 시작
		int result = 0;

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("rowList"), dtlMap);

		String userId = "";
		String sYear = "";
		userId = paramMap.get("userId");
		sYear = paramMap.get("year");

		if(dtlParam.size()>0) {
			
			for (Map<String, String> dtl : dtlParam) {
	
				String dtaChk = dtl.get("dtaChk").toString();
				dtl.put("creatId", userId);
				dtl.put("year", sYear);
	
				if("U".equals(dtaChk))  {
	
					List<Map<String, String>> chkCount = wb22Mapper.selectWbcPlan(dtl);
					
					if(chkCount.size() == 0) {
						//insert
						List<Map<String, String>> chkList = wb22Mapper.selectWbcPlan(paramMap);
						
						for (Map<String, String> chkMap : chkList) {

						  dtl.put("wbsPlanCodeId", chkMap.get("wbsPlanCodeId")); 
						  dtl.put("verNo", "1");
						  dtl.put("sortNo", chkMap.get("seq")); 
						  dtl.put("wbsPlanMngId", chkMap.get("wbsPlanMngId"));
						  dtl.put("wbsPlansDt", chkMap.get("wbsPlansDt")); 
						  dtl.put("wbsPlaneDt", chkMap.get("wbsPlaneDt")); 
						  dtl.put("daycnt", chkMap.get("daycnt"));
						  dtl.put("wbsPlanStsCodeId", chkMap.get("wbsPlanStsCodeId"));
						  dtl.put("creatPgm", chkMap.get("creatPgm"));
							
						  int wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(dtl);
						  dtl.put("wbsPlanNo", Integer.toString(wbsPlanNo));

						  int fileTrgtKey = wb22Mapper.selectWbsSeqNext(dtl);
						  dtl.put("fileTrgtKey", Integer.toString(fileTrgtKey));
						  
						  result = wb22Mapper.wbsLevel1Insert(dtl);
						}
					}else {
						//update
						List<Map<String, String>> chkList = wb22Mapper.selectWbcPlan(paramMap);
						
						if(chkList.size() == 0) {  //데이터가 없을시 대상데이터를 null 셋팅한다.
							  dtl.put("wbsPlanCodeId", ""); 
							  dtl.put("wbsPlanMngId", "");
							  dtl.put("wbsPlansDt", ""); 
							  dtl.put("wbsPlaneDt", ""); 
							  dtl.put("daycnt", "");
							  dtl.put("wbsPlanStsCodeId", "");
							  dtl.put("creatPgm", "WB2201M01");
								
							  result = wb22Mapper.updateWbcPlan(dtl);
						}else {
							for (Map<String, String> chkMap : chkList) {

							  dtl.put("wbsPlanCodeId", chkMap.get("wbsPlanCodeId")); 
							  dtl.put("wbsPlanMngId", chkMap.get("wbsPlanMngId"));
							  dtl.put("wbsPlansDt", chkMap.get("wbsPlansDt")); 
							  dtl.put("wbsPlaneDt", chkMap.get("wbsPlaneDt")); 
							  dtl.put("daycnt", chkMap.get("daycnt"));
							  dtl.put("wbsPlanStsCodeId", chkMap.get("wbsPlanStsCodeId"));
							  dtl.put("creatPgm", chkMap.get("creatPgm"));
								
							  result = wb22Mapper.updateWbcPlan(dtl);
							}
						}
					}

				}
			}
	    	
	    }
		//데이터 처리 끝
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectWbcPlanTodoList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbcPlanTodoList(paramMap);
	}
	
	
	@Override
	public int wbcPlanTodoInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	   	   
		int result = 0;
		
		Gson gson = new Gson();		
		
		
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	
	            	   String pgParam = "{\"coCd\":\""+ sharngMap.get("coCd") +"\",";
	 			              pgParam += "\"salesCd\":\""+ sharngMap.get("salesCd") +"\",";
	 			              pgParam += "\"planVerNo\":\""+ sharngMap.get("verNo") +"\",";
	 			              pgParam += "\"histYn\":\""+ sharngMap.get("histYn") +"\"}";
	            	    sharngMap.put("sanCtnSn",Integer.toString(i+1));
	            	    sharngMap.put("pgParam", pgParam);
	            	    result = QM01Mapper.insertWbsSharngList(sharngMap);       		
	            	i++;
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
        return result;
	}

	// 일괄확정부분
	@Override
	public int Modalwb22noconfirmListCount(Map<String, String> paramMap) {
		return wb22Mapper.Modalwb22noconfirmListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> Modalwb22noconfirmList(Map<String, String> paramMap) {
		return wb22Mapper.Modalwb22noconfirmList(paramMap);
	}

	// 일괄확정
	@Override
	public int confirm_wb22(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
		
		//데이터처리 시작
		int result = 0;

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			//dtl.put("userId", paramMap.get("userId"));
	    	//dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("U".equals(dataChk)) {
				//데이터 처리
				result = wb22Mapper.confirm_wb22(dtl);
	    	} 
	    }
		//데이터 처리 끝
		return result;
	}
	// 일괄확정부분 끝
	
	
	@Override
	public List<Map<String, String>> selectWbcPlanUpdteTodoList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbcPlanUpdteTodoList(paramMap);
	}

	@Override
	public Map<String, String> wbsResultLastVerNoSearch(Map<String, String> paramMap) {
		return wb22Mapper.wbsResultLastVerNoSearch(paramMap);
	}
}