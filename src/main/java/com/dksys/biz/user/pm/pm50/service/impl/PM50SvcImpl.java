package com.dksys.biz.user.pm.pm50.service.impl;

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
import com.dksys.biz.user.pm.pm50.mapper.PM50Mapper;
import com.dksys.biz.user.pm.pm50.service.PM50Svc;
import com.dksys.biz.user.wb.wb22.mapper.WB22Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class PM50SvcImpl implements PM50Svc{

	@Autowired
	PM50Mapper pm50Mapper;

	@Autowired
    CM08Svc cm08Svc;

	@Autowired
	WB22Mapper wb22Mapper;

	@Autowired
    CM15Svc cm15Svc;

	@Override
	public int select_pm50_ListCount(Map<String, String> paramMap) {
		return pm50Mapper.select_pm50_ListCount(paramMap);
	}

	@Override
	public List<Map<String, Object>> select_pm50_List(Map<String, String> paramMap) {
		return pm50Mapper.select_pm50_List(paramMap);
	}

	@Override
	public int selectSendFileCount(Map<String, String> paramMap) {
		return pm50Mapper.selectSendFileCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSendFileList(Map<String, String> paramMap) {
		return pm50Mapper.selectSendFileList(paramMap);
	}

	@Override
	public Map<String, String> select_pm50_Info(Map<String, String> paramMap) {
		return pm50Mapper.select_pm50_Info(paramMap); 
	}

	@Override
	public Map<String, String> select_salesCd_info(Map<String, String> paramMap) {
		return pm50Mapper.select_salesCd_info(paramMap); 
	}

	@Override
	public List<Map<String, String>> selectBfuFileRows(Map<String, String> paramMap) {
		return pm50Mapper.selectBfuFileRows(paramMap);
	}

	@Override
	public List<Map<String, String>> selectBfuAllFileRows(Map<String, String> paramMap) {
		return pm50Mapper.selectBfuAllFileRows(paramMap);
	}

	@Override
	public int insert_pm50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
	    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

		String bfuFileTrgtKey = pm50Mapper.fileTrgtKeySeqNext(paramMap);
		paramMap.put("fileTrgtKey", bfuFileTrgtKey);	// BFU250001

		int result = pm50Mapper.insert_pm50_main(paramMap);

		//사진 Insert 처리
        List<Map<String, String>> fileArr = gsonDtl.fromJson(paramMap.get("bfuS"), dtlMap);
		if (fileArr != null && !fileArr.isEmpty()) {	//경비내역이 있으면 처리함.
			for (Map<String, String> fileMap : fileArr) {
				String updCheck = fileMap.get("updCheck"); //구분코드 C, D  : 처음 등록시에는 C, D만 있음. Delete 는 무시하면 됨
				if ("C".equals(updCheck)) { //처음 등록시에는 C인것만 처리하면 됨.
					// 상세 내역별 insert
					fileMap.put("fileTrgtKey", bfuFileTrgtKey);	//상세내역 fileTrgtKey 번호 생성 필요함.
					fileMap.put("fileSeq", fileMap.get("fileSeq"));					//백엔드에서 신규 등록시 생성된 번호로 대체함
					fileMap.put("bfuCnts", fileMap.get("bfuCnts"));
					fileMap.put("pgmId", paramMap.get("pgmId"));
					pm50Mapper.insert_pm50_d01(fileMap);
					
					// 첨부파일처리
					String fileTrgtTyp = "PM5001M01";
					String fileTrgtKey = bfuFileTrgtKey +  "-" + fileMap.get("fileSeq");
					cm08Svc.uploadFile(fileTrgtTyp, fileTrgtKey, mRequest) ;
				}
			}
        }
		return result;
	}

	@Override
	public int update_pm50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

		int result = pm50Mapper.update_pm50_main(paramMap);

		//1. 사진 업로드 중 삭제된 내역  처리 (작업상세 + 첨부파일 삭제처리)
		List<Map<String, String>> bfuFileRowDeleteArr = gsonDtl.fromJson(paramMap.get("bfuFileRowDeleteArr"), dtlMap);
		if (bfuFileRowDeleteArr != null && !bfuFileRowDeleteArr.isEmpty()) {	// 첨부파일이 있으면 처리함.
			for (Map<String, String> fileMap : bfuFileRowDeleteArr) {
				// fileTrgtKey : PM5001M01 으로 저장함.
				String fileTrgtTyp = "PM5001M01";
				String fileTrgtKey = fileMap.get("fileTrgtKey") +  "-" + fileMap.get("fileSeq");
				String updCheck = fileMap.get("updCheck"); //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
				if ("D".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
					//출장자 사진 업로드 상세 내역 삭제 처리
					result += pm50Mapper.delete_pm50_d01(fileMap);
					//첨부파일 상세내역 연계자료 삭제 처리 필요함
					String fileKey = fileMap.get("fileKey");
					if (fileKey != null && !fileKey.isEmpty()) {	
						cm08Svc.deleteFile( fileKey );
					}
				}
			}
		}  

		//2. 사진 중 첨부파일만 삭제된 내역  처리 (첨부파일만 변경시 기존 첨부 파일 삭제처리임)
		List<Map<String, String>> bfuRowFileDeleteArr = gsonDtl.fromJson(paramMap.get("bfuRowFileDeleteArr"), dtlMap);
		if (bfuRowFileDeleteArr != null && !bfuRowFileDeleteArr.isEmpty()) {	// 첨부파일이 있으면 처리함.
			for (Map<String, String> fileMap : bfuRowFileDeleteArr) { //fileMap={fileKey, fileName} 만 있음.
				//첨부파일만 삭제됨
				String fileKey = fileMap.get("fileKey");
				if (fileKey != null && !fileKey.isEmpty()) {	// 첨부파일이 있으면 처리함.
					cm08Svc.deleteFile( fileKey );
				}
			}
		}  

		
		//3. 사진 Update 처리
		List<Map<String, String>> bfuArr = gsonDtl.fromJson(paramMap.get("bfuS"), dtlMap);
		if (bfuArr != null && !bfuArr.isEmpty()) {	// 첨부파일이 있으면 처리함.
			for (Map<String, String> fileMap : bfuArr) {
				//상세관련 첨부파일은 아래 함수를 활용함
				// fileTrgtKey : PM5001M01 으로 저장함.
				String fileTrgtTyp = "PM5001M01";
				//세부내역별 키값에 대한부분은 등록번호 + 사진인련번호로 구성함
				String fileTrgtKey = paramMap.get("fileTrgtKey") +  "-" + fileMap.get("fileSeq");

				String updCheck = fileMap.get("updCheck"); //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
				if ("C".equals(updCheck)) { //처음 등록시에는 C인 것만 처리하면 됨.
					pm50Mapper.insert_pm50_d01(fileMap);
					
					// 첨부파일 처리
					cm08Svc.uploadFile(fileTrgtTyp, fileTrgtKey, mRequest) ;
				} else if ("U".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D.
					pm50Mapper.update_pm50_d01(fileMap);
					//파일만 Update 처리는?
					// 첨부파일 처리
					cm08Svc.uploadFile(fileTrgtTyp, fileTrgtKey, mRequest) ;
					
				} else if ("D".equals(updCheck)) { //구분코드 C, U, D  : 처음 등록시에는 C, 수정은 U, 삭제는 D. //최종버젼에서는 update에는 삭제데이터 처리내역 없음. 위에 1., 2.에서 처리됨

					//상세 내역 삭제 처리
					result += pm50Mapper.delete_pm50_d01(fileMap);
					//첨부파일 상세내역 연계자료 삭제 처리 필요함
					String fileKey = fileMap.get("fileKey");
					if (fileKey != null && !fileKey.isEmpty()) {	//경비내역이 있으면 처리함.
						cm08Svc.deleteFile( fileKey );
					}
				}
			}
		}

		return result;
	}

	@Override
	public int update_issue_pm50_d01(Map<String, Object> paramMap) throws Exception {
		// issuNo, userId, pgmId 는 paramMap 에 담겨 있고
		List<Map<String, Object>> pm50Info = (List<Map<String,Object>>) paramMap.get("pm50Info");
		int result = 0;
		for (Map<String,Object> fileMeta : pm50Info) {
			Map<String,Object> updateMap = new HashMap<>();
			updateMap.put("issNo",       paramMap.get("issuNo"));
			updateMap.put("userId",      paramMap.get("userId"));
			updateMap.put("pgmId",       paramMap.get("pgmId"));
			updateMap.put("fileTrgtKey", fileMeta.get("fileTrgtKey"));
			updateMap.put("fileSeq",     fileMeta.get("fileSeq"));

			result += pm50Mapper.update_issue_pm50_d01(updateMap);
		}
		return result;
	}
	@Override
	public int update_issue_reset_pm50_d01(Map<String, String> paramMap) throws Exception {
		int result = pm50Mapper.update_issue_reset_pm50_d01(paramMap);
		return result;
	}

	@Override
	public int delete_pm50(Map<String, String> paramMap) throws Exception {
		int result = 0;
		//출장경비용 삭제
		List<Map<String, String>> deleteBfuFileList = pm50Mapper.selectBfuFileRows(paramMap); 
		if (deleteBfuFileList.size() > 0) {		  
		    for (Map<String, String> deleteDtl : deleteBfuFileList) {
		    	String fileKey = deleteDtl.get("fileKey");
		        if (fileKey != null && !fileKey.isEmpty()) {	// 첨부파일이 있으면 처리함.
					//첨부파일 상세내역 연계자료 삭제 처리
				    cm08Svc.deleteFile( fileKey );
		        }
				// 사진등록 상세 내역 삭제 처리
				result += pm50Mapper.delete_pm50_d01(paramMap);
		    }
			
			List<Map<String, String>> selectBfuAllFileRows = pm50Mapper.selectBfuAllFileRows(paramMap);
			if (selectBfuAllFileRows.size() == 0) {
				result += pm50Mapper.delete_pm50_main(paramMap);
			}
		} else {
			return 0;
		}
		return result;
	}

	

}