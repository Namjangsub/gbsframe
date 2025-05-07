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
        paramMap.put("salesCd", fileTrgtKey);

        int result = sm30Mapper.insert_sm30(paramMap);

		List<Map<String, String>> dtlList = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
		for (Map<String, String> dtl : dtlList) {
			dtl.put("fileTrgtKey", fileTrgtKey);
			dtl.put("coCd", paramMap.get("coCd"));
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			sm30Mapper.insert_sm30_list(dtl);

			// 결재진행 요청시 TB_SM20M01 테이블 APR_REQ_NO 컬럼 업데이트
			sm30Mapper.update_sm20_aprReqNo(dtl);
		}

		
		// result = sm30Mapper.update_sm20_aprReqNo(paramMap);

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
	public int delete_all_sm30_info(Map<String, String> paramMap) throws Exception {

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


        // 1. 매입대금 지급결재 기본 정보 자료 삭제
	  	int result = sm30Mapper.delete_sm30_info(paramMap);

        // 2. 매입대금 지급결재 결재자 정보 삭제 처리
		List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
		if (sharngChk.size() > 0) {
			QM01Mapper.deleteWbsSharngList(paramMap); 
		}

		return result;
	}

	@Override
	public List<Map<String, String>> delete_sm30_List(Map<String, Object> paramMap) throws Exception {
		// 1) 클라이언트에서 보낸 rows 배열을 꺼내기
		// @SuppressWarnings("unchecked")
		List<Map<String, Object>> rows = (List<Map<String, Object>>) paramMap.get("rows");
	
		String fileTrgtKey = (String) paramMap.get("fileTrgtKey");
		List<Map<String, String>> resultList = new ArrayList<>();
	
		// 2) 한 건씩 delete 호출
		for (Map<String, Object> row : rows) {
			String seq = String.valueOf(row.get("seq"));
			String ctrtNo = (String) row.get("ctrtNo");
			String clntCd = (String) row.get("clntCd");
	
			Map<String, String> deleteParam = new HashMap<>();
			deleteParam.put("fileTrgtKey", fileTrgtKey);
			deleteParam.put("seq", seq);
			deleteParam.put("clntCd", clntCd);
			deleteParam.put("ctrtNo", ctrtNo);
	
			sm30Mapper.delete_sm30_List(deleteParam);

			// TB_SM20M01 APR_REQ_NO 초기화
			sm30Mapper.update_sm20_aprReqNo_is_null(deleteParam);
	
			// 리턴용 맵에 seq만 담아서 반환
			Map<String, String> res = new HashMap<>();
			res.put("seq", seq);
			resultList.add(res);
		}
	
		// 3) 컨트롤러에서 isEmpty 체크용으로 사용
		return resultList;
	}

	@Override
	public List<Map<String, String>> selectApprovalUserChk(Map<String, String> paramMap) {
		return sm30Mapper.selectApprovalUserChk(paramMap);
	}


}
