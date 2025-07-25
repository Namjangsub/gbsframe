package com.dksys.biz.user.qm.qm01.service.impl;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.dksys.biz.user.qm.qm01.service.QM01Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
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

  @Autowired
  WB24Mapper wb24Mapper;
  
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
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();

        // ---------------------------------------------------------------
        // 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        // 필수값 : jobType, userId, comonCd
        // ---------------------------------------------------------------
        HashMap<String, String> param = new HashMap<>();
        param.put("userId", paramMap.get("userId"));
        param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

        String midCode = paramMap.get("CODECOBGB");
        String midCd = midCode.substring(0, 7);
        paramMap.put("midCd", midCd);

        List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
        if (uploadFileList.size() > 0) {
            // 접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
            param.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(param);
        }
        String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);
        for (String fileKey : deleteFileList) { // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
            Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
            // 접근 권한 없으면 Exception 발생
            param.put("comonCd", fileInfo.get("comonCd")); // 삭제할 파일이 보관된 저장 위치 정보
            param.put("jobType", "fileDelete");
            cm15Svc.selectFileAuthCheck(param);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 끝
        // ---------------------------------------------------------------
        int result = QM01Mapper.updateQualityReq(paramMap);

        result += wb24Mapper.updateVendCd(paramMap);
	
        /**********************************************************************************
         * 2025.01.21 남장섭 수정 시작 - 요인별발주요청서 작성과 결과등록 처리 기능을 하나로 합침 - 아래는 요인별 발주요청서 결과등록 처리하는 로직을 복사함
         **********************************************************************************/
        String procType = paramMap.get("sameTimeResult"); // 결과 분리여부
        if (procType.equals("Y")) {

			// String fdmtSolutCd = paramMap.get("FDMTSOLUT");
			// paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)
			
            // 현재 날짜를 yyyyMMdd 형식으로 저장
            String 현재일 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            paramMap.put("resDt", 현재일); // 실적일자
            paramMap.put("resDeptCd", paramMap.get("deptCd")); // 소속부서 대분류
            paramMap.put("copArr", paramMap.get("deptCd2")); // 소속부서 분류
            paramMap.put("resMidCd", paramMap.get("midCd")); // 중분류

            paramMap.put("resRegId", paramMap.get("regId")); // 결과등록자
            paramMap.put("thghRst", ""); // 고찰
            paramMap.put("rsltRmk", paramMap.get("reqRmk")); // 결과등록자
            paramMap.put("resRegId", paramMap.get("regId")); // 결과등록자
            paramMap.put("sameTimeResult", "Y"); // 발주요청서와 결과를 동시에 처리한경우 'Y'

            int result2 = QM01Mapper.updateQualityResp(paramMap);
            result2 = QM01Mapper.updateReqRsltChg(paramMap);// 실적등록 여부 갱신 RSLT_YN = 'Y'
            result2 = QM01Mapper.updateReqStRslt(paramMap);// // 상태코드 결과처리완료로 변경 REQ_ST = 'REQST03'
        }
        /**********************************************************************************
         * 2025.01.21 남장섭 수정 끝 - 요인별발주요청서 작성과 결과등록 처리 기능을 하나로 합침 - 아래는 요인별 발주요청서 결과등록 처리하는 로직을 복사함
         **********************************************************************************/

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작
        // ---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
            paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
            paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        for (String fileKey : deleteFileList) {
            cm08Svc.deleteFile(fileKey);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------


        // ---------------------------------------------------------------
        // 결재라인 처리 시작
        // ---------------------------------------------------------------
        if (paramMap.containsKey("approvalArr")) {
            // 결제라인 insert
            result += wb20Svc.insertTodoMaster(paramMap);
        }
        // ---------------------------------------------------------------
        // 결재라인 처리 end
        // ---------------------------------------------------------------


        return result;
  }
  
  @Override
  public int updateQualityResp(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
	 paramMap.put("reqNo", paramMap.get("rsltNo")); //번호
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
	// String fdmtSolutCd = paramMap.get("FDMTSOLUT");
	// paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)
	int result = QM01Mapper.updateQualityResp(paramMap);
	
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

	// 결재시 원인/결과/근본원인에 대한 수정작업
	@Override
	public int updateQualityResultComment(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		return QM01Mapper.updateQualityResultComment(paramMap);
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
//		System.out.println("요청관리 번호==?"+reqNo);
		
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		paramMap.put("reqNo", reqNo);
		int result = QM01Mapper.insertQualityReq(paramMap);

		// 발주요청서 등록시 해당 발주 요청서 문제와 연계될때 한개의 문제에서 여러개의 발주요청서를 등록 못하게 해야함
		// 한개의 문제에는 한개의 발주요청서만 존재해야함
		int issueReqExistChk = wb24Mapper.issueReqExistChk(paramMap);
		if (issueReqExistChk > 0) {
			throw new RuntimeException("이미 문제에 대한 발주요청서를 작성하셨습니다. 확인해주세요.");
		}
		

		// 문제발생내역의 발주요청번호 update 처리하기 (ISSNO에 해당하는 문제에 발주요청번호 Update 처리함.
		result += QM01Mapper.updateWbsIssueReqNo(paramMap);

		// 발주요청서 등록 시 발생공급업체 저장기능 추가
		result += wb24Mapper.updateVendCd(paramMap);

		rtnMap.put("result", String.valueOf(result)); // 문자열로 변환하여 rtnMap에 "result"키로 저장
		rtnMap.put("reqNo", reqNo);// rtnMap에 "reqNo"키로 저장
		
        /**********************************************************************************
         * 2025.01.21 남장섭 수정 시작 - 요인별발주요청서 작성과 결과등록 처리 기능을 하나로 합침 - 아래는 요인별 발주요청서 결과등록 처리하는 로직을 복사함
         **********************************************************************************/
        String procType = paramMap.get("procType"); // 결과 분리여부
        if (procType.equals("결과일괄등록")) {
            String rsltNoCopy = "RES" + reqNo.substring(3, 10);
            paramMap.put("rsltNo", rsltNoCopy);

			// String fdmtSolutCd = paramMap.get("FDMTSOLUT");
			// paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)

            // 현재 날짜를 yyyyMMdd 형식으로 저장
            String 현재일 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            paramMap.put("resDt", 현재일); // 실적일자
            paramMap.put("resDeptCd", paramMap.get("deptCd")); // 소속부서 대분류
            paramMap.put("copArr", paramMap.get("deptCd2")); // 소속부서 분류
            paramMap.put("resMidCd", paramMap.get("midCd")); // 중분류

            paramMap.put("resRegId", paramMap.get("regId")); // 결과등록자
            paramMap.put("thghRst", ""); // 고찰
            paramMap.put("rsltRmk", paramMap.get("reqRmk")); // 결과등록자
            paramMap.put("resRegId", paramMap.get("regId")); // 결과등록자
            paramMap.put("sameTimeResult", "Y"); // 발주요청서와 결과를 동시에 처리한경우 'Y'

            int result2 = QM01Mapper.insertQualityResp(paramMap);
            result2 = QM01Mapper.updateReqRsltChg(paramMap);// 실적등록 여부 갱신 RSLT_YN = 'Y'
            result2 = QM01Mapper.updateReqStRslt(paramMap);// // 상태코드 결과처리완료로 변경 REQ_ST = 'REQST03'
        }
        /**********************************************************************************
         * 2025.01.21 남장섭 수정 끝 - 요인별발주요청서 작성과 결과등록 처리 기능을 하나로 합침 - 아래는 요인별 발주요청서 결과등록 처리하는 로직을 복사함
         **********************************************************************************/

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

        	    sharngMap.put("reqNo", paramMap.get("reqNo"));
        	    sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
        	    sharngMap.put("pgmId", paramMap.get("pgmId"));
        	    sharngMap.put("userId", paramMap.get("userId"));
        	    
	        	if ("공유".equals(sharngMap.get("gb"))) {
                    sharngMap.put("sanCtnSn", Integer.toString(iSharng));
                    sharngMap.put("pgParam", pgParam1);
                    QM01Mapper.insertWbsSharngList(sharngMap);
                    iSharng++;
	        	} else {
                    sharngMap.put("sanCtnSn", Integer.toString(iApproval));
                    sharngMap.put("pgParam", pgParam2);
                    QM01Mapper.insertWbsApprovalList(sharngMap);
                    iApproval++;
                    // 조치자가 팀장일경우 insertWbsApprovalList 에서 결재완료처리로 등록되므로 상태코드를 진행으로 변경하기 위해 아래 쿼리 실행함
                    // insertWbsApprovalList --> usrNm 을 todoId 에 저장하고 있음
                    if (sharngMap.get("userId").equals(sharngMap.get("usrNm"))) {
//                        QM01Mapper.updateReqSt(sharngMap);
                        sharngMap.put("todoCfOpn", "자체승인");
                        sharngMap.put("todoNo", sharngMap.get("reqNo"));

                        Object value = sharngMap.get("toDoKey");

                        if (value != null) {
                            // String으로 변환 후 Map에 저장 (Integer 또는 String 모두 처리 가능)
                            sharngMap.put("todoKey", value.toString());
                        }

                        wb20Svc.insertApprovalLine(sharngMap);
                    }
	        	}
	        }
		}
		
		
		//---------------------------------------------------------------  
		// 결재라인 처리 시작 
		//---------------------------------------------------------------	
//		if( paramMap.containsKey("approvalArr") ) {	
//	        		//결제라인 insert
//	        		result += wb20Svc.insertTodoMaster(paramMap);	
//		}		
		//---------------------------------------------------------------  
		// 결재라인 처리 end
		//---------------------------------------------------------------
		
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

		if( !paramMap.containsKey("rsltNo") ) {	
			String reqNo = mRequest.getParameter("reqNo");
			String rsltNoCopy = "RES" + reqNo.substring(3,10); 
			paramMap.put("rsltNo", rsltNoCopy);
		}
		// String fdmtSolutCd = paramMap.get("FDMTSOLUT");
		// paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)
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


        // 1. 발주요청서 자료 삭제
	  	int result = QM01Mapper.deleteQualityReq(paramMap);

        // 2. 문제현황 자료에서 발주요청서 번호 필드 clear 처리
		//문제발생내역의 발주요청번호 update 처리하기 (ISSNO에 해당하는 문제에 발주요청번호 clear 처리함.
		if( paramMap.containsKey("issNo") ) {	
			if (paramMap.get("issNo") != null && !paramMap.get("issNo").isEmpty()) {	
				result += QM01Mapper.clearWbsIssueReqNo(paramMap);
			}
		}

        // 3. 발주요청서 결재자 정보 삭제 처리
		List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
			if (sharngChk.size() > 0) {
				QM01Mapper.deleteWbsSharngList(paramMap); 
			 }

//	        // 발주요청서 요청 및 결과 동시처리건이면 결과도 함게 삭제처리함.
//	        if (paramMap.containsKey("sameTimeResult")) {
//	            if (paramMap.get("sameTimeResult").equals("Y")) {
//	                result += QM01Mapper.deleteQualityResp(paramMap);
//	            }
//	        }

        // **************************************************************************************
        // 여기서부터는 발주요청 결과자료 삭제처리 시작입니다.
        // **************************************************************************************
        if (paramMap.containsKey("rsltNo")) {
            // 4.발주요청서 자료가 삭제되면 결과도 같이 삭제처리합니다. (남장섭, 2025.01.23) 위의 동시처리와 무관하게 진행되어야 함.
            result += QM01Mapper.deleteQualityResp(paramMap);

            // 5.결과자료에 대한 결재자 정보가 있으면 삭제처리합니다.
            // --> reqNo 에 rsltNo=결과처리번호에 해당하는 결재정보 삭제처리 진행
            HashMap<String, String> paramApproval = new HashMap<>();
            paramApproval.put("salesCd", paramMap.get("salesCd"));
            paramApproval.put("reqNo", paramMap.get("rsltNo"));
            paramApproval.put("histNo", "");
            sharngChk = QM01Mapper.deleteWbsSharngListChk(paramApproval);
            if (sharngChk.size() > 0) {
                QM01Mapper.deleteWbsSharngList(paramApproval);
            }
        }
        // **************************************************************************************
        // 여기까지 발주요청 결과자료 삭제처리 끝입니다. --아래 7번 첨부파일 삭제 까지 처리되어야 합니다.
        // **************************************************************************************

        // 6. 발주요청서 첨부자료 삭제 처리
	  	//---------------------------------------------------------------  
		//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
		//---------------------------------------------------------------  
		if (deleteFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteFileList) {
		    	String fileKey = deleteDtl.get("fileKey").toString();
			    cm08Svc.deleteFile( fileKey );
		    }
		}

        if (paramMap.containsKey("rsltNo")) {
            // 7.결과에 등록된 첨부파일도 같이 삭제처해야함. 현재의 Class의 마지막에 파일 삭제처리해야함.~~꼭
            // WHERE FILE_TRGT_TYP = #{fileTrgtTyp} --> 결과는 'QM0101P03'으로 저장됨.
            // AND FILE_TRGT_KEY = #{fileTrgtKey} --> 발주요청서의 fileTrgtKey 값으로 동일하게 처리함.
            HashMap<String, String> paramResultFile = new HashMap<>();
            paramResultFile.put("fileTrgtTyp", "QM0101P03");
            paramResultFile.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            deleteFileList = cm08Svc.selectFileListAll(paramResultFile);
            if (deleteFileList.size() > 0) {
                for (Map<String, String> deleteDtl : deleteFileList) {
                    String fileKey = deleteDtl.get("fileKey").toString();
                    cm08Svc.deleteFile(fileKey);
                }
            }
        }
		//---------------------------------------------------------------  
		//첨부 화일 처리  끝 
		//---------------------------------------------------------------  
		 	  
	    return result;
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
	  int result = 0;
	  
	  //REQxxxx : 발주요청서, RESxxxx : 발주요청서 결과서 
	  if (rsltNo.substring(0, 2).equals("RE")) {
//	  if(rsltNo.substring(0, 3).equals("RES")) {
//		  paramMap.put("reqSt", "REQST03");
//	  }
//	  else {
//		  paramMap.put("reqSt", "REQST02");
//	  }
//	  // updateReqStChk실행 쿼리안에 F_QM01RESULT_CHECK(#{coCd}, #{salesCd}, #{todoNo}) 함수에서 상태코드 변경함
		  result = QM01Mapper.updateReqStChk(paramMap);
	  }
	  
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
 
  @Override
  public List<Map<String, String>> nonOrderStatusList(Map<String, String> paramMap) {
    return QM01Mapper.nonOrderStatusList(paramMap);
  }
  
}