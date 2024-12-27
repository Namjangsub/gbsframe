package com.dksys.biz.user.im.im01.service.impl;

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
import com.dksys.biz.user.im.im01.mapper.IM01Mapper;
import com.dksys.biz.user.im.im01.service.IM01Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;

@Service
@Transactional(rollbackFor = Exception.class)
public class IM01SvcImpl implements IM01Svc {

    @Autowired
    IM01Mapper im01Mapper;

    @Autowired
    QM01Mapper qm01Mapper;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;

    @Autowired
    WB20Svc wb20Svc;

    @Override
    public int selectImprovementListCount(Map<String, String> paramMap) {
        return im01Mapper.selectImprovementListCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectImprovementList(Map<String, String> paramMap) {
        return im01Mapper.selectImprovementList(paramMap);
    }

    @Override
    public Map<String, String> selectImprovement(Map<String, String> paramMap) {
        return im01Mapper.selectImprovement(paramMap);
    }

    @Override
    public int insertImprovement(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {


        // 개선 제안서 등록
        int result = im01Mapper.insertImprovement(paramMap);

        // ---------------------------------------------------------------
        // 결재라인 처리 시작
        // wb20Svc.insertTodoMaster(paramMap); 에서는 결재자 정보를 approvalArr 키값으로 처리하고 있음
        // ---------------------------------------------------------------
        // Gson 객체 생성
//        Gson gson = new Gson();
//        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
//        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
//        }.getType();

//        if (paramMap.containsKey("imprApprovalArr")) {
//
//            // 새로운 리스트 초기화
//            List<Map<String, String>> newdetailMap = new ArrayList<>();
//            List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("imprApprovalArr"), dtlMap);
//            // 각 맵 수정 및 추가
//            for (Map<String, String> dtl : detailMap) {
//
//                // JSON 문자열을 JsonObject로 변환
//                String pgParam = dtl.get("pgParam");
//                JsonObject jsonObject = gson.fromJson(pgParam, JsonObject.class);
//                // "imprvmNo" 값을 "등록값으로"로 변경
//                jsonObject.addProperty("imprvmNo", paramMap.get("imprvmNo"));
//                jsonObject.addProperty("rsltNo", paramMap.get("imprvmNo"));
//
//                dtl.put("todoNo", paramMap.get("imprvmNo")); // "todoNo" 수정
//                dtl.put("todoFileTrgtKey", paramMap.get("imprvmNo")); // "todoNo" 수정
//                dtl.put("pgParam", gson.toJson(jsonObject));
//                newdetailMap.add(dtl); // 수정된 맵 추가
//            }
//            // 결과를 paramMap에 저장
//            paramMap.put("approvalArr", gson.toJson(newdetailMap));
//            // 결제라인 insert
//            result += wb20Svc.insertTodoMaster(paramMap);
//        }

//        if (paramMap.containsKey("execApprovalArr")) {
//            // 새로운 리스트 초기화
//            List<Map<String, String>> newdetailMap2 = new ArrayList<>();
//            List<Map<String, String>> detailMap2 = gsonDtl.fromJson(paramMap.get("execApprovalArr"), dtlMap);
//            // 각 맵 수정 및 추가
//            for (Map<String, String> dtl : detailMap2) {
//
//                // JSON 문자열을 JsonObject로 변환
//                String pgParam = dtl.get("pgParam");
//                JsonObject jsonObject = gson.fromJson(pgParam, JsonObject.class);
//                // "imprvmNo" 값을 "등록값으로"로 변경
//                jsonObject.addProperty("imprvmNo", paramMap.get("imprvmNo"));
//                jsonObject.addProperty("rsltNo", paramMap.get("imprvmNo"));
//
//                dtl.put("todoNo", paramMap.get("imprvmNo")); // "todoNo" 수정
//                dtl.put("todoFileTrgtKey", paramMap.get("imprvmNo")); // "todoNo" 수정
//                dtl.put("pgParam", gson.toJson(jsonObject));
//                newdetailMap2.add(dtl); // 수정된 맵 추가
//            }
//
//            // 결과를 paramMap에 저장
//            paramMap.put("approvalArr", gson.toJson(newdetailMap2));
//
//            // 결제라인 insert
//            result += wb20Svc.insertTodoMaster(paramMap);
//        }
//        // ---------------------------------------------------------------
        // 결재라인 처리 end
        // ---------------------------------------------------------------

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작
        // ---------------------------------------------------------------
        // 개선제안서는 작업권한 체크없음 무조건 등록가능 첨부파일 처리
        cm08Svc.uploadFile("IM0101P01", paramMap.get("imprvmNo"), mRequest);
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------

        return result;
    }

    @Override
    public int updateImprovement(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

        // 개선 제안서 자료 변경 처리
        int result = im01Mapper.updateImprovement(paramMap);

        // ---------------------------------------------------------------
        // 결재라인 처리 시작
        // wb20Svc.insertTodoMaster(paramMap); 에서는 결재자 정보를 approvalArr 키값으로 처리하고 있음
        // ---------------------------------------------------------------

        HashMap<String, String> wbDelChkParam = new HashMap<>();
        wbDelChkParam.put("reqNo", paramMap.get("imprvmNo")); // 개선제안서 번호를 키로 검색함
        im01Mapper.updateDelApprovalList1(wbDelChkParam);// TB_WB20M03 --> SET ETC_FIELD3 = 'DEL' 로 변경처리

        if (paramMap.containsKey("imprApprovalArr")) {
            paramMap.put("approvalArr", paramMap.get("imprApprovalArr"));
            // 결제라인 insert
            result += wb20Svc.insertTodoMaster(paramMap);
        }
        if (paramMap.containsKey("execApprovalArr")) {
            paramMap.put("approvalArr", paramMap.get("execApprovalArr"));
            // 결제라인 insert
            result += wb20Svc.insertTodoMaster(paramMap);
        }
        im01Mapper.deleteDelApprovalgList1(wbDelChkParam);// TB_WB20M03 --> TRIM(ETC_FIELD3) = 'DEL' 정보 삭제처리
        // ---------------------------------------------------------------
        // 결재라인 처리 end
        // ---------------------------------------------------------------

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작
        // ---------------------------------------------------------------
        cm08Svc.uploadFile("IM0101P01", paramMap.get("imprvmNo"), mRequest);
        Gson gson = new Gson();
        String[] deleteFileArr = gson.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);
        if (deleteFileList != null && !deleteFileList.isEmpty()) {
            for (String fileKey : deleteFileList) {
                if (fileKey != null && !fileKey.isEmpty()) {
                    cm08Svc.deleteFile(fileKey);
                }
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------

        return result;
    }

    @Override
    public int deleteImprovement(Map<String, String> paramMap) throws Exception {
        // ---------------------------------------------------------------
        // 첨부 화일 삭제대상 목록 작업
        // WHERE FILE_TRGT_TYP = #{fileTrgtTyp}
        // AND FILE_TRGT_KEY = #{fileTrgtKey}
        // ---------------------------------------------------------------
        List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);

        // ---------------------------------------------------------------
        // 첨부 화일 삭제대상 목록 작업 끝
        // ---------------------------------------------------------------

        int result = im01Mapper.deleteImprovement(paramMap);

        // ---------------------------------------------------------------
        // 결재자 등록정보 일괄 삭제 처리 시작
        // ---------------------------------------------------------------
        paramMap.put("reqNo", paramMap.get("imprvmNo"));
        im01Mapper.deleteImprovementNoAllList(paramMap); // 개선제안서 공유, 결재 삭제
        // ---------------------------------------------------------------
        // 결재자 등록정보 일괄 삭제 끝
        // ---------------------------------------------------------------

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
        // ---------------------------------------------------------------
        if (deleteFileList.size() > 0) {
            for (Map<String, String> deleteDtl : deleteFileList) {
                String fileKey = deleteDtl.get("fileKey");
                if (fileKey != null && !fileKey.isEmpty()) {
                    cm08Svc.deleteFile(fileKey);
                }
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------


        return result;
    }

    @Override
    public List<Map<String, String>> selectImprovementShareUserlst(Map<String, String> paramMap) {
        return im01Mapper.selectImprovementShareUserlst(paramMap);
    }

}