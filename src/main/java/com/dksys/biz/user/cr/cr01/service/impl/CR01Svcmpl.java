package com.dksys.biz.user.cr.cr01.service.impl;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr01.mapper.CR01Mapper;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR01Svcmpl implements CR01Svc {
    @Autowired
    CR01Mapper cr01Mapper;
    @Autowired
    CM08Mapper cm08Mapper;

    @Autowired
    CM08Svc cm08Svc;

    @Override
    public String selectMaxEstNo(Map<String, String> paramMap) {
        return cr01Mapper.selectMaxEstNo(paramMap);
    }

    @Override
    public int selectEstCount(Map<String, String> param) {
        return cr01Mapper.selectEstCount(param);
    }

    @Override
    public List<Map<String, Object>> selectEstList(Map<String, String> param) {

        return cr01Mapper.selectEstList(param);
    }

    @Override
    public List<Map<String, Object>> selectEstListNotOrdrs(Map<String, String> param) {

        return cr01Mapper.selectEstListNotOrdrs(param);
    }

    @Override
    public Map<String, Object> selectEstInfo(Map<String, String> paramMap) {

        Map<String, Object> estInfo = cr01Mapper.selectEstInfo(paramMap);

        return estInfo;

    }

    @Override
    public String insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        Type stringList = new TypeToken<ArrayList<String>>() {
        }.getType();
        try {
            String maxEstNo=selectMaxEstNo(paramMap);
            paramMap.put("estNo", maxEstNo);
            String fileTrgtKey;
            fileTrgtKey = cm08Mapper.selectNextFileTrgtKey();
            paramMap.put("fileTrgtKey", fileTrgtKey);
            cr01Mapper.insertEst(paramMap);
            List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);


            for (Map<String, String> detailMap : detailArr) {
                System.out.println("여기실행");
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", maxEstNo);
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));

                cr01Mapper.insertEstDetail(detailMap);
            }
            cm08Svc.uploadTreeFile("TB_CR01M01", fileTrgtKey, mRequest);

        } catch (Exception e) {
            System.out.println(e.getMessage() + "에러명");
        }
        return paramMap.get("estNo");

    }

    @Override
    public Map<String, String> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        Type stringList = new TypeToken<ArrayList<String>>() {
        }.getType();
        Map<String, String> responseMap = new HashMap<>();
        try {
            String fileTrgtKey;
            fileTrgtKey = cm08Mapper.selectNextFileTrgtKey();
            paramMap.put("fileTrgtKey", fileTrgtKey);

            String maxEstDeg = cr01Mapper.selectMaxEstDeg(paramMap);
            System.out.println(maxEstDeg+"맥스값");
            int newEstDeg = (maxEstDeg != null ? Integer.parseInt(maxEstDeg) : 0) + 1;
            paramMap.put("estDeg", String.valueOf(newEstDeg));

            System.out.println(    String.valueOf(newEstDeg)+"맥스값2");


            cr01Mapper.insertEst(paramMap);
            List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);
            System.out.println(detailArr + "여기2");


            for (Map<String, String> detailMap : detailArr) {
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", paramMap.get("estNo"));
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));
                cr01Mapper.insertEstDetail(detailMap);
            }
            cm08Svc.copyTreeFile("TB_CR01M01", fileTrgtKey, mRequest);


            responseMap.put("estNo", paramMap.get("estNo"));
            responseMap.put("estDeg", paramMap.get("estDeg"));
        } catch (Exception e) {
            System.out.println(e.getMessage() + "에러명");
        }
        return responseMap;

    }

    public static String removeEmptyObjects(String jsonArrayString) {
        String nullAndEmptyObjectPattern = "(\\{\\s*\\}|null),?";
        String resultString = jsonArrayString.replaceAll(nullAndEmptyObjectPattern, "");
        // In case the last empty object doesn't have a comma
        if (resultString.endsWith(",}")) {
            resultString = resultString.substring(0, resultString.length() - 2) + "}";
        }
        return resultString;
    }

    @Override
    public int updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();

        System.out.println(paramMap);
        int result = cr01Mapper.updateEst(paramMap);

    // 데이터베이스에서 현재 견적 상세 목록 가져오기
        List<Map<String, Object>> dbDetailListRaw = cr01Mapper.selectEstDetail(paramMap);

    // 데이터베이스 목록의 Object를 String으로 변환
        List<Map<String, String>> dbDetailList = dbDetailListRaw.stream()
                .map(rawMap -> rawMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
                .collect(Collectors.toList());

    // 클라이언트에서 전달된 견적 상세 목록
        List<Map<String, String>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);

    // 삭제된 견적 상세 처리
        for (Map<String, String> dbDetail : dbDetailList) {
            boolean found = false;
            for (Map<String, String> estDetail : detailList) {
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
        for (Map<String, String> estDetail : detailList) {
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
                estDetail.put("userId", paramMap.get("userId"));
                estDetail.put("pgmId", paramMap.get("pgmId"));
                estDetail.put("estDeg", paramMap.get("estDeg"));

                // 업데이트 쿼리를 실행해야 합니다.
                System.out.println(estDetail+"최종123412");
                cr01Mapper.updateEstDetail(estDetail);
            } else {
                // 견적 상세 삽입
                estDetail.put("coCd", paramMap.get("coCd"));
                estDetail.put("estNo", paramMap.get("estNo"));
                estDetail.put("userId", paramMap.get("userId"));
                estDetail.put("pgmId", paramMap.get("pgmId"));
                estDetail.put("estDeg", paramMap.get("estDeg"));

                cr01Mapper.insertEstDetail(estDetail);
            }
        }
        System.out.println("여기까지2"+paramMap.get("deleteFileArr"));
        String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);

        for(String fileKey : deleteFileList) {

            cm08Svc.deleteFile(fileKey);
        }
        System.out.println(paramMap.get("fileTrgtKey")+"해당위치");
        cm08Svc.uploadTreeFile("TB_CR01M01", paramMap.get("fileTrgtKey"), mRequest);

        return result;
    }


    public int updateEstConfirm(Map<String, String> paramMap) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        int result = cr01Mapper.updateEstConfirm(paramMap);

        return 1;
    }

    @Override
    public int deleteEst(Map<String, String> paramMap) {
        int result = cr01Mapper.deleteEst(paramMap);
        result += cr01Mapper.deleteAllEstDetails(paramMap);
        return result;
    }
}