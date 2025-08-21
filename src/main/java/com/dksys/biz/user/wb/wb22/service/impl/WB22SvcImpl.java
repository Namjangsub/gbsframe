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
import com.dksys.biz.rest.url.Base62Util;
import com.dksys.biz.rest.url.mapper.UrlMapper;
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
    private UrlMapper urlMapper;

	@Autowired
    private Base62Util base62Util;

	@Autowired
	ExceptionThrower thrower;

	@Override
	public int selectWbsSjListCount(Map<String, String> paramMap) {
		;
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
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("coCd", paramMap.get("coCd"));

				if (!sharngMap.containsKey("fileTrgtKey")) {
					String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
					sharngMap.put("wbsPlanNo", wbsPlanNo);

					String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", fileTrgtKey);

					sharngMap.put("seq", String.valueOf(i + 1));

					result += wb22Mapper.wbsLevel1Insert(sharngMap);
				} else {
					result += wb22Mapper.wbsLevel1Update(sharngMap);

					// level2 Task정보 변경 처리
					sharngMap.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
					String chkValue = sharngMap.get("wbsPlanMngId");
					if (chkValue == null || chkValue.equals("")) {
						// level1 담당자와 일정이 없으면 TASK등록된 일정 일괄 삭제처리
						result += wb22Mapper.deleteWbsPlanlist(sharngMap);
					} else {
						// level1 담당자로 TASK 담당자 일괄 수정
						result += wb22Mapper.wbsLevel2MngIdUpdate(sharngMap);
					}
				}

				i++;
			}
		}
		return result;
	}

	@Override
	public int wbsLevel1Update(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				result = wb22Mapper.wbsLevel1Update(sharngMap);
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

		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				// System.out.println(sharngMap.get("fileTrgtKey"));
				if (sharngMap.get("fileTrgtKey").length() > 0) {
					sharngMap.put("seq", String.valueOf(i + 1));
					result = wb22Mapper.wbsLevel2Update(sharngMap);
				} else {
					sharngMap.put("coCd", paramMap.get("coCd"));
					String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
					sharngMap.put("wbsPlanNo", wbsPlanNo);
					sharngMap.put("wbsPlanCodeKind", paramMap.get("wbsPlanCodeKind"));
					int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);

					String codeId = "";
					if (wbsPlanCodeId < 10) {
						codeId = "0" + String.valueOf(wbsPlanCodeId);
					} else {
						codeId = String.valueOf(wbsPlanCodeId);
					}
					sharngMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);

					sharngMap.put("seq", String.valueOf(i + 1));
					String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
					sharngMap.put("fileTrgtKey", fileTrgtKey);
					result = wb22Mapper.wbsLevel2Insert(sharngMap);
				}
				i++;
			}
		}

		Type stringList1 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> deleteRowArr = gson.fromJson(paramMap.get("deleteRowArr"), stringList1);
		if (deleteRowArr != null && deleteRowArr.size() > 0) {
			for (Map<String, String> sharngMap : deleteRowArr) {
				// 실적먼저 지워져야함
				wb22Mapper.wbsRsltsDelete(sharngMap);
				result = wb22Mapper.wbsLevel2Delete(sharngMap);
			}
		}

		return result;
	}

	@Override
	public int wbsVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		// Ver.up Check : 해당 버전에 ClOSE_YN이 Y인지 체크
		List<Map<String, String>> chkList = wb22Mapper.wbsVerUpInsertChk(paramMap);

		// 조회 결과가 없으면 이미 CLOSE_YN이 'N'인 상태이므로 실행 중지
		if (chkList == null || chkList.isEmpty()) {
			return 0;
		}

		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("seq", String.valueOf(i + 1));
				wb22Mapper.wbsVerUpInsert(sharngMap);
				result++;
				i++;
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
				result = wb22Mapper.wbsLevel1confirm(sharngMap);
				result += wb22Mapper.wbsLevel1confirmAll(sharngMap); // 전체 Y 설정
				sharngMap.put("wbsPlanCodeKind", sharngMap.get("wbsPlanCodeId"));
				String chkValue = sharngMap.get("wbsPlanMngId");
				if (chkValue == null || chkValue.equals("")) {
					// level1 담당자와 일정이 없으면 TASK등록된 일정 일괄 삭제처리
					result += wb22Mapper.deleteWbsPlanlist(sharngMap);
				} else {
					// level1 담당자로 TASK 담당자 일괄 수정
					result += wb22Mapper.wbsLevel2MngIdUpdate(sharngMap);
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
				result += wb22Mapper.wbsLevel2confirm(sharngMap);
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
	public int wbsRsltsInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int fileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("rsltsFileTrgtKey", Integer.toString(fileTrgtKey));

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		int result = wb22Mapper.wbsRsltsInsert(paramMap);
		Gson gson = new Gson();

		// String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		// pgParam1 += "\"fileTrgtKey\":\""+ fileTrgtKey +"\",";
		// pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\",";
		// pgParam1 += "\"lvl\":\""+ paramMap.get("lvl") +"\",";
		// pgParam1 += "\"idx\":\""+ paramMap.get("idx") +"\",";
		// pgParam1 += "\"salesCd\":\""+ paramMap.get("salesCd") +"\",";
		// pgParam1 += "\"ordrsNo\":\""+ paramMap.get("ordrsNo") +"\",";
		// pgParam1 += "\"codeKind\":\""+ paramMap.get("codeKind") +"\",";
		// pgParam1 += "\"codeId\":\""+ paramMap.get("codeId") +"\"}";

		String pgParam = "{\"fileTrgtKey\":\"" + fileTrgtKey + "\"}";

//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_등록]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");

		// String todoTitle = "TASK명 : " + paramMap.get("wbsPlanCodeNm2_P") + ", SALES CODE : " + paramMap.get("salesCd2_P") + " 실적일자 : " +
		// paramMap.get("wbsRsltssDt") + " ~ " + paramMap.get("wbsRsltseDt");

		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
				sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
				sharngMap.put("pgmId", paramMap.get("pgmId"));
				sharngMap.put("userId", paramMap.get("userId"));
				sharngMap.put("sanCtnSn", Integer.toString(i + 1));
				sharngMap.put("pgParam", pgParam);
				sharngMap.put("todoTitle", todoTitle1);
				QM01Mapper.insertWbsSharngList(sharngMap);
				i++;
			}
		}

