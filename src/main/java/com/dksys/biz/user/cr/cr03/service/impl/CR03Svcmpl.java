package com.dksys.biz.user.cr.cr03.service.impl;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.user.cr.cr03.mapper.CR03Mapper;
import com.dksys.biz.user.cr.cr03.service.CR03Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR03Svcmpl  implements CR03Svc {
    @Autowired
    CR03Mapper cr03Mapper;
    @Autowired
    CM08Mapper cm08Mapper;

    @Autowired
    CM08Svc cm08Svc;

    @Override
    public String selectMaxEstNo(Map<String, String> paramMap) {

        return cr03Mapper.selectMaxEstNo(paramMap);
    }
    @Override
    public String selectMaxEstDeg(Map<String, String> paramMap) {

        return cr03Mapper.selectMaxEstDeg(paramMap);
    }

    @Override
    public int selectEstCount(Map<String, String> param) {
        return cr03Mapper.selectEstCount(param);
    }
    public int selectEstDetailCount(Map<String, String> param) {
        return cr03Mapper.selectEstDetailCount(param);}
    @Override
    public List<Map<String, Object>> selectEstList(Map<String, String> param) {

        return cr03Mapper.selectEstList(param);
    }
    @Override
    public List<Map<String, Object>> selectEstListNotOrdrs(Map<String, String> param) {

        return cr03Mapper.selectEstListNotOrdrs(param);
    }
    @Override
    public Map<String, Object> selectEstInfo(Map<String, String> paramMap) {

        Map<String, Object> estInfo = cr03Mapper.selectEstInfo(paramMap);

        return estInfo;

    }
    @Override
    public Map<String, Object> insertEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        Type stringList = new TypeToken<ArrayList<String>>() {
        }.getType();
        Map<String, Object> responseMap = new HashMap<>();
        try {

            String maxEstNo = selectMaxEstNo(paramMap);
            paramMap.put("estNo", maxEstNo);

            // issueDate가 'yyyyMMdd' 형식으로 들어온다고 가정합니다
            String issueDateStringOriginal = paramMap.get("pblsDt");
            DateTimeFormatter incomingFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

            // incomingFormat을 사용하여 issueDate를 파싱합니다
            LocalDate issueDate = LocalDate.parse(issueDateStringOriginal, incomingFormat);

            // issueDate에 1개월을 추가합니다
            LocalDate expiryDate = issueDate.plusMonths(1);

            // issueDate와 expiryDate를 java.sql.Date 형식으로 변환합니다
            java.sql.Date sqlIssueDate = java.sql.Date.valueOf(issueDate);
            java.sql.Date sqlExpiryDate = java.sql.Date.valueOf(expiryDate);

            // java.sql.Date를 문자열로 변환합니다
            String issueDateString = sqlIssueDate.toString();
            String expiryDateString = sqlExpiryDate.toString();

            // 정확한 형식의 날짜를 paramMap에 넣습니다
            paramMap.put("estsDt", issueDateString);
            paramMap.put("esteDt", expiryDateString);
            // 데이터베이스 작업을 계속합니다
            System.out.println(paramMap.get("fileTrgtKey")+"리턴전값");

            cr03Mapper.insertEst(paramMap);

            System.out.println(paramMap.get("fileTrgtKey")+"여기 리턴값");


            List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);


            for (Map<String, String> detailMap : detailArr) {
                System.out.println("여기실행");
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", maxEstNo);
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));

                cr03Mapper.insertEstDetail(detailMap);
            }
            cm08Svc.uploadTreeFile("TB_CR01M01",paramMap, mRequest);
            responseMap.put("resultCode", true); // 결과 코드를 성공으로 설정합니다.
            Map<String, String> newEst = new HashMap<>();
            newEst.put("estNo", maxEstNo);
            newEst.put("estDeg", paramMap.get("estDeg"));
            responseMap.put("newEst", newEst);
        } catch (Exception e) {
            System.out.println( e+ "에러명");
        }

        return responseMap;

    }
    @Override
    public Map<String, Object> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        Type stringList = new TypeToken<ArrayList<String>>() {
        }.getType();
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String maxEstDeg = cr03Mapper.selectMaxEstDeg(paramMap);
            int newEstDeg = (maxEstDeg != null ? Integer.parseInt(maxEstDeg) : 0) + 1;
            paramMap.put("estDeg", String.valueOf(newEstDeg));
            // issueDate가 'yyyyMMdd' 형식으로 들어온다고 가정합니다
            String issueDateStringOriginal = paramMap.get("pblsDt");
            DateTimeFormatter incomingFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

            // incomingFormat을 사용하여 issueDate를 파싱합니다
            LocalDate issueDate = LocalDate.parse(issueDateStringOriginal, incomingFormat);

            // issueDate에 1개월을 추가합니다
            LocalDate expiryDate = issueDate.plusMonths(1);

            // issueDate와 expiryDate를 java.sql.Date 형식으로 변환합니다
            java.sql.Date sqlIssueDate = java.sql.Date.valueOf(issueDate);
            java.sql.Date sqlExpiryDate = java.sql.Date.valueOf(expiryDate);

            // java.sql.Date를 문자열로 변환합니다
            String issueDateString = sqlIssueDate.toString();
            String expiryDateString = sqlExpiryDate.toString();

            // 정확한 형식의 날짜를 paramMap에 넣습니다
            paramMap.put("estsDt", issueDateString);
            paramMap.put("esteDt", expiryDateString);
            cr03Mapper.insertEst(paramMap);
            List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);


            for (Map<String, String> detailMap : detailArr) {
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", paramMap.get("estNo"));
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));
                cr03Mapper.insertEstDetail(detailMap);
            }
            cm08Svc.copyTreeFile("TB_CR01M01", paramMap, mRequest);

            responseMap.put("resultCode", true); // 결과 코드를 성공으로 설정합니다.
            Map<String, String> newEst = new HashMap<>();
            newEst.put("estNo", paramMap.get("estNo"));
            newEst.put("estDeg", String.valueOf(newEstDeg));
            responseMap.put("newEst", paramMap.get("estDeg"));
            System.out.println(paramMap.get("estDeg")+"최종+"+paramMap.get("estNo"));

        } catch (Exception e) {

        }
        return responseMap;

    }
    public static String removeEmptyObjects(String jsonArrayString) {
        String nullAndEmptyObjectPattern = "(\\{\\s*\\}|null),?";
        String result = jsonArrayString.replaceAll(nullAndEmptyObjectPattern, "");
        // In case the last empty object doesn't have a comma
        if (result.endsWith(",}")) {
            result = result.substring(0, result.length() - 2) + "}";
        }
        return result;
    }
    @Override
    public Map<String, Object> updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {

        Map<String, Object> estData = cr03Mapper.selectEstInfo(paramMap);
        System.out.println("yn값"+estData.get("ordrsYn"));
        Map<String, Object> responseMap = new HashMap<>();
        if ("Y".equals(estData.get("ordrsYn"))) {

            responseMap.put("resultCode",0);
            return responseMap;
        }
        else {


            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            // issueDate가 'yyyyMMdd' 형식으로 들어온다고 가정합니다
            String issueDateStringOriginal = paramMap.get("pblsDt");
            DateTimeFormatter incomingFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

            // incomingFormat을 사용하여 issueDate를 파싱합니다
            LocalDate issueDate = LocalDate.parse(issueDateStringOriginal, incomingFormat);

            // issueDate에 1개월을 추가합니다
            LocalDate expiryDate = issueDate.plusMonths(1);

            // issueDate와 expiryDate를 java.sql.Date 형식으로 변환합니다
            java.sql.Date sqlIssueDate = java.sql.Date.valueOf(issueDate);
            java.sql.Date sqlExpiryDate = java.sql.Date.valueOf(expiryDate);

            // java.sql.Date를 문자열로 변환합니다
            String issueDateString = sqlIssueDate.toString();
            String expiryDateString = sqlExpiryDate.toString();

            // 정확한 형식의 날짜를 paramMap에 넣습니다
            paramMap.put("estsDt", issueDateString);
            paramMap.put("esteDt", expiryDateString);
            System.out.println(paramMap);
            int result = cr03Mapper.updateEst(paramMap);

            // 데이터베이스에서 현재 견적 상세 목록 가져오기
            List<Map<String, Object>> dbDetailListRaw = cr03Mapper.selectEstDetail(paramMap);

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
                    cr03Mapper.deleteEstDetail(dbDetail);
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
                    cr03Mapper.updateEstDetail(estDetail);
                } else {
                    // 견적 상세 삽입
                    estDetail.put("coCd", paramMap.get("coCd"));
                    estDetail.put("estNo", paramMap.get("estNo"));
                    estDetail.put("userId", paramMap.get("userId"));
                    estDetail.put("pgmId", paramMap.get("pgmId"));
                    estDetail.put("estDeg", paramMap.get("estDeg"));

                    cr03Mapper.insertEstDetail(estDetail);
                }
            }
            System.out.println("여기까지2" + paramMap.get("deleteFileArr"));
            String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
            List<String> deleteFileList = Arrays.asList(deleteFileArr);

            for (String fileKey : deleteFileList) {

                cm08Svc.deleteFile(fileKey);
            }
            paramMap.get("fileTrgtKey");
            responseMap.put("resultCode",200);
            Map<String, String> newEst = new HashMap<>();
            newEst.put("estNo", paramMap.get("estNo"));
            newEst.put("estDeg", paramMap.get("estDeg"));
            responseMap.put("updateEst", newEst);

            cm08Svc.uploadTreeFile("TB_CR01M01", paramMap,mRequest);

            return responseMap;
        }
    }
    @Override
    public int updateEstConfirm(Map<String, String> paramMap) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        int result = cr03Mapper.updateEstConfirm(paramMap);

        return result;
    }

    @Override
    public int deleteEst(Map<String, String> paramMap) {
        Map<String, Object> estData = cr03Mapper.selectEstInfo(paramMap);
        System.out.println("yn값"+estData.get("ordrsYn"));
        if ("Y".equals(estData.get("ordrsYn"))) {
            return 0;
        }
        else {
            int result = cr03Mapper.deleteEst(paramMap);
            result += cr03Mapper.deleteAllEstDetails(paramMap);
            return result;
        }
    }
}
