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
import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.user.am.sync.ErpAmSyncHelper;
import com.dksys.biz.user.dw.dw03.mapper.DW03Mapper;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB24SvcImpl implements WB24Svc {

	// 화면 actEditorYn 특수부서 조치담당자 기준(WB2401P11)
	private static final List<String> ISSUE_ACT_EDITOR_SPECIAL_DEPT_CDS = Arrays.asList("TRN50", "GUN60", "GUN70");

	@Autowired
	WB24Mapper wb24Mapper;

	@Autowired
	WB24Svc wb24Svc;

	@Autowired
	WB20Svc wb20Svc;

    @Autowired
    CM08Svc cm08Svc;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    QM01Mapper QM01Mapper;

	@Autowired
	DW03Mapper dw03Mapper;

	@Autowired
	ExceptionThrower thrower;

	@Autowired
	ErpAmSyncHelper erpAmSyncHelper;

	@Autowired
	AM11Svc am11Svc;

	@Autowired
	WbsIssueAmSyncAdapter wbsIssueAmSyncAdapter;

	@Override
	public int selectWbsIssueListCount(Map paramMap) {
		return wb24Mapper.selectWbsIssueListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsIssueList(Map paramMap) {
		return wb24Mapper.selectWbsIssueList(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsIssueCountBySalesCds(Map paramMap) {
		return wb24Mapper.selectWbsIssueCountBySalesCds(paramMap);
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

		syncAmApproval(paramMap, "TODODIV2060");
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
		    String issSts = paramMap.get("issSts");
		    String issNo = paramMap.get("issNo");
		    String loginUserId = resolveLoginUserId(paramMap);
		    assertIssueStatusTransition(issNo, issSts, loginUserId);
			if (!"ISSSTS03".equals(issSts) && !"ISSSTS04".equals(issSts)) {	//접수, 진행중(2-1 분기)만 수정 권한 검증
				assertIssueEditPermitted(issNo, loginUserId);
			} else {
				refillIssueFieldsForNonEditor(paramMap);	// 조치 단계: 담당팀장·특수 예외 외 요청의 변경은 DB 현재값으로 되채움
			}
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

	    if (!"ISSSTS03".equals(paramMap.get("issSts")) && !"ISSSTS04".equals(paramMap.get("issSts"))) {	//접수, 또는 진행중이면
			boolean preserveExisting2060Lines = !isIssueRejected(issNo)
					&& hasStageApprovedLine(issNo, "TODODIV2060")
					&& !(isIssueCreator(issNo, loginUserId) && !hasOtherIssueApproval(issNo, loginUserId));
			if (preserveExisting2060Lines) {
				// 타인 결재가 이미 진행된 건을 담당팀장이 최종 수정할 때는 기존 결재선을 보존하고 AM 문서 내용만 갱신한다.
				updateAmDocContentOnly(paramMap, "TODODIV2060");
			} else {
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
				resetWbsIssueRejectedApprovalLines(paramMap, "TODODIV2060");
				applyIssueDrafterSelfApproval(paramMap, approvalArr);
				syncAmApproval(paramMap, "TODODIV2060");
			}

	    } else {
  	    /************************************************************************
  	     *    2-2. 문제처리 결과 등록 또는 수정 작업 진행
  	     *
  	     *************************************************************************/
	    //진행상태가 완료("ISSSTS03") 또는 진행상태("ISSSTS04") 일경우 결과 등록 또는 Update 진행
//	    if ("ISSSTS03".equals(paramMap.get("issSts"))) {
//			paramMap.put("reqNo", paramMap.get("actReqNo"));
	    	//TB_WB24M03 테이블에 issNo 가 등록되어있는지 확인
			List<Map<String, String>> issueResultChk = wb24Mapper.issueResultChk(paramMap);

			String fdmtSolutCd = paramMap.get("FDMTSOLUT");
			paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)
			if (issueResultChk.size() > 0) {
				result += wb24Mapper.wbsActUpdate(paramMap);
			} else {
				result += wb24Mapper.wbsActInsert(paramMap);
			}

			// DW Version테이블에 확정정보 Update 처리
			Type listType = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
			List<Map<String, String>> actFiles = gson.fromJson(paramMap.get("actFilesJson"), listType);

			if (actFiles != null && !actFiles.isEmpty()) {
				for (Map<String, String> fileMap : actFiles) {
					fileMap.put("confirmActNo",  paramMap.get("issNo"));
					fileMap.put("userId",  paramMap.get("userId"));
					result += dw03Mapper.updateDwConfirm(fileMap);
				}
			}

  	    /************************************************************************
  	     *    2-3. 문제처리 결과의 결재정보 처리 영역임
  	     *       -rowSharngListArr2 : 문제등록 결과의 공유정보
  	     *       -rowApprovalListArr2 : 문제등록 결과의 결재정보
  	     *************************************************************************/
			boolean preserveExisting2090Lines = "ISSSTS03".equals(paramMap.get("issSts"))
					&& !isStageRejected(issNo, "TODODIV2090")
					&& (hasOtherStageApproval(issNo, loginUserId, "TODODIV2090")
							|| (isIssueTeamManager(issNo, loginUserId) && hasStageApprovedLine(issNo, "TODODIV2090")));
			if (preserveExisting2090Lines) {
				updateAmDocContentOnly(paramMap, "TODODIV2090");
			} else {
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

				// 2024.03.20 김성욱 추가 (자체승인 검증 전에 삭제 예정 임시행 DEL 을 먼저 정리한다)
				QM01Mapper.deleteWb24SharngList2(paramMap);	// TB_WB20M03 --> TODO_DIV2_CODE_ID = 'TODODIV1090' AND TRIM(ETC_FIELD3) = 'DEL' 정보 삭제처리
				// 2024.03.20 김성욱 추가
				QM01Mapper.deleteWb24ApprovalList2(paramMap);	// TB_WB20M03 --> TODO_DIV2_CODE_ID = 'TODODIV2090' AND TRIM(ETC_FIELD3) = 'DEL' 정보 삭제처리
				resetWbsIssueRejectedApprovalLines(paramMap, "TODODIV2090");

				// 전체 조치 결재선을 DB에 반영한 뒤 기안자 본인 자체승인을 처리해야 타 결재자 존재 여부를 정확히 판정한다.
				if (approvalArr2 != null && approvalArr2.size() > 0) {
					for (Map<String, String> approvalMap2 : approvalArr2) {
						if (approvalMap2.get("userId") != null
								&& approvalMap2.get("userId").equals(approvalMap2.get("todoId"))
								&& !"Y".equals(approvalMap2.get("sanctnSttus"))) {
							approvalMap2.put("todoCfOpn", "자체승인");
							approvalMap2.put("sanctnSn", approvalMap2.get("sanCtnSn"));
							approvalMap2.put("todoNo", paramMap.get("issNo"));
							approvalMap2.put("issNo", paramMap.get("issNo"));

							String appDeptId = approvalMap2.get("deptId");
							if (appDeptId == null || appDeptId.isEmpty()) {
								appDeptId = paramMap.get("actDeptId");
							}
							if (appDeptId == null || appDeptId.isEmpty()) {
								appDeptId = paramMap.get("deptId");
							}
							if (appDeptId != null && appDeptId.length() >= 5) {
								appDeptId = appDeptId.substring(0, 5);
							}

							if ("GUN30".equals(appDeptId) ||
								"GUN40".equals(appDeptId) ||
								"TRN50".equals(appDeptId) ||
								"GUN60".equals(appDeptId) ||
								"GUN70".equals(appDeptId)) {
									approvalMap2.put("actMh", paramMap.get("actMh"));
									approvalMap2.put("etcField1", paramMap.get("actMh"));
									approvalMap2.put("deptId", appDeptId);
							}
							wb20Svc.insertApprovalLine(approvalMap2);
						}
					}
				}
				syncAmApproval(paramMap, "TODODIV2090");
			}
			updateAmDocContentOnly(paramMap, "TODODIV2060");
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
	public int updateIssueComment(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		String issNo = paramMap.get("issNo");
			String classEditStage = resolveIssueClassEditStage(issNo, resolveLoginUserId(paramMap));
			if (classEditStage.isEmpty()) {
				throw new RuntimeException("담당 팀장만 수정할 수 있습니다.");
			}
			if ("TODODIV2090".equals(classEditStage)) {	// 근본원인·대책·원인·결과는 조치결재(2090) 단계에서만 반영 (2060 단계 요청의 해당 값은 무시)
				// 기존 코멘트 업데이트 (원인, 결과, 근본원인, 근본대책)
				String fdmtSolutCd = paramMap.get("FDMTSOLUT");
				paramMap.put("fdmtSolutCd", fdmtSolutCd);		// 선택된 근본대책 추가 (FDMTSOLUT=FDMTSOLUT01,FDMTSOLUT02,FDMTSOLUT03..)
				result += wb24Mapper.updateIssueComment(paramMap);
			}

		String cobgbCode = paramMap.get("CODECOBGB");
		if (cobgbCode != null && cobgbCode.length() >= 7) {
			String midCd = cobgbCode.substring(0, 7);
			paramMap.put("midCd", midCd);
			paramMap.put("subCd", cobgbCode);
			// 장애유형 업데이트
			result += wb24Mapper.updateCobgb(paramMap);
		}
		if (hasValue(paramMap.get("mcPartCd")) || hasValue(paramMap.get("importantCd")) || hasValue(paramMap.get("impactCd"))) {
			result += wb24Mapper.updateIssueClassification(paramMap);	// 문제영역(M02) 기계분류·중요도·영향도
			result += wb24Mapper.updateActClassification(paramMap);	// 조치결과(M03) 동일 값 반영
		}
		if (result > 0) {
				updateAmDocContentOnly(paramMap, classEditStage);
				if ("TODODIV2090".equals(classEditStage)) {
					updateAmDocContentOnly(paramMap, "TODODIV2060");
				}
		}
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
		Map<String, String> result = wb24Mapper.select_wb2401p01_Info(paramMap);
		if (result != null && result.get("issNo") != null && !result.get("issNo").trim().isEmpty()) {
				String loginUserId = resolveLoginUserId(paramMap);
				result.put("issEditAllowedYn", isIssueEditAllowed(result.get("issNo"), loginUserId) ? "Y" : "N");
				String classEditStage = resolveIssueClassEditStage(result.get("issNo"), loginUserId);
				result.put("issClassEditAllowedYn", classEditStage.isEmpty() ? "N" : "Y");
				result.put("issClassEditStage", classEditStage);
		}
		return result;
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
			param.put("coCd", paramMap.get("coCd"));
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
  		//---------------------------------------------------------------
  		//첨부 화일 권한체크  끝
  		//---------------------------------------------------------------

        //발주요청서 등록여부 체크 발주요청서 등록되었으면 삭제물가 start
        Map<String, String> select_wb2401p01_Info = wb24Mapper.select_wb2401p01_Info(paramMap);
        if (select_wb2401p01_Info != null && select_wb2401p01_Info.get("reqNo") != null) {
			if ("".equals(select_wb2401p01_Info.get("reqNo"))) {
				//요인별 발주요청서가 등록되지 않았으므로 삭제가능 체크.
			} else {
				//요인별 발주요청서가 있으므로 삭제불가능합니다.
				thrower.throwCommonException("fail");
			}
        }
        //발주요청서 등록여부 체크 발주요청서 등록되었으면 삭제물가 end

        //결재 진행여부 체크: 작업자 본인 결재는 무시하고 타인 결재처리 건 존재 시 삭제 불가
		List<Map<String, String>> actChkList = wb24Mapper.selectIssueOtherApprovalChk(paramMap);
		//issNo에 해당하는 타인 결재가 있으면 삭제 불가함.
		int result = 0;
		if (actChkList.size() > 0) {
			throw new RuntimeException("결재 진행중인 자료는 삭제할 수 없습니다.\n\n결재 진행 상태 확인 바랍니다.");
		}
        //결재 진행여부 체크 end

		//issNo에 해당하는 이슈정보  삭제.
	    result += wb24Mapper.wbsIssueDelete(paramMap);

		//issNo에 해당하는 결재, 공유정보 삭제.
    	//Key : CO_CD, SALES_CD, TODO_NO
		paramMap.put("reqNo", paramMap.get("issNo"));

		QM01Mapper.deleteWbsSharngList1(paramMap);		//TODO_DIV2_CODE_ID = 'TODODIV1030'  WBS이슈-공유
		QM01Mapper.deleteWbsApprovalList1(paramMap);	//TODO_DIV2_CODE_ID = 'TODODIV2060'  WBS이슈-결재
		QM01Mapper.deleteWbsSharngList2(paramMap);		//TODO_DIV2_CODE_ID = 'TODODIV1090'  WBS조치-공유
		QM01Mapper.deleteWbsApprovalList2(paramMap);	//TODO_DIV2_CODE_ID = 'TODODIV2090'  WBS조치-결재
		erpAmSyncHelper.deleteAmDocs(wbsIssueAmSyncAdapter, paramMap);

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
		int result = wb24Mapper.updateVendCd(param);
		if (result > 0 && param != null && hasValue(param.get("issNo"))) {
			updateAmDocContentOnly(param, "TODODIV2060");
			updateAmDocContentOnly(param, "TODODIV2090");
		}
		return result;
	}


	// 문제정보를 가지고 발주요청서 등록
	@Override
	public Map<String, String> selectIssueInfo(Map<String, String> paramMap) {
		return wb24Mapper.selectIssueInfo(paramMap);
	}

	@Override
	public boolean isIssueApprovedOrMissing(String issNo) {
		Map<String, String> status = selectApprovalStatus(issNo, "TODODIV2060");
		return status == null || toInt(status.get("cnt")) == 0 || "Y".equals(status.get("todoYn"));
	}

	@Override
	public boolean isIssueRejected(String issNo) {
		Map<String, String> status = selectApprovalStatus(issNo, "TODODIV2060");
		return status != null && toInt(status.get("rejCnt")) > 0;
	}

	/**
	 * 조치결재 자체승인 예외: 기안자(조치 작성자 TB_WB24M03.CREAT_ID) 본인이 결재선 첫 번째 행이고, 다른 결재자가 있을 때만 허용.
	 */
	@Override
	public boolean isResultDrafterSelfApprovalAllowed(String issNo, String todoId, String userId) {
		if (issNo == null || issNo.trim().isEmpty() || todoId == null || todoId.trim().isEmpty() || !todoId.equals(userId)) {
			return false;
		}
		Map<String, String> creatorQuery = new HashMap<>();
		creatorQuery.put("issNo", issNo);
		Map<String, String> creator = wb24Mapper.selectWbsIssueCreatorInfo(creatorQuery);
		if (creator == null || !todoId.equals(creator.get("actCreatId"))) {
			return false;
		}
		Map<String, String> head = null;
		boolean hasOtherApprover = false;
		for (Map<String, String> row : selectApprovalLines(issNo, "TODODIV2090")) {
			if (head == null || toInt(row.get("sanctnSn")) < toInt(head.get("sanctnSn"))) {
				head = row;
			}
			if (!todoId.equals(row.get("todoId"))) {
				hasOtherApprover = true;
			}
		}
		return head != null && todoId.equals(head.get("todoId")) && hasOtherApprover;
	}

		private void applyIssueDrafterSelfApproval(Map<String, String> paramMap, List<Map<String, String>> approvalArr) {
			String userId = paramMap.get("userId");
			if (userId == null || userId.trim().isEmpty() || approvalArr == null) {
				return;
			}
			Map<String, String> drafterRow = null;
			for (Map<String, String> row : approvalArr) {
				if (userId.equals(row.get("usrNm"))) {
					drafterRow = row;
					break;
				}
			}
			if (drafterRow == null) {
				return;
			}
			Map<String, String> creator = selectIssueCreator(paramMap.get("issNo"));
			if (creator == null || !userId.equals(creator.get("issCreatId"))) {
				return;
			}
			Map<String, String> selfParam = new HashMap<>();
			selfParam.put("issNo", paramMap.get("issNo"));
			selfParam.put("userId", userId);
			selfParam.put("etcField1", drafterRow.get("etcField1"));
			selfParam.put("actMh", drafterRow.get("actMh"));
			wb24Mapper.updateIssueDrafterSelfApproval(selfParam);
		}

		private boolean isIssueEditAllowed(String issNo, String userId) {
			if (userId == null || userId.trim().isEmpty()) {
				return false;
			}
			if (isIssueTeamManager(issNo, userId)) {
				return true;
			}
			Map<String, String> creator = selectIssueCreator(issNo);
			if (creator == null || !userId.equals(creator.get("issCreatId"))) {
				return false;
			}
			return !hasOtherIssueApproval(issNo, userId);
		}

		private boolean hasOtherIssueApproval(String issNo, String userId) {
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("userId", userId);
			return wb24Mapper.selectIssueOther2060ApprovedCnt(query) > 0;
		}

		/** 2-1 분기 수정 가드: 담당팀장(ACT_ID의 MNG_ID)은 타인 결재 후에도 최종 수정 가능, 작성자는 타인 확정승인 0건일 때만, 그 외는 특수 조치편집 예외(F6)만 허용. */
		private void assertIssueEditPermitted(String issNo, String userId) {
			if (isIssueTeamManager(issNo, userId)) {
				return;
			}
			Map<String, String> creator = selectIssueCreator(issNo);
			String creatorId = creator == null ? null : creator.get("issCreatId");
			if (creatorId == null || creatorId.trim().isEmpty()) {
				return;
			}
			if (creatorId.equals(userId)) {
				if (hasOtherIssueApproval(issNo, userId)) {
					throw new RuntimeException("다른 결재자가 이미 승인하여 수정할 수 없습니다.");
				}
				return;
			}
			if (isIssueSpecialActEditor(issNo, userId)) {
				return;
			}
			throw new RuntimeException("문제 등록자 본인만 수정할 수 있습니다.");
		}

		/** 조치담당자(ACT_ID)의 실제 팀장(TB_CM06M01.MNG_ID) 여부 */
		private boolean isIssueTeamManager(String issNo, String userId) {
			if (!hasValue(issNo) || !hasValue(userId)) {
				return false;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("userId", userId);
			Map<String, String> check = wb24Mapper.selectIssueActEditorCheck(query);
			return check != null && "Y".equals(check.get("actMngYn"));
		}

		private boolean isIssueCreator(String issNo, String userId) {
			if (!hasValue(issNo) || !hasValue(userId)) {
				return false;
			}
			Map<String, String> creator = selectIssueCreator(issNo);
			return creator != null && userId.equals(creator.get("issCreatId"));
		}

		private boolean hasStageApprovedLine(String issNo, String todoDiv2CodeId) {
			if (!hasValue(issNo) || !hasValue(todoDiv2CodeId)) {
				return false;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("todoDiv2CodeId", todoDiv2CodeId);
			return wb24Mapper.selectIssueStageApprovedCnt(query) > 0;
		}

		private boolean hasOtherStageApproval(String issNo, String userId, String todoDiv2CodeId) {
			if (!hasValue(issNo) || !hasValue(userId) || !hasValue(todoDiv2CodeId)) {
				return false;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("userId", userId);
			query.put("todoDiv2CodeId", todoDiv2CodeId);
			return wb24Mapper.selectIssueStageOtherApprovedCnt(query) > 0;
		}

		private boolean isStageRejected(String issNo, String todoDiv2CodeId) {
			Map<String, String> status = selectApprovalStatus(issNo, todoDiv2CodeId);
			return status != null && toInt(status.get("rejCnt")) > 0;
		}

		private void updateAmDocContentOnly(Map<String, String> paramMap, String todoDiv2CodeId) {
			if (paramMap == null || !hasValue(paramMap.get("issNo"))) {
				return;
			}
			String existingDocId = wbsIssueAmSyncAdapter.selectExistingDocId(paramMap, todoDiv2CodeId);
			if (!hasValue(existingDocId)) {
				return;
			}
			String userId = resolveLoginUserId(paramMap);
			if (!hasValue(userId)) {
				userId = "SYSTEM";
			}
			Map<String, Object> amParam = new HashMap<>();
			amParam.put("docId", existingDocId);
			amParam.put("docTitle", wbsIssueAmSyncAdapter.buildTitle(paramMap, todoDiv2CodeId));
			amParam.put("docDataJson", wbsIssueAmSyncAdapter.buildDocDataJson(paramMap, todoDiv2CodeId));
			amParam.put("docRenderHtml", wbsIssueAmSyncAdapter.buildHtml(paramMap, todoDiv2CodeId));
			amParam.put("userId", userId);
			amParam.put("pgmId", hasValue(paramMap.get("pgmId")) ? paramMap.get("pgmId") : wbsIssueAmSyncAdapter.pgmId());
			am11Svc.updateApprovalDocContent(amParam);
		}

		/** 담당팀장 판정(공용): 로그인 사용자가 조치담당자(ACT_ID)의 팀장(MNG_ID)이고, 해당 단계(2060/2090) 본인 결재행이 존재하며 미승인(승인 아님 또는 반려)인지 DB 재조회 */
		private boolean isIssueTeamManagerPending(String issNo, String userId, String todoDiv2CodeId) {
			if (!hasValue(issNo) || !hasValue(userId)) {
				return false;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("userId", userId);
			query.put("todoDiv2CodeId", todoDiv2CodeId);
			return wb24Mapper.selectIssueTeamManagerPendingCnt(query) > 0;
		}

		/** 문제 수정 단계(화면 issClassEditStage 와 동일 판정): 2090 우선, 없으면 2060, 해당 없으면 빈값 */
		private String resolveIssueClassEditStage(String issNo, String userId) {
			if (isIssueTeamManagerPending(issNo, userId, "TODODIV2090")) {
				return "TODODIV2090";
			}
			if (isIssueTeamManagerPending(issNo, userId, "TODODIV2060")) {
				return "TODODIV2060";
			}
			return "";
		}

		/** 특수 조치편집 예외(F6): 특수부서(TRN50/GUN60/GUN70) 조치담당자 본인, 또는 조치담당부서 팀장 결재자 */
		private boolean isIssueSpecialActEditor(String issNo, String userId) {
			if (!hasValue(issNo) || !hasValue(userId)) {
				return false;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("userId", userId);
			Map<String, String> check = wb24Mapper.selectIssueActEditorCheck(query);
			if (check == null) {
				return false;
			}
			boolean subManagerOfActDept = "Y".equals(check.get("actSameDeptYn"))
					&& toInt(check.get("userSubCnt")) > 0 && toInt(check.get("userActLineCnt")) > 0;
			boolean specialDeptActUser = "Y".equals(check.get("actSelfYn"))
					&& ISSUE_ACT_EDITOR_SPECIAL_DEPT_CDS.contains(check.get("userDeptCd"));
			return subManagerOfActDept || specialDeptActUser;
		}

		/** 조치 단계(03/04) 저장에서 6개 필드(문제영역·근본원인·대책) 반영 가능 여부: 담당팀장(2090 본인 미승인) 또는 특수 조치편집 예외 */
		private boolean isIssueRefillExempt(String issNo, String userId) {
			return isIssueTeamManagerPending(issNo, userId, "TODODIV2090") || isIssueSpecialActEditor(issNo, userId);
		}

		/** 상태 전환 가드(DB 기준): 조치 단계(03/04) 문제를 접수/진행중(01/02)으로 되돌리기 금지, 접수/진행중 → 완료(03) 전환은 조치담당자 본인만 */
		private void assertIssueStatusTransition(String issNo, String requestedSts, String userId) {
			boolean reopenRequested = "ISSSTS01".equals(requestedSts) || "ISSSTS02".equals(requestedSts);
			boolean completeRequested = "ISSSTS03".equals(requestedSts);
			if (!reopenRequested && !completeRequested) {
				return;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			Map<String, String> current = wb24Mapper.selectIssueStsActInfo(query);
			if (current == null) {
				return;
			}
			String currentSts = current.get("issSts");
			if (reopenRequested && ("ISSSTS03".equals(currentSts) || "ISSSTS04".equals(currentSts))) {
				throw new RuntimeException("조치 단계로 진행된 문제는 접수/진행중 상태로 되돌릴 수 없습니다.");
			}
			if (completeRequested && ("ISSSTS01".equals(currentSts) || "ISSSTS02".equals(currentSts))
					&& (userId == null || !userId.equals(current.get("actId")))) {
				throw new RuntimeException("완료 상태 변경은 조치담당자만 할 수 있습니다.");
			}
		}

		/** 조치 단계(03/04) 저장: 담당팀장은 문제·조치 내용 모두 수정 가능. 그 외에는 제목·내용 DB 현재값 되채움, 담당팀장(2090)·특수 예외 외에는 분류·근본원인·대책도 되채움 */
		private void refillIssueFieldsForNonEditor(Map<String, String> paramMap) throws Exception {
			String issNo = paramMap.get("issNo");
			String loginUserId = resolveLoginUserId(paramMap);
			if (isIssueTeamManager(issNo, loginUserId)) {
				return;
			}
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			query.put("issFileTrgtKey", paramMap.get("issFileTrgtKey"));
			Map<String, Object> current = wb24Mapper.selectIssueRefillValues(query);
			if (current == null) {
				throw new RuntimeException("문제 정보를 찾을 수 없습니다.");
			}
			paramMap.put("issSj", textOf(current.get("issSj")));
			paramMap.put("issCnts", textOf(current.get("issCnts")));
			if (isIssueRefillExempt(issNo, loginUserId)) {
				return;
			}
			paramMap.put("CODECOBGB", textOf(current.get("subCd")));
			paramMap.put("midCd", textOf(current.get("midCd")));
			paramMap.put("etcInput", textOf(current.get("etcInput")));
			paramMap.put("mcPartCd", textOf(current.get("mcPartCd")));
			paramMap.put("importantCd", textOf(current.get("importantCd")));
			paramMap.put("impactCd", textOf(current.get("impactCd")));
			paramMap.put("FDMTSOLUT", textOf(current.get("fdmtSolutCd")));
			paramMap.put("fdmtSolutCnt", textOf(current.get("fdmtSolutCnt")));
		}

		private static String textOf(Object value) throws Exception {
			if (value == null) {
				return "";
			}
			if (value instanceof java.sql.Clob) {
				java.sql.Clob clob = (java.sql.Clob) value;
				return clob.getSubString(1, (int) clob.length());
			}
			return String.valueOf(value);
		}

		private static boolean hasValue(String value) {
			return value != null && !value.trim().isEmpty();
		}

		private String resolveLoginUserId(Map<String, String> paramMap) {
			String loginUserId = paramMap.get("loginUserId");
			return (loginUserId != null && !loginUserId.trim().isEmpty()) ? loginUserId : paramMap.get("userId");
		}

		private Map<String, String> selectIssueCreator(String issNo) {
			Map<String, String> query = new HashMap<>();
			query.put("issNo", issNo);
			return wb24Mapper.selectWbsIssueCreatorInfo(query);
		}

		private void syncAmApproval(Map<String, String> paramMap, String todoDiv2CodeId) {
		boolean isAction = "TODODIV2090".equals(todoDiv2CodeId);
		syncWbsIssueApprovalCreatDttm(
				paramMap.get("issNo"),
				todoDiv2CodeId,
				isAction ? "TODODIV1090" : "TODODIV1030");
		Map<String, String> amParam = new HashMap<>(paramMap);
		Map<String, String> creator = wb24Mapper.selectWbsIssueCreatorInfo(paramMap);
		if (creator != null) {
			String reqId = creator.get(isAction ? "actCreatId" : "issCreatId");
			if (reqId != null && !reqId.trim().isEmpty()) {
				amParam.put("reqId", reqId);
				amParam.put("reqNm", creator.get(isAction ? "actCreatNm" : "issCreatNm"));
			}
		}
		erpAmSyncHelper.sync(wbsIssueAmSyncAdapter, amParam, todoDiv2CodeId);
		updateAmDocContentOnly(amParam, todoDiv2CodeId);
	}

	private void resetWbsIssueRejectedApprovalLines(Map<String, String> paramMap, String todoDiv2CodeId) {
		String issNo = paramMap != null ? paramMap.get("issNo") : null;
		if (issNo == null || issNo.trim().isEmpty()) {
			return;
		}
		Map<String, String> query = new HashMap<>();
		query.put("issNo", issNo);
		query.put("todoDiv2CodeId", todoDiv2CodeId);
		query.put("userId", paramMap.get("userId"));
		query.put("pgmId", paramMap.get("pgmId"));
		wb24Mapper.resetWbsIssueRejectedApprovalLines(query);
	}

	private void syncWbsIssueApprovalCreatDttm(String issNo, String apprDiv2, String refDiv2) {
		if (issNo == null || issNo.trim().isEmpty()) {
			return;
		}
		Map<String, String> query = new HashMap<>();
		query.put("issNo", issNo);
		query.put("apprDiv2", apprDiv2);
		query.put("refDiv2", refDiv2);
		wb24Mapper.syncWbsIssueApprovalCreatDttm(query);
	}

	private Map<String, String> selectApprovalStatus(String issNo, String todoDiv2CodeId) {
		if (issNo == null || issNo.trim().isEmpty()) {
			return null;
		}
		Map<String, String> query = new HashMap<>();
		query.put("issNo", issNo);
		query.put("todoDiv2CodeId", todoDiv2CodeId);
		return wb24Mapper.selectWbsIssueApprovalStatus(query);
	}

	private List<Map<String, String>> selectApprovalLines(String issNo, String todoDiv2CodeId) {
		Map<String, String> query = new HashMap<>();
		query.put("issNo", issNo);
		query.put("todoDiv2CodeId", todoDiv2CodeId);
		return wb24Mapper.selectWbsIssueApprovalLines(query);
	}

	private static int toInt(String value) {
		try {
			return Integer.parseInt(value.trim());
		} catch (Exception e) {
			return 0;
		}
	}
}
