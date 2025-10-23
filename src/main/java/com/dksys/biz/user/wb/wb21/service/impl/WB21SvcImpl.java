package com.dksys.biz.user.wb.wb21.service.impl;

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
import com.dksys.biz.user.wb.wb21.mapper.WB21Mapper;
import com.dksys.biz.user.wb.wb21.service.WB21Svc;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB21SvcImpl implements WB21Svc {
    
    @Autowired
    WB21Mapper wb21Mapper;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;
    
    @Autowired
    QM01Mapper QM01Mapper;

    @Autowired
    WB21Svc wb21Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
    @Override
    public List<Map<String, String>> selectMaxSjNo(Map<String, String> paramMap) {
  		return wb21Mapper.selectMaxSjNo(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectSalesCodeCheck(Map<String, String> paramMap) {
  		return wb21Mapper.selectSalesCodeCheck(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectCodeList(Map<String, String> paramMap) {
  		return wb21Mapper.selectCodeList(paramMap);
    }
    
    @Override
	public int selectSjListCount(Map<String, String> paramMap) {
		return wb21Mapper.selectSjListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectSjList(Map<String, String> paramMap) {
		return wb21Mapper.selectSjList(paramMap);
	}

	// 과제일괄확정부분
	@Override
	public int ModalsjnoconfirmListCount(Map<String, String> paramMap) {
		return wb21Mapper.ModalsjnoconfirmListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> ModalsjnoconfirmList(Map<String, String> paramMap) {
		return wb21Mapper.ModalsjnoconfirmList(paramMap);
	}

	// 과제일괄확정
	@Override
	public int confirm_wb21(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
		
		//데이터처리 시작
		int result = 0;

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			//dtl.put("userId", paramMap.get("userId"));
	    	//dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("U".equals(dataChk)) {
				//데이터 처리
				result = wb21Mapper.confirm_wb21(dtl);
	    	} 
	    }
		//데이터 처리 끝
		return result;
	}
	// 과제일괄확정부분 끝

	// 과제복사
	@Override
	public int ModalsjnocopyListCount(Map<String, String> paramMap) {
		return wb21Mapper.ModalsjnocopyListCount(paramMap);
	}

	@Override
	public List<Map<String, String>> ModalsjnocopyList(Map<String, String> paramMap) {
		return wb21Mapper.ModalsjnocopyList(paramMap);
	}

	@Override
	public int copy_wb21(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
		HashMap<String, String> param = new HashMap<>();
		param.put("userId", paramMap.get("userId"));
		
		//데이터처리 시작
		int result = 0;

		//상세수정
		List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
	    for (Map<String, String> dtl : dtlParam) {
	    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
			//dtl.put("userId", paramMap.get("userId"));
	    	//dtl.put("pgmId", paramMap.get("pgmId"));
			
			String dataChk = dtl.get("dataChk").toString();	    	
			//"dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
	    	if ("I".equals(dataChk)) {
	    		dtl.put("coCd", paramMap.get("coCd_PS"));
				//데이터 처리
	    		result = wb21Mapper.delete_wb21(dtl);
				result = wb21Mapper.copy_wb21(dtl);
	    	} 
	    }
		//데이터 처리 끝
		return result;
	}
	// 과제복사 끝
	
	@Override
	public List<Map<String, String>> deleteSjListChk(Map<String, String> paramMap) {
		return wb21Mapper.deleteSjListChk(paramMap);
	}
	
	@Override
	public int deleteSjList(Map<String, String> paramMap) {
		int result = wb21Mapper.deleteSjList(paramMap);
		return result;
	}
	
	@Override
	public int sjInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb21Mapper.selectSjSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
		
		Gson gson = new Gson(); 
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
            	    wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb21Mapper.sjInsert(paramMap);
	    return result;
	}
    
	@Override
	public int sjUpdate(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		Gson gson = new Gson(); 
		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();

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
		
		
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
	            	wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}

		int result = wb21Mapper.sjUpdate(paramMap);
	    
		//---------------------------------------------------------------  
		//첨부 화일 처리 시작 
		//---------------------------------------------------------------  
	    if (uploadFileList.size() > 0) {
		    paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
		    paramMap.put("fileTrgtKey", paramMap.get("sjNo"));
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
	public List<Map<String, String>> selectSjDtlList(Map<String, String> paramMap) {
		return wb21Mapper.selectSjDtlList(paramMap);
	}
	
	@Override
	public Map<String, String> selectSjInfo(Map<String, String> paramMap) {
		return wb21Mapper.selectSjInfo(paramMap);
	}
	
	@Override
	public int sjConfirmY(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
        // 확정전 수정자료 자동 저장후 확정 처리함. (수정 + 확정)
        int sjUpdate = sjUpdate(paramMap, mRequest);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		//---------------------------------------------------------------
		//공유처리[]
		//---------------------------------------------------------------
		//공유
			
		String pgParam1 = "{\"pgmId\":\""+ paramMap.get("pgmId") +"\",";
		pgParam1 += "\"fileTrgtKey\":\""+ paramMap.get("fileTrgtKey") +"\","; 
		pgParam1 += "\"coCd\":\""+ paramMap.get("coCd") +"\","; 
		pgParam1 += "\"sjNo\":\""+ paramMap.get("sjNo") +"\","; 
		pgParam1 += "\"verNo\":\""+ paramMap.get("verNo") +"\",";
		pgParam1 += "\"ordrsNo\":\""+ paramMap.get("userId") +"\"}";
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowSharngListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
			int i = 0;
			for (Map<String, String> sharngMap : sharngArr) {
				try {	 
					sharngMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
					sharngMap.put("pgmId", paramMap.get("pgmId"));
					sharngMap.put("userId", paramMap.get("userId"));
					sharngMap.put("sanCtnSn",Integer.toString(i+1));
					sharngMap.put("pgParam", pgParam1);
					QM01Mapper.deleteSJApprovalList1(sharngMap); 
					QM01Mapper.insertWbsSharngList(sharngMap);       		
					i++;
				} catch (Exception e) {
					System.out.println("error2"+e.getMessage());
				}
			}
		}
			  
	
		int result = wb21Mapper.sjConfirmY(paramMap);
		result = wb21Mapper.sjCloseY(paramMap);
	    return result;
	}
	
	@Override
	public int sjConfirmN(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
		int result = wb21Mapper.sjConfirmN(paramMap);
		result = wb21Mapper.sjCloseN(paramMap);
	    return result;
	}
	
	@Override
	public int sjVerUpInsert(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
	
		int fileTrgtKey = wb21Mapper.selectSjSeqNext(paramMap);
		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));

		//기존건 확정해제
		sjConfirmN(paramMap, mRequest);
			
		Gson gson = new Gson(); 
		Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> sharngArr = gson.fromJson(paramMap.get("rowListArr"), stringList);
		if (sharngArr != null && sharngArr.size() > 0 ) {
	        for (Map<String, String> sharngMap : sharngArr) {
	            try {
            	    wb21Mapper.insertSjDtlList(sharngMap);
	            } catch (Exception e) {
	                System.out.println("error2"+e.getMessage());
	            }
	        }
		}
		
		
		if((paramMap.get("sjRmk")).equals("null")) {
			paramMap.put("sjRmk", "");
		}
		
		System.out.println("@@@@@"+paramMap.get("closeYn"));
		System.out.println("!!!!!"+paramMap.get("closeYn"));
		
		int result = wb21Mapper.sjInsert(paramMap);
		if(result != 0) {
			return fileTrgtKey;
		}else {
			return result;
		}
	}
	
	@Override
    public List<Map<String, String>> selectSjVerNoNext(Map<String, String> paramMap) {
  		return wb21Mapper.selectSjVerNoNext(paramMap);
    }
	
	//DATA DELETE
	@Override
	public int deleteSjNo(Map<String, String> paramMap) throws Exception {
		int result = 0;

		//데이터 처리
		result = wb21Mapper.deleteSjNo(paramMap);
		
		return result;
	}
	
	//Project 과제 체크
	@Override
	public Map<String, String> selectWbChk(Map<String, String> paramMap) {
		return wb21Mapper.selectWbChk(paramMap);
	}

	//외주 제작업체 수정
	@Override
	public int planMkerCdChange(Map<String, String> paramMap) {
		int result = wb21Mapper.planMkerCdChange(paramMap);
		return result;
	}

    // 과제등록,수정시에 매뉴얼 파일 등록하기 (삭제는 아래 deleteSalesCdManual 함수이용)
    // 파일 첨부하는 시점에 즉시 실행되며, 하나의 파일만 Upload 처리되게 구현됨
    // case1 최초 등록시 실행됨
    // case2 파일변경시 대체파일과 삭제대상 Key값이 manualFileDeleteArr에 전달됨(추후 감안 array로 처리함)
    @Override
    public int updateSalesCdManual(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new Gson();
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        int result = 0;
        // ---------------------------------------------------------------
        // 매뉴얼 파일 등록삭제는 권한 체크 없이 처리함
        // ---------------------------------------------------------------
        HashMap<String, String> param = new HashMap<>();
        param.put("userId", paramMap.get("userId"));
        param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

        String tripfileTrgtTyp = "WB2101P01";
        String manualType = paramMap.get("manualCall");
        // 세부내역별 키값에 대한부분은 작업일보 번호 + 경비인련번호로 구성함
        String tripfileTrgtKey = paramMap.get("fileTrgtKey") + "-" + manualType;
        String tempFileName = "";
        if (manualType.equals("manualMc")) {
            tempFileName = paramMap.get("manualMc");
        } else {
            tempFileName = paramMap.get("manualElec");
        }
        // 매뉴얼 첨부파일 처리
        try {
            paramMap.put("fileTrgtTyp", "WB2101P01");
            paramMap.put("fileTrgtKey", tripfileTrgtKey);
            cm08Svc.uploadFile(paramMap, mRequest);
        } catch (Exception e) {
            System.out.println(tempFileName + " 파일 삭제중 오류발생!!" + e.getMessage());
        }

        // Upload된 파일의 등록번호를 과제테이블에 반영하기
        // 매뉴얼은 최종본만 관리하는 것으로 설정함. salesCd별로 기계, 전기 매뉴얼 한개씩 관리하며,
        // 메뉴얼 이력을 관리하려면 테이블 구조를 바꿔야 함.
        if (manualType.equals("manualMc")) {
            paramMap.put("manualMc", paramMap.get("fileKey"));
        } else {
            paramMap.put("manualElec", paramMap.get("fileKey"));
        }
        result = wb21Mapper.updateSalesCdManual(paramMap);


        // ---------------------------------------------------------------
        // 이전 등록된 화일이 잇으면 삭제하기
        // ---------------------------------------------------------------
        List<Map<String, String>> deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
        if (deleteFileArr != null && deleteFileArr.size() > 0) {
            for (Map<String, String> dtlMap : deleteFileArr) {
                try {
                    cm08Svc.deleteFile(dtlMap.get("fileKey"));
                } catch (Exception e) {
                    System.out.println(dtlMap.get("fileName") + " 파일 등록중 오류발생!!" + e.getMessage());
                }
            }
        }

        // ---------------------------------------------------------------
        // 이전 등록된 화일이 잇으면 삭제하기 끝
        // ---------------------------------------------------------------

        return result;
    }

    // 과제등록,수정시에 매뉴얼 파일 삭제하기 (등록처리는 updateSalesCdManual 함수이용)
    @Override
    public int deleteSalesCdManual(Map<String, String> paramMap) throws Exception {

        String manualType = paramMap.get("manualCall");
        if (manualType.equals("manualMc")) {
            paramMap.put("manualMc", "");
        } else {
            paramMap.put("manualElec", "");
        }
        int result = wb21Mapper.updateSalesCdManual(paramMap);

        // 매뉴얼 파일 등록삭제는 권한 체크 없이 처리함
        Gson gson = new Gson();
        Type stringList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        List<Map<String, String>> deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), stringList);
        if (deleteFileArr != null && deleteFileArr.size() > 0) {

            // ---------------------------------------------------------------
            // 첨부 화일 처리 시작
            // ---------------------------------------------------------------
            for (Map<String, String> dtlMap : deleteFileArr) {
                try {
                    cm08Svc.deleteFile(dtlMap.get("fileKey"));
                } catch (Exception e) {
                    System.out.println(dtlMap.get("fileName") + " 파일 삭제중 오류발생!!" + e.getMessage());
                }
            }
            // ---------------------------------------------------------------
            // 첨부 화일 처리 끝
            // ---------------------------------------------------------------
        }
        return result;
    }

	@Override
	public List<Map<String, String>> deDtChangChk(Map<String, String> paramMap) {
		return wb21Mapper.deDtChangChk(paramMap);
	}
}