package com.dksys.biz.user.sm.sm04.service.impl;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.sm.sm04.mapper.SM04Mapper;
import com.dksys.biz.user.sm.sm04.service.SM04Svc;
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
public class SM04Svcmpl implements SM04Svc {
    private static final Logger logger = LoggerFactory.getLogger(SM04Svcmpl.class);

    @Autowired
    SM04Mapper sm04Mapper;
    @Autowired
    CM08Mapper cm08Mapper;
	@Autowired
	CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;

	/* @Override */
    public String selectMaxEstNo(Map<String, String> paramMap) {

        return sm04Mapper.selectMaxEstNo(paramMap);
    }

//    @Override
    public String selectMaxEstDeg(Map<String, String> paramMap) {

        return sm04Mapper.selectMaxEstDeg(paramMap);
    }

	/* @Override */
    public int selectIoCount(Map<String, String> param) {
        return sm04Mapper.selectIoCount(param);
    }

    public int selectIoDetailCount(Map<String, String> param) {
        return sm04Mapper.selectIoDetailCount(param);
    }

	/* @Override */
    public List<Map<String, Object>> selectIoList(Map<String, String> param) {

        return sm04Mapper.selectIoList(param);
    }

	/* @Override */
    public List<Map<String, Object>> selectIoDetail(Map<String, String> param) {

        return sm04Mapper.selectIoDetail(param);
    }
    
	/* @Override */
    public List<Map<String, Object>> selectStInfo(Map<String, String> param) {

        return sm04Mapper.selectStInfo(param);
    }

	/* @Override */
    public List<Map<String, Object>> selectWhCd(Map<String, String> param) {

        return sm04Mapper.selectWhCd(param);
    }
    
    @Override
	public int insert_sm04_Info(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
		int result = sm04Mapper.insert_sm04_Info(paramMap);
		
		return result;
	}
    
    
  //DATA INSERT
  	@Override
  	public int insert_sm04(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
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
  		
  		int fileTrgtKey = sm04Mapper.select_sm04_SeqNext(paramMap);
  		paramMap.put("fileTrgtKey", Integer.toString(fileTrgtKey));
  		
  		int result = sm04Mapper.insert_sm04(paramMap);
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
  		return result;
  	}
  	

	

	/* @Override */
    public Map<String, Object> insertEstDeg(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        Type stringList = new TypeToken<ArrayList<String>>() {
        }.getType();
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String maxEstDeg = sm04Mapper.selectMaxEstDeg(paramMap);
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

            sm04Mapper.insert_sm04(paramMap);
            List<Map<String, Object>> detailArr = gson.fromJson(removeEmptyObjects(paramMap.get("detailArr")), mapList);


            for (Map<String, Object> detailMap : detailArr) {
                detailMap.put("coCd", paramMap.get("coCd"));
                detailMap.put("estNo", paramMap.get("estNo"));
                detailMap.put("estDeg", paramMap.get("estDeg"));
                detailMap.put("userId", paramMap.get("userId"));
                detailMap.put("pgmId", paramMap.get("pgmId"));
                detailMap.put("creatId", paramMap.get("userId"));
                detailMap.put("creatPgm", "TB_CR01M01");

                sm04Mapper.insertEstDetail(detailMap);
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


    public int updateEstConfirm(Map<String, String> paramMap) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        int result = sm04Mapper.updateEstConfirm(paramMap);

        return result;
    }

	
}