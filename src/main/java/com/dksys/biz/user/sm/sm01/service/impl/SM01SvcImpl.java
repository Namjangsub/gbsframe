package com.dksys.biz.user.sm.sm01.service.impl;

import com.dksys.biz.user.sm.sm01.mapper.SM01Mapper;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM01SvcImpl implements SM01Svc {
    @Autowired
    SM01Mapper sm01Mapper;
    
    @Override
    public int selectBomCount(Map<String, String> param) {
        return sm01Mapper.selectBomCount(param);
    }
    @Override
    public int selectBomDetailCount(Map<String, String> param) {
        return sm01Mapper.selectBomDetailCount(param);
    }

    @Override
    public List<Map<String, Object>> selectBomList(Map<String, String> param) {
        return sm01Mapper.selectBomList(param);
    }
    @Override
    public List<Map<String, Object>> selectBomDetailList(Map<String, String> param) {
        return sm01Mapper.selectBomDetailList(param);
    }
    @Override
    public Map<String, Object> insertBomDetailList(Map<String, String> param) {
        Map<String, String> estDetail = new HashMap<String, String>();
        estDetail.put("coCd", param.get("coCd"));
        estDetail.put("salesCd", param.get("salesCd"));
        estDetail.put("bomSeq", param.get("bomSeq"));
        estDetail.put("unitNo", param.get("unitNo"));
        estDetail.put("revNo", param.get("revNo"));
        return sm01Mapper.insertBomDetailList(param);
    }

    @Override
    public int deleteBom(Map<String, String> paramMap) {
        int result = sm01Mapper.deleteBom(paramMap);
        result += sm01Mapper.deleteAllBomDetails(paramMap);
        return result;
    }

    @Override
    public Map<String, Object> selectBomInfo(Map<String, String> param) {
        return sm01Mapper.selectBomInfo(param);
    }

    @Override
    public List<Map<String, Object>> selectBomListMatr(Map<String, String> param) {
        return sm01Mapper.selectBomListMatr(param);
    }

    @Override
    public Map<String, Object> updateBom(Map<String, String> paramMap) {


/*

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();

        List<Map<String, Object>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);

        List<Map<String, Object>> dbDetailListRaw = sm01Mapper.selectBomList(paramMap);

        // 삭제된 견적 상세 처리
        for (Map<String, String> dbDetail : dbDetailList) {
            boolean found = false;
            for (Map<String, Object> estDetail : detailList) {
                if (dbDetail.get("estSeq").equals(estDetail.get("estSeq"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                cr01Mapper.deleteEstDetail(dbDetail);
            }
        }

        // 견적 상세 목록 처리
        for (Map<String, Object> estDetail : detailList) {
            boolean found = false;
            for (Map<String, String> dbDetail : dbDetailList) {
                // 조건을 검사할 때 detailList 대신 dbDetailList를 사용해야 합니다.
                if (dbDetail.get("estSeq").equals(estDetail.get("estSeq"))) {
                    found = true;
                    break;
                }
            }
            if (found) {

                // 견적 상세 업데이트
                estDetail.put("coCd", paramMap.get("coCd"));
                estDetail.put("estNo", paramMap.get("estNo"));
                estDetail.put("estDeg", paramMap.get("estDeg"));

                estDetail.put("userId", paramMap.get("userId"));
                estDetail.put("pgmId", paramMap.get("pgmId"));
                estDetail.put("udtId", paramMap.get("userId"));
                estDetail.put("udtPgm", "TB_CR01M01");

                System.out.println("23232" + estDetail);
                // 업데이트 쿼리를 실행해야 합니다.
                cr01Mapper.updateEstDetail(estDetail);
            } else {
                // 견적 상세 삽입
                estDetail.put("coCd", paramMap.get("coCd"));
                estDetail.put("estNo", paramMap.get("estNo"));
                estDetail.put("userId", paramMap.get("userId"));
                estDetail.put("pgmId", paramMap.get("pgmId"));
                estDetail.put("estDeg", paramMap.get("estDeg"));
                estDetail.put("creatId", paramMap.get("userId"));
                estDetail.put("creatPgm", "TB_CR01M01");


                cr01Mapper.insertEstDetail(estDetail);
            }














            Map<String, Object> responseMap = new HashMap<>();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);
            for (Map<String, String> estDetail : detailList) {
                sm01Mapper.updateBom(estDetail);
            }
            responseMap.put("resultCode",0);
            return responseMap;
*/









            Map<String, Object> responseMap = new HashMap<>();

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        List<Map<String, String>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);
        for (Map<String, String> estDetail : detailList) {
            sm01Mapper.updateBom(estDetail);
        }
        responseMap.put("resultCode",0);
        return responseMap;











    }
}
