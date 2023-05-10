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
  public List<Map<String, String>> selectPrdtList(Map<String, String> param) {
    return bm16Mapper.selectPrdtList(param);
  }

  @Override
  public Map<String, String> selectPrjctInfo(Map<String, String> paramMap) {
    return bm16Mapper.selectPrjctInfo(paramMap);
  }

  @Override
  public Map<String, String> selectPrdtInfo(Map<String, String> param) {

    return bm16Mapper.selectPrdtInfo(param);
  }

  @Override
  public int selectDetail01Count(Map<String, String> param) {
    return bm16Mapper.selectDetail01Count(param);
  }

  @Override
  public List<Map<String, String>> selectDetail01List(Map<String, String> param) {
    return bm16Mapper.selectDetail01List(param);
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
    Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();
    List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
//      dtl.put("prjctSeq", paramMap.get("prjctSeq"));
//      dtl.put("prjctPrdtCd", paramMap.get("prjctPrdtCd"));
//      dtl.put("prjctItemCd", paramMap.get("prjctItemCd"));
//      dtl.put("prjctItemRmk", paramMap.get("prjctItemRmk"));
      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));
//
      String dtaChk = dtl.get("dtaChk").toString();
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
//    paramMap.put("prjctSeq", paramMap.get("prjctSeq")); // parammap에 put할 인수
//    paramMap.put("prjctPrdtCd", paramMap.get("prjctPrdtCd"));
//    paramMap.put("prjctItemCd", paramMap.get("prjctItemCd"));
//    paramMap.put("prjctItemRmk", paramMap.get("prjctItemRmk"));
//    paramMap.put("prjctItemSort", paramMap.get("prjctItemSort"));

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();
    List<Map<String, String>> dtlParam = gson.fromJson(paramMap.get("detailArr"), dtlMap);
    for (Map<String, String> dtl : dtlParam) {
//      dtl.put("prjctSeq", paramMap.get("prjctSeq"));
//      dtl.put("prjctPrdtCd", paramMap.get("prjctPrdtCd"));
//      dtl.put("prjctItemCd", paramMap.get("prjctItemCd"));
      dtl.put("userId", paramMap.get("userId"));
      dtl.put("pgmId", paramMap.get("pgmId"));

      String dtaChk = dtl.get("dtaChk").toString();
      if ("I".equals(dtaChk)) {
        bm16Mapper.insertPrjctDtl(dtl);
      }
    }
    return result;
  }

  @Override
  public int deletePrjct(Map<String, String> param) {
    int result = bm16Mapper.deletePrjctDtlAll(param);
    return bm16Mapper.deletePrjct(param);
  }

  @Override
  public void deletePrjctDtl(List<Map<String, String>> paramList) {
    for (Map<String, String> param : paramList) {
      bm16Mapper.deletePrjctDtl(param);
    }
  }

//  이하 PRDT 추가

  @Override
  public int selectPrdtCount(Map<String, String> param) {
    return bm16Mapper.selectPrdtCount(param);
  }

  @Override
  public int selectPrdtClntCount(Map<String, String> param) {
    return bm16Mapper.selectPrdtClntCount(param);
  }

  @Override
  public List<Map<String, String>> selectPrdtClntList(Map<String, String> param) {
    return bm16Mapper.selectPrdtClntList(param);
  }

  @Override
  public int selectOneMasterClntCount(Map<String, String> param) {
    return bm16Mapper.selectOneMasterClntCount(param);
  }

  @Override
  public Map<String, String> seletOneMasterClnt(Map<String, String> param) {
    return bm16Mapper.seletOneMasterClnt(param);
  }

  @Override
  public int insertOneMasterClnt(Map<String, String> param) {
    return bm16Mapper.insertOneMasterClnt(param);

  }

  @Override
  public Map<String, String> seletOneMaster(Map<String, String> param) {
    return bm16Mapper.seletOneMaster(param);
  }

  @Override
  public int insertOneMaster(Map<String, String> param) {
    return bm16Mapper.insertOneMaster(param);
  }

  @Override
  public int selectOneMasterCount(Map<String, String> param) {
    return bm16Mapper.selectOneMasterCount(param);
  }

  @Override
  public int insertOneDetail01(Map<String, String> param) {
    return bm16Mapper.insertOneDetail01(param);
  }

  @Override
  public int updateOneDetail01(Map<String, String> param) {
    return bm16Mapper.updateOneDetail01(param);
  }

  @Override
  public Map<String, String> seletOneDetail01(Map<String, String> param) {
    return bm16Mapper.seletOneDetail01(param);
  }
}