//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
//		String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 결재";
		String todoTitle2 = "[WBS실적_등록]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");

		// 결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0) {
			int i = 0;
			for (Map<String, String> approvalMap : approvalArr) {
//	            try {
				approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
				approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
				approvalMap.put("pgmId", paramMap.get("pgmId"));
				approvalMap.put("userId", paramMap.get("userId"));
				approvalMap.put("sanCtnSn", Integer.toString(i + 1));
				approvalMap.put("pgParam", pgParam);
				approvalMap.put("todoTitle", todoTitle2);
				QM01Mapper.insertWbsApprovalList(approvalMap);
				i++;
//	            } catch (Exception e) {
//	                System.out.println("error2"+e.getMessage());
//	            }
			}
		}

		// ---------------------------------------------------------------
		// 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
		// 필수값 : jobType, userId, comonCd
		// ---------------------------------------------------------------
		List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
		if (uploadFileList.size() > 0) {
			// 접근 권한 없으면 Exception 발생
			paramMap.put("jobType", "fileUp");
			cm15Svc.selectFileAuthCheck(paramMap);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 권한체크 끝
		// ---------------------------------------------------------------
		// ---------------------------------------------------------------
		// 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
		// ---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 처리 끝
		// ---------------------------------------------------------------

		return result;
	}

	@Override
	public int wbsRsltsUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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

		int result = wb22Mapper.wbsRsltsUpdate(paramMap);
		Gson gson = new Gson();

		String pgParam = "{\"fileTrgtKey\":\"" + paramMap.get("rsltsFileTrgtKey") + "\"}";

