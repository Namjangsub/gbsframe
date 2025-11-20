package com.dksys.biz.user.wb.wb25.service.impl;

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
import com.dksys.biz.user.sm.sm50.mapper.SM50Mapper;
import com.dksys.biz.user.wb.wb25.mapper.WB25Mapper;
import com.dksys.biz.user.wb.wb25.service.WB25Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB25SvcImpl implements WB25Svc {

	@Autowired
	WB25Mapper wb25Mapper;

	@Autowired
	WB25Svc wb25Svc;
	
	@Autowired
    SM50Mapper sm50Mapper;

    @Autowired
    CM08Svc cm08Svc;
	
    @Autowired
    CM15Svc cm15Svc;
    
    @Autowired
    QM01Mapper QM01Mapper;
    
	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsTaskEvlCount(Map<String, String> paramMap) {;		
		return wb25Mapper.selectWbsTaskEvlCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsTaskEvlList(Map<String, String> paramMap) {
		return wb25Mapper.selectWbsTaskEvlList(paramMap);
	}
	
	@Override
	public int wbsTaskEvlInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb25Mapper.selectTaskEvlSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
				
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    	    	   	  
		int result = wb25Mapper.wbsTaskEvlInsert(paramMap);
		
		Gson gson = new Gson();		
		
		String pgParam = "{\"actionType\":\""+ "U" +"\",";
               pgParam += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
               //pgParam += "\"evlCnts\":\""+ paramMap.get("evlCnts") +"\","; 
		       pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		       pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\"}";
		
		       
		//String todoTitle = "과제평가 : SALES CODE " + paramMap.get("salesCd") + ",   과제명 : " + paramMap.get("sjNm");
		         
		String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("sjNm") + " 과제평가 공유";      
		
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {	 
	            	    sharngMap.put("reqNo", paramMap.get("sjFileTrgtKey"));
	            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
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

		//결재
		String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("sjNm") + " 과제평가 결재"; 
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
	            try {	 
		            	approvalMap.put("reqNo", paramMap.get("sjFileTrgtKey"));
		            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
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
		    paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
		    cm08Svc.uploadFile(paramMap, mRequest);
		}
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
				
		return result;
	}
	
	@Override
    public int wbsTaskEvlUpdate(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
	    
	    //---------------------------------------------------------------  
  		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
  	  	//   필수값 :  jobType, userId, comonCd
  		//---------------------------------------------------------------  
  	    HashMap<String, String> param = new HashMap<>();
     	param.put("userId", paramMap.get("userId"));
		param.put("coCd", paramMap.get("coCd"));
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
	  	    
	    int result = wb25Mapper.wbsTaskEvlUpdate(paramMap);	
	    
		Gson gson = new Gson();					

		String pgParam = "{\"actionType\":\""+ "U" +"\",";
	           pgParam += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
	           //pgParam += "\"evlCnts\":\""+ paramMap.get("evlCnts") +"\","; 
			   pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
			   pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\"}";
		
	    String todoTitle = "과제평가 : SALES CODE " + paramMap.get("salesCd") + ",   과제명 : " + paramMap.get("sjNm");  
			   
		paramMap.put("reqNo", paramMap.get("sjFileTrgtKey"));
		paramMap.put("salesCd", paramMap.get("salesCd"));
		
		// 수정시 결재내역이 존재하지 않을 경우 결재 공유 리스트 삭제
		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
			
			String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("sjNm") + " 과제평가 공유";      
			
			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> sharngMap : sharngArr) {
		            try {	 
		            	    sharngMap.put("reqNo", paramMap.get("sjFileTrgtKey"));
		            	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
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

			//결재
			
			String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("sjNm") + " 과제평가 공유";      
			
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			if (approvalArr != null && approvalArr.size() > 0 ) {
				int i = 0;
		        for (Map<String, String> approvalMap : approvalArr) {
		            try {	 
			            	approvalMap.put("reqNo", paramMap.get("sjFileTrgtKey"));
			            	approvalMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
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
	public int wbsTaskEvlCloseYnConfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				try {
					if (sharngMap.containsKey("fileTrgtKey")) {
						result = wb25Mapper.wbsTaskEvlCloseYnConfirm(sharngMap);
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
	public List<Map<String, String>> selectWbsTaskEvlResultList(Map<String, String> paramMap) {
		sm50Mapper.callBomTempUpd(paramMap);
		return wb25Mapper.selectWbsTaskEvlResultList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectWbsTaskEvlIssList(Map<String, String> paramMap) {
		return wb25Mapper.selectWbsTaskEvlIssList(paramMap);
	}
	
	@Override
	  public List<Map<String, String>> selectEvlCloseChk(Map<String, String> paramMap) {
			return wb25Mapper.selectEvlCloseChk(paramMap);
	}
	
	
	
}