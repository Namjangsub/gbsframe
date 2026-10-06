package com.dksys.biz.user.cr.cr02.service.impl;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.bm.bm16.mapper.BM16Mapper;
import com.dksys.biz.admin.cm.cm05.service.CM05Svc;
import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.util.ApprovalLineTypeGuard;
import com.dksys.biz.util.ExceptionThrower;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR02Svcmpl implements CR02Svc {

    @Autowired
    CR01Svc cr01Svc;
    @Autowired
    CR02Mapper cr02Mapper;

    @Autowired
    QM01Mapper QM01Mapper;

    @Autowired
    CM08Mapper cm08Mapper;

    @Autowired
    CM08Svc cm08Svc;

    @Autowired
    CM15Svc cm15Svc;

    @Autowired
    CM05Svc cm05Svc;

    @Autowired
    BM16Mapper bm16Mapper;

    @Autowired
    ExceptionThrower thrower;

    @Autowired
    com.dksys.biz.user.am.am11.service.AM11Svc am11Svc;

    @Autowired
    com.dksys.biz.user.wb.wb20.service.WB20Svc wb20Svc;

    @Override
    public int selectOrdrsCount(Map<String, String> param) {
        return cr02Mapper.selectOrdrsCount(param);
    }

    @Override
    public List<Map<String, Object>> selectOrdrsList(Map<String, String> param) {
        return cr02Mapper.selectOrdrsList(param);
    }

    @Override
    public int selectOrdrsListPopCount(Map<String, String> param) {
        return cr02Mapper.selectOrdrsListPopCount(param);
    }

    @Override
    public List<Map<String, Object>> selectOrdrsListPop(Map<String, String> param) {
        return cr02Mapper.selectOrdrsListPop(param);
    }

    @Override
    public Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap) {
        Map<String, Object> ordrsInfo = cr02Mapper.selectOrdrsInfo(paramMap);
        return ordrsInfo;
    }

    @Override
    public Map<String, Object> selectOrdrsWithEst(Map<String, String> params) {
        return cr02Mapper.selectOrdrsWithEst(params);
    }


    @Override
    public String selectMaxOrdrsNo(Map<String, String> param) {
        return cr02Mapper.selectMaxOrdrsNo(param);
    }

    @Override
    public String selectAsMaxOrdrsNo(Map<String, String> param) {
        return cr02Mapper.selectAsMaxOrdrsNo(param);
    }

    @Override
    public String selectItemDivEtc(Map<String, String> param) {
        return cr02Mapper.selectItemDivEtc(param);
    }

    @Override
    public Map<String, String> insertOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        Map rtnMap = new HashMap();
            //---------------------------------------------------------------
            //첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
            //   필수값 :  jobType, userId, comonCd
            //---------------------------------------------------------------
            List<Map<String, String>> uploadFileList = gson.fromJson(param.get("uploadFileArr"), dtlMap);
            if (uploadFileList.size() > 0) {
                //접근 권한 없으면 Exception 발생
                param.put("jobType", "fileUp");
                cm15Svc.selectFileAuthCheck(param);
            }
            //---------------------------------------------------------------
            //첨부 화일 권한체크  끝
            //---------------------------------------------------------------

        param.put("estNo", param.get("estNoOrdrs"));
        //param.get("newOrdrsNo") => 회사 == 'TRN' && 거래처 = '104' 일때
        //수주구분이 A/S 일때 수주번호에 AS23024 번호 만들기
        if("".equals(param.get("newOrdrsNo")) || param.get("newOrdrsNo") == null) {
            //단일번호체계로 가면수 수주구분으로 분류하면 문제가 있나?
//            if (param.get("ordrsDiv").equals("ORDRSDIV1") || param.get("ordrsDiv").equals("ORDRSDIV9")) {
                param.put("ordrsNo", selectMaxOrdrsNo(param));
//            }
//            else {
//              String orderNo = selectAsMaxOrdrsNo(param);
//              param.put("ordrsNo", "AS"+orderNo);
//            }
        }else {
            param.put("ordrsNo", param.get("newOrdrsNo"));
        }

        rtnMap.put("ordrsNo", param.get("ordrsNo"));// rtnMap에 "ordrsNo"키로 저장

        String fileTrgtKey;
        String OrderSeq = "";
        
        //=============================================================================================================================
		// 프로젝트 자동 생성 Start
		if ("ORDRSDIV2".equals(param.get("ordrsDiv")) || "ORDRSDIV3".equals(param.get("ordrsDiv"))) {

            // param에 insertUpdateAsPrjct 함수 실행에 필요한 키-값 넣기
            List<Map<String, String>> detailArrFirst = gson.fromJson(param.get("detailArr"), mapList);
            param.put("eqpNm", detailArrFirst.get(0).get("eqpNm"));
            param.put("eqpQty", detailArrFirst.get(0).get("ordrsQty"));
            param.put("dudtPlanDt", detailArrFirst.get(0).get("dudtIntendDt").replace("-", ""));

            Map<String, String> param2 = insertUpdateAsPrjct(param);
            param.put("prjctSeq", param2.get("prjctSeq")); // 업데이트한 수주정보를 AS프로젝트에 연결
            param.put("prjctSeqNm", param2.get("prjctSeqNm"));
		}

		// 프로젝트 자동생성 End
		//=============================================================================================================================

        cr01Svc.updateEstConfirm(param);
        cr02Mapper.insertOrdrs(param);
        cr02Mapper.insertOrdrsLgist(param);

        //List<Map<String, String>> planArr = gson.fromJson(removeEmptyObjects(param.get("planArr")), mapList);
        List<Map<String, String>> planArr = gson.fromJson(param.get("planArr"), mapList);
        for (Map<String, String> planMap : planArr) {
            try {
                planMap.put("coCd", param.get("coCd"));
                planMap.put("ordrsNo", param.get("ordrsNo"));
                planMap.put("estNo", param.get("estNo"));
                planMap.put("currCd", param.get("currCd"));
                planMap.put("userId", param.get("userId"));
                planMap.put("pgmId", param.get("pgmId"));
                planMap.put("udtId", param.get("userId"));
                planMap.put("udtPgm", "TB_CR02P01");

                cr02Mapper.insertClmnPlanHis(planMap);
                cr02Mapper.insertClmnPlan(planMap);

            } catch (Exception e) {
                System.out.println("error2" + e.getMessage());
                thrower.throwCommonException("수금정보 추가오류!");

            }
        }

