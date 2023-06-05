package com.dksys.biz.admin.bm.bm16.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.bm.bm16.mapper.BM16Mapper;
import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class BM16SvcImpl implements BM16Svc {

  @Autowired
  BM16Mapper bm16Mapper;

  @Override
  public int selectPrjctCount(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectPrjctList(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectItemList(Map<String, String> paramMap) {
    return bm16Mapper.selectItemList(paramMap);
  }

  @Override
  public List<Map<String, String>> selectPrdtList(Map<String, String> paramMap) {
    return bm16Mapper.selectPrdtList(paramMap);
  }

  @Override
  public Map<String, String> selectPrjctInfo(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctInfo(paramMap);
  }

  @Override
  public int selectConfirmCount(Map<String, String> paramMap) {
    return bm16Mapper.selectConfirmCount(paramMap);
  }

  @Override
  public List<Map<String, String>> selectFileList(Map<String, String> paramMap) {
    return bm16Mapper.selectFileList(paramMap);
  }

  @Override
  public int updatePrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
    int result = bm16Mapper.updatePrjct(paramMap);
    	//  bm16Mapper.updatePrjct(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 업데이트하고 그 결과를 result 변수에 저장.
    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
    	//  Gson을 사용하여 paramMap의 "detailArr" 값을 가져와서 dtlParam 변수에 ArrayList<Map<String, String>> 형식으로 변환
    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();

    List<Map<String, String>> dtlPrdt = gsonDtl.fromJson(paramMap.get("prdtArr"), dtlMap);
    for (Map<String, String> dtl : dtlPrdt) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행
    	
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	//      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
    	String dtaChk = dtl.get("prjctCrudChk").toString();
    	/* "dtaChk" 값을 확인하여
    	 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
    	 * "U"인 경우 bm16Mapper.updatePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
    	 * "D"인 경우 * bm16Mapper.deletePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
    	if ("I".equals(dtaChk)) {
    		dtl.put("prjctSeq", paramMap.get("prjctSeq"));
    		bm16Mapper.insertPrjctPrdt(dtl);
    	} else if ("U".equals(dtaChk)) {
    		bm16Mapper.updatePrjctPrdt(dtl);
    	} else if ("D".equals(dtaChk)) {
    		bm16Mapper.deletePrjctPrdt(dtl);
    		bm16Mapper.deletePrjctDtlPrdtAll(dtl);
    	}
    }
    
    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
    	//  dtlParam 리스트의 각 맵 요소에 대해 반복문을 실행

      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
      //      반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
      String dtaChk = dtl.get("dtaChk").toString();
		/* "dtaChk" 값을 확인하여
		 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입하고,
		 * "U"인 경우 bm16Mapper.updatePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 업데이트하고,
		 * "D"인 경우 * bm16Mapper.deletePrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삭제.		 */
      if ("I".equals(dtaChk)) {
	        bm16Mapper.insertPrjctDtl(dtl);
	      } else if ("U".equals(dtaChk)) {
	        bm16Mapper.updatePrjctDtl(dtl);
	      } else if ("D".equals(dtaChk)) {
	        bm16Mapper.deletePrjctDtl(dtl);
	      }
    }
    return result;
  }

  @Override
  public int insertPrjct(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
    int result = bm16Mapper.insertPrjct(paramMap);
	//    bm16Mapper.updatePrjct(paramMap)을 호출하여 paramMap을 사용하여 프로젝트를 삽입하고 그 결과를 result 변수에 저장.
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	//    Gson을 사용하여 paramMap의 "detailArr" 값을 가져와서 dtlParam 변수에 ArrayList<Map<String, String>> 형식으로 변환
    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();
    List<Map<String, String>> dtlPrdt = gson.fromJson(paramMap.get("prdtArr"), dtlMap);
    for (Map<String, String> dtl : dtlPrdt) {

      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
      //반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
      bm16Mapper.insertPrjctPrdt(dtl);
    }
    List<Map<String, String>> dtlParam = gson.fromJson(paramMap.get("detailArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
    	
    	dtl.put("userId", paramMap.get("userId"));
    	dtl.put("pgmId", paramMap.get("pgmId"));
    	//반복문에서는 각 맵(dtl)에 "userId"와 "pgmId"를 추가
    	String dtaChk = dtl.get("dtaChk").toString();
    	/* "dtaChk" 값을 확인하여
    	 * "I"인 경우 bm16Mapper.insertPrjctDtl(dtl)을 호출하여 프로젝트 세부정보를 삽입 */
    	if ("I".equals(dtaChk)) {
    		bm16Mapper.insertPrjctDtl(dtl);
    	} 
    }
    return result;
  }

  @Override
  public int deletePrjct(Map<String, String> param) {
	  int result = bm16Mapper.deletePrjctPrdtSeqAll(param);
	  result = bm16Mapper.deletePrjctDtlSeqAll(param);
	  result = bm16Mapper.deletePrjct(param);
    return result;
  }

}