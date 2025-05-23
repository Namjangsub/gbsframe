package com.dksys.biz.user.sm.sm30.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.dksys.biz.user.sm.sm20.mapper.SM20Mapper;
import com.dksys.biz.user.sm.sm21.mapper.SM21Mapper;
import com.dksys.biz.user.sm.sm30.mapper.SM30Mapper;
import com.dksys.biz.user.sm.sm30.service.SM30Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM30Svclmpl implements SM30Svc {

	@Autowired
    QM01Mapper QM01Mapper;

	@Autowired
	SM20Mapper sm20Mapper;

	@Autowired
    SM21Mapper sm21Mapper;

	@Autowired
    SM30Mapper sm30Mapper;

	@Autowired
    CM15Svc cm15Svc;

	@Autowired
    CM08Svc cm08Svc;


	@Override
	public Map<String, String> selectPjtInfo(Map<String, String> paramMap) {
		return sm30Mapper.selectPjtInfo(paramMap);
	}

	@Override
	public List<Map<String, String>> selectPjtInfoDetail(Map<String, String> paramMap) {
		return sm30Mapper.selectPjtInfoDetail(paramMap);
	}

	@Override
	public List<Map<String, String>> select_sm21_chk(Map<String, Object> paramMap) {
		
		List<Map<String, String>> excelRows = (List<Map<String, String>>) paramMap.get("uploadGridView");

		List<Map<String, String>> result = new ArrayList<>(excelRows.size());
		for (Map<String, String> row : excelRows) {
			List<Map<String, String>> list = sm30Mapper.select_sm21_chk(row);
			if (list != null && !list.isEmpty()) {
				result.add(list.get(0));
			}
		}

		return result;
	}

	@Override
	public int insert_sm21_excelUpload(Map<String, String> paramMap) {
		int result = 0;

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

		List<Map<String, String>> excelList = gsonDtl.fromJson(paramMap.get("uploadGridView"), dtlMap);
		for (Map<String, String> dtl : excelList) {
			String fileTrgtKey = sm21Mapper.select_sm21_SeqNext(paramMap);

			dtl.put("fileTrgtKey", fileTrgtKey);
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("closeYm", paramMap.get("closeYm"));
			dtl.put("payDt", paramMap.get("payDt"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("payAmt", dtl.get("payTot"));
			dtl.put("uploadId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			// 대금지급등록연결
			sm21Mapper.insert_sm21_payChk(dtl);

			// 대금지급연결
			sm21Mapper.update_sm21_payChk_billNo(dtl);
			// 대금 지불 완료/미결 처리
			sm20Mapper.update_sm20_payCompleteChke(dtl);

			result++;
		}
		return result;
	}

	@Override
	public int insert_sm30(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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

		// 공급업체 대금 지불 결제요청
        String fileTrgtKey = sm30Mapper.select_sm30_SeqNext(paramMap);
        paramMap.put("fileTrgtKey", fileTrgtKey);
		paramMap.put("todoNo", fileTrgtKey);
        paramMap.put("todoFileTrgtKey", fileTrgtKey);
        paramMap.put("reqNo", fileTrgtKey);
        paramMap.put("issNo", fileTrgtKey);
		paramMap.put("salesCd", "");

        int result = sm30Mapper.insert_sm30(paramMap);

		List<Map<String, String>> dtlList = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : dtlList) {
			dtl.put("fileTrgtKey", fileTrgtKey);
			dtl.put("aprReqNo", fileTrgtKey);
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			sm30Mapper.insert_sm30_list(dtl);

			// 매입계산서 발행내역에 결제요청번호 등록하기
			sm20Mapper.update_sm20_Approval(dtl);
		}

		

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

        // Gson gson = new Gson();
		
		List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
		if (sharngChk.size() > 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
		}
		
		String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
		pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\",";
		pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\"}";
		//공유
		Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gsonDtl.fromJson(paramMap.get("rowSharngListArr"), stringList2);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				try {
						sharngMap.put("reqNo", fileTrgtKey);
						sharngMap.put("salesCd", fileTrgtKey);
						sharngMap.put("fileTrgtKey", fileTrgtKey);
						sharngMap.put("pgmId", paramMap.get("pgmId"));
						sharngMap.put("userId", paramMap.get("userId"));
						sharngMap.put("sanCtnSn",Integer.toString(i+1));
						sharngMap.put("pgParam", pgParam1);
						// sharngMap.put("todoTitle", paramMap.get("todoTitle"));
						QM01Mapper.insertWbsSharngList(sharngMap);
					i++;
				} catch (Exception e) {
					System.out.println("error2"+e.getMessage());
				}
			}
		}

		String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
		pgParam2 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\",";
		pgParam2 += "\"coCd\":\""+ paramMap.get("coCd") +"\"}";
		//결재
		Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> approvalArr = gsonDtl.fromJson(paramMap.get("rowApprovalListArr"), stringList3);
		if (approvalArr != null && approvalArr.size() > 0 ) {
			int i = 0;
			for (Map<String, String> approvalMap : approvalArr) {
				try {
						approvalMap.put("reqNo", fileTrgtKey);
						approvalMap.put("salesCd", fileTrgtKey);
						approvalMap.put("fileTrgtKey", fileTrgtKey);
						approvalMap.put("pgmId", paramMap.get("pgmId"));
						approvalMap.put("userId", paramMap.get("userId"));
						approvalMap.put("sanCtnSn",Integer.toString(i+1));
						approvalMap.put("pgParam", pgParam2);
						// approvalMap.put("todoTitle", paramMap.get("todoTitle"));
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
	public Map<String, String> select_sm30_info(Map<String, String> paramMap) {
		return sm30Mapper.select_sm30_info(paramMap);
	}

	@Override
	public List<Map<String, String>> sm30_pop_grid1_selectList(Map<String, String> paramMap) {
		return sm30Mapper.sm30_pop_grid1_selectList(paramMap);
	}

	@Override
	public int delete_all_sm30(Map<String, String> paramMap) throws Exception {

        // 1. 매입대금 지급결재 기본 정보 자료 삭제
		int result = sm30Mapper.delete_sm30_master(paramMap);
		sm30Mapper.delete_sm30_all(paramMap);

        // 2. 매입대금 지급결재 결재자 정보 삭제 처리
		// fileTrgtKey
		QM01Mapper.deleteApprovalList(paramMap);

		// 매입계산서 발행내역에 결재요청 번호 제거하기
		sm20Mapper.update_sm20_Approval_All_clear(paramMap);

		return result;
	}

	@Override
	public int delete_sm30_detail(Map<String, String> paramMap) throws Exception {

		int result = sm30Mapper.delete_sm30_detail(paramMap);

		// 매입계산서 발행내역에 결제요청번호 지우기
		paramMap.put("aprReqNo", "");
		sm20Mapper.update_sm20_Approval(paramMap);

		// 마지막 상세내역이면 마스터 삭제 처리
		if (paramMap.get("totCount").equals("1")) {
			// 마지막 상세건이면 마스터 삭제
			sm30Mapper.delete_sm30_master(paramMap);
			// 매입대금 지급결재 결재자 정보 삭제 처리
			QM01Mapper.deleteApprovalList(paramMap);
			// 매입계산서 발행내역에 결재요청 번호 제거하기
			sm20Mapper.update_sm20_Approval_All_clear(paramMap);
		}

		return result;
	}

	@Override
	public Map<String, String> selectApprovalUserChk(Map<String, String> paramMap) {
		return sm30Mapper.selectApprovalUserChk(paramMap);
	}

	@Override
	public int updateApprovalHold(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int cnt = 0;

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type listType = new TypeToken<ArrayList<Map<String,String>>>(){}.getType();

		List<Map<String,String>> dtlList = gson.fromJson(paramMap.get("detailArr"), listType);		
		for (Map<String,String> dtl : dtlList) {
			dtl.put("userId",   paramMap.get("userId"));
			dtl.put("holdId",   paramMap.get("userId"));
			dtl.put("pgmId",   paramMap.get("pgmId"));
			// 보류 업데이트진행
			cnt += sm30Mapper.updateApprovalHold(dtl);
		}

		// 결재자 이력업데이트
		List<Map<String,String>> todoList = gson.fromJson(paramMap.get("todoArr"), listType);
		if (!todoList.isEmpty()) {
			Map<String,String> todo = todoList.get(0);
			todo.put("userId", paramMap.get("userId"));
			todo.put("pgmId",  paramMap.get("pgmId"));
			cnt += sm30Mapper.updateApprovalSm30(todo);
		}

		return cnt;
	}

	@Override
	public int updateShareUser(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();

		int result = 0;
		String fileTrgtKey = paramMap.get("fileTrgtKey");

		paramMap.put("reqNo", fileTrgtKey);
		paramMap.put("salesCd", fileTrgtKey);
		// 기존 결재자 및 공유자 조회
		List<Map<String, String>> existingUserEntries = QM01Mapper.deleteWbsSharngListChk(paramMap);

		List<Map<String, String>> newSharedUsers = gson.fromJson(paramMap.get("rowSharngListArr"), listType);
		List<Map<String, String>> newApprovals = gson.fromJson(paramMap.get("rowApprovalListArr"), listType);
		
		// -----------------------------------START--------------------------------------------------------------
		// 1. 공유자 및 결재자 프론트에서 넘어온 리스트와 DB에서 조회된 리스트와 비교해서
		// 2. 각각 DELETE 작업 후 INSERT 작업
		// ------------------------------------------------------------------------------------------------------
		// 공유자 DELETE
		for (Map<String, String> existingEntry : existingUserEntries) {
			if (!"TODODIV10".equals(existingEntry.get("todoDiv1CodeId"))) {
				continue;
			}
			boolean isRetainedInRequest = newSharedUsers.stream().anyMatch(requestedUser ->
				requestedUser.get("todoId").equals(existingEntry.get("todoId")) &&
				requestedUser.get("sanctnSn").equals(existingEntry.get("sanctnSn"))
			);
			if (!isRetainedInRequest) {
				Map<String, String> deleteParams = new HashMap<>();
				deleteParams.put("reqNo",    fileTrgtKey);
				deleteParams.put("salesCd",  fileTrgtKey);
				deleteParams.put("todoKey",  existingEntry.get("todoKey"));
				deleteParams.put("sanctnSn", existingEntry.get("sanctnSn"));
				result += sm30Mapper.deleteShareUser(deleteParams);
			}
		}
		// 순번 재계산 후 신규 공유자 INSERT
		int nextShareIndex = (int) existingUserEntries.stream()
			.filter(e -> "TODODIV10".equals(e.get("todoDiv1CodeId")))
			.filter(e -> newSharedUsers.stream().anyMatch(requestedUser ->
				requestedUser.get("todoId").equals(e.get("todoId")) &&
				requestedUser.get("sanctnSn").equals(e.get("sanctnSn"))
			))
			.count();

		String sharePgParam = String.format(
			"{\"actionType\":\"T\",\"fileTrgtKey\":\"%s\",\"coCd\":\"%s\"}",
			fileTrgtKey, paramMap.get("coCd")
		);

		for (Map<String, String> requestedUser : newSharedUsers) {
			boolean alreadyExists = existingUserEntries.stream().anyMatch(existingEntry ->
				"TODODIV10".equals(existingEntry.get("todoDiv1CodeId")) &&
				requestedUser.get("todoId").equals(existingEntry.get("todoId")) &&
				requestedUser.get("sanctnSn").equals(existingEntry.get("sanctnSn"))
			);
			if (alreadyExists) {
				continue;
			}
			nextShareIndex++;
			requestedUser.put("reqNo",       fileTrgtKey);
			requestedUser.put("salesCd",     fileTrgtKey);
			requestedUser.put("fileTrgtKey", fileTrgtKey);
			requestedUser.put("pgmId",       paramMap.get("pgmId"));
			requestedUser.put("userId",      paramMap.get("userId"));
			requestedUser.put("sanCtnSn",    Integer.toString(nextShareIndex));
			requestedUser.put("pgParam",     sharePgParam);
			result += sm30Mapper.insertSm30ApprovalList(requestedUser);
		}

		// 결재자 DELETE
		for (Map<String, String> existingEntry : existingUserEntries) {
			if (!"TODODIV20".equals(existingEntry.get("todoDiv1CodeId"))) {
				continue;
			}
			boolean isRetainedInRequest = newApprovals.stream().anyMatch(requestedApproval ->
				requestedApproval.get("todoId").equals(existingEntry.get("todoId")) &&
				requestedApproval.get("sanctnSn").equals(existingEntry.get("sanctnSn"))
			);
			if (!isRetainedInRequest) {
				Map<String, String> deleteParams = new HashMap<>();
				deleteParams.put("reqNo",    fileTrgtKey);
				deleteParams.put("salesCd",  fileTrgtKey);
				deleteParams.put("todoKey",  existingEntry.get("todoKey"));
				deleteParams.put("sanctnSn", existingEntry.get("sanctnSn"));
				result += sm30Mapper.deleteShareUser(deleteParams);
			}
		}

		// 순번 재계산 후 신규 결재자 INSERT
		int nextApprovalIndex = (int) existingUserEntries.stream()
			.filter(e -> "TODODIV20".equals(e.get("todoDiv1CodeId")))
			.filter(e -> newApprovals.stream().anyMatch(requestedApproval ->
				requestedApproval.get("todoId").equals(e.get("todoId")) &&
				requestedApproval.get("sanctnSn").equals(e.get("sanctnSn"))
			))
			.count();

		String approvalPgParam = String.format(
			"{\"actionType\":\"S\",\"fileTrgtKey\":\"%s\",\"coCd\":\"%s\"}",
			fileTrgtKey, paramMap.get("coCd")
		);

		for (Map<String, String> requestedApproval : newApprovals) {
			boolean alreadyExists = existingUserEntries.stream().anyMatch(existingEntry ->
				"TODODIV20".equals(existingEntry.get("todoDiv1CodeId")) &&
				requestedApproval.get("todoId").equals(existingEntry.get("todoId")) &&
				requestedApproval.get("sanctnSn").equals(existingEntry.get("sanctnSn"))
			);
			if (alreadyExists) {
				continue;
			}
			nextApprovalIndex++;
			requestedApproval.put("reqNo",       fileTrgtKey);
			requestedApproval.put("salesCd",     fileTrgtKey);
			requestedApproval.put("fileTrgtKey", fileTrgtKey);
			requestedApproval.put("pgmId",       paramMap.get("pgmId"));
			requestedApproval.put("userId",      paramMap.get("userId"));
			requestedApproval.put("sanCtnSn",    Integer.toString(nextApprovalIndex));
			requestedApproval.put("pgParam",     approvalPgParam);
			result += sm30Mapper.insertSm30ApprovalList(requestedApproval);
		}

		return result;

		// -----------------------------------END--------------------------------------------------------------
	}

}