//        List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(param.get("detailArr")), mapList);
        List<Map<String, String>> detailArr = gson.fromJson(param.get("detailArr"), mapList);

		// 설비정보 salesCode 중복 체크
		Set<String> salesCdCheck = new HashSet<>();
		for (Map<String, String> detailMap : detailArr) {
			String salesCd = detailMap.get("salesCd");
			if (salesCd != null && !salesCd.trim().isEmpty()) {
				if (!salesCdCheck.add(salesCd)) {
					// 중복이면 바로 예외 발생시켜 catch로 이동
					throw new RuntimeException("중복된 salesCd 존재가 존재합니다.");
				}
			}
		}

        for (Map<String, String> detailMap : detailArr) {
            try {
                detailMap.put("coCd", param.get("coCd"));
                detailMap.put("ordrsNo", param.get("ordrsNo"));
                detailMap.put("estNo", param.get("estNo"));
                detailMap.put("currCd", param.get("currCd"));
                detailMap.put("userId", param.get("userId"));
                detailMap.put("pgmId", param.get("pgmId"));
                detailMap.put("udtId", param.get("userId"));
                detailMap.put("ordrsClntNm", param.get("ordrsClntNm"));
                detailMap.put("clntPjt", param.get("clntPjt"));
                detailMap.put("udtPgm", "TB_CR02M01");

                //Sales Cd 만들떄 ITEM_DIV의 CODE_ETC 값 추출
                String ItemDoov = (selectItemDivEtc(detailMap));
                String Salad = detailMap.get("salesCd");
				String saveSalesCd = "";
                //입력구분이 '설비'코드 일때 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                if (detailMap.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                    if ("".equals(Salad) || Salad == null || Salad.length() == 0) {
                        // 등록모드에서는 등록처리만 있음(삭제건은 처리하지 않음)
                        OrderSeq = cr02Mapper.selectSalesCdLastNumberPlusOne(param);
                        String newSalesCode = param.get("ordrsNo") + "-" + OrderSeq.trim() + detailMap.get("prdtCd") + ItemDoov;
                        detailMap.put("salesCd", newSalesCode);
						saveSalesCd = newSalesCode;
						if (saveSalesCd != null && saveSalesCd.contains("null")) {
							throw new RuntimeException("설비코드 생성오류입니다. 전산실에 문의해주세요.");
						}
                    }
                }
                cr02Mapper.insertOrdrsDetail(detailMap);

				// 해당 설비에 해당하는 설계BOM, 구매BOM 자료 있는지 확인(중복 등록 방지)
				HashMap<String, String> bomParMap = new HashMap<>();
				bomParMap.put("salesCd", saveSalesCd);
				Map<String, String> selectBomCheck = cr02Mapper.selectBomCheck(bomParMap);
				if (Integer.parseInt(selectBomCheck.get("bm14Cnt")) > 1 || Integer.parseInt(selectBomCheck.get("sm01Cnt")) > 1 ) {
					throw new RuntimeException("설비 ("+ saveSalesCd + ")에 설계BOM 또는 구매BOM에 이미 등록되어 있습니다. 전산실에 문의해주세요.");
				}
            } catch (Exception e) {
                System.out.println("error3" + e.getMessage());
                thrower.throwCommonException("설비&원가 추가오류!");

            }
        }

            //---------------------------------------------------------------
            //첨부 화일 처리 시작  (처음 등록시에는 화일 삭제할게 없음)
            //---------------------------------------------------------------
            if (uploadFileList.size() > 0) {
                param.put("fileTrgtTyp", param.get("pgmId"));
                param.put("fileTrgtKey", param.get("fileTrgtKey")); 
                cm08Svc.uploadFile(param, mRequest);
            }
            //---------------------------------------------------------------
            //첨부 화일 처리  끝
            //---------------------------------------------------------------

            //---------------------------------------------------------------
            //결재처리[1. 수주에서는  SalesCd를 가질수 없음]
            //---------------------------------------------------------------
            param.put("reqNo", param.get("ordrsNo"));
            param.put("salesCd", param.get("ordrsNo"));

            List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(param);
            if (sharngChk.size() > 0) {
                QM01Mapper.deleteWbsSharngList(param);
            }

            String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
            pgParam1 += "\"fileTrgtKey\":\""+ param.get("fileTrgtKey") +"\",";
            pgParam1 += "\"coCd\":\""+ param.get("coCd") +"\",";
            //pgParam1 += "\"salesCd\":\""+ param.get("salesCd") +"\",";
            pgParam1 += "\"ordrsNo\":\""+ param.get("ordrsNo") +"\"}";
            //공유
            Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
            List<Map<String, String>> sharngArr = gson.fromJson(param.get("rowSharngListArr"), stringList2);
            if (sharngArr != null && sharngArr.size() > 0 ) {
                int i = 0;
                for (Map<String, String> sharngMap : sharngArr) {
                    try {
                            String targetUsrNm = sharngMap.get("usrNm");
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = sharngMap.get("todoId");
                            }
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = sharngMap.get("id");
                            }
                            sharngMap.put("usrNm", targetUsrNm);
                            sharngMap.put("todoId", targetUsrNm);
                            if (sharngMap.get("todoCoCd") == null || sharngMap.get("todoCoCd").isEmpty()) {
                                sharngMap.put("todoCoCd", param.get("coCd"));
                            }
                            if (sharngMap.get("coCd") == null || sharngMap.get("coCd").isEmpty()) {
                                sharngMap.put("coCd", param.get("coCd"));
                            }
                            sharngMap.put("reqNo", param.get("ordrsNo"));
                            sharngMap.put("salesCd", param.get("ordrsNo"));
                            sharngMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                            sharngMap.put("pgmId", param.get("pgmId"));
                            sharngMap.put("userId", param.get("userId"));
                            sharngMap.put("histNo", "1");
                            sharngMap.put("sanCtnSn",Integer.toString(i+1));
                            sharngMap.put("pgParam", pgParam1);
                            String shTitle = sharngMap.get("todoTitle");
                            if (shTitle != null && !shTitle.startsWith(param.get("ordrsNo"))) {
                                sharngMap.put("todoTitle", param.get("ordrsNo") +" , " + shTitle);
                            }
                            QM01Mapper.insertWbsSharngList(sharngMap);
                        i++;
                    } catch (Exception e) {
                        System.err.println("insertOrdrs insertWbsSharngList error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
            pgParam2 += "\"fileTrgtKey\":\""+ param.get("fileTrgtKey") +"\",";
            pgParam2 += "\"coCd\":\""+ param.get("coCd") +"\",";
            //pgParam2 += "\"salesCd\":\""+ param.get("salesCd") +"\",";
            pgParam2 += "\"ordrsNo\":\""+ param.get("ordrsNo") +"\"}";
            //결재
            Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
            List<Map<String, String>> approvalArr = gson.fromJson(param.get("rowApprovalListArr"), stringList3);
            approvalArr = ApprovalLineTypeGuard.moveStagePostToEnd(approvalArr, param.get("ordrsNo"));
            if (approvalArr != null && approvalArr.size() > 0 ) {
                int i = 0;
                for (Map<String, String> approvalMap : approvalArr) {
                    try {
                            String targetUsrNm = approvalMap.get("usrNm");
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = approvalMap.get("todoId");
                            }
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = approvalMap.get("id");
                            }
                            approvalMap.put("usrNm", targetUsrNm);
                            approvalMap.put("todoId", targetUsrNm);
                            if (approvalMap.get("todoCoCd") == null || approvalMap.get("todoCoCd").isEmpty()) {
                                approvalMap.put("todoCoCd", param.get("coCd"));
                            }
                            if (approvalMap.get("coCd") == null || approvalMap.get("coCd").isEmpty()) {
                                approvalMap.put("coCd", param.get("coCd"));
                            }
                            approvalMap.put("reqNo", param.get("ordrsNo"));
                            approvalMap.put("salesCd", param.get("ordrsNo"));
                            approvalMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                            approvalMap.put("pgmId", param.get("pgmId"));
                            approvalMap.put("userId", param.get("userId"));
                            approvalMap.put("histNo", "1");
                            approvalMap.put("sanCtnSn",Integer.toString(i+1));
                            approvalMap.put("pgParam", pgParam2);
                            String apTitle = approvalMap.get("todoTitle");
                            if (apTitle != null && !apTitle.startsWith(param.get("ordrsNo"))) {
                                approvalMap.put("todoTitle", param.get("ordrsNo") +" , " + apTitle);
                            }
                            QM01Mapper.insertWbsApprovalList(approvalMap);
                            i++;
                    } catch (Exception e) {
                        System.err.println("insertOrdrs insertWbsApprovalList error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            cr02Mapper.callCopyOrdrs(param); //이력생성

            // AM 전자결재 연동 (정방향)
            try {
                syncOrdrsToAm(param);
            } catch (Exception e) {
                throw new RuntimeException("전자결재(AM) 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
            }

//      if("".equals(param.get("newOrdrsNo")) || param.get("newOrdrsNo") == null) {
//          // 수주일자의 년도가 변경되었을 경우 수주번호를 갱신
//          cr02Mapper.callUpdateOrdrsNo(param);
//      }

        // 수주관리의 정보를 프로젝트 관리에 반영
        // 남장섭 240401 프로젝트 등록후 선택하게 수정
        // param.get("prjctSeq") 값이 있으면 해당 프로젝트에 연결하고, 없으면 프로젝트 신규 생성함.
        // 해당 수주에 프로젝트번호 update 처리
        // if (param.get("prjctSeq").equals("")) {
        //     cr02Mapper.callUpdateProjectMaster(param);
        // }

        return rtnMap;
    }

    @Override
    public int updateOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
		int result = 0;
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
        String OrderSeq = "";

        //---------------------------------------------------------------
        //첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        //   필수값 :  jobType, userId, comonCd
        //---------------------------------------------------------------
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", param.get("userId"));
        paramMap.put("comonCd", param.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
        paramMap.put("coCd", param.get("coCd"));
        paramMap.put("ordrsNo", param.get("ordrsNo"));
        paramMap.put("clntCd", param.get("clntCd"));
        paramMap.put("prjctCd", param.get("prjctCd"));
        paramMap.put("uploadFileArr", param.get("uploadFileArr"));

        List<Map<String, String>> uploadFileList = gson.fromJson(paramMap.get("uploadFileArr"), mapList);
        if (uploadFileList.size() > 0) {
            //접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
            paramMap.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(paramMap);
        }

        String[] deleteFileArr = gson.fromJson(param.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);

        for(String fileKey : deleteFileList) {
            // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
            Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
            //접근 권한 없으면 Exception 발생
            paramMap.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
            paramMap.put("jobType", "fileDelete");
            cm15Svc.selectFileAuthCheck(paramMap);
        }
        //---------------------------------------------------------------
        //첨부 화일 권한체크  끝
        //---------------------------------------------------------------

        //수금정보,  설비&원가 정보, HIST 삭제,
//      cr02Mapper.deleteOrdrsPlan(param);
//      cr02Mapper.deleteOrdrsDetailAll(param);
//      cr02Mapper.deleteOrdrsPlanHis(param);

        String newOrdrsDiv = param.get("newOrdrsDiv");

//      // 건양수주번호 있으면 수주번호는 건양수주번호를 따라감
//        if("".equals(param.get("newOrdrsNo")) || param.get("newOrdrsNo") == null) {
//          // 수주구분이 달라지는 경우
//            if (!newOrdrsDiv.equals(param.get("ordrsDiv"))) {
//              //정상수주면 정상수주번호
//                if (param.get("ordrsDiv").equals("ORDRSDIV1") || param.get("ordrsDiv").equals("ORDRSDIV9")) {
//                  param.put("ordrsNo", selectMaxOrdrsNo(param));
//                } else {
//                  // 그외는 AS수주번호
//                  String orderNo = selectAsMaxOrdrsNo(param);
//                  param.put("ordrsNo", "AS"+orderNo);
//                }
//            }
//
//            // 건양수주번호가 있다가 사라진 경우 수주번호를 새로 체번해야한다.
//            if (!"".equals(param.get("oldOrdrsNo")) && param.get("oldOrdrsNo") != null) {
//              //정상수주면 정상수주번호
//              if (param.get("ordrsDiv").equals("ORDRSDIV1") || param.get("ordrsDiv").equals("ORDRSDIV9")) {
//                  param.put("ordrsNo", selectMaxOrdrsNo(param));
//              } else {
//                  // 그외는 AS수주번호
//                  String orderNo = selectAsMaxOrdrsNo(param);
//                  param.put("ordrsNo", "AS"+orderNo);
//              }
//            }
//        }else {//          // 건양수주번호 있으면 수주번호는 건양수주번호를 따라감
//          param.put("ordrsNo", param.get("newOrdrsNo"));
//        }

        //=============================================================================================================================
		// 프로젝트 자동 수정 로직 Start

        Map<String, String> originalOrdrsInfo = gson.fromJson(param.get("originalOrdrsInfo"), Map.class); // 수정 전 데이터

        // =============================================================================================================================변경전 수정 시작
        Map<String, String> beforePrjctInfo = bm16Mapper.selectPrjctInfo(originalOrdrsInfo);
        DateTimeFormatter projectDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime asProjectApplyDate = LocalDateTime.parse("2024-12-27 00:00:00", projectDateFormatter);
        // 수정 전의 값이 AS이고 기존 프로젝트가 2024-12-27 이후 생성된 경우에만 차감/삭제 처리함.
        // 조건 보완 isBeforeCnd1 + isBeforeCnd2 = isBefore
        boolean isBeforeCnd1 = (("ORDRSDIV2".equals(originalOrdrsInfo.get("ordrsDiv")) || "ORDRSDIV3".equals(originalOrdrsInfo.get("ordrsDiv"))));
        boolean isBeforeCnd2 = beforePrjctInfo != null
                && beforePrjctInfo.get("creatDttm") != null
                && LocalDateTime.parse(beforePrjctInfo.get("creatDttm"), projectDateFormatter).isAfter(asProjectApplyDate);
        boolean isBefore = isBeforeCnd1 && isBeforeCnd2;
        // 수정 전의 값이 AS 이고 2024-12-27일이후인 경우에만 수정 및 삭제 처리함
        if (isBefore) {

            originalOrdrsInfo.put("pgmId", param.get("pgmId"));
            originalOrdrsInfo.put("userId", param.get("userId"));
            // 수정전 수주금액 마이너스 처리하기위함
            originalOrdrsInfo.put("newOrdrsAmt", String.valueOf(Double.parseDouble(originalOrdrsInfo.get("exchangeAmt")) * -1));
            // 해당 프로젝트 조회(prjctSeq) 후 업데이트 및 삭제
            List<Map<String, String>> selectPrdtList = bm16Mapper.selectPrdtList(originalOrdrsInfo);    // 프로젝트 조회
            // 조회한 프로젝트가 1개이면서 전후 금액이 값으면 프로젝트 삭제
            if (selectPrdtList.size() == 1 && param.get("exchangeAmt").equals(originalOrdrsInfo.get("exchangeAmt"))) {  
                bm16Mapper.deletePrjct(originalOrdrsInfo);
            } else {
                bm16Mapper.updateAsOrdrsOnlyPrjct(originalOrdrsInfo);
            }
        }
        // ============================================================================================================================= 변경전 수정 끝

        // ============================================================================================================================= 변경후 수정 시작
        Map<String, String> afterPrjctInfo = bm16Mapper.selectPrjctInfo(param);
        // 수정후의 값이 AS 이고 2024-12-27일이후인 경우에만 추가 등록 처리함.
        // 조건 보완 isAfterCnd1 + isAfterCnd2 = isAfter
        boolean isAfterCnd1 = (("ORDRSDIV2".equals(param.get("ordrsDiv")) || "ORDRSDIV3".equals(param.get("ordrsDiv"))));
        boolean isAfterCnd2 = afterPrjctInfo != null
                && afterPrjctInfo.get("creatDttm") != null
                && LocalDateTime.parse(afterPrjctInfo.get("creatDttm"), projectDateFormatter).isAfter(asProjectApplyDate);
        boolean isAfter = isAfterCnd1 && isAfterCnd2;

        // 수정 후의 값이 AS 이고 2024-12-27일이후인 겨우에만 추가 등록 처리함.
        if (isAfter) {
            // param에 insertUpdateAsPrjct 함수 실행에 필요한 키-값 넣기
            List<Map<String, String>> detailArrFirst = gson.fromJson(param.get("detailArr"), mapList);
            param.put("eqpNm", detailArrFirst.get(0).get("eqpNm"));
            param.put("eqpQty", detailArrFirst.get(0).get("ordrsQty"));
            param.put("dudtPlanDt", detailArrFirst.get(0).get("dudtIntendDt").replace("-", ""));

            Map<String, String> param2 = insertUpdateAsPrjct(param);
            param.put("prjctSeq", param2.get("prjctSeq")); // 업데이트한 수주정보를 AS프로젝트에 연결
            param.put("prjctSeqNm", param2.get("prjctSeqNm"));
        }
        // ============================================================================================================================= 변경후 수정 끝
        
        // 프로젝트 자동 수정 로직 End
        // =============================================================================================================================

        param.put("udtId", param.get("userId"));
        param.put("udtPgm", "TB_CR02M01");
        param.put("estNo", param.get("estNoOrdrs"));
        if(!"".equals(param.get("estNoOrdrs")) || param.get("estNoOrdrs") != null) {
            cr01Svc.updateEstConfirm(param);
        }
        result += cr02Mapper.updateOrdrs(param);
        cr02Mapper.mergeOrdrsLgist(param);

        //////////////수금정보  update 수정////////
        updateOrdrsPmntPlanProcess(param, mRequest );

        ////////////////////////////////////////////////////////////////////////////////////
        //이하 문장은 위의 함수로 대체함.  남장섭 -->Strat
        ////////////////////////////////////////////////////////////////////////////////////
        // //데이터 처리 시작
        // int clmnPlanDegKey = cr02Mapper.selectDegKey(param);
        // param.put("clmnPlanDegKey", Integer.toString(clmnPlanDegKey));

        // //DB 저장된 수금정보 가져오기
        // List<Map<String, Object>> dbPlanListRaw = cr02Mapper.selectPmntPlan(param);

        // //Convert Object to String
        // List<Map<String, String>> dbPlanList = dbPlanListRaw.stream()
        // .map(rawMap -> rawMap.entrySet().stream()
        //         .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
        // .collect(Collectors.toList());

        // ///////////////


        // //수금정보 처리
        // List<Map<String, String>> planArr = gson.fromJson(param.get("planArr"), mapList);
        // //1. 수정부분
        // for (Map<String, String> dbPlan : dbPlanList) {
        //  boolean found = false;
        //     for (Map<String, String> plan : planArr) {
        //         if (dbPlan.get("clmnPlanSeq").equals(plan.get("clmnPlanSeq"))) {
        //             found = true;
        //             break;
        //         }
        //     }
        //     if (!found) {
        //         cr02Mapper.deleteOrdrsPlanEx(dbPlan);
        //     }
        // }//1.

        // for (Map<String, String> planMap : planArr) {
        //     try {
        //      boolean found = false; //1.
        //         for (Map<String, String> dbPlan : dbPlanList) {
        //             if (dbPlan.get("clmnPlanSeq").equals(planMap.get("clmnPlanSeq"))) {
        //                 found = true;
        //                 break;
        //             }
        //         }//1.
        //         planMap.put("coCd", param.get("coCd"));
        //         planMap.put("ordrsNo", param.get("ordrsNo"));
        //         planMap.put("estNo", param.get("estNo"));
        //         planMap.put("currCd", param.get("currCd"));
        //         planMap.put("userId", param.get("userId"));
        //         planMap.put("pgmId", param.get("pgmId"));
        //         planMap.put("udtId", param.get("userId"));
        //         planMap.put("udtPgm", "TB_CR02P01");

        //         planMap.put("clmnPlanDegKey", param.get("clmnPlanDegKey"));

        //      if (found) {
        //          // Update plan
        //          cr02Mapper.updateClmnPlan(planMap);
        //          cr02Mapper.insertUpdatePlanHis(planMap);
        //      } else {
        //          // Insert new plan
        //          //cr02Mapper.updateClmnPlan(planMap);


        //          cr02Mapper.insertClmnPlan(planMap);
        //          cr02Mapper.insertUpdatePlanHis(planMap);
        //      }

        //     } catch (Exception e) {
        //         System.out.println("error2" + e.getMessage());
        //      thrower.throwCommonException("수금정보 수정오류!");
        //     }
        // }

        ////////////////////////////////////////////////////////////////////////////////////
        //이하 문장은 위의 함수로 대체함.  남장섭 -->End
        ////////////////////////////////////////////////////////////////////////////////////

        ///////////////////설비&원가 정보 update /////////////////////
        // 설비&원가 정보 처리
        //1. 설비삭제내역 처리
        List<Map<String, String>> deleteArr = gson.fromJson(param.get("gridOrdrsDetaildeleteArr"), mapList);
        for (Map<String, String> dbDelete : deleteArr) {
            //프론트단에서 복사 붙여넣기로 추가한것을 삭제할때 반드시 ORDRS_SEQ값을 clear 0으로 초기화 시킬것
            //아니면 기존 설비레코드 삭제처리됨.
            cr02Mapper.deleteOrdrsDetail(dbDelete);
        }


     	//2. 설비정보 추가, 수정 건 처리
        List<Map<String, String>> detailArr = gson.fromJson(param.get("detailArr"), mapList);
		// 설비정보 salesCode 중복 체크
		Set<String> salesCdCheck = new HashSet<>();
		for (Map<String, String> detailMap : detailArr) {
			String salesCd = detailMap.get("salesCd");
			if (salesCd != null && !salesCd.trim().isEmpty()) {
				if (!salesCdCheck.add(salesCd)) {
					// 중복이면 바로 예외 발생시켜 catch로 이동
					throw new RuntimeException("중복된 salesCd 존재가 존재합니다.");
				}
			}
			
		}
        String jobType = "";  //추가="C",  수정 = "U" 코드 저장용
        for (Map<String, String> detailMap : detailArr) {
            try {
                Object jobTypeObject = detailMap.get("cudCheck");
				String saveSalesCd = "";
                if (jobTypeObject != null) {
                    jobType = detailMap.get("cudCheck");
                    detailMap.put("coCd", param.get("coCd"));
                    detailMap.put("ordrsNo", param.get("ordrsNo"));
                    detailMap.put("estNo", param.get("estNo"));
                    detailMap.put("currCd", param.get("currCd"));
                    detailMap.put("userId", param.get("userId"));
                    detailMap.put("pgmId", param.get("pgmId"));
                    detailMap.put("udtId", param.get("userId"));
                    detailMap.put("ordrsClntNm", param.get("ordrsClntNm"));
                    detailMap.put("clntPjt", param.get("clntPjt"));
                    detailMap.put("udtPgm", "TB_CR02M01");

                    //Sales Cd 만들떄 ITEM_DIV의 CODE_ETC 값 추출
                    String ItemDoov = (selectItemDivEtc(detailMap));
                    //입력구분이 '설비'코드 일때 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                    //프론트엔드에서 cudCheck 값은 추가면 C코드가 수정이면 U 코드가 넘어온다.
					
                    if (jobType.equals("U")) {
                        //입력구분이 '설비'코드 일때 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                        //수정모드일때는 설비이면 sales_cd 없으면 새로 만들고
                        //설비가 아니면 sales_cd는 공백으로 처리 함
                        if (detailMap.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                            String newSalesCode = "";
                            String Salad = detailMap.get("salesCd");
                            if ("".equals(Salad) || Salad == null || Salad.length() == 0) {
                                OrderSeq = cr02Mapper.selectSalesCdLastNumberPlusOne(param);

                                newSalesCode = param.get("ordrsNo") + "-" + OrderSeq.trim() + detailMap.get("prdtCd") + ItemDoov;
                                detailMap.put("salesCd", newSalesCode);
								saveSalesCd = newSalesCode;
                            }
                        }
                        // 수주 상세 업데이트
                        cr02Mapper.updateOrdrsDetail(detailMap);
						// 설비 수정시 과제테이블 update 처리하기
						if ("ORDRSDTLDIV3010".equals(detailMap.get("ordrsDtlDiv30"))) {
							detailMap.put("mkerDiv", "MAKERDIV10");
						} else {
							detailMap.put("mkerDiv", "MAKERDIV20");
						}
						cr02Mapper.updateWb21m01(detailMap);

                    } else if (jobType.equals("C")) {
                            //신규 입력이면서 '설비'코드 일때 신규 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                            if (detailMap.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                                OrderSeq = cr02Mapper.selectSalesCdLastNumberPlusOne(param);

                                String newSalesCode = param.get("ordrsNo") + "-" + OrderSeq.trim() + detailMap.get("prdtCd") + ItemDoov;
                                detailMap.put("salesCd", newSalesCode);
								saveSalesCd = newSalesCode;
                            }
                            cr02Mapper.insertOrdrsDetail(detailMap);
                    } // (jobType.equals("U") or jobType.equals("C"))
                } //
                //cr02Mapper.insertOrdrsDetail(detailMap);
				// 해당 설비에 해당하는 설계BOM, 구매BOM 자료 있는지 확인(중복 등록 방지)
				HashMap<String, String> bomParMap = new HashMap<>();
				bomParMap.put("salesCd", saveSalesCd);
				Map<String, String> selectBomCheck = cr02Mapper.selectBomCheck(bomParMap);
				if (Integer.parseInt(selectBomCheck.get("bm14Cnt")) > 1 ||Integer.parseInt(selectBomCheck.get("sm01Cnt")) > 1 ) {
					throw new RuntimeException("설비 ("+ saveSalesCd + ")에 설계BOM 또는 구매BOM에 이미 등록되어 있습니다. 전산실에 문의해주세요.");
				}

            } catch (Exception e) {
                System.out.println("error3" + e.getMessage());
                thrower.throwCommonException("설비&원가 수정오류!");
            }
        }

        //---------------------------------------------------------------
        //첨부 화일 처리 시작
        //---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
            paramMap.put("fileTrgtTyp", param.get("pgmId"));
            paramMap.put("fileTrgtKey", param.get("fileTrgtKey"));
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        for(String fileKey : deleteFileList) {
            cm08Svc.deleteFile(fileKey);
        }
        //---------------------------------------------------------------
        //첨부 화일 처리  끝
        //---------------------------------------------------------------

        //---------------------------------------------------------------
        //결재처리[1. 수주에서는  SalesCd를 가질수 없음]
        //---------------------------------------------------------------
        param.put("reqNo", param.get("ordrsNo"));
        param.put("salesCd", param.get("ordrsNo"));

        List<Map<String, String>> approvalChk = QM01Mapper.deleteWbsApprovalListChk(param);
        boolean canUpdateApproval = (approvalChk == null || approvalChk.isEmpty());

        // 기존 결재선/공유선 삭제는 등록(INSERT) 이전에 완료 (공유자 INSERT 후 deleteWbsApprovalList 실행으로 인한 공유자 삭제 방지)
        if (canUpdateApproval) {
            QM01Mapper.deleteWbsApprovalList(param);
        } else {
            List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(param);
            if (sharngChk != null && sharngChk.size() > 0) {
                QM01Mapper.deleteWbsSharngList(param);
            }
        }

        String pgParam1 = "{\"actionType\":\""+ "T" +"\",";
        pgParam1 += "\"fileTrgtKey\":\""+ param.get("fileTrgtKey") +"\",";
        pgParam1 += "\"coCd\":\""+ param.get("coCd") +"\",";
        pgParam1 += "\"histNo\":\""+ param.get("histNo") +"\",";
        //pgParam1 += "\"salesCd\":\""+ param.get("salesCd") +"\",";
        pgParam1 += "\"ordrsNo\":\""+ param.get("ordrsNo") +"\"}";
        //공유
        Type stringList2 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        List<Map<String, String>> sharngArr = gson.fromJson(param.get("rowSharngListArr"), stringList2);
        if (sharngArr != null && sharngArr.size() > 0 ) {
            int i = 0;
            for (Map<String, String> sharngMap : sharngArr) {
                try {
                        String targetUsrNm = sharngMap.get("usrNm");
                        if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                            targetUsrNm = sharngMap.get("todoId");
                        }
                        if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                            targetUsrNm = sharngMap.get("id");
                        }
                        sharngMap.put("usrNm", targetUsrNm);
                        sharngMap.put("todoId", targetUsrNm);
                        if (sharngMap.get("todoCoCd") == null || sharngMap.get("todoCoCd").isEmpty()) {
                            sharngMap.put("todoCoCd", param.get("coCd"));
                        }
                        if (sharngMap.get("coCd") == null || sharngMap.get("coCd").isEmpty()) {
                            sharngMap.put("coCd", param.get("coCd"));
                        }
                        sharngMap.put("reqNo", param.get("ordrsNo"));
                        sharngMap.put("salesCd", param.get("ordrsNo"));
                        sharngMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                        sharngMap.put("pgmId", param.get("pgmId"));
                        sharngMap.put("userId", param.get("userId"));
                        sharngMap.put("histNo", param.get("histNo"));
                        sharngMap.put("sanCtnSn",Integer.toString(i+1));
                        sharngMap.put("pgParam", pgParam1);
                        String shTitle = sharngMap.get("todoTitle");
                        if (shTitle != null && !shTitle.startsWith(param.get("ordrsNo"))) {
                            sharngMap.put("todoTitle", param.get("ordrsNo") +" , " + shTitle);
                        }
                        QM01Mapper.insertWbsSharngList(sharngMap);
                    i++;
                } catch (Exception e) {
                    System.err.println("updateOrdrs insertWbsSharngList error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        String pgParam2 = "{\"actionType\":\""+ "S" +"\",";
        pgParam2 += "\"fileTrgtKey\":\""+ param.get("fileTrgtKey") +"\",";
        pgParam2 += "\"coCd\":\""+ param.get("coCd") +"\",";
        pgParam2 += "\"histNo\":\""+ param.get("histNo") +"\",";
        //pgParam2 += "\"salesCd\":\""+ param.get("salesCd") +"\",";
        pgParam2 += "\"ordrsNo\":\""+ param.get("ordrsNo") +"\"}";
        //결재
        if (canUpdateApproval) {
            Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
            List<Map<String, String>> approvalArr = gson.fromJson(param.get("rowApprovalListArr"), stringList3);
            approvalArr = ApprovalLineTypeGuard.moveStagePostToEnd(approvalArr, param.get("ordrsNo"));
            if (approvalArr != null && approvalArr.size() > 0 ) {
                int i = 0;
                for (Map<String, String> approvalMap : approvalArr) {
                    try {
                            String targetUsrNm = approvalMap.get("usrNm");
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = approvalMap.get("todoId");
                            }
                            if (targetUsrNm == null || targetUsrNm.trim().isEmpty()) {
                                targetUsrNm = approvalMap.get("id");
                            }
                            approvalMap.put("usrNm", targetUsrNm);
                            approvalMap.put("todoId", targetUsrNm);
                            if (approvalMap.get("todoCoCd") == null || approvalMap.get("todoCoCd").isEmpty()) {
                                approvalMap.put("todoCoCd", param.get("coCd"));
                            }
                            if (approvalMap.get("coCd") == null || approvalMap.get("coCd").isEmpty()) {
                                approvalMap.put("coCd", param.get("coCd"));
                            }
                            approvalMap.put("reqNo", param.get("ordrsNo"));
                            approvalMap.put("salesCd", param.get("ordrsNo"));
                            approvalMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                            approvalMap.put("pgmId", param.get("pgmId"));
                            approvalMap.put("userId", param.get("userId"));
                            approvalMap.put("histNo", param.get("histNo"));
                            approvalMap.put("sanCtnSn",Integer.toString(i+1));
                            approvalMap.put("pgParam", pgParam2);
                            String apTitle = approvalMap.get("todoTitle");
                            if (apTitle != null && !apTitle.startsWith(param.get("ordrsNo"))) {
                                approvalMap.put("todoTitle", param.get("ordrsNo") +" , " + apTitle);
                            }
                            QM01Mapper.insertWbsApprovalList(approvalMap);
                            i++;
                    } catch (Exception e) {
                        System.err.println("updateOrdrs insertWbsApprovalList error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        // AM 전자결재 연동 (정방향/재저장 폴백)
        try {
            syncOrdrsToAm(param);
        } catch (Exception e) {
            throw new RuntimeException("전자결재(AM) 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

//      if("".equals(param.get("newOrdrsNo")) || param.get("newOrdrsNo") == null) {
//          // 수주일자의 년도가 변경되었을 경우 수주번호를 갱신
//          cr02Mapper.callUpdateOrdrsNo(param);
//      }

        // 수주관리의 정보를 프로젝트 관리에 반영
        // 남장섭 240401 프로젝트 등록후 선택하게 수정
        // param.get("prjctSeq") 값이 있으면 해당 프로젝트에 연결하고, 없으면 프로젝트 신규 생성함.
        // 해당 수주에 프로젝트번호 update 처리
        if (param.get("prjctSeq").equals("")) {
            cr02Mapper.callUpdateProjectMaster(param);
        }

		return result;

    }


    @Override
    public void updateOrdrsPmntPlanProcess(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();

        //////////////수금정보  update 수정////////

        //데이터 처리 시작
        int clmnPlanDegKey = cr02Mapper.selectDegKey(param);
        param.put("clmnPlanDegKey", Integer.toString(clmnPlanDegKey));

        //DB 저장된 수금정보 가져오기
        List<Map<String, Object>> dbPlanListRaw = cr02Mapper.selectPmntPlan(param);

        //Convert Object to String
        List<Map<String, String>> dbPlanList = dbPlanListRaw.stream()
        .map(rawMap -> rawMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
        .collect(Collectors.toList());

        ///////////////


        //수금정보 처리
        List<Map<String, String>> planArr = gson.fromJson(param.get("planArr"), mapList);
        //1. 수정부분
        for (Map<String, String> dbPlan : dbPlanList) {
            boolean found = false;
            for (Map<String, String> plan : planArr) {
                if (dbPlan.get("clmnPlanSeq").equals(plan.get("clmnPlanSeq"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                cr02Mapper.deleteOrdrsPlanEx(dbPlan);
            }
        }//1.

        for (Map<String, String> planMap : planArr) {
            try {
                boolean found = false; //1.
                for (Map<String, String> dbPlan : dbPlanList) {
                    if (dbPlan.get("clmnPlanSeq").equals(planMap.get("clmnPlanSeq"))) {
                        found = true;
                        break;
                    }
                }//1.
                planMap.put("coCd", param.get("coCd"));
                planMap.put("ordrsNo", param.get("ordrsNo"));
                planMap.put("estNo", param.get("estNo"));
                planMap.put("currCd", param.get("currCd"));
                planMap.put("userId", param.get("userId"));
                planMap.put("pgmId", param.get("pgmId"));
                planMap.put("udtId", param.get("userId"));
                planMap.put("udtPgm", "TB_CR02P01");

                planMap.put("clmnPlanDegKey", param.get("clmnPlanDegKey"));

                if (found) {
                    // Update plan
                    cr02Mapper.updateClmnPlan(planMap);
                    cr02Mapper.insertUpdatePlanHis(planMap);
                } else {
                    // Insert new plan
                    //cr02Mapper.updateClmnPlan(planMap);


                    cr02Mapper.insertClmnPlan(planMap);
                    cr02Mapper.insertUpdatePlanHis(planMap);
                }

            } catch (Exception e) {
                System.out.println("error2" + e.getMessage());
                thrower.throwCommonException("수금정보 수정오류!");
            }
        }

    }

    @Override
    public void updateOrdrs_OLD(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() { }.getType();
        String OrderSeq = "";

        //---------------------------------------------------------------
        //첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
        //   필수값 :  jobType, userId, comonCd
        //---------------------------------------------------------------
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", param.get("userId"));
        paramMap.put("comonCd", param.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
        paramMap.put("coCd", param.get("coCd"));
        paramMap.put("uploadFileArr", param.get("uploadFileArr"));

        List<Map<String, String>> uploadFileList = gson.fromJson(paramMap.get("uploadFileArr"), mapList);
        if (uploadFileList.size() > 0) {
            //접근 권한 없으면 Exception 발생 (jobType, userId, comonCd 3개 필수값 필요)
            paramMap.put("jobType", "fileUp");
            cm15Svc.selectFileAuthCheck(paramMap);
        }

        String[] deleteFileArr = gson.fromJson(param.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);

        for(String fileKey : deleteFileList) {
            // 삭제할 파일 하나씩 점검 필요(전체 목록에서 삭제 선택시 필요함)
            Map<String, String> fileInfo = cm08Svc.selectFileInfo(fileKey);
            //접근 권한 없으면 Exception 발생
            paramMap.put("comonCd", fileInfo.get("comonCd"));  //삭제할 파일이 보관된 저장 위치 정보
            paramMap.put("jobType", "fileDelete");
            cm15Svc.selectFileAuthCheck(paramMap);
        }
        //---------------------------------------------------------------
        //첨부 화일 권한체크  끝
        //---------------------------------------------------------------

        param.put("udtId", param.get("userId"));
        param.put("udtPgm", "TB_CR02M01");
        param.put("estNo", param.get("estNoOrdrs"));
        cr02Mapper.updateOrdrs(param);

        //데이터 처리 시작
        int clmnPlanDegKey = cr02Mapper.selectDegKey(param);
        param.put("clmnPlanDegKey", Integer.toString(clmnPlanDegKey));

        // 데이터베이스에서 현재 수주 상세 목록 가져오기
        List<Map<String, Object>> dbDetailListRaw = cr02Mapper.selectOrdrsDetails(param);
        // 데이터베이스 목록의 Object를 String으로 변환
        List<Map<String, String>> dbDetailList = dbDetailListRaw.stream()
                .map(rawMap -> rawMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
                .collect(Collectors.toList());

        // 클라이언트에서 전달된 수주 상세 목록
        List<Map<String, String>> detailList = gson.fromJson(param.get("detailArr"), mapList);

        // 삭제된 수주 상세 처리
        for (Map<String, String> dbDetail : dbDetailList) {
            boolean found = false;
            for (Map<String, String> ordrsDetail : detailList) {
                if (dbDetail.get("ordrsSeq").equals(ordrsDetail.get("ordrsSeq"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                cr02Mapper.deleteOrdrsDetail(dbDetail);
            }
        }

        // 수주 상세 목록 처리
        for (Map<String, String> ordrsDetail : detailList) {
            boolean found = false;
            for (Map<String, String> dbDetail : dbDetailList) {
                if (dbDetail.get("ordrsSeq").equals(ordrsDetail.get("ordrsSeq"))) {
                    found = true;
                    break;
                }
            }
            ordrsDetail.put("coCd", param.get("coCd"));
            ordrsDetail.put("ordrsNo", param.get("ordrsNo"));
            ordrsDetail.put("estNo", param.get("estNo"));
            ordrsDetail.put("currCd", param.get("currCd"));
            ordrsDetail.put("userId", param.get("userId"));
            ordrsDetail.put("pgmId", param.get("pgmId"));
            ordrsDetail.put("udtId", param.get("userId"));
            ordrsDetail.put("udtPgm", "TB_CR02M01");

            if (found) {
                // 수주 상세 업데이트

                System.out.println("23232" + ordrsDetail);
                cr02Mapper.updateOrdrsDetail(ordrsDetail);
            } else {
                // 수주 상세 삽입
              //Sales Cd 만들떄 ITEM_DIV의 CODE_ETC 값 추출
                String ItemDoov = (selectItemDivEtc(ordrsDetail));
                String Salad = ordrsDetail.get("salesCd");
                //입력구분이 '설비'코드 일때 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                if (ordrsDetail.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                    if ("".equals(Salad) || Salad == null || Salad.length() == 0) {
                        if (ordrsDetail.get("ordrsSeq").length() == 1) {
                            OrderSeq = '0'+ ordrsDetail.get("ordrsSeq");
                            System.out.println("OrderSeq :" + OrderSeq);
                        }
                        else {
                            OrderSeq = ordrsDetail.get("ordrsSeq");
                            System.out.println("OrderSeq :" + OrderSeq);
                        }

                        String newSalesCode = param.get("ordrsNo") + "-" + OrderSeq + ordrsDetail.get("prdtCd") + ItemDoov;
                        ordrsDetail.put("salesCd", newSalesCode);
                    }
                }

                System.out.println("최종231541" + ordrsDetail);
                cr02Mapper.insertOrdrsDetail(ordrsDetail);
            }
        }
        // Similarly, for planArr
        List<Map<String, String>> planArr = gson.fromJson(removeEmptyObjects(param.get("planArr")), mapList);
        List<Map<String, Object>> dbPlanListRaw = cr02Mapper.selectPmntPlan(param);

        // Convert Object to String
        List<Map<String, String>> dbPlanList = dbPlanListRaw.stream()
                .map(rawMap -> rawMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))))
                .collect(Collectors.toList());

        // Handling deleted, updated, and new records in planArr
        for (Map<String, String> dbPlan : dbPlanList) {
            boolean found = false;
            for (Map<String, String> plan : planArr) {
                if (dbPlan.get("clmnPlanSeq").equals(plan.get("clmnPlanSeq"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                cr02Mapper.deleteOrdrsPlan(dbPlan);
            }
        }

        for (Map<String, String> plan : planArr) {
            try {
                boolean found = false;
                for (Map<String, String> dbPlan : dbPlanList) {
                    if (dbPlan.get("clmnPlanSeq").equals(plan.get("clmnPlanSeq"))) {
                        found = true;
                        break;
                    }
                }

                plan.put("coCd", param.get("coCd"));
                plan.put("ordrsNo", param.get("ordrsNo"));
                plan.put("estNo", param.get("estNo"));
                plan.put("userId", param.get("userId"));
                plan.put("pgmId", param.get("pgmId"));
                plan.put("currCd", param.get("currCd"));

                plan.put("clmnPlanDegKey", param.get("clmnPlanDegKey"));

                if (found) {
                    // Update plan
                    cr02Mapper.updateClmnPlan(plan);
                    cr02Mapper.insertUpdatePlanHis(plan);
                } else {
                    // Insert new plan
                    cr02Mapper.updateClmnPlan(plan);
                    cr02Mapper.insertClmnPlan(plan);
                    cr02Mapper.insertUpdatePlanHis(plan);
                }


            }catch(Exception e){
                e.getStackTrace();
            }
        }

        //---------------------------------------------------------------
        //첨부 화일 처리 시작
        //---------------------------------------------------------------
        if (uploadFileList.size() > 0) {
            paramMap.put("fileTrgtTyp", param.get("pgmId"));
            paramMap.put("fileTrgtKey", param.get("fileTrgtKey"));
            cm08Svc.uploadFile(paramMap, mRequest);
        }

        for(String fileKey : deleteFileList) {
            cm08Svc.deleteFile(fileKey);
        }
        //---------------------------------------------------------------
        //첨부 화일 처리  끝
        //---------------------------------------------------------------

        // 수주일자의 년도가 변경되었을 경우 수주번호를 갱신
        cr02Mapper.callUpdateOrdrsNo(param);

        // 수주관리의 정보를 프로젝트 관리에 반영
        // 남장섭 240401 프로젝트 등록후 선택하게 수정
//      cr02Mapper.callUpdateProjectMaster(param);
    }

    //ex) [{}, null, {"name": "Nam"}, {}] --> [{"name": "Nam"}]으로 만들어줌
    public static String removeEmptyObjects(String jsonArrayString) {
        String nullAndEmptyObjectPattern = "(\\{\\s*\\}|null),?";  //비어있는 객체 ({}) 또는 "null" 값의 정규 표현식
        String result = jsonArrayString.replaceAll(nullAndEmptyObjectPattern, "");  //비어있는 객체나 "null" 값을 제거

        if (result.endsWith(",}")) { //마지막 요소가 ",}" 형식으로 끝나는 경우, 쉼표(",")를 제거
            result = result.substring(0, result.length() - 2) + "}";
        }
        return result;
    }

    @Override
    public int deleteOrdrs(Map<String, String> paramMap) throws Exception {

        // 프로젝트 자동 삭제 로직 Start
		//=============================================================================================================================

		Map<String, Object> selectOrdrsInfo = cr02Mapper.selectOrdrsInfo(paramMap); // 선택한 수주 정보 조회
        if (selectOrdrsInfo != null) {
            if ((paramMap.get("histNo") == null || paramMap.get("histNo").trim().isEmpty()) && selectOrdrsInfo.get("histNo") != null) {
                paramMap.put("histNo", selectOrdrsInfo.get("histNo").toString());
            }
            if ("ORDRSDIV2".equals(selectOrdrsInfo.get("ordrsDiv")) || "ORDRSDIV3".equals(selectOrdrsInfo.get("ordrsDiv"))) {
                HashMap<String, String> param2 = new HashMap<>();
                param2.putAll(paramMap);
                param2.put("clntPjt", selectOrdrsInfo.get("clntPjt").toString());
                param2.put("inpexpCd", selectOrdrsInfo.get("inpexpCd").toString());
                param2.put("ordrsClntCd", selectOrdrsInfo.get("ordrsClntCd").toString());
                param2.put("userId", paramMap.get("userId"));
                param2.put("pgmId", "CR0201P02");
    
                String selectAsPrjct = bm16Mapper.selectAsPrjct(param2);			// 고객사의 AS 프로젝트 조회

                if (selectAsPrjct != null) {                    
                    param2.put("prjctSeq", selectAsPrjct);
                    List<Map<String, String>> selectPrdtList = bm16Mapper.selectPrdtList(param2);

                    if(selectPrdtList.size() == 1) {
                        bm16Mapper.deletePrjctPrdtSeqAll(param2);
                        bm16Mapper.deletePrjctDtlSeqAll(param2);
                        bm16Mapper.deletePrjct(param2);
                    } else {
                        param2.put("newOrdrsAmt", String.valueOf(Double.parseDouble(selectOrdrsInfo.get("exchangeAmt").toString()) * -1));
                        bm16Mapper.updateAsOrdrsOnlyPrjct(param2);
                    }
                }
            }
        }

		// 프로젝트 자동 삭제 로직 End
		//=============================================================================================================================

        //---------------------------------------------------------------
        //첨부 화일 권한체크  시작 -->삭제 권한 없으면 Exception, 관련 화일 전체 체크
        //   필수값 :  jobType, userId, comonCd
        //---------------------------------------------------------------
        List<Map<String, String>> deleteFileList = cm08Svc.selectFileListAll(paramMap);
        HashMap<String, String> param = new HashMap<>();
        param.put("jobType", "fileDelete");
		param.put("coCd", paramMap.get("coCd"));
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

        //데이터 처리
        int result = 0;
        String lvl = paramMap.get("lvl").toString();
        String onNumber = paramMap.get("ordrsSeq");
        String estNo = paramMap.get("estNo");

        result = cr02Mapper.deleteOrdrs(paramMap);
        result += cr02Mapper.deleteOrdrsPlan(paramMap);
        result += cr02Mapper.deleteOrdrsPlanHis(paramMap);
        result += cr02Mapper.deleteOrdrsDetailAll(paramMap);
        if (!"".equals(estNo) && estNo != null) {
            result += cr02Mapper.updateEstDeleteConfirm(paramMap);
        }
        paramMap.put("reqNo", paramMap.get("ordrsNo"));
        paramMap.put("salesCd", paramMap.get("ordrsNo"));
        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(paramMap);
        if (sharngChk.size() > 0) {
            QM01Mapper.deleteWbsSharngList(paramMap);
        }

        // CR02 AM 전자결재 문서 삭제 (D01 먼저, M01 나중)
        deleteUnprocessedCr02AmDocs(paramMap);

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

        return result;
    }

    @Override
    public int selectOrdrsPlanHisCount(Map<String, String> param) {

        return cr02Mapper.selectOrdrsPlanHisCount(param);
    }

    @Override
    public List<Map<String, Object>> selectOrdrsPlanHis(Map<String, String> param) {
        return cr02Mapper.selectOrdrsPlanHis(param);
    }

    @Override
    public List<Map<String, Object>> selectWbsLeftSalesCodeTreeList(Map<String, String> param) {
        return cr02Mapper.selectWbsLeftSalesCodeTreeList(param);
    }

    @Override
    public List<Map<String, Object>> selectItemSalesCodeTreeList(Map<String, String> param) {
        return cr02Mapper.selectItemSalesCodeTreeList(param);
    }

    @Override
    public List<Map<String, Object>> selectItemSalesCodeTreeList2(Map<String, String> param) {
        return cr02Mapper.selectItemSalesCodeTreeList2(param);
    }

    @Override
    public int updateEstDeleteConfirm(Map<String, String> paramMap) {
        return cr02Mapper.updateEstDeleteConfirm(paramMap);
    }

    @Override
    public void callCopyOrdrs(Map<String, String> paramMap) {
        cr02Mapper.callCopyOrdrs(paramMap);
        cr02Mapper.updateMainHistNo(paramMap);
    }

    @Override
    public int selectOrdrsKey(Map<String, String> paramMap) throws Exception {
        int result = 0;
        result = cr02Mapper.selectOrdrsKey(paramMap);
        return result;
    }

    @Override
    public int selectNoSalesCdOrdrsListPopCount(Map<String, String> param) {
        return cr02Mapper.selectNoSalesCdOrdrsListPopCount(param);
    }

    @Override
    public List<Map<String, Object>> selectNoSalesCdOrdrsListPop(Map<String, String> param) {
        return cr02Mapper.selectNoSalesCdOrdrsListPop(param);
    }

    @Override
    public int selectJunmooApproval(Map<String, String> param) {
        return cr02Mapper.selectJunmooApproval(param);
    }

      //wb20 todo 삭제
      @Override
      public int deleteOrdrsDetail(Map<String, String> param) {
          int result = cr02Mapper.deleteOrdrsDetail(param);
              result = cr02Mapper.updateOrdrsDetailSoonban(param);
          return  result;
      }

        @Override
        public List<Map<String, Object>> selectOrdrsDetails(Map<String, String> param) {
            return cr02Mapper.selectOrdrsDetails(param);
        }

        @Override
        public Map<String, String> salesCdSearchOrderInfo(Map<String, String> paramMap) {
            return cr02Mapper.salesCdSearchOrderInfo(paramMap);
        }

    @Override
    public List<Map<String, String>> selectOrderChangeTitle(Map<String, String> paramMap) {
        return cr02Mapper.selectOrderChangeTitle(paramMap);
    }

    //수금관리사항 수정 처리
    @Override
    public int clmnPlanRmkUpdate(Map<String, String> paramMap)  throws Exception {
        Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("detailArr"), dtlMap);

        int result = 0;
        String jobType = paramMap.get("jobType");
        //upate
        for(Map<String, String> dtl : detailMap) {
            dtl.put("pgmId", paramMap.get("pgmId"));
            dtl.put("userId", paramMap.get("userId"));
            result += cr02Mapper.clmnPlanRmkUpdate(dtl);
        }
        return result;
    }

    // 프로젝트 자동 생성 및 수정 메서드
    public Map<String, String> insertUpdateAsPrjct(Map<String, String> param) {

        String selectAsPrjct = bm16Mapper.selectAsPrjct(param); // 고객사의 AS 프로젝트 조회
        HashMap<String, String> param2 = new HashMap<>();
        param2.putAll(param);
        
        String asPrjctSeq;
        String asPrjctSeqNm;

        if (selectAsPrjct == null) { // AS 프로젝트가 없으면 새로 생성
            asPrjctSeq = String.valueOf(bm16Mapper.selectPrjctSeqNext(param));
            asPrjctSeqNm = param.get("ctrtNm");
            param2.put("prjctSeq", asPrjctSeq);
            param2.put("yyyymm", param.get("ordrsDt").substring(0, 4) + "12");
            param2.put("newPrdtCd", "ORDRSDTLDIV2040");
            param2.put("clntMngNm", "");
            param2.put("odrCd", "PCHORD03");        // 구매방법은 기타로 처리함(PCHORD03)
            param2.put("epctAmt", param.get("exchangeAmt"));
            param2.put("ordrsPlanAmt", param.get("exchangeAmt"));
            param2.put("ordrsAmt", param.get("exchangeAmt"));
            param2.put("winbdCd", "Y");         // 수주여부는 완료로 처리
            param2.put("winbdRmk", param.get("ctrtNm"));
            param2.put("prjctNm", param.get("ctrtNm"));
            param2.put("ordrsPlanDt", param.get("ordrsDt"));
            param2.put("dsgnDt", param.get("ordrsDt"));
            param2.put("purchsDt", param.get("ordrsDt"));
            param2.put("prdctnDt", param.get("ordrsDt"));
            param2.put("acptncDt", param.get("ordrsDt"));
            param2.put("dlivyDt", param.get("ordrsDt"));
            param2.put("prjctRmk", "-AS 자동 등록 건-\n" + param.get("ordrsRmk"));
            param2.put("useYn", "Y");
            param2.put("ordrsPct", "100");  // AS인경우 진행율 관계없이 100% 진행으로 처리하기위함
        
            bm16Mapper.insertPrjct(param2);
        } else {
            asPrjctSeq = selectAsPrjct;
            asPrjctSeqNm = param.get("ctrtNm");

            param2.put("prjctSeq", asPrjctSeq);
            param2.put("newOrdrsAmt", param.get("exchangeAmt"));

            bm16Mapper.updateAsOrdrsOnlyPrjct(param2);
        }
        param2.put("prjctSeq", asPrjctSeq);
        param2.put("prjctSeqNm", asPrjctSeqNm);

        return param2;
    }

    @Override
    public Map<String, String> ordrsDeleteChk(Map<String, String> paramMap) {
        return cr02Mapper.ordrsDeleteChk(paramMap);
    }

    @Override
    public Map<String, String> deleteDetailChk(Map<String, String> paramMap) {
        return cr02Mapper.deleteDetailChk(paramMap);
    }

    @Override
    public Map<String, String> ordrsDivChangeChk(Map<String, String> paramMap) {
        return cr02Mapper.ordrsDivChangeChk(paramMap);
    }
    
    @Override
    public List<Map<String, Object>> unsettledAmtCreditChk(Map<String, String> paramMap) {
    	return cr02Mapper.unsettledAmtCreditChk(paramMap);
    }
    
    @Override
    public Map<String, Object> settledAmtCreditTotalAmt(Map<String, String> paramMap) {
    	return cr02Mapper.settledAmtCreditTotalAmt(paramMap);
    }
    
    @Override
    public List<Map<String, Object>> selectUnsettledAmtSalesCodeList(Map<String, String> paramMap) {
    	return cr02Mapper.selectUnsettledAmtSalesCodeList(paramMap);
    }

    private List<Map<String, Object>> buildAmLineListFromWb20(String ordrsNo, String coCd, String histNo) {
        if (ordrsNo == null || ordrsNo.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (coCd == null || coCd.trim().isEmpty()) {
            coCd = "GUN";
        }

        if (histNo == null || histNo.trim().isEmpty()) {
            histNo = "1";
        }

        // 결재 재조회 (차수+구분별)
        Map<String, String> apprQuery = new HashMap<>();
        apprQuery.put("todoNo", ordrsNo);
        apprQuery.put("coCd", coCd);
        apprQuery.put("histNo", histNo);
        apprQuery.put("todoDiv2CodeId", "TODODIV2100");
        List<Map<String, String>> apprList = wb20Svc.selectGetApprovalList(apprQuery);
        if (apprList == null) apprList = new ArrayList<>();

        // 공유 재조회 (차수+구분별)
        Map<String, String> refQuery = new HashMap<>();
        refQuery.put("todoNo", ordrsNo);
        refQuery.put("coCd", coCd);
        refQuery.put("histNo", histNo);
        refQuery.put("todoDiv2CodeId", "TODODIV1100");
        List<Map<String, String>> refList = wb20Svc.selectGetApprovalList(refQuery);
        if (refList == null) refList = new ArrayList<>();

        if (apprList.isEmpty() && refList.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort by sanctnSn
        java.util.Collections.sort(apprList, (o1, o2) -> {
            int s1 = parseIntSafe(o1.get("sanctnSn"));
            int s2 = parseIntSafe(o2.get("sanctnSn"));
            return Integer.compare(s1, s2);
        });

        java.util.Collections.sort(refList, (o1, o2) -> {
            int s1 = parseIntSafe(o1.get("sanctnSn"));
            int s2 = parseIntSafe(o2.get("sanctnSn"));
            return Integer.compare(s1, s2);
        });

        // AM lineList 구성: APPR 먼저, REF 나중, 1..N 재채번
        List<Map<String, Object>> amLineList = new ArrayList<>();
        int lineSeq = 1;
        for (Map<String, String> row : apprList) {
            Map<String, Object> amLine = new HashMap<>();
            amLine.put("approverId", row.get("todoId"));
            String approverNm = row.get("todoNm");
            if (approverNm == null || approverNm.trim().isEmpty()) {
                approverNm = row.get("name");
            }
            amLine.put("approverNm", approverNm);
            amLine.put("deptId", row.get("deptId"));
            amLine.put("lineSeq", lineSeq++);
            amLine.put("wb20TodoKey", row.get("todoKey"));
            amLine.put("wb20CoCd", row.get("coCd"));
            amLine.put("wb20TodoNo", row.get("todoNo"));
            amLine.put("wb20SanctnSn", row.get("sanctnSn"));
            amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
            amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
            String lineType = row.get("lineType");
            if (lineType == null || lineType.trim().isEmpty()) {
                lineType = "APPR";
            }
            amLine.put("lineType", lineType);
            amLine.put("sourceApproved", "Y".equalsIgnoreCase(row.get("sanctnSttus")) ? "Y" : "N");
            amLineList.add(amLine);
        }

        for (Map<String, String> row : refList) {
            Map<String, Object> amLine = new HashMap<>();
            amLine.put("approverId", row.get("todoId"));
            String approverNm = row.get("todoNm");
            if (approverNm == null || approverNm.trim().isEmpty()) {
                approverNm = row.get("name");
            }
            amLine.put("approverNm", approverNm);
            amLine.put("deptId", row.get("deptId"));
            amLine.put("lineSeq", lineSeq++);
            amLine.put("wb20TodoKey", row.get("todoKey"));
            amLine.put("wb20CoCd", row.get("coCd"));
            amLine.put("wb20TodoNo", row.get("todoNo"));
            amLine.put("wb20SanctnSn", row.get("sanctnSn"));
            amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
            amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
            String lineType = row.get("lineType");
            if (lineType == null || lineType.trim().isEmpty()) {
                lineType = "REF";
            }
            amLine.put("lineType", lineType);
            amLine.put("sourceApproved", "N");
            amLineList.add(amLine);
        }

        return amLineList;
    }

    private void syncOrdrsToAm(Map<String, String> paramMap) {
        try {
            String ordrsNo = paramMap.get("ordrsNo");
            if (ordrsNo == null || ordrsNo.trim().isEmpty()) {
                return;
            }

            String coCd = paramMap.get("coCd");
            if (coCd == null || coCd.trim().isEmpty()) {
                coCd = "GUN";
            }

            String histNo = paramMap.get("histNo");
            if (histNo == null || histNo.trim().isEmpty()) {
                histNo = "1";
            }

            Map<String, String> rejectedLineParam = new HashMap<>();
            rejectedLineParam.put("ordrsNo", ordrsNo);
            rejectedLineParam.put("coCd", coCd);
            rejectedLineParam.put("histNo", histNo);
            rejectedLineParam.put("userId", paramMap.get("userId"));
            rejectedLineParam.put("pgmId", paramMap.get("pgmId") == null ? "CR0202P01" : paramMap.get("pgmId"));
            cr02Mapper.resetRejectedApprovalLineByOrdrsNo(rejectedLineParam);

            List<Map<String, Object>> amLineList = buildAmLineListFromWb20(ordrsNo, coCd, histNo);
            Map<String, Object> docIdParam = new HashMap<>();
            docIdParam.put("erpBizKey", ordrsNo);
            docIdParam.put("coCd", coCd);
            docIdParam.put("todoDiv2CodeId", "TODODIV2100");
            docIdParam.put("histNo", histNo);
            String existingDocId = am11Svc.selectDocIdByBizKey(docIdParam);

            if (existingDocId != null && !existingDocId.trim().isEmpty()) {
                Map<String, String> statusParam = new HashMap<>();
                statusParam.put("docId", existingDocId);
                String existingDocStatus = cr02Mapper.selectAmDocStatusByDocId(statusParam);
                if ("REJECTED".equals(existingDocStatus)) {
                    // 반려 문서는 이력 보존을 위해 유지하고, 수정 저장은 새 AM 문서로 상신한다.
                    existingDocId = null;
                }
            }

            boolean hasApprover = false;
            for (Map<String, Object> line : amLineList) {
                String lineType = String.valueOf(line.get("lineType"));
                if ("APPR".equals(lineType) || "AGREE".equals(lineType) || "POST".equals(lineType)) {
                    hasApprover = true;
                    break;
                }
            }
            if (!hasApprover) {
                if (existingDocId != null && !existingDocId.trim().isEmpty()) {
                    Map<String, String> docParam = new HashMap<>();
                    docParam.put("docId", existingDocId);
                    if (cr02Mapper.selectAmApprovalProgressCountByDocId(docParam) == 0) {
                        cr02Mapper.deleteAmD01ByDocId(docParam);
                        cr02Mapper.deleteAmM01ByDocId(docParam);
                    }
                }
                return;
            }

            // AM submitApproval은 autoApprovedCount를 현재 차례의 0-based 위치로 사용한다.
            // 협조/참조는 결재 차례를 막지 않으므로 미완료 APPR/AGREE를 먼저 찾고,
            // 결재가 모두 끝난 경우에만 미완료 POST를 후결 차례로 선택한다.
            int autoApprovedCount = amLineList.size();
            for (int i = 0; i < amLineList.size(); i++) {
                Map<String, Object> line = amLineList.get(i);
                String lineType = String.valueOf(line.get("lineType"));
                boolean isApprovalLine = "APPR".equals(lineType) || "AGREE".equals(lineType);
                if (isApprovalLine && !"Y".equals(line.get("sourceApproved"))) {
                    autoApprovedCount = i;
                    break;
                }
            }
            if (autoApprovedCount == amLineList.size()) {
                for (int i = 0; i < amLineList.size(); i++) {
                    Map<String, Object> line = amLineList.get(i);
                    if ("POST".equals(String.valueOf(line.get("lineType")))
                            && !"Y".equals(line.get("sourceApproved"))) {
                        autoApprovedCount = i;
                        break;
                    }
                }
            }

            // 기안자 정보
            String userId = paramMap.get("userId");
            String userNm = paramMap.get("userNm");
            if (userId == null || userId.trim().isEmpty()) {
                userId = "";
            }
            if (userNm == null || userNm.trim().isEmpty()) {
                userNm = userId;
            }

            // AM 파라미터 구성
            Map<String, Object> amParam = new HashMap<>();
            if (existingDocId != null && !existingDocId.trim().isEmpty()) {
                amParam.put("docId", existingDocId);
            }
            amParam.put("coCd", coCd);
            amParam.put("userId", userId);
            amParam.put("userNm", userNm);
            amParam.put("docTitle", buildOrdrsDocTitle(paramMap, histNo));
            amParam.put("formCd", "CR0202");
            amParam.put("formVer", 1);
            amParam.put("erpBizType", "CR02");
            amParam.put("erpBizKey", ordrsNo);
            amParam.put("docDataJson", new GsonBuilder().disableHtmlEscaping().create().toJson(paramMap));
            amParam.put("docRenderHtml", buildOrdrsApprovalHtml(paramMap));
            amParam.put("pgmId", "CR0202P01");
            amParam.put("lineList", amLineList);
            amParam.put("autoApprovedCount", autoApprovedCount);
            amParam.put("histNo", histNo);

            // AM submitApproval 호출
            Map<String, Object> amResult = am11Svc.submitApproval(amParam);

            // 재저장 폴백
            if (existingDocId != null && !existingDocId.trim().isEmpty()
                    && amResult != null && !"200".equals(String.valueOf(amResult.get("resultCode")))) {
                Map<String, Object> lineChangeParam = new HashMap<>(amParam);
                lineChangeParam.put("changeReason", "CR02 수주목표원가 수정 동기화");
                lineChangeParam.put("lineList", amLineList);
                am11Svc.changeApprovalLines(lineChangeParam);
            }
        } catch (Exception e) {
            throw new RuntimeException("전자결재(AM) 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> resyncOrdrsApprovalLines(Map<String, String> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", "200");

        try {
            String ordrsNo = paramMap.get("ordrsNo");
            String coCd = paramMap.get("coCd");
            String histNo = paramMap.get("histNo");

            if (ordrsNo == null || ordrsNo.trim().isEmpty()) {
                resultMap.put("skipped", true);
                resultMap.put("reason", "ordrsNo 없음");
                return resultMap;
            }

            if (coCd == null || coCd.trim().isEmpty()) {
                coCd = "GUN";
            }
            if (histNo == null || histNo.trim().isEmpty()) {
                histNo = "1";
            }

            List<Map<String, Object>> amLineList = buildAmLineListFromWb20(ordrsNo, coCd, histNo);
            if (amLineList.isEmpty()) {
                resultMap.put("skipped", true);
                resultMap.put("reason", "WB20 결재선 없음");
                return resultMap;
            }

            String userId = paramMap.get("userId");
            String userNm = paramMap.get("userNm");
            if (userId == null || userId.trim().isEmpty()) {
                userId = "";
            }
            if (userNm == null || userNm.trim().isEmpty()) {
                userNm = userId;
            }

            Map<String, Object> docIdParam = new HashMap<>();
            docIdParam.put("erpBizKey", ordrsNo);
            docIdParam.put("coCd", coCd);
            docIdParam.put("todoDiv2CodeId", "TODODIV2100");
            docIdParam.put("histNo", histNo);
            String docId = am11Svc.selectDocIdByBizKey(docIdParam);

            if (docId == null || docId.trim().isEmpty()) {
                resultMap.put("skipped", true);
                resultMap.put("reason", "AM 문서 미존재");
                return resultMap;
            }

            Map<String, Object> amParam = new HashMap<>();
            amParam.put("docId", docId);
            amParam.put("coCd", coCd);
            amParam.put("userId", userId);
            amParam.put("userNm", userNm);
            amParam.put("pgmId", "CR0202P01");
            amParam.put("erpBizType", "CR02");
            amParam.put("erpBizKey", ordrsNo);
            amParam.put("histNo", histNo);
            amParam.put("lineList", amLineList);

            Map<String, Object> resyncResult = am11Svc.resyncApprovalLinesPreApproval(amParam);
            resultMap.putAll(resyncResult);

        } catch (Exception e) {
            System.out.println("CR02 결재선 재동기화 중 오류: " + e.getMessage());
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재선 재동기화 중 오류가 발생했습니다: " + e.getMessage());
        }

        return resultMap;
    }

    private String buildOrdrsDocTitle(Map<String, String> paramMap, String histNo) {
        StringBuilder sb = new StringBuilder();
        sb.append("[수주목표원가]");

        String ordrsClntNm = paramMap.get("ordrsClntNm");
        if (ordrsClntNm != null && !ordrsClntNm.trim().isEmpty()) {
            sb.append(" ").append(ordrsClntNm);
        }

        String clntPjtNm = paramMap.get("clntPjtNm");
        String clntPjt = paramMap.get("clntPjt");
        if (clntPjtNm == null || clntPjtNm.trim().isEmpty() || clntPjtNm.equals(clntPjt)) {
            if (clntPjt != null && !clntPjt.trim().isEmpty()) {
                clntPjtNm = resolveCodeNm(clntPjt);
            }
        }
        String pjtDisplay = (clntPjtNm != null && !clntPjtNm.trim().isEmpty()) ? clntPjtNm : clntPjt;
        if (pjtDisplay != null && !pjtDisplay.trim().isEmpty()) {
            sb.append(" ").append(pjtDisplay);
        }

        if (histNo != null && !histNo.trim().isEmpty()) {
            sb.append(" (차수:").append(histNo).append(")");
        }

        return sb.toString();
    }

    private String buildOrdrsApprovalHtml(Map<String, String> paramMap) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"approval-document\"><h3>수주목표원가</h3><table class=\"table table-bordered\">");

        // 필드(라벨, 값) 수집 후 2열(1행당 필드쌍 2개)로 배치하여 가독성 향상
        java.util.List<String[]> rows = new java.util.ArrayList<String[]>();

        String coCd = paramMap.get("coCd");
        String coNm = resolveCodeNm(coCd != null ? coCd : "");
        rows.add(new String[]{"회사", escapeHtml(coNm != null && !coNm.isEmpty() ? coNm : (coCd != null ? coCd : ""))});

        String estNoOrdrs = paramMap.get("estNoOrdrs");
        rows.add(new String[]{"견적서번호", escapeHtml(estNoOrdrs != null ? estNoOrdrs : "")});

        String estDeg = paramMap.get("estDeg");
        rows.add(new String[]{"견적차수", escapeHtml(estDeg != null ? estDeg : "")});

        String ordrsNo = paramMap.get("ordrsNo");
        rows.add(new String[]{"수주번호", escapeHtml(ordrsNo != null ? ordrsNo : "")});

        String newOrdrsNo = paramMap.get("newOrdrsNo");
        if (newOrdrsNo == null || newOrdrsNo.isEmpty()) {
            newOrdrsNo = paramMap.get("oldOrdrsNo");
        }
        rows.add(new String[]{"건양수주번호", escapeHtml(newOrdrsNo != null ? newOrdrsNo : "")});

        String ordrsDt = paramMap.get("ordrsDt");
        rows.add(new String[]{"수주일자", escapeHtml(formatDateDisplay(ordrsDt))});

        String ordrsDiv = paramMap.get("ordrsDiv");
        String ordrsDivNm = resolveCodeNm(ordrsDiv != null ? ordrsDiv : "");
        rows.add(new String[]{"수주구분", escapeHtml(ordrsDivNm != null && !ordrsDivNm.isEmpty() ? ordrsDivNm : (ordrsDiv != null ? ordrsDiv : ""))});

        String ordrsClntNm = paramMap.get("ordrsClntNm");
        rows.add(new String[]{"고객사", escapeHtml(ordrsClntNm != null ? ordrsClntNm : "")});

        String ctrtNm = paramMap.get("ctrtNm");
        rows.add(new String[]{"계약명", escapeHtml(ctrtNm != null ? ctrtNm : "")});

        String mngIdNm = paramMap.get("mngIdNm");
        rows.add(new String[]{"담당자", escapeHtml(mngIdNm != null ? mngIdNm : "")});

        String histNo = paramMap.get("histNo");
        rows.add(new String[]{"차수", escapeHtml(histNo != null ? histNo : "")});

        String pmntMtd = paramMap.get("pmntMtd");
        String pmntMtdNm = resolveCodeNm(pmntMtd != null ? pmntMtd : "");
        rows.add(new String[]{"결재방법", escapeHtml(pmntMtdNm != null && !pmntMtdNm.isEmpty() ? pmntMtdNm : (pmntMtd != null ? pmntMtd : ""))});

        String vatCd = paramMap.get("vatCd");
        String vatCdNm = resolveCodeNm(vatCd != null ? vatCd : "");
        rows.add(new String[]{"부가세", escapeHtml(vatCdNm != null && !vatCdNm.isEmpty() ? vatCdNm : (vatCd != null ? vatCd : ""))});

        String ordrsAmt = paramMap.get("ordrsAmt");
        rows.add(new String[]{"수주금액", escapeHtml(formatAmount(ordrsAmt))});

        String currCd = paramMap.get("currCd");
        String currCdNm = resolveCodeNm(currCd != null ? currCd : "");
        rows.add(new String[]{"통화단위", escapeHtml(currCdNm != null && !currCdNm.isEmpty() ? currCdNm : (currCd != null ? currCd : ""))});

        String ordrger = paramMap.get("ordrger");
        rows.add(new String[]{"발주자", escapeHtml(ordrger != null ? ordrger : "")});

        String etcField3 = paramMap.get("etcField3");
        rows.add(new String[]{"발주자.TEL", escapeHtml(etcField3 != null ? etcField3 : "")});

        String fwdExchChkList = paramMap.get("fwdExchChkList");
        String fwdExchChkListNm = resolveCodeNm(fwdExchChkList != null ? fwdExchChkList : "");
        rows.add(new String[]{"선물환CheckList", escapeHtml(fwdExchChkListNm != null && !fwdExchChkListNm.isEmpty() ? fwdExchChkListNm : (fwdExchChkList != null ? fwdExchChkList : ""))});

        String fwdExchJoinDt = paramMap.get("fwdExchJoinDt");
        rows.add(new String[]{"선물환가입일", escapeHtml(formatDateDisplay(fwdExchJoinDt))});

        String exchangeAmt = paramMap.get("exchangeAmt");
        rows.add(new String[]{"원화금액", escapeHtml(formatAmount(exchangeAmt))});

        String exrate = paramMap.get("exrate");
        rows.add(new String[]{"환율", escapeHtml(exrate != null ? exrate : "")});

        String ctrtDoc = paramMap.get("ctrtDoc");
        String ctrtDocNm = resolveCodeNm(ctrtDoc != null ? ctrtDoc : "");
        rows.add(new String[]{"계약문서", escapeHtml(ctrtDocNm != null && !ctrtDocNm.isEmpty() ? ctrtDocNm : (ctrtDoc != null ? ctrtDoc : ""))});

        String inpexpCd = paramMap.get("inpexpCd");
        String inpexpCdNm = resolveCodeNm(inpexpCd != null ? inpexpCd : "");
        rows.add(new String[]{"국내/해외", escapeHtml(inpexpCdNm != null && !inpexpCdNm.isEmpty() ? inpexpCdNm : (inpexpCd != null ? inpexpCd : ""))});

        String prjctSeqNm = paramMap.get("prjctSeqNm");
        rows.add(new String[]{"프로젝트명", escapeHtml(prjctSeqNm != null ? prjctSeqNm : "")});

        String clntPjt = paramMap.get("clntPjt");
        String clntPjtNm = resolveCodeNm(clntPjt != null ? clntPjt : "");
        rows.add(new String[]{"고객사PJT", escapeHtml(clntPjtNm != null && !clntPjtNm.isEmpty() ? clntPjtNm : (clntPjt != null ? clntPjt : ""))});

        // 2열(1행당 필드쌍 2개) 배치
        int pairsPerRow = 2;
        for (int i = 0; i < rows.size(); i += pairsPerRow) {
            html.append("<tr>");
            for (int j = 0; j < pairsPerRow; j++) {
                if (i + j < rows.size()) {
                    String[] r = rows.get(i + j);
                    html.append("<th>").append(r[0]).append("</th><td>").append(r[1]).append("</td>");
                } else {
                    html.append("<th></th><td></td>");
                }
            }
            html.append("</tr>");
        }

        // 비고 (전체 폭)
        String ordrsRmk = paramMap.get("ordrsRmk");
        html.append("<tr><th>비고</th><td colspan=\"3\" style=\"white-space: pre-wrap;\">").append(escapeHtml(ordrsRmk != null ? ordrsRmk : "")).append("</td></tr>");

        html.append("</table></div>");
        return html.toString();
    }

    private String formatDateDisplay(String dtm) {
        if (dtm == null || dtm.trim().isEmpty()) return "";
        String clean = dtm.trim().replace("-", "");
        if (clean.length() >= 8) {
            return clean.substring(0, 4) + "-" + clean.substring(4, 6) + "-" + clean.substring(6, 8);
        }
        return dtm;
    }

    private String formatAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) return "";
        try {
            double num = Double.parseDouble(amount.trim());
            return new java.text.DecimalFormat("#,##0.##").format(num);
        } catch (Exception e) {
            return amount;
        }
    }

    private String resolveCodeNm(String codeId) {
        if (codeId == null || codeId.trim().isEmpty()) return "";
        try {
            Map<String, String> codeMap = new HashMap<>();
            codeMap.put("codeId", codeId);
            Map<String, String> codeDetail = cm05Svc.selectCodeInfo(codeMap);
            if (codeDetail != null && codeDetail.get("codeNm") != null && !codeDetail.get("codeNm").isEmpty()) {
                return codeDetail.get("codeNm");
            }
        } catch (Exception e) {
            System.out.println("공통코드 코드명 조회 실패: codeId=" + codeId + ", error=" + e.getMessage());
        }
        return codeId;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;");
    }

    private void deleteUnprocessedCr02AmDocs(Map<String, String> paramMap) {
        String ordrsNo = paramMap.get("ordrsNo");
        String coCd = paramMap.get("coCd");
        if (ordrsNo == null || ordrsNo.trim().isEmpty()) {
            return;
        }

        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("ordrsNo", ordrsNo);
        queryParam.put("coCd", coCd != null && !coCd.trim().isEmpty() ? coCd : "GUN");
        List<String> docIds = cr02Mapper.selectCr02AmDocIdsByOrdrsNo(queryParam);
        if (docIds == null || docIds.isEmpty()) {
            return;
        }

        for (String docId : docIds) {
            if (docId == null || docId.trim().isEmpty()) {
                continue;
            }
            Map<String, String> docParam = new HashMap<>();
            docParam.put("docId", docId);
            if (cr02Mapper.selectAmApprovalProgressCountByDocId(docParam) == 0) {
                cr02Mapper.deleteAmD01ByDocId(docParam);
                cr02Mapper.deleteAmM01ByDocId(docParam);
            }
        }
    }

    private int parseIntSafe(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> rerenderOrdrsApprovalHtml(Map<String, String> paramMap) {
        Map<String, Object> result = new HashMap<>();
        java.util.List<Map<String, Object>> docs = am11Svc.selectCr02DocsForRerender();

        Gson gson = new com.google.gson.Gson();
        java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType();

        int updated = 0;
        int skipped = 0;
        int failed = 0;
        String firstFailDocId = null;
        String firstFailMessage = null;

        String userId = paramMap.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            userId = "SYSTEM";
        }

        for (Map<String, Object> doc : docs) {
            String docId = doc.get("docId") == null ? null : String.valueOf(doc.get("docId"));
            Object jsonObj = doc.get("docDataJson");

            if (docId == null || docId.trim().isEmpty()) {
                skipped++;
                continue;
            }

            String json = null;
            if (jsonObj instanceof java.sql.Clob) {
                try {
                    java.sql.Clob clob = (java.sql.Clob) jsonObj;
                    json = clob.getSubString(1, (int) clob.length());
                } catch (Exception e) {
                    if (firstFailDocId == null) {
                        firstFailDocId = docId;
                        firstFailMessage = "Clob 읽기 실패: " + e.getMessage();
                    }
                    failed++;
                    continue;
                }
            } else if (jsonObj != null) {
                json = String.valueOf(jsonObj);
            }

            if (json == null || json.trim().isEmpty()) {
                skipped++;
                continue;
            }

            try {
                Map<String, String> dataMap = gson.fromJson(json, mapType);
                if (dataMap == null) {
                    skipped++;
                    continue;
                }

                String html = buildOrdrsApprovalHtml(dataMap);
                Map<String, Object> updateParam = new HashMap<>();
                updateParam.put("docId", docId);
                updateParam.put("docRenderHtml", html);
                updateParam.put("userId", userId);
                updateParam.put("pgmId", "CR02_RERENDER");

                am11Svc.updateDocRenderHtmlById(updateParam);
                updated++;
            } catch (Exception e) {
                if (firstFailDocId == null) {
                    firstFailDocId = docId;
                    firstFailMessage = "렌더링 오류: " + e.getMessage();
                }
                failed++;
            }
        }

        result.put("resultCode", "200");
        result.put("total", docs.size());
        result.put("updated", updated);
        result.put("skipped", skipped);
        result.put("failed", failed);
        if (firstFailDocId != null) {
            result.put("firstFailDocId", firstFailDocId);
            result.put("firstFailMessage", firstFailMessage);
        }

        return result;
    }
}
