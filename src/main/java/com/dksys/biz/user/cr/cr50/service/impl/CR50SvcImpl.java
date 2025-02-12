package com.dksys.biz.user.cr.cr50.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr50.mapper.CR50Mapper;
import com.dksys.biz.user.cr.cr50.service.CR50Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.mapper.WB20Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR50SvcImpl implements CR50Svc {

    @Autowired
    CR50Mapper cr50Mapper;

    @Autowired
    QM01Mapper QM01Mapper;
    
    @Autowired
    WB20Mapper wb20Mapper;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM08Svc cm08Svc;
    
    @Override
    public int selectPfuListCount(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuListCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectPfuList(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuList(paramMap);
    }
    
    @Override
    public List<Map<String, String>> selectPFUAreaItemList(Map<String, String> paramMap) {
        List<Map<String, String>> result = cr50Mapper.selectPFUAreaItemList(paramMap);
        return result;
    }

    @Override
    public List<Map<String, String>> selectPFUAreaRetriveList(Map<String, String> paramMap) {
        return cr50Mapper.selectPFUAreaRetriveList(paramMap);
    }

    @Override
    public List<Map<String, String>> selectPFUChangedList(Map<String, String> paramMap) {
        return cr50Mapper.selectPFUChangedList(paramMap);
    }

    @Override
    public Map<String, String> selectSalesCdInfo(Map<String, String> paramMap) {
        Map<String, String> result = cr50Mapper.selectSalesCdInfo(paramMap);
        return result;
    }

    @Override
    public Map<String, String> selectPfuInfo(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuInfo(paramMap);
    }

    @Override
    public List<Map<String, String>> selectPfuInfoSalesCdList(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuInfoSalesCdList(paramMap);
    }

//  @Override
//  public Map<String, String> selectPfuClobInfo(Map<String, String> paramMap) {
//      return cr50Mapper.selectPfuClobInfo(paramMap);
//  }

    @Override
    public List<Map<String, String>> selectStdPfuClobInfo(Map<String, String> paramMap) {
        return cr50Mapper.selectStdPfuClobInfo(paramMap);
    }

    @Override
    public int insertPfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();

        // ---------------------------------------------------------------
        // 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        // 필수값 : jobType, userId, comonCd
        // ---------------------------------------------------------------
        List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
        if (uploadFileList.size() > 0) {
            // 접근 권한 없으면 Exception 발생
            paramMap.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(paramMap);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 끝
        // ---------------------------------------------------------------
        int result = cr50Mapper.insertPfu(paramMap);
        String newPfuSeq = paramMap.get("fileTrgtKey");

        String sysCreateDttm = cr50Mapper.selectSystemCreateDttm(paramMap);
        int newSeq = 0;
        for (int i = 1; i <= 6; i++) {
            // a01ListRow, a02ListRow, a03ListRow, a04ListRow를 동적으로 가져옵니다.
            String key = "a0" + i + "ListRow"; // "a01ListRow", "a02ListRow", ...
            List<Map<String, String>> aListArr = gsonDtl.fromJson(paramMap.get(key), dtlMap);

            // 정보 저장
            if (aListArr != null && !aListArr.isEmpty()) {
                for (Map<String, String> detailMap : aListArr) {
                    detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));

                    newSeq += 1;
                    detailMap.put("pfuSeq", Integer.toString(newSeq));
                    detailMap.put("sortNo", Integer.toString(newSeq));

                    String area = detailMap.get("partDiv");
                    String resultString = area + String.format("%02d", newSeq);
                    detailMap.put("partId", resultString);

                    detailMap.put("creatId", paramMap.get("userId"));
                    detailMap.put("creatDttm", sysCreateDttm);
                    detailMap.put("creatPgm", paramMap.get("pgmId"));
                    detailMap.put("udtId", "");
                    detailMap.put("udtDttm", "");
                    detailMap.put("udtPgm", "");

                    result += cr50Mapper.insertPfuArea(detailMap);
                }
            }
        }

        // SalesCd ListRow를 가져옵니다.
        List<Map<String, String>> salesCdListArr = gsonDtl.fromJson(paramMap.get("salesCdListRow"), dtlMap);

        // 정보 저장
        if (salesCdListArr != null && !salesCdListArr.isEmpty()) {
            for (Map<String, String> detailMap : salesCdListArr) {
                detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                detailMap.put("creatId", paramMap.get("userId"));
                detailMap.put("creatDttm", sysCreateDttm);
                detailMap.put("creatPgm", paramMap.get("pgmId"));

                result += cr50Mapper.insertPfuSalesCd(detailMap);
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
        // ---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
            paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
            paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            cm08Svc.uploadFile(paramMap, mRequest);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------

        // ---------------------------------------------------------------
        // 결재처리.
        // ---------------------------------------------------------------
        String ordrsNo = paramMap.get("ordrsNo");
        String salesCd = paramMap.get("salesCd");
        String fileTrgtKey = paramMap.get("fileTrgtKey");
        String coCd = paramMap.get("coCd");
        String pgmId = paramMap.get("pgmId");
        String userId = paramMap.get("userId");

        paramMap.put("reqNo", fileTrgtKey);
        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
        if (!sharngChk.isEmpty()) {
            QM01Mapper.deleteWbsSharngList(paramMap); 
        }


        // Process sharing data
        processInformation(paramMap.get("rowSharngListArr"), "T", QM01Mapper::insertWbsSharngList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        // Process approval data
        processInformation(paramMap.get("rowApprovalListArr"), "S", QM01Mapper::insertWbsApprovalList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        return result;
    }

    private String createPgParam(String actionType, String fileTrgtKey, String coCd, String ordrsNo, String salesCd) {
        return String.format("{\"actionType\":\"%s\",\"fileTrgtKey\":\"%s\",\"coCd\":\"%s\",\"ordrsNo\":\"%s\",\"salesCd\":\"%s\"}", 
                             actionType, fileTrgtKey, coCd, ordrsNo, salesCd);
    }


    private void processInformation(String jsonData, String actionType, Consumer<Map<String, String>> insertFunction,
                                    String fileTrgtKey, String coCd, String ordrsNo, String salesCd, String pgmId, String userId, String histNo) {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        List<Map<String, String>> list = gsonDtl.fromJson(jsonData, dtlMap);
        if (list != null && !list.isEmpty()) {
            int i = 0;
            for (Map<String, String> map : list) {
//                try {
                    map.put("reqNo", fileTrgtKey);
                    map.put("salesCd", salesCd);
                    map.put("fileTrgtKey", fileTrgtKey);
                    map.put("pgmId", pgmId);
                    map.put("userId", userId);
                    map.put("usrNm", userId);
                    map.put("histNo", histNo);
                    map.put("todoCoCd", coCd);
                    map.put("sanCtnSn", Integer.toString(++i));
                    map.put("pgParam", createPgParam(actionType, fileTrgtKey, coCd, ordrsNo, salesCd));
                    map.put("todoTitle", fileTrgtKey + ", " + map.get("todoTitle"));
                    QM01Mapper.insertWbsSharngList(map);
//                } catch (Exception e) {
//                    System.out.println("error2: " + e.getMessage());
//                }
            }
        }
    }

    @Override
    public int updatePfu(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
//      Gson gson = new Gson();
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();

        // ---------------------------------------------------------------
        // 첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        // 필수값 : jobType, userId, comonCd
        // ---------------------------------------------------------------
        HashMap<String, String> param = new HashMap<>();
        param.put("userId", paramMap.get("userId"));
        param.put("comonCd", paramMap.get("comonCd")); // 프로트엔드에 넘어온 화일 저장 위치 정보

        List<Map<String, String>> uploadFileList = gsonDtl.fromJson(paramMap.get("uploadFileArr"), dtlMap);
        if (uploadFileList.size() > 0) {
            // 접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
            param.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(param);
        }
        String[] deleteFileArr = gsonDtl.fromJson(paramMap.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);
        for (String fileKey : deleteFileList) { // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
            Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
            // 접근 권한 없으면 Exception 발생
            param.put("comonCd", fileInfo.get("comonCd")); // 삭제할 파일이 보관된 저장 위치 정보
            param.put("jobType", "fileDelete");
            cm15Svc.selectFileAuthCheck(param);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 끝
        // ---------------------------------------------------------------

        int result = cr50Mapper.updatePfu(paramMap);

        int deleteCnt = cr50Mapper.deletePfuAreaAll(paramMap); // 동록된 사양 일괄 삭제 처리

//      List<Map<String, String>> a01ListArr = gsonDtl.fromJson(paramMap.get("a01ListRow"), dtlMap);
//      //Area01 정보 저장
//      if (a01ListArr != null && !a01ListArr.isEmpty()) {
//          for (Map<String, String> detailMap : a01ListArr) {
//                  detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
//                  Object indexObj = paramMap.get("__index");
//                  int tempSeq = (indexObj instanceof Integer) ? (Integer) indexObj : 0;
//                  int newSeq = tempSeq + 1;
//                  detailMap.put("pfuSeq", Integer.toString(newSeq)) ;
//                  
//                  String area = (String) paramMap.get("partDiv");
//                  String formattedSeq = String.format("%02d", tempSeq);
//                  String resultString = area + formattedSeq;
//                  detailMap.put("partId", resultString);
//                  
//                  detailMap.put("userId", paramMap.get("userId"));
//                  detailMap.put("pgmId", paramMap.get("pgmId"));
//                  
//                  result += cr50Mapper.insertPfuArea(detailMap);
//          }
//      }

        String sysCreateDttm = cr50Mapper.selectSystemCreateDttm(paramMap);
        int newSeq = 0;
        for (int i = 1; i <= 6; i++) {
            // a01ListRow, a02ListRow, a03ListRow, a04ListRow를 동적으로 가져옵니다.
            String key = "a0" + i + "ListRow"; // "a01ListRow", "a02ListRow", ...
            List<Map<String, String>> aListArr = gsonDtl.fromJson(paramMap.get(key), dtlMap);

            // 정보 저장
            if (aListArr != null && !aListArr.isEmpty()) {
                for (Map<String, String> detailMap : aListArr) {
                    detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));

//                  Object indexObj = detailMap.get("__index");
//                  int tempSeq = (indexObj instanceof Integer) ? (Integer) indexObj : 0;

                    newSeq += 1;
                    detailMap.put("pfuSeq", Integer.toString(newSeq));
                    detailMap.put("sortNo", Integer.toString(newSeq));

                    String area = detailMap.get("partDiv");
                    String resultString = area + String.format("%02d", newSeq);
                    detailMap.put("partId", resultString);

                    String originData = detailMap.get("originData");
                    String checkData = detailMap.get("checkData");
                    if ("I".equals(detailMap.get("updChk"))) { // 추가항목 추가항목은 신규 Insert 임.
                        detailMap.put("creatId", paramMap.get("userId"));
                        detailMap.put("creatDttm", sysCreateDttm);
                        detailMap.put("creatPgm", paramMap.get("pgmId"));
                        detailMap.put("udtId", "");
                        detailMap.put("udtPgm", "");
                        detailMap.put("udtDttm", "");
                    } else if (originData.equals(checkData)) { // 변경 안됨
//                      detailMap.put("creatId", detailMap.get("userId"));
//                      detailMap.put("creatDttm", detailMap.get("creatDttm"));
//                      detailMap.put("creatPgm", detailMap.get("pgmId"));;
//                      detailMap.put("udtId", "");
//                      detailMap.put("udtPgm", "");
//                      detailMap.put("udtDttm", "");
                    } else { // 변경항목
                        detailMap.put("udtId", paramMap.get("userId"));
                        detailMap.put("udtPgm", paramMap.get("pgmId"));
                        detailMap.put("udtDttm", sysCreateDttm);
                    }

                    result += cr50Mapper.insertPfuArea(detailMap); // 삭제하고 전부 다시 등록처리함. (일련번호 또는 정렬순서)
                }
            }
        }


        deleteCnt += cr50Mapper.deletePfuSalesCdAll(paramMap); // 동록된 SalesCd 일괄 삭제 처리

        // SalesCd ListRow를 가져옵니다.
        List<Map<String, String>> salesCdListArr = gsonDtl.fromJson(paramMap.get("salesCdListRow"), dtlMap);

        // 정보 저장
        if (salesCdListArr != null && !salesCdListArr.isEmpty()) {
            for (Map<String, String> detailMap : salesCdListArr) {
                detailMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
                String createId = detailMap.get("creatId");
                if (createId == null || "".equals(createId)) { // 추가항목 추가항목은 신규 Insert 임.
                    detailMap.put("creatId", paramMap.get("userId"));
                    detailMap.put("creatDttm", sysCreateDttm);
                    detailMap.put("creatPgm", paramMap.get("pgmId"));
                    detailMap.put("udtId", "");
                    detailMap.put("udtPgm", "");
                    detailMap.put("udtDttm", "");
                } else {
                    detailMap.put("udtId", paramMap.get("userId"));
                    detailMap.put("udtDttm", sysCreateDttm);
                    detailMap.put("udtPgm", paramMap.get("pgmId"));
                }

                result += cr50Mapper.insertPfuSalesCd(detailMap);
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작
        // ---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
            paramMap.put("fileTrgtTyp", paramMap.get("pgmId"));
            paramMap.put("fileTrgtKey", paramMap.get("fileTrgtKey"));
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        for (String fileKey : deleteFileList) {
            cm08Svc.deleteFile(fileKey);
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------

        // ---------------------------------------------------------------
        // 결재처리.
        // ---------------------------------------------------------------
        String ordrsNo = paramMap.get("ordrsNo");
        String salesCd = paramMap.get("salesCd");
        String fileTrgtKey = paramMap.get("fileTrgtKey");
        String coCd = paramMap.get("coCd");
        String pgmId = paramMap.get("pgmId");
        String userId = paramMap.get("userId");

        paramMap.put("reqNo", fileTrgtKey);
        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap); 
        if (!sharngChk.isEmpty()) {
            QM01Mapper.deleteWbsSharngList(paramMap); 
        }


        // Process sharing data
        processInformation(paramMap.get("rowSharngListArr"), "T", QM01Mapper::insertWbsSharngList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        // Process approval data
        processInformation(paramMap.get("rowApprovalListArr"), "S", QM01Mapper::insertWbsApprovalList, fileTrgtKey, coCd, ordrsNo, salesCd, pgmId, userId, "1");

        return result;
    }

    @Override
    public int deletePfuNo(Map<String, String> paramMap) throws Exception {
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
        // 필수값 : jobType, userId, comonCd
        // ---------------------------------------------------------------
        List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
        HashMap<String, String> param = new HashMap<>();
        param.put("jobType", "fileDelete");
        param.put("userId", paramMap.get("userId"));
        if (deleteFileList.size() > 0) {
            for (Map<String, String> dtl : deleteFileList) {
                // 접근 권한 없으면 Exception 발생
                param.put("comonCd", dtl.get("comonCd"));

                cm15Svc.selectFileAuthCheck(param);
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 끝
        // ---------------------------------------------------------------

        // 1명이라도 결재완료이면 'Y' 아니면 'N'
        String deleteCheck = cr50Mapper.selectPfuDeleteCheck(paramMap);
        if ("Y".equals(deleteCheck)) {
            // 한명이라도 결재가 완료되면 삭제는 불가능합니다.
            throw new Exception("결재완료건이 있습니다. 확인바랍니다.");
        }

        int result = cr50Mapper.deletePfuNo(paramMap);
//          //상세내역 삭제
        result += cr50Mapper.deletePfuAreaAll(paramMap);
        // SalesCd 삭제
        result += cr50Mapper.deletePfuSalesCdAll(paramMap);

        // 결재요청정보 삭제처리
        result += wb20Mapper.deleteAllTodoMaster(paramMap);

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
        // ---------------------------------------------------------------
        if (deleteFileList.size() > 0) {
            for (Map<String, String> deleteDtl : deleteFileList) {
                String fileKey = deleteDtl.get("fileKey").toString();
                cm08Svc.deleteFile(fileKey);
            }
        }
        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------
        return result;
    }


    // PFU복사부분 시작
    @Override
    public int copy_cr50(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();
        HashMap<String, String> param = new HashMap<>();
        param.put("userId", paramMap.get("userId"));

        // 데이터처리 시작
        int result = 0;
        int resultDel = 0;

        // 상세수정
        List<Map<String, String>> dtlParam = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);
        for (Map<String, String> dtl : dtlParam) {
            String dataChk = dtl.get("dataChk").toString();
            // "dataChk" 값을 확인하여 "I"인 경우 세부정보를 삽입
            if ("I".equals(dataChk)) {
                // 데이터 처리
//                  resultDel = cr50Mapper.delete_cr50_master(dtl);
//                  resultDel = cr50Mapper.delete_cr50_detail(dtl);
                result += cr50Mapper.copy_cr50_master(dtl); // TB_CR50M01 마스터 정보 복사
                result += cr50Mapper.copy_cr50_detail(dtl); // TB_CR50D01 상세내역 복사
                result += cr50Mapper.copy_cr50_salescd(dtl); // TB_CR50D02 salesCd내역 복사
            }
        }
        // 데이터 처리 끝
        return result;
    }

    @Override
    public int selectPfuCopyTargetListCount(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuCopyTargetListCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectPfuCopyTargetList(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuCopyTargetList(paramMap);
    }

    @Override
    public int selectPfuIsThereListCount(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuIsThereListCount(paramMap);
    }


    @Override
    public List<Map<String, String>> selectTagetSalesCodeList(Map<String, String> paramMap) {
        return cr50Mapper.selectTagetSalesCodeList(paramMap);
    }

    @Override
    public List<Map<String, String>> selectPfuReferenceTargetList(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuReferenceTargetList(paramMap);
    }

    // 그리드 카운트
    @Override
    public int selectPfuReferenceTargetListCount(Map<String, String> paramMap) {
        return cr50Mapper.selectPfuReferenceTargetListCount(paramMap);
    }

    @Override
    public List<Map<String, String>> selectIssueReferenceList(Map<String, String> paramMap) {
        return cr50Mapper.selectIssueReferenceList(paramMap);
    }

    @Override
    public List<Map<String, String>> selectImprovementReferenceList(Map<String, String> paramMap) {
        return cr50Mapper.selectImprovementReferenceList(paramMap);
    }

    @Override
    public int updatePfuVersionUpProcess(Map<String, String> paramMap) throws Exception {

        // 버전업시 첨부 파일 처리에 대한 copy 기능 구현 필요
        //
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
        // 필수값 : jobType, userId, comonCd
        // ---------------------------------------------------------------
//        List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
//        HashMap<String, String> param = new HashMap<>();
//        param.put("jobType", "fileDelete");
//        param.put("userId", paramMap.get("userId"));
//        if (deleteFileList.size() > 0) {
//            for (Map<String, String> dtl : deleteFileList) {
//                // 접근 권한 없으면 Exception 발생
//                param.put("comonCd", dtl.get("comonCd"));
//
//                cm15Svc.selectFileAuthCheck(param);
//            }
//        }
        // ---------------------------------------------------------------
        // 첨부 화일 권한체크 끝
        // ---------------------------------------------------------------

        // 1명이라도 결재완료이면 'Y' 아니면 'N'
//        String deleteCheck = cr50Mapper.selectPfuDeleteCheck(paramMap);
//        if ("Y".equals(deleteCheck)) {
//            // 한명이라도 결재가 완료되면 삭제는 불가능합니다.
//            throw new Exception("결재완료건이 있습니다. 확인바랍니다.");
//        }

        // 버젼업 작업 진행 내역
        // 1. TB_CR50M01의 자료를 TB_CR50M01_HIST에 복사
        // 2. TB_CR50D01의 자료를 TB_CR50D01_HIST에 복사 TB_CR50M01의 VER_NO 적용
        // 3. TB_CR50D02의 자료를 TB_CR50D02_HIST에 복사 TB_CR50M01의 VER_NO 적용
        // 4. TB_CR50M01의 VER_NO + 1 로 변경함.
        // 5. 결재정보는 버젼별로 관리됨으로 조회 안됨.

        // Master 이력
        int result = cr50Mapper.insertPfuMasterHistory(paramMap);
        // 상세내역 이력
        result += cr50Mapper.insertPfuDetailHistory(paramMap);
        // SalesCd 이력
        result += cr50Mapper.insertPfuSalesCdHistory(paramMap);

        // MasterVerNo + 1
        result += cr50Mapper.updatePfuMasterVersionUp(paramMap);
        // 결재요청정보 삭제처리
//        result += cr50Mapper.insertPfuTodoMaster(paramMap);

        // ---------------------------------------------------------------
        // 첨부 화일 처리 시작 (처음 등록시에는 화일 삭제할게 없음)
        // ---------------------------------------------------------------
//        if (deleteFileList.size() > 0) {
//            for (Map<String, String> deleteDtl : deleteFileList) {
//                String fileKey = deleteDtl.get("fileKey").toString();
//                cm08Svc.deleteFile(fileKey);
//            }
//        }
//        // ---------------------------------------------------------------
        // 첨부 화일 처리 끝
        // ---------------------------------------------------------------
        return result;
    }

}
