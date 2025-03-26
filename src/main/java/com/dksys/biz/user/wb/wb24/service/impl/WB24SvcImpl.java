package com.dksys.biz.user.wb.wb24.service.impl;

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
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.dksys.biz.util.ExceptionThrower;
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
	public List<Map<String, String>> selectWbsIssueListDashboard(Map<String, String> paramMap) {
		return wb24Mapper.selectWbsIssueListDashboard(paramMap);
	}

	@Override
	  public Map<String, String> selectMaxWbsIssueNo(Map<String, String> paramMap) {
			return wb24Mapper.selectMaxWbsIssueNo(paramMap);
	}

	@Override
	public int wbsIssueInsert(Map<String, String> paramMap , MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb24Mapper.selectWbsIssueSeqNext(paramMap);
		paramMap.put("issFileTrgtKey", Integer.toString(fileTrgtKey));

		//이슈번호 발번
		Map<String, String> createIssNo = wb24Mapper.selectMaxWbsIssueNo(paramMap);
		paramMap.put("issNo", createIssNo.get("issNo"));

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		String midCode = paramMap.get("CODECOBGB");
		String midCd = midCode.substring(0, 7);
		paramMap.put("midCd", midCd);
		int result = wb24Mapper.wbsIssueInsert(paramMap);

		Gson gson = new Gson();

		String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 공유";

		String pgParam = "{\"actionType\":\""+ "I" +"\",";
		pgParam += "\"fileTrgtKey\":\""+ fileTrgtKey +"\",";
		pgParam += "\"issFileTrgtKey\":\""+ fileTrgtKey +"\",";
		pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\",";
		pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		pgParam += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\"}";

		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("reqNo", paramMap.get("issNo"));
				sharngMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
				sharngMap.put("pgmId", paramMap.get("pgmId"));
				sharngMap.put("userId", paramMap.get("userId"));
				sharngMap.put("sanCtnSn",Integer.toString(i+1));
				sharngMap.put("pgParam", pgParam);
				sharngMap.put("todoTitle", todoTitle1);
				QM01Mapper.insertWbsSharngList(sharngMap);
				i++;
			}
		}

		String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") " + paramMap.get("wbsPlanCodeNm") + " 이슈 결재";

		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
	        for (Map<String, String> approvalMap : approvalArr) {
				approvalMap.put("reqNo", paramMap.get("issNo"));
				approvalMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
				approvalMap.put("pgmId", paramMap.get("pgmId"));
				approvalMap.put("userId", paramMap.get("userId"));
				approvalMap.put("sanCtnSn",Integer.toString(i+1));
				approvalMap.put("pgParam", pgParam);
				approvalMap.put("todoTitle", todoTitle2);
				QM01Mapper.insertWbsApprovalList(approvalMap);
				i++;
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

  	    for(String fileKey : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			//접근 권한 없으면 Exception 발생
			param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
  		//---------------------------------------------------------------
  		//첨부 화일 권한체크  끝
  		//---------------------------------------------------------------

  	    /************************************************************************
  	     * 1. 문제등록 내역 변경적용
  	     *************************************************************************/
		String midCode = paramMap.get("CODECOBGB");
		String midCd = midCode.substring(0, 7);
		paramMap.put("midCd", midCd);
	    int result = wb24Mapper.wbsIssueUpdate(paramMap);

		Gson gson = new Gson();

		String pgParam = "{\"actionType\":\""+ "I" +"\",";
		pgParam += "\"fileTrgtKey\":\""+ paramMap.get("issFileTrgtKey") +"\",";
		pgParam += "\"issFileTrgtKey\":\""+ paramMap.get("issFileTrgtKey") +"\",";
		pgParam += "\"coCd\":\""+ paramMap.get("coCd") +"\",";
		pgParam += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		pgParam += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\"}";

		//String todoTitle = "이슈번호 : " + paramMap.get("issNo") + ",   이슈제목 : " + paramMap.get("issSj");

		paramMap.put("actReqNo", paramMap.get("reqNo"));
		paramMap.put("reqNo", paramMap.get("issNo"));
		paramMap.put("salesCd", paramMap.get("salesCd"));

  	    /************************************************************************
  	     * 2. 문제등록 발생영역 결재정보 및 카톡 생성
  	     *    2-1. 접수 또는 진행중인경우 결재정보 처리 영역임
  	     *       -rowSharngListArr : 문제등록의 공유정보
  	     *       -rowApprovalListArr : 문제등록의 결재정보
  	     *************************************************************************/

	    if (!"ISSSTS03".equals(paramMap.get("issSts"))) {	//접수, 또는 진행중이면
            QM01Mapper.updateWb24SharngList1(paramMap); // TB_WB20M03 --> SET ETC_FIELD3 = 'DEL' 로 변경처리
            QM01Mapper.updateWb24ApprovalList1(paramMap); // TB_WB20M03 --> SET ETC_FIELD3 = 'DEL' 로 변경처리

            String todoTitle1 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") "
                    + paramMap.get("wbsPlanCodeNm") + " 이슈 공유";

            Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);

            if (sharngArr != null && sharngArr.size() > 0) {
                int i = 0;
                for (Map<String, String> sharngMap : sharngArr) {
                    sharngMap.put("reqNo", paramMap.get("issNo"));
                    sharngMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
                    sharngMap.put("pgmId", paramMap.get("pgmId"));
                    sharngMap.put("userId", paramMap.get("userId"));
                    sharngMap.put("sanCtnSn", Integer.toString(i + 1));
                    sharngMap.put("pgParam", pgParam);
                    sharngMap.put("todoTitle", todoTitle1);
                    QM01Mapper.insertWb24SharngList(sharngMap); // TB_WB20M03 --> SET T.ETC_FIELD3 = 'KAKAO' 로 변경처리, Merge 문에서 insert만 N으로 설정하여 카톡
                                                                // 전송처리함.
                    i++;
				}
            }

            String todoTitle2 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") "
                    + paramMap.get("wbsPlanCodeNm") + " 이슈 결재";

            // 결재
            Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);

            if (approvalArr != null && approvalArr.size() > 0) {
                int i = 0;
                for (Map<String, String> approvalMap : approvalArr) {
                    approvalMap.put("reqNo", paramMap.get("issNo"));
                    approvalMap.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
                    approvalMap.put("pgmId", paramMap.get("pgmId"));
                    approvalMap.put("userId", paramMap.get("userId"));
                    approvalMap.put("sanCtnSn", Integer.toString(i + 1));
                    approvalMap.put("pgParam", pgParam);
                    approvalMap.put("todoTitle", todoTitle2);
                    QM01Mapper.insertWb24SharngList(approvalMap); // TB_WB20M03 --> SET T.ETC_FIELD3 = 'KAKAO' 로 변경처리, Merge 문에서 insert만 N으로 설정하여
                                                                  // 카톡 전송처리함.
                    i++;
                }
			}

	        // 2024.03.20 김성욱 추가
            QM01Mapper.deleteWb24SharngList1(paramMap); // TB_WB20M03 --> ETC_FIELD3 = 'DEL'삭제처리
	        // 2024.03.20 김성욱 추가
            QM01Mapper.deleteWb24ApprovalList1(paramMap); // TB_WB20M03 --> ETC_FIELD3 = 'DEL'삭제처리

	    } else {
  	    /************************************************************************
  	     *    2-2. 문제처리 결과 등록 또는 수정 작업 진행
  	     *
  	     *************************************************************************/
	    //진행상태가 완료("ISSSTS03") 일경우 결과 등록 또는 Update 진행
//	    if ("ISSSTS03".equals(paramMap.get("issSts"))) {
//			paramMap.put("reqNo", paramMap.get("actReqNo"));
	    	//TB_WB24M03 테이블에 issNo 가 등록되어있는지 확인
			List<Map<String, String>> issueResultChk = wb24Mapper.issueResultChk(paramMap);

			if (issueResultChk.size() > 0) {
				result += wb24Mapper.wbsActUpdate(paramMap);
			} else {
				result += wb24Mapper.wbsActInsert(paramMap);
			}

  	    /************************************************************************
  	     *    2-3. 문제처리 결과의 결재정보 처리 영역임
  	     *       -rowSharngListArr2 : 문제등록 결과의 공유정보
  	     *       -rowApprovalListArr2 : 문제등록 결과의 결재정보
  	     *************************************************************************/
			QM01Mapper.updateWb24SharngList2(paramMap);// TB_WB20M03 --> SET    ETC_FIELD3 = 'DEL' 로 변경처리
			QM01Mapper.updateWb24ApprovalList2(paramMap);// TB_WB20M03 --> SET    ETC_FIELD3 = 'DEL' 로 변경처리
            // 조치정보 공유 결재 처리 프로세스
            String todoTitle3 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") "
                    + paramMap.get("wbsPlanCodeNm") + " 조치 공유";

            Type stringList4 = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> sharngArr2 = gson.fromJson(paramMap.get("rowSharngListArr2"), stringList4);
            if (sharngArr2 != null && sharngArr2.size() > 0) {

                int i = 0;

                for (Map<String, String> sharngMap2 : sharngArr2) {
                    sharngMap2.put("reqNo", paramMap.get("issNo"));
                    sharngMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
                    sharngMap2.put("pgmId", paramMap.get("pgmId"));
                    sharngMap2.put("userId", paramMap.get("userId"));
                    sharngMap2.put("sanCtnSn", Integer.toString(i + 1));
                    sharngMap2.put("pgParam", pgParam);
                    sharngMap2.put("todoTitle", todoTitle3);
                    QM01Mapper.insertWb24SharngList(sharngMap2);// TB_WB20M03 --> SET T.ETC_FIELD3 = 'KAKAO'
                    i++;
                }
            }

            // 결재
            String todoTitle4 = paramMap.get("clntNm") + "-" + paramMap.get("clntPjtNm") + "(" + paramMap.get("salesCd") + ") "
                    + paramMap.get("wbsPlanCodeNm") + " 조치 결재";

            Type stringList5 = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> approvalArr2 = gson.fromJson(paramMap.get("rowApprovalListArr2"), stringList5);
            if (approvalArr2 != null && approvalArr2.size() > 0) {

                int i = 0;
                for (Map<String, String> approvalMap2 : approvalArr2) {
                    approvalMap2.put("reqNo", paramMap.get("issNo"));
                    approvalMap2.put("fileTrgtKey", paramMap.get("issFileTrgtKey"));
                    approvalMap2.put("pgmId", paramMap.get("pgmId"));
                    approvalMap2.put("userId", paramMap.get("userId"));
                    approvalMap2.put("sanCtnSn", Integer.toString(i + 1));
                    approvalMap2.put("pgParam", pgParam);
                    approvalMap2.put("todoTitle", todoTitle4);
                    QM01Mapper.insertWb24SharngList(approvalMap2);// TB_WB20M03 --> SET T.ETC_FIELD3 = 'KAKAO'
                    i++;
                }
			}

			// 2024.03.20 김성욱 추가
	        QM01Mapper.deleteWb24SharngList2(paramMap);	// TB_WB20M03 --> TODO_DIV2_CODE_ID = 'TODODIV1090' AND TRIM(ETC_FIELD3) = 'DEL' 정보 삭제처리
	        // 2024.03.20 김성욱 추가
	        QM01Mapper.deleteWb24ApprovalList2(paramMap);	// TB_WB20M03 --> TODO_DIV2_CODE_ID = 'TODODIV2090' AND TRIM(ETC_FIELD3) = 'DEL' 정보 삭제처리
	    }

  	    /************************************************************************
  	     *    3. 첨부 화일 처리 시작  영역임
  	     *************************************************************************/
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
				if (sharngMap.containsKey("issFileTrgtKey")) {
					result = wb24Mapper.wbsIssCloseYnConfirm(sharngMap);
					result++;
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

	@Override
	public Map<String, String> select_wb2401p01_Info(Map<String, String> paramMap) {
		return wb24Mapper.select_wb2401p01_Info(paramMap);
	}


	@Override
    public int wbsIssueDelete(Map<String, String> paramMap) throws Exception {

	    //---------------------------------------------------------------
  		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
  	  	//   필수값 :  jobType, userId, comonCd
  		//---------------------------------------------------------------
  	    HashMap<String, String> param = new HashMap<>();
     	param.put("userId", paramMap.get("userId"));

	    List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
        for (Map<String, String> deleteFile : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			//접근 권한 없으면 Exception 발생
			param.put("comonCd", deleteFile.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
  		//---------------------------------------------------------------
  		//첨부 화일 권한체크  끝
  		//---------------------------------------------------------------

        //발주요청서 등록여부 체크 발주요청서 등록되었으면 삭제물가 start
        Map<String, String> select_wb2401p01_Info = wb24Mapper.select_wb2401p01_Info(paramMap);
        if (select_wb2401p01_Info.get("reqNo") != null) {
			if ("".equals(select_wb2401p01_Info.get("reqNo"))) {
				//요인별 발주요청서가 등록되지 않았으므로 삭제가능 체크.
			} else {
				//요인별 발주요청서가 있으므로 삭제불가능합니다.
				thrower.throwCommonException("fail");
			}
        }
        //발주요청서 등록여부 체크 발주요청서 등록되었으면 삭제물가 end

        //결재 진행여부 체크 한명이라도 결재처리 되었으면 삭제 물가 처리 start
		List<Map<String, String>> actChkList = wb24Mapper.actChk(paramMap);
		//issNo에 해당하는 조치결과가 있으면 삭제 불가함.
		int result = 0;
		if (actChkList.size() > 0) {
//		    result = wb24Mapper.wbsIssueResultDelete(paramMap);
			thrower.throwCommonException("fail");
		}
        //결재 진행여부 체크 한명이라도 결재처리 되었으면 삭제 물가 처리 start

		//issNo에 해당하는 이슈정보  삭제.
	    result += wb24Mapper.wbsIssueDelete(paramMap);

		//issNo에 해당하는 결재, 공유정보 삭제.
    	//Key : CO_CD, SALES_CD, TODO_NO
		paramMap.put("reqNo", paramMap.get("issNo"));

		QM01Mapper.deleteWbsSharngList1(paramMap);		//TODO_DIV2_CODE_ID = 'TODODIV1030'  WBS이슈-공유
		QM01Mapper.deleteWbsApprovalList1(paramMap);	//TODO_DIV2_CODE_ID = 'TODODIV2060'  WBS이슈-결재
		QM01Mapper.deleteWbsSharngList2(paramMap);		//TODO_DIV2_CODE_ID = 'TODODIV1090'  WBS조치-공유
		QM01Mapper.deleteWbsApprovalList2(paramMap);	//TODO_DIV2_CODE_ID = 'TODODIV2090'  WBS조치-결재

		//---------------------------------------------------------------
		//첨부 화일 처리 시작
//		//---------------------------------------------------------------
        for (Map<String, String> deleteFile : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
	    	cm08Svc.deleteFile(deleteFile.get("fileKey"));
	    }
		//---------------------------------------------------------------
		//첨부 화일 처리  끝
		//---------------------------------------------------------------

		return result;
	}

	//조치담당자의 팀장 정보가져오기
	@Override
	public Map<String, String> selectTeamManagerInfo(Map<String, String> paramMap) {
		return wb24Mapper.selectTeamManagerInfo(paramMap);
	}

    // 부서코드로 팀장 정보가져오기
    @Override
    public Map<String, String> selectDept2TeamManagerInfo(Map<String, String> paramMap) {
        return wb24Mapper.selectDept2TeamManagerInfo(paramMap);
    }

	//조치담당자의 팀장 정보가져오기
	@Override
	public Map<String, String> selectTeamManagerSpecialInfo(Map<String, String> paramMap) {
		return wb24Mapper.selectTeamManagerSpecialInfo(paramMap);
	}

	//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
	@Override
	public int updateWbsIssueResultEvaluate(Map<String, String> paramMap) {
		return wb24Mapper.updateWbsIssueResultEvaluate(paramMap);
	}

	@Override
	public Map<String, String> select_wb2401p01_planInfo(Map<String, String> paramMap) {
		return wb24Mapper.select_wb2401p01_planInfo(paramMap);
	}

	@Override
	public Map<String, String> select_wb2401p01_rsltInfo(Map<String, String> paramMap) {
		return wb24Mapper.select_wb2401p01_rsltInfo(paramMap);
	}

    @Override
    public List<Map<String, String>> selectVendProblemList(Map<String, String> paramMap) {
        return wb24Mapper.selectVendProblemList(paramMap);
    }

	// 발생공급업체 Update
	@Override
	public int updateVendCd(Map<String, String> param) {
		return wb24Mapper.updateVendCd(param);
	}


	// 문제정보를 가지고 발주요청서 등록
	@Override
	public Map<String, String> selectIssueInfo(Map<String, String> paramMap) {
		return wb24Mapper.selectIssueInfo(paramMap);
	}
}