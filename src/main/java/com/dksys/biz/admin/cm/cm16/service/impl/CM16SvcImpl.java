package com.dksys.biz.admin.cm.cm16.service.impl;

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
import com.dksys.biz.admin.cm.cm16.mapper.CM16Mapper;
import com.dksys.biz.admin.cm.cm16.service.CM16Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM16SvcImpl implements CM16Svc {

    @Autowired
    QM01Mapper QM01Mapper;

    @Autowired
    CM16Mapper cm16Mapper;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;
    
    @Autowired
    WB20Svc wb20Svc;
    
    @Override
    public int selectItoaIssueListCount(Map<String, String> paramMap) {
        return cm16Mapper.selectItoaIssueListCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> selectItoaIssueList(Map<String, String> paramMap) {
        return cm16Mapper.selectItoaIssueList(paramMap);
    }

    @Override
    public Map<String, String> selectItoaIssueInfo(Map<String, String> paramMap) {
    return cm16Mapper.selectItoaIssueInfo(paramMap);
    }

    @Override
    public int selectConfirmCount(Map<String, String> paramMap) {
        return cm16Mapper.selectConfirmCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap) {
        
        return cm08Svc.selectFileList(paramMap);
    }

    @Override
    public int insertItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

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


        String fileTrgtKey = cm16Mapper.selectItoaIssueSeqNext(paramMap);
        paramMap.put("fileTrgtKey", fileTrgtKey);
        paramMap.put("todoNo", fileTrgtKey);
        paramMap.put("todoFileTrgtKey", fileTrgtKey);
        paramMap.put("reqNo", fileTrgtKey);
        paramMap.put("salesCd", fileTrgtKey);

        //문제현황 등록
        int result = cm16Mapper.insertItoaIssue(paramMap);

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
		
		//공유
		String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
			pgParam1 += "\"issNo\":\""+ paramMap.get("workRptNo") +"\",";
		}
		pgParam1 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
		
		//결재
		String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
		pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		pgParam2 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		if (!"".equals(paramMap.get("workRptNo"))) {	//issNo가 존재하면 문제건, 없으면 정상건
			pgParam2 += "\"issNo\":\""+ paramMap.get("workRptNo") +"\",";
		}
		pgParam2 += "\"reqNo\":\""+ paramMap.get("reqNo") +"\"}";
		//공유-결재
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("approvalArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int iSharng = 1;
			int iApproval = 1;
	        for (Map<String, String> sharngMap : sharngArr) {

        	    sharngMap.put("reqNo", fileTrgtKey);
        	    sharngMap.put("fileTrgtKey", fileTrgtKey);
        	    sharngMap.put("salesCd", fileTrgtKey);
        	    sharngMap.put("pgmId", paramMap.get("pgmId"));
        	    sharngMap.put("userId", paramMap.get("userId"));
        	    
	        	if ("공유".equals(sharngMap.get("gb"))) {
	            	    sharngMap.put("sanCtnSn",Integer.toString(iSharng));
	            	    sharngMap.put("pgParam", pgParam1);
	                	QM01Mapper.insertWbsSharngList(sharngMap);       		
	                	iSharng++;
	        	} else {
	        		sharngMap.put("sanCtnSn",Integer.toString(iApproval));
	        		sharngMap.put("pgParam", pgParam2);
	                	QM01Mapper.insertWbsApprovalList(sharngMap);       		
	                	iApproval++;
	        	}
	        }
		}

        return result;
    }

    @Override
    public int updateItoaIssue(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

        int result = cm16Mapper.updateItoaIssue(paramMap);

        // 파일이 존재할 때만 파일 업로드 로직 실행
        if (mRequest != null && mRequest.getFileNames().hasNext()) {
            // 첨부파일 처리
            cm08Svc.uploadFile("CM1601M01", paramMap.get("fileTrgtKey"), mRequest);
        }

        // 삭제할 파일이 있을 때 삭제 처리
        Gson gson = new Gson();
        String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);
        if (deleteFileList != null && !deleteFileList.isEmpty()) {
            for (String fileKey : deleteFileList) {
                if (fileKey != null && !fileKey.isEmpty()) {
                    cm08Svc.deleteFile(fileKey);
                }
            }
        }

        //---------------------------------------------------------------  
        // 결재라인 처리 시작 
        //---------------------------------------------------------------	
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
    public int deleteItoaIssue(Map<String, String> paramMap) throws Exception {
        //---------------------------------------------------------------  
		//첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
        List<Map<String, String>> deleteFileList = cm08Svc.selectFileList(paramMap);
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

        int result = cm16Mapper.deleteItoaIssue(paramMap);

        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
			if (sharngChk.size() > 0) {
				QM01Mapper.deleteWbsSharngList(paramMap); 
            }

        //---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------
        if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey");
		        if (fileKey != null && !fileKey.isEmpty()) {	//경비내역이 있으면 처리함.
		        	cm08Svc.deleteFile( fileKey );
		        }
		    }
		}
        //---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  

        return result;
    }

    //요청자 multi select 검색
    @Override
    public List<Map<String, String>> select_reqId_code(Map<String, String> paramMap) {
        return cm16Mapper.select_reqId_code(paramMap);
    }

}