//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 공유";
//		String todoTitle1 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P") + " 실적 공유";
		String todoTitle1 = "[WBS실적_변경]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");

		paramMap.put("reqNo", paramMap.get("wbsRsltsNo"));
		paramMap.put("salesCd", paramMap.get("salesCd2_P"));

		if (Integer.parseInt(paramMap.get("approvalYnCnt")) == 0) {
			QM01Mapper.deleteWbsSharngList(paramMap);

			Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {
			}.getType();
			List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList2);
			if (sharngArr != null && sharngArr.size() > 0) {
				int i = 0;
				for (Map<String, String> sharngMap : sharngArr) {
					try {
						sharngMap.put("reqNo", paramMap.get("wbsRsltsNo"));
						sharngMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
						sharngMap.put("pgmId", paramMap.get("pgmId"));
						sharngMap.put("userId", paramMap.get("userId"));
						sharngMap.put("sanCtnSn", Integer.toString(i + 1));
						sharngMap.put("pgParam", pgParam);
						sharngMap.put("todoTitle", todoTitle1);
						QM01Mapper.insertWbsSharngList(sharngMap);
						i++;
					} catch (Exception e) {
						System.out.println("error2" + e.getMessage());
					}
				}
			}

//			String todoTitle2 = paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + "(" + paramMap.get("salesCd2_P") + ") " + paramMap.get("wbsPlanCodeNm2_P") + " 실적 결재";
			String todoTitle2 = "[WBS실적_변경]" + paramMap.get("clntNm_P") + "-" + paramMap.get("clntPjtNm_P") + paramMap.get("salesCd2_P");

			// 결재
			Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {
			}.getType();
			List<Map<String, String>> approvalArr = gson.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
			if (approvalArr != null && approvalArr.size() > 0) {
				int i = 0;
				for (Map<String, String> approvalMap : approvalArr) {
					try {
						approvalMap.put("reqNo", paramMap.get("wbsRsltsNo"));
						approvalMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
						approvalMap.put("pgmId", paramMap.get("pgmId"));
						approvalMap.put("userId", paramMap.get("userId"));
						approvalMap.put("sanCtnSn", Integer.toString(i + 1));
						approvalMap.put("pgParam", pgParam);
						approvalMap.put("todoTitle", todoTitle2);
						QM01Mapper.insertWbsApprovalList(approvalMap);
						i++;
					} catch (Exception e) {
						System.out.println("error2" + e.getMessage());
					}
				}
			}

		}

		// List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap);
		// if (sharngChk.size() == 0) {
		// QM01Mapper.deleteWbsSharngList(paramMap);
		// }

		// List<Map<String, String>> approvalgChk = QM01Mapper.deleteWbsApprovalListChk(paramMap);
		// if (approvalgChk.size() > 0) {
		// QM01Mapper.deleteWbsApprovalList(paramMap);
		// }

		// ---------------------------------------------------------------
		// 첨부 화일 처리 시작
		// ---------------------------------------------------------------
		if (uploadFileList.size() > 0) {
			paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
			paramMap.put("fileTrgtKey", paramMap.get("rsltsFileTrgtKey"));
			cm08Svc.uploadFile(paramMap, mRequest);
		}

		for (String fileKey : deleteFileList) {
			cm08Svc.deleteFile(fileKey);
		}
		// ---------------------------------------------------------------
		// 첨부 화일 처리 끝
		// ---------------------------------------------------------------

		return result;
	}

	@Override
	public int wbsRsltsconfirm(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
//		try {
		result = wb22Mapper.wbsRsltsconfirm(paramMap);
//		} catch (Exception e) {
//			System.out.println("error2" + e.getMessage());
//		}
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
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		List<Map<String, String>> delArr = gson.fromJson(paramMap.get("taskDeleteRowArr"), stringList);
		if (delArr != null && delArr.size() > 0) {
			for (Map<String, String> sharngMap : delArr) {
				result = wb22Mapper.wbsTaskTempletDelete(sharngMap);
			}
		}

		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				// 상위 코드
				sharngMap.put("codeKind", paramMap.get("wbsPlanCodeId"));
				// codeId 값이 없으면 insert
				if (sharngMap.get("codeId").isEmpty()) {
					String wbsPlanNo = wb22Mapper.selectNewWbsTaskTempletCd(sharngMap);
					sharngMap.put("codeId", wbsPlanNo);

					result = wb22Mapper.wbsTaskTempletInsert(sharngMap);
				} else {
					result = wb22Mapper.wbsTaskTempletUpdate(sharngMap);
				}

			}
		}

		return result;
	}

	// 유저별 템플릿 serviceImplement
	// ------------------------------------------------------------------------------------------------------
	@Override
	public int selectWbsUserTaskTempletCount(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsUserTaskTempletCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectWbsUserTaskTempletList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbsUserTaskTempletList(paramMap);
	}

	@Override
	public int saveWbsUserTaskTempletList(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = 0;
		Gson gson = new Gson();
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		result = wb22Mapper.wbsUserTaskTempletDelete(paramMap);

		result = 0;
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			for (Map<String, String> sharngMap : sharngArr) {
				// 상위 코드
				sharngMap.put("codeKind", paramMap.get("codeKind"));
				sharngMap.put("codeDesc", paramMap.get("codeDesc"));
				sharngMap.put("userId", paramMap.get("userId"));
				// codeId 값이 없으면 insert
				if (sharngMap.get("codeId").isEmpty()) {
					result = wb22Mapper.wbsUserTaskTempletInsert(sharngMap);
				} else {
					result = wb22Mapper.wbsUserTaskTempletUpdate(sharngMap);
				}

			}
		}

		return result;
	}

	// ------------------------------------------------------------------------------------------------------

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
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));

		// 데이터처리 시작
		int result = 0;

		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("rowList"), dtlMap);

		String userId = "";
		String sYear = "";
		userId = paramMap.get("userId");
		sYear = paramMap.get("year");

		if (dtlParam.size() > 0) {

			for (Map<String, String> dtl : dtlParam) {

				String dtaChk = dtl.get("dtaChk").toString();
				dtl.put("creatId", userId);
				dtl.put("year", sYear);

				if ("U".equals(dtaChk)) {

					List<Map<String, String>> chkCount = wb22Mapper.selectWbcPlan(dtl);

					if (chkCount.size() == 0) {
						// insert
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

							String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(dtl);
							dtl.put("wbsPlanNo", wbsPlanNo);

							String fileTrgtKey = wb22Mapper.selectWbsSeqNext(dtl);
							dtl.put("fileTrgtKey", fileTrgtKey);

							result = wb22Mapper.wbsLevel1Insert(dtl);
						}
					} else {
						// update
						List<Map<String, String>> chkList = wb22Mapper.selectWbcPlan(paramMap);

						if (chkList.size() == 0) { // 데이터가 없을시 대상데이터를 null 셋팅한다.
							dtl.put("wbsPlanCodeId", "");
							dtl.put("wbsPlanMngId", "");
							dtl.put("wbsPlansDt", "");
							dtl.put("wbsPlaneDt", "");
							dtl.put("daycnt", "");
							dtl.put("wbsPlanStsCodeId", "");
							dtl.put("creatPgm", "WB2201M01");

							result = wb22Mapper.updateWbcPlan(dtl);
						} else {
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
		// 데이터 처리 끝
		return result;
	}

	@Override
	public List<Map<String, String>> selectWbcPlanTodoList(Map<String, String> paramMap) {
		return wb22Mapper.selectWbcPlanTodoList(paramMap);
	}

	@Override
	public int wbcPlanTodoInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();

		int result = 0;

		Gson gson = new Gson();

		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				String pgParam = "{\"coCd\":\"" + sharngMap.get("coCd") + "\",";
				pgParam += "\"salesCd\":\"" + sharngMap.get("salesCd") + "\",";
				pgParam += "\"planVerNo\":\"" + sharngMap.get("verNo") + "\",";
				pgParam += "\"histYn\":\"" + sharngMap.get("histYn") + "\"}";
				sharngMap.put("sanCtnSn", Integer.toString(i + 1));
				sharngMap.put("pgParam", pgParam);
				result = QM01Mapper.insertWbsSharngList(sharngMap);
				i++;
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
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
		}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

		// 데이터처리 시작
		int result = 0;

		// 상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : dtlParam) {
			// 반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			// dtl.put("userId", paramMap.get("userId"));
			// dtl.put("pgmId", paramMap.get("pgmId"));

			String dataChk = dtl.get("dataChk").toString();
			// "dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
			if ("U".equals(dataChk)) {
				// 데이터 처리
				result = wb22Mapper.confirm_wb22(dtl);
			}
		}
		// 데이터 처리 끝
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

	// wbs계획 관리 변경사항 저장
	@Override
	public int updateWbsChanges(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
		
		// Gson을 사용하여 JSON 데이터를 파싱
		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		// JSON 형식의 "changedData" 추출
		List<Map<String, String>> changedDataList = gson.fromJson(paramMap.get("changedData"), listType);

		// 데이터가 존재할 경우 처리
		if (changedDataList != null && !changedDataList.isEmpty()) {
			for (Map<String, String> changedData : changedDataList) {
				// 필요시 추가 처리 (예: coCd, salesCd와 같은 추가 필드 처리)
				changedData.put("coCd", paramMap.get("coCd"));
				changedData.put("salesCd", paramMap.get("salesCd"));

				result += wb22Mapper.updateWbsChanges(changedData);
			}
		}
		
		return result;
	}

	@Override
	public Map<String, String> generateShortUrl(Map<String, String> paramMap) {

		String longUrl = paramMap.get("longUrl");
        String strId = urlMapper.getUrlIdByLongUrl(longUrl);
        int id = 0;
        if (strId == null || strId.equals("")) {
        	String chkCode = "QRGeration"; // 비밀코드는 고정 문자열 -> DB에 저장용도임
//        	Map<String, String> paramMap = new HashMap<>();
        	paramMap.put("longUrl", longUrl);
            paramMap.put("chkCode", chkCode);
            id = urlMapper.insertLongUrl(paramMap);

        	Object paramValue = paramMap.get("id");
        	if (paramValue != null && paramValue instanceof Integer) {
        		id = ((Integer) paramValue).intValue();
        	}

        	if (id > 0) {
//        		id = Integer.parseInt(paramMap.get("id"));
        	} else {
        		throw new IllegalArgumentException("등록중 오류가 발생 했습니다.");
        	}
        	
        } else {
        	id = Integer.parseInt(strId);
        }
        String shortUrl = base62Util.encoding(id);

    	Map<String, String> returnMap = new HashMap<>();
    	returnMap.put("shortUrl", paramMap.get("hostAddress") + "/s/" + shortUrl);
    	returnMap.put("chkCode", urlMapper.getChkCodeById(id));
    	
        return returnMap;
	}
	


	@Override
	public int wbsLevel1GantUpdate(Map<String, String> paramMap) {
		return wb22Mapper.wbsLevel1GantUpdate(paramMap);
	}
	
	@Override
	public int wbsLevel2GantInsert(Map<String, String> paramMap) {
		String fileTrgtKey = wb22Mapper.selectWbsSeqNext(paramMap);
		paramMap.put("fileTrgtKey", fileTrgtKey);

		String wbsPlanNo = wb22Mapper.selectMaxWbsPlanNo(paramMap);
		paramMap.put("wbsPlanNo", wbsPlanNo);
		
		int wbsPlanCodeId = wb22Mapper.selectMaxWbsCode(paramMap);

		String codeId = "";
		if (wbsPlanCodeId < 10) {
			codeId = "0" + String.valueOf(wbsPlanCodeId);
		} else {
			codeId = String.valueOf(wbsPlanCodeId);
		}
		paramMap.put("wbsPlanCodeId", paramMap.get("wbsPlanCodeKind") + codeId);
		
		return wb22Mapper.wbsLevel2Insert(paramMap);
	}
	
	@Override
	public int wbsRsltsGantInsert(Map<String, String> paramMap) {
		int fileTrgtKey = wb22Mapper.selectWbsRstlsSeqNext(paramMap);
		paramMap.put("rsltsFileTrgtKey", Integer.toString(fileTrgtKey));

		return wb22Mapper.wbsRsltsInsert(paramMap);
	}
	
	//간트에서 일정 삭제 요청 처리
	@Override
	public int wbsRsltsGantDelete(Map<String, String> paramMap) {
		String cat = paramMap.get("cat");
		if (cat == null || cat.equals("PLAN")) {
			return wb22Mapper.wbsLevel2Delete(paramMap);	// 담당자 계획 삭제
		} else if (cat == null || cat.equals("DO")) {
			return wb22Mapper.wbsRsltsGantDelete(paramMap);		// 담당자 실적 삭제
		} else {
			return 0;
		}
	}
	
}