package com.dksys.biz.user.wb.wb21.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
	
}