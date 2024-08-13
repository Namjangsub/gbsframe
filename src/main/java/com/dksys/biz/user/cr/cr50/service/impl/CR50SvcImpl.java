package com.dksys.biz.user.cr.cr50.service.impl;

import java.util.function.Function;
import java.util.function.Consumer;
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
import com.dksys.biz.user.cr.cr50.mapper.CR50Mapper;
import com.dksys.biz.user.cr.cr50.service.CR50Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR50SvcImpl implements CR50Svc {

	@Autowired
	CR50Mapper cr50Mapper;

    @Autowired
    QM01Mapper QM01Mapper;
    
	@Autowired
	CM15Svc cm15Svc;
	  
	@Autowired
	CM08Svc cm08Svc;
	
	@Override
	public List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap) {
		return cr50Mapper.selectPFUAreaItemList(paramMap);
	}

	
	@Override
	public List<Map<String, String>> selectPFUAreaRetriveList(Map<String, String> paramMap) {
		return cr50Mapper.selectPFUAreaRetriveList(paramMap);
	}
	
	@Override
	public Map<String, String> selectPfuInfo(Map<String, String> paramMap) {
	    return cr50Mapper.selectPfuInfo(paramMap);
	}
	
	
//	@Override
//	public Map<String, String> selectPfuClobInfo(Map<String, String> paramMap) {
//	    return cr50Mapper.selectPfuClobInfo(paramMap);
//	}
	
	@Override
	public int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		int result = cr50Mapper.insertPfu(paramMap);
		String  newPfuSeq = paramMap.get("fileTrgtKey");

		
		String sysCreateDttm =  cr50Mapper.selectSystemCreateDttm(paramMap);
        int newSeq = 0;
		for (int i = 1; i <= 4; i++) {
		    // a01ListRow, a02ListRow, a03ListRow, a04ListRow를 동적으로 가져옵니다.
		    String key = "a0" + i + "ListRow"; // "a01ListRow", "a02ListRow", ...
		    List<Map<String, String>> aListArr = gsonDtl.fromJson(paramMap.get(key), dtlMap);

		    // 정보 저장
		    if (aListArr != null && !aListArr.isEmpty()) {
		        for (Map<String, String> detailMap : aListArr) {
		            detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));

		            newSeq += 1;
		            detailMap.put("pfuSeq", Integer.toString(newSeq));
		            detailMap.put("sortNo", Integer.toString(newSeq));

		            String area = (String) detailMap.get("partDiv");
		            String resultString = area + String.format("%02d", newSeq);
		            detailMap.put("partId", resultString);

		            detailMap.put("creatId", paramMap.get("userId"));
		            detailMap.put("creatDttm", sysCreateDttm);
		            detailMap.put("creatPgm", paramMap.get("pgmId"));;
		            detailMap.put("udtId", "");
		            detailMap.put("udtPgm", "");
		            detailMap.put("udtDttm", "");

		            result += cr50Mapper.insertPfuArea(detailMap);
		        }
		    }
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
		
  		//---------------------------------------------------------------
  		//결재처리. 
  		//---------------------------------------------------------------
		String ordrsNo = paramMap.get("ordrsNo");
		String salesCd = paramMap.get("salesCds");
		String fileTrgtKey = paramMap.get("fileTrgtKey");
		String coCd = paramMap.get("coCd");
		String pgmId = paramMap.get("pgmId");
		String userId = paramMap.get("userId");
  		
		paramMap.put("reqNo", fileTrgtKey);
        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
        if (!sharngChk.isEmpty()) {
            QM01Mapper.deleteWbsSharngList(paramMap); 
        }


        // Process sharing data
        processInformation(paramMap.get("rowSharngListArr"), "T", QM01Mapper::insertWbsSharngList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        // Process approval data
        processInformation(paramMap.get("rowApprovalListArr"), "S", QM01Mapper::insertWbsApprovalList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

		
		return result;
	}
	

    private String createPgParam(String actionType, String fileTrgtKey, String coCd, String ordrsNo, String salesCd) {
        return String.format("{\"actionType\":\"%s\",\"fileTrgtKey\":\"%s\",\"coCd\":\"%s\",\"ordrsNo\":\"%s\",\"salesCd\":\"%s\"}", 
                             actionType, fileTrgtKey, coCd, ordrsNo, salesCd);
    }
	

    private void processInformation(String jsonData, String actionType, Consumer<Map<String, String>> insertFunction,
                                    String fileTrgtKey, String coCd, String ordrsNo, String salesCd, String pgmId, String userId, String histNo) {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> list = gsonDtl.fromJson(jsonData, dtlMap);
        if (list != null && !list.isEmpty()) {
            int i = 0;
            for (Map<String, String> map : list) {
//                try {
                    map.put("reqNo", fileTrgtKey);
                    map.put("salesCd", salesCd);
                    map.put("fileTrgtKey", fileTrgtKey);
                    map.put("pgmId", pgmId);
                    map.put("userId", userId);
                    map.put("usrNm", userId);
                    map.put("histNo", histNo);
                    map.put("todoCoCd", coCd);
                    map.put("sanCtnSn", Integer.toString(++i));
                    map.put("pgParam", createPgParam(actionType, fileTrgtKey, coCd, ordrsNo, salesCd));
                    map.put("todoTitle", fileTrgtKey + ", " + map.get("todoTitle"));
                    QM01Mapper.insertWbsSharngList(map);
//                } catch (Exception e) {
//                    System.out.println("error2: " + e.getMessage());
//                }
            }
        }
    }
	
	
	
	  @Override
	  public int updatePfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//		Gson gson = new Gson();
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

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

		int result = cr50Mapper.updatePfu(paramMap);

		int deleteCnt = cr50Mapper.deletePfuAreaAll(paramMap);	//동록된 사양 일괄 삭제 처리
		
//		List<Map<String, String>> a01ListArr = gsonDtl.fromJson(paramMap.get("a01ListRow"), dtlMap);
//		//Area01 정보 저장
//		if (a01ListArr != null && !a01ListArr.isEmpty()) {
//			for (Map<String, String> detailMap : a01ListArr) {
//					detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//					Object indexObj = paramMap.get("__index");
//					int tempSeq = (indexObj instanceof Integer) ? (Integer) indexObj : 0;
//					int newSeq = tempSeq + 1;
//					detailMap.put("pfuSeq", Integer.toString(newSeq)) ;
//					
//					String area = (String) paramMap.get("partDiv");
//					String formattedSeq = String.format("%02d", tempSeq);
//					String resultString = area + formattedSeq;
//					detailMap.put("partId", resultString);
//					
//					detailMap.put("userId", paramMap.get("userId"));
//					detailMap.put("pgmId", paramMap.get("pgmId"));
//					
//					result += cr50Mapper.insertPfuArea(detailMap);
//			}
//		}

		String sysCreateDttm =  cr50Mapper.selectSystemCreateDttm(paramMap);
        int newSeq = 0;
		for (int i = 1; i <= 4; i++) {
		    // a01ListRow, a02ListRow, a03ListRow, a04ListRow를 동적으로 가져옵니다.
		    String key = "a0" + i + "ListRow"; // "a01ListRow", "a02ListRow", ...
		    List<Map<String, String>> aListArr = gsonDtl.fromJson(paramMap.get(key), dtlMap);

		    // 정보 저장
		    if (aListArr != null && !aListArr.isEmpty()) {
		        for (Map<String, String> detailMap : aListArr) {
		            detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));

//		            Object indexObj = detailMap.get("__index");
//		            int tempSeq = (indexObj instanceof Integer) ? (Integer) indexObj : 0;
		            
		            newSeq += 1;
		            detailMap.put("pfuSeq", Integer.toString(newSeq));
		            detailMap.put("sortNo", Integer.toString(newSeq));

		            String area = (String) detailMap.get("partDiv");
		            String resultString = area + String.format("%02d", newSeq);
		            detailMap.put("partId", resultString);

		            String originData = (String) detailMap.get("originData");
		            String checkData = (String) detailMap.get("checkData");
		            if ("I".equals(detailMap.get("updChk"))) {  //추가항목 추가항목은 신규 Insert 임.
			            detailMap.put("creatId", paramMap.get("userId"));
			            detailMap.put("creatDttm", sysCreateDttm);
			            detailMap.put("creatPgm", paramMap.get("pgmId"));;
			            detailMap.put("udtId", "");
			            detailMap.put("udtPgm", "");
			            detailMap.put("udtDttm", "");
		            } else if (originData.equals(checkData)) {  //변경 안됨
//			            detailMap.put("creatId", detailMap.get("userId"));
//			            detailMap.put("creatDttm", detailMap.get("creatDttm"));
//			            detailMap.put("creatPgm", detailMap.get("pgmId"));;
//			            detailMap.put("udtId", "");
//			            detailMap.put("udtPgm", "");
//			            detailMap.put("udtDttm", "");
		            } else {									//변경항목
			            detailMap.put("udtId", paramMap.get("userId"));
			            detailMap.put("udtPgm", paramMap.get("pgmId"));
			            detailMap.put("udtDttm", sysCreateDttm);
		            }
		            		
		            result += cr50Mapper.insertPfuArea(detailMap);	//삭제하고 전부 다시 등록처리함. (일련번호 또는 정렬순서)
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

  		//---------------------------------------------------------------
  		//결재처리. 
  		//---------------------------------------------------------------
		String ordrsNo = paramMap.get("ordrsNo");
		String salesCd = paramMap.get("salesCds");
		String fileTrgtKey = paramMap.get("fileTrgtKey");
		String coCd = paramMap.get("coCd");
		String pgmId = paramMap.get("pgmId");
		String userId = paramMap.get("userId");
  		
		paramMap.put("reqNo", fileTrgtKey);
        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
        if (!sharngChk.isEmpty()) {
            QM01Mapper.deleteWbsSharngList(paramMap); 
        }


        // Process sharing data
        processInformation(paramMap.get("rowSharngListArr"), "T", QM01Mapper::insertWbsSharngList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        // Process approval data
        processInformation(paramMap.get("rowApprovalListArr"), "S", QM01Mapper::insertWbsApprovalList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

		
	     return result;
	  }

	  @Override
	  public int deletePfu(Map<String, String> paramMap) throws Exception {
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
		    int result = cr50Mapper.deletePfu(paramMap);

//		    //상세내역 삭제
//		    result += cr50Mapper.deleteTurnKeyDetail(paramMap);
//		    
//		    //지급계획 삭제
//		    result += cr50Mapper.deleteTurnKeyClmnPlan(paramMap);
		    
			//---------------------------------------------------------------  
			//첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
			//---------------------------------------------------------------  
			if (deleteFileList.size() > 0) {		  
			    for (Map<String, String> deleteDtl : deleteFileList) {
			    	String fileKey = deleteDtl.get("fileKey").toString();
				    cm08Svc.deleteFile( fileKey );
			    }
			}
			//---------------------------------------------------------------  
			//첨부 화일 처리  끝 
			//---------------------------------------------------------------  	  
		    return result;
	  }
		
	
}
