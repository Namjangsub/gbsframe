package com.dksys.biz.user.cr.cr01.service.impl;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR01Svcmpl implements CR01Svc {
    private static final Logger logger = LoggerFactory.getLogger(CR01Svcmpl.class);

    @Autowired
    CR01Mapper cr01Mapper;
    
    @Autowired
    CM08Mapper cm08Mapper;
    
    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;

    @Override
    public String selectMaxEstNo(Map<String, String> paramMap) {
        return cr01Mapper.selectMaxEstNo(paramMap);
    }

    @Override
    public String selectMaxEstDeg(Map<String, String> paramMap) {
        return cr01Mapper.selectMaxEstDeg(paramMap);
    }

    @Override
    public int selectEstCount(Map<String, String> param) {
        return cr01Mapper.selectEstCount(param);
    }

    @Override
    public int selectEstCountModal(Map<String, String> param) {
        return cr01Mapper.selectEstCountModal(param);
    }

    public int selectEstDetailCount(Map<String, String> param) {
        return cr01Mapper.selectEstDetailCount(param);
    }

    @Override
    public List<Map<String, Object>> selectEstList(Map<String, String> param) {
        return cr01Mapper.selectEstList(param);
    }

    @Override
    public List<Map<String, Object>> selectEstListModal(Map<String, String> param) {
        return cr01Mapper.selectEstListModal(param);
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
    private Map<String, String> processIssueDate(String issueDateStringOriginal) {
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
        // 날짜들을 Map에 넣고 반환합니다
        Map<String, String> dateMap = new HashMap<>();
        dateMap.put("estsDt", issueDateString);
        dateMap.put("esteDt", expiryDateString);

        return dateMap;
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
    		
            String maxEstNo = selectMaxEstNo(paramMap);
            paramMap.put("estNo", maxEstNo);
            Map<String, String> dateMap = processIssueDate(paramMap.get("pblsDt"));
            // 정확한 형식의 날짜를 paramMap에 넣습니다
            paramMap.put("estsDt", dateMap.get("estsDt"));
            paramMap.put("esteDt", dateMap.get("esteDt"));
            // 데이터베이스 작업을 계속합니다
            paramMap.put("creatId", paramMap.get("userId"));
            paramMap.put("creatPgm", "TB_CR01M01");
            cr01Mapper.insertEst(paramMap);

            List<Map<String, Object>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);
            for (Map<String, Object> detailMap : detailArr) {
                System.out.println("여기실행");
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", maxEstNo);
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));
                detailMap.put("creatId", paramMap.get("userId"));
                detailMap.put("creatPgm", "TB_CR01M01");

                cr01Mapper.insertEstDetail(detailMap);
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
            
            responseMap.put("resultCode", true); // 결과 코드를 성공으로 설정합니다.
            Map<String, String> newEst = new HashMap<>();
            newEst.put("estNo", maxEstNo);
            newEst.put("estDeg", paramMap.get("estDeg"));
            responseMap.put("newEst", newEst);
        } catch (Exception e) {
            System.out.println(e + "에러명");
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
            String maxEstDeg = cr01Mapper.selectMaxEstDeg(paramMap);
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
            System.out.println(paramMap.get("userId") + "리턴전값");
            // 정확한 형식의 날짜를 paramMap에 넣습니다
            paramMap.put("estsDt", issueDateString);
            paramMap.put("esteDt", expiryDateString);
            paramMap.put("creatId", paramMap.get("userId"));
            paramMap.put("creatPgm", "TB_CR01M01");

            cr01Mapper.insertEst(paramMap);
            List<Map<String, Object>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);

            for (Map<String, Object> detailMap : detailArr) {
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", paramMap.get("estNo"));
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));
                detailMap.put("creatId", paramMap.get("userId"));
                detailMap.put("creatPgm", "TB_CR01M01");

                cr01Mapper.insertEstDetail(detailMap);
            }
            //  cm08Svc.copyTreeFile("TB_CR01M01", paramMap, mRequest);

            responseMap.put("resultCode", true); // 결과 코드를 성공으로 설정합니다.
            Map<String, String> newEst = new HashMap<>();
            newEst.put("estNo", paramMap.get("estNo"));
            newEst.put("estDeg", String.valueOf(newEstDeg));
            responseMap.put("newEst", paramMap.get("estDeg"));
            System.out.println(paramMap.get("estDeg") + "최종+" + paramMap.get("estNo"));

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
    public Map<String, Object> updateEst(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
		
		for(String fileKey : deleteFileList) {
			// 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
			Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
			//접근 권한 없으면 Exception 발생
			param.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
			param.put("jobType", "fileDelete");
			cm15Svc.selectFileAuthCheck(param);
		}
		//---------------------------------------------------------------
		//첨부 화일 권한체크  끝
		//---------------------------------------------------------------
        
        Map<String, Object> estData = cr01Mapper.selectEstInfo(paramMap);
        System.out.println("yn값" + estData.get("ordrsYn"));
        Map<String, Object> responseMap = new HashMap<>();
        if ("Y".equals(estData.get("ordrsYn"))) {
            responseMap.put("resultCode", 0);
            return responseMap;
        } else {
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

            paramMap.put("udtId", paramMap.get("userId"));
            paramMap.put("udtPgm", "TB_CR01M01");
            int result = cr01Mapper.updateEst(paramMap);

            // 데이터베이스에서 현재 견적 상세 목록 가져오기
            List<Map<String, Object>> dbDetailListRaw = cr01Mapper.selectEstDetail(paramMap);

            // 데이터베이스 목록의 Object를 String으로 변환
            List<Map<String, String>> dbDetailList = dbDetailListRaw.stream()
                    .map(rawMap -> rawMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
                    .collect(Collectors.toList());

            // 클라이언트에서 전달된 견적 상세 목록
            List<Map<String, Object>> detailList = gson.fromJson(paramMap.get("detailArr"), mapList);

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
            }

            ///---------------------------------------------------------------
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
            responseMap.put("resultCode", 200);
            return responseMap;
        }
    }

    public int updateEstConfirm(Map<String, String> paramMap) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        int result = cr01Mapper.updateEstConfirm(paramMap);

        return result;
    }

    @Override
    public int deleteEst(Map<String, String> paramMap) throws Exception {
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

        int returnValue = 200;

        Map<String, Object> estData = cr01Mapper.selectEstInfo(paramMap);
        if ("Y".equals(estData.get("ordrsYn"))) {
            returnValue = 0;
        } else {
            cr01Mapper.deleteEst(paramMap);
            cr01Mapper.deleteAllEstDetails(paramMap);
            returnValue = 200;
        }

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

        return returnValue;
    }
}