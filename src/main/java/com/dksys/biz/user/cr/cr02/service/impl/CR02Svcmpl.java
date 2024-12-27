package com.dksys.biz.user.cr.cr02.service.impl;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
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

import com.dksys.biz.admin.bm.bm16.mapper.BM16Mapper;
import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
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
    BM16Mapper bm16Mapper;

    @Autowired
    ExceptionThrower thrower;

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

        //---------------------------------------------------------------
		// 프로젝트 자동생성 Start
        // int newOrdrsAmt = Integer.parseInt(param.get("ordrsAmt")); // param에서 넘어온 수정한 금액
        //     int gapOrdrsAmt = newOrdrsAmt - oldOrdrsAmt;
            

		// 신규 AS 수주 처리
		if ("ORDRSDIV2".equals(param.get("ordrsDiv")) || "ORDRSDIV3".equals(param.get("ordrsDiv"))) {
            HashMap<String, String> param2 = new HashMap<>();
			param2.putAll(param);
			
			Map<String, String> updatePrjct = bm16Mapper.selectAsPrjct(param);              // 고객사의 AS 프로젝트 조회

            List<Map<String, String>> detailArrFirst = gson.fromJson(param.get("detailArr"), mapList);
            String dudtIntendDt = detailArrFirst.get(0).get("dudtIntendDt").replace("-", "");
            String yyyymm = param.get("ordrsDt").substring(0, 4) + "12";

            int newOrdrsAmt = Integer.parseInt(param.get("ordrsAmt"));
            int updatedAmt; // 최종업데이트할 금액

			if (updatePrjct == null) {		// AS 프로젝트가 없으면 새로 생성
				insertNewProject(param, detailArrFirst, yyyymm, dudtIntendDt);      // 새로운 AS프로젝트 생성
			} else {
				updatedAmt = Integer.parseInt(updatePrjct.get("ordrsAmt")) + newOrdrsAmt;
                updateNewProject(updatePrjct, updatedAmt, param2);      // AS프로젝트가 이미 존재하면 업데이트트

                param.put("prjctSeq", updatePrjct.get("prjctSeq"));     // 업데이트한 수주정보를 AS프로젝트에 연결
			}
		}

		// 프로젝트 자동생성 End
		//---------------------------------------------------------------

        cr01Svc.updateEstConfirm(param);
        cr02Mapper.insertOrdrs(param);

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
                //입력구분이 '설비'코드 일때 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                if (detailMap.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                    if ("".equals(Salad) || Salad == null || Salad.length() == 0) {
                        // 등록모드에서는 등록처리만 있음(삭제건은 처리하지 않음)
                        OrderSeq = cr02Mapper.selectSalesCdLastNumberPlusOne(param);
                        String newSalesCode = param.get("ordrsNo") + "-" + OrderSeq.trim() + detailMap.get("prdtCd") + ItemDoov;
                        detailMap.put("salesCd", newSalesCode);
                    }
                }
                cr02Mapper.insertOrdrsDetail(detailMap);
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
                            sharngMap.put("reqNo", param.get("ordrsNo"));
                            sharngMap.put("salesCd", param.get("ordrsNo"));
                            sharngMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                            sharngMap.put("pgmId", param.get("pgmId"));
                            sharngMap.put("userId", param.get("userId"));
                            sharngMap.put("histNo", "1");
                            sharngMap.put("sanCtnSn",Integer.toString(i+1));
                            sharngMap.put("pgParam", pgParam1);
                            sharngMap.put("todoTitle", param.get("ordrsNo") +" , " + sharngMap.get("todoTitle"));
                            QM01Mapper.insertWbsSharngList(sharngMap);
                        i++;
                    } catch (Exception e) {
                        System.out.println("error2"+e.getMessage());
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
            if (approvalArr != null && approvalArr.size() > 0 ) {
                int i = 0;
                for (Map<String, String> approvalMap : approvalArr) {
                    try {
                            approvalMap.put("reqNo", param.get("ordrsNo"));
                            approvalMap.put("salesCd", param.get("ordrsNo"));
                            approvalMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                            approvalMap.put("pgmId", param.get("pgmId"));
                            approvalMap.put("userId", param.get("userId"));
                            approvalMap.put("histNo", "1");
                            approvalMap.put("sanCtnSn",Integer.toString(i+1));
                            approvalMap.put("pgParam", pgParam2);
                            approvalMap.put("todoTitle", param.get("ordrsNo") +" , " + approvalMap.get("todoTitle"));
                            QM01Mapper.insertWbsApprovalList(approvalMap);
                            i++;
                    } catch (Exception e) {
                        System.out.println("error2"+e.getMessage());
                    }
                }
            }
            cr02Mapper.callCopyOrdrs(param); //이력생성

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
    public void updateOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
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
//        }else {
//          // 건양수주번호 있으면 수주번호는 건양수주번호를 따라감
//          param.put("ordrsNo", param.get("newOrdrsNo"));
//        }

        //=============================================================================================================================
		// 프로젝트 자동 수정 로직 Start
        
        Map<String, String> originalOrdrsInfo = gson.fromJson(param.get("originalOrdrsInfo"), Map.class); // 수정 전 데이터
        Map<String, String> originPrjct = bm16Mapper.selectAsPrjct(originalOrdrsInfo);  // 수정 전 데이터 기반 AS프로젝트 조회
        Map<String, String> updatePrjct = bm16Mapper.selectAsPrjct(param);              // 수정 후 데이터 기반 AS프로젝트 조회

        //---------------------------------------------------------------
        // 프로젝트 조회 (이전의 등록되어 있는 프로젝트를 구분하기 위해) => true : 신규 등록된 프로젝트 / false : 기존에 등록된 프로젝트
        
        Map<String, String> selectPrjctInfo = bm16Mapper.selectPrjctInfo(originalOrdrsInfo);
        // 고정 날짜 기준으로 이전에 등록되어 있던 수주 내용을 구분함 -> 이전 데이터는 기존 방식 적용 / 이후 데이터는 새로운 방식 적용용
        boolean isAfter = LocalDateTime.parse(selectPrjctInfo.get("creatDttm"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                .isAfter(LocalDateTime.parse("2024-12-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); 
        //---------------------------------------------------------------

        if (isAfter) {
            HashMap<String, String> param2 = new HashMap<>();
            param2.putAll(param);

            List<Map<String, String>> detailArrFirst = gson.fromJson(param.get("detailArr"), mapList);
            String dudtIntendDt = detailArrFirst.get(0).get("dudtIntendDt").replace("-", "");
            String yyyymm = param.get("ordrsDt").substring(0, 4) + "12";

            int newOrdrsAmt = Integer.parseInt(param.get("ordrsAmt")); // param에서 넘어온 수정한 금액
            int oldOrdrsAmt = Integer.parseInt(originalOrdrsInfo.get("ordrsAmt"));  // 기존 금액
            int gapOrdrsAmt = newOrdrsAmt - oldOrdrsAmt;
            int updatedAmt; // 최종업데이트할 금액

            // 변경사항 체크(회사, 고객사PJT, 국내해외, 고객사<업체명>, 수주구분) -> 변경사항에 따라 새로운 프로젝트를 생성 및 업데이트
            boolean isDataChanged = Arrays.asList("coCd", "clntPjt", "inpexpCd", "ordrsClntCd", "ordrsDiv")
                                        .stream()
                                        .anyMatch(key -> !param.get(key).equals(originalOrdrsInfo.get(key)));
    
            // 새로운 구조 구상 유지보수를 위해 
            if (("ORDRSDIV2".equals(param.get("ordrsDiv")) || "ORDRSDIV3".equals(param.get("ordrsDiv")))) {

                //! originPrjct 있고 updatePrjct 없음
                if (originPrjct != null && updatePrjct == null) {
                    if (isDataChanged) {
                        insertNewProject(param, detailArrFirst, yyyymm, dudtIntendDt);
                    }
                    updateOldProject(originPrjct, oldOrdrsAmt, param2);

                //! originPrjct 있고 updatePrjct 있음
                } else if (originPrjct != null && updatePrjct != null) {  
                    if (!updatePrjct.get("prjctSeq").equals(originPrjct.get("prjctSeq"))) {
                        updateOldProject(originPrjct, oldOrdrsAmt, param2);             // originPrjct 프로젝트 수정
                    } 
                    if (updatePrjct.get("prjctSeq").equals(originPrjct.get("prjctSeq"))) {
                        updatedAmt = Integer.parseInt(updatePrjct.get("ordrsAmt")) + gapOrdrsAmt;   // updatePrjct와 originPrjct가 같다면 차이 금액만 더해줌
                    } else {
                        updatedAmt = Integer.parseInt(updatePrjct.get("ordrsAmt")) + newOrdrsAmt;   // updatePrjct와 originPrjct가 다르면 새로운 금액을 더함
                    }
                    updateNewProject(updatePrjct, updatedAmt, param2);
        
                    param.put("prjctSeq", updatePrjct.get("prjctSeq")); // param에 updatePrjct의 프로젝트 코드 넣기(수주관리에서 사용)

                //! originPrjct 없고 updatePrjct 없음 (이외의 수주에서 신규AS)
                } else if (originPrjct == null && updatePrjct == null) {
                    if (isDataChanged) {
                        insertNewProject(param, detailArrFirst, yyyymm, dudtIntendDt);   
                    }

                //! originPrjct 없고 updatePrjct 있음 (이외의 수주에서 기존AS)
                } else if (originPrjct == null && updatePrjct != null) {
                    if (isDataChanged) {
                        updatedAmt = Integer.parseInt(updatePrjct.get("ordrsAmt")) + newOrdrsAmt;
                        updateNewProject(updatePrjct, updatedAmt, param2);

                        param.put("prjctSeq", updatePrjct.get("prjctSeq")); // param에 updatePrjct의 프로젝트 코드 넣기(수주관리에서 사용)
                    }
                }
            } else {   // 수주구분이 유무상 AS가 아닐때(예: 유무상 AS에서 이외의 수주로 변경하고 들어온 정보들)
                //! originPrjct는 유무상 AS임(반드시 존재함)
                if (originPrjct != null && isDataChanged) {
                    updateOldProject(originPrjct, oldOrdrsAmt, param2);
                }
            } 
        }

		// 프로젝트 자동 수정 로직 End
		//=============================================================================================================================

        param.put("udtId", param.get("userId"));
        param.put("udtPgm", "TB_CR02M01");
        param.put("estNo", param.get("estNoOrdrs"));
        if(!"".equals(param.get("estNoOrdrs")) || param.get("estNoOrdrs") != null) {
            cr01Svc.updateEstConfirm(param);
        }
        cr02Mapper.updateOrdrs(param);

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
        String jobType = "";  //추가="C",  수정 = "U" 코드 저장용
        for (Map<String, String> detailMap : detailArr) {
            try {
                Object jobTypeObject = detailMap.get("cudCheck");
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
                            }
                        }
                        // 수주 상세 업데이트
                        cr02Mapper.updateOrdrsDetail(detailMap);

                    } else if (jobType.equals("C")) {
                            //신규 입력이면서 '설비'코드 일때 신규 sales_cd 만들기(sales_cd 값이 빈칸, null, 길이 0 일떄)
                            if (detailMap.get("ordrsDtlDiv10").equals("ORDRSDTLDIV1010")) {
                                OrderSeq = cr02Mapper.selectSalesCdLastNumberPlusOne(param);

                                String newSalesCode = param.get("ordrsNo") + "-" + OrderSeq.trim() + detailMap.get("prdtCd") + ItemDoov;
                                detailMap.put("salesCd", newSalesCode);
                            }
                            cr02Mapper.insertOrdrsDetail(detailMap);
                    } // (jobType.equals("U") or jobType.equals("C"))
                } //
                //cr02Mapper.insertOrdrsDetail(detailMap);
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

        List<Map<String, String>> sharngChk = QM01Mapper.deleteWbsSharngListChk(param);
        if (sharngChk.size() > 0) {
            QM01Mapper.deleteWbsSharngList(param);
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
                        sharngMap.put("reqNo", param.get("ordrsNo"));
                        sharngMap.put("salesCd", param.get("ordrsNo"));
                        sharngMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                        sharngMap.put("pgmId", param.get("pgmId"));
                        sharngMap.put("userId", param.get("userId"));
                        sharngMap.put("histNo", param.get("histNo"));
                        sharngMap.put("sanCtnSn",Integer.toString(i+1));
                        sharngMap.put("pgParam", pgParam1);
                        sharngMap.put("todoTitle", param.get("ordrsNo") +" , " + sharngMap.get("todoTitle"));
                        QM01Mapper.insertWbsSharngList(sharngMap);
                    i++;
                } catch (Exception e) {
                    System.out.println("error2"+e.getMessage());
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
        Type stringList3 = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        List<Map<String, String>> approvalArr = gson.fromJson(param.get("rowApprovalListArr"), stringList3);
        if (approvalArr != null && approvalArr.size() > 0 ) {
            int i = 0;
            for (Map<String, String> approvalMap : approvalArr) {
                try {
                        approvalMap.put("reqNo", param.get("ordrsNo"));
                        approvalMap.put("salesCd", param.get("ordrsNo"));
                        approvalMap.put("fileTrgtKey", param.get("fileTrgtKey"));
                        approvalMap.put("pgmId", param.get("pgmId"));
                        approvalMap.put("userId", param.get("userId"));
                        approvalMap.put("histNo", param.get("histNo"));
                        approvalMap.put("sanCtnSn",Integer.toString(i+1));
                        approvalMap.put("pgParam", pgParam2);
                        approvalMap.put("todoTitle", param.get("ordrsNo") +" , " + approvalMap.get("todoTitle"));
                        QM01Mapper.insertWbsApprovalList(approvalMap);
                        i++;
                } catch (Exception e) {
                    System.out.println("error2"+e.getMessage());
//                  thrower.throwCommonException("공유정보발송!");
                }
            }
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
		//---------------------------------------------------------------
        
		Map<String, Object> selectOrdrsInfo = cr02Mapper.selectOrdrsInfo(paramMap); // 선택한 수주 정보 조회
        if (selectOrdrsInfo != null) {
            if ("ORDRSDIV2".equals(selectOrdrsInfo.get("ordrsDiv")) || "ORDRSDIV3".equals(selectOrdrsInfo.get("ordrsDiv"))) {
                HashMap<String, String> param2 = new HashMap<>();
                param2.putAll(paramMap);
                param2.put("clntPjt", selectOrdrsInfo.get("clntPjt").toString());
                param2.put("inpexpCd", selectOrdrsInfo.get("inpexpCd").toString());
                param2.put("ordrsClntCd", selectOrdrsInfo.get("ordrsClntCd").toString());
                param2.put("userId", paramMap.get("userId"));
                param2.put("pgmId", "CR0201P02");
    
                Map<String, String> asPrjct = bm16Mapper.selectAsPrjct(param2);			// 고객사의 AS 프로젝트 조회
                if (asPrjct != null) {
                    int totalOrdrsAmt = Integer.parseInt(asPrjct.get("ordrsAmt"));
                    int oldOrdrsAmt = Integer.parseInt(selectOrdrsInfo.get("ordrsAmt").toString());
                    int updatedOrdrsAmt = totalOrdrsAmt - oldOrdrsAmt;
                    
                    param2.put("prjctSeq", asPrjct.get("prjctSeq"));

                    List<Map<String, String>> selectPrdtList = bm16Mapper.selectPrdtList(param2);
                    int selectPrdtListCount = selectPrdtList.size();

                    if(selectPrdtListCount != 1) {
                        param2.put("epctAmt", String.valueOf(updatedOrdrsAmt));
                        param2.put("ordrsPlanAmt", String.valueOf(updatedOrdrsAmt));
                        param2.put("ordrsAmt", String.valueOf(updatedOrdrsAmt));
                        bm16Mapper.updateOrdrsPrjct(param2);
                    } else {
                        bm16Mapper.deletePrjctPrdtSeqAll(param2);
                        bm16Mapper.deletePrjctDtlSeqAll(param2);
                        bm16Mapper.deletePrjct(param2);
                    }
                }
            }
        }

		// 프로젝트 자동 삭제 로직 End
		//---------------------------------------------------------------

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

    // 새로운 프로젝트 생성
    private void insertNewProject(Map<String, String> param, List<Map<String, String>> detailArrFirst, String yyyymm, String dudtIntendDt) {
        String newPrjctSeq = String.valueOf(bm16Mapper.selectPrjctSeqNext(param));

        param.put("prjctSeq", newPrjctSeq);
        param.put("prjctSeqNm", param.get("ctrtNm"));
        
        // 새로운 프로젝트 생성 데이터 설정
        Map<String, String> param2 = new HashMap<>(param);
        param2.put("prjctSeq", newPrjctSeq);
        param2.put("yyyymm", yyyymm);
        param2.put("newPrdtCd", "ORDRSDTLDIV2040");
        param2.put("eqpNm", detailArrFirst.get(0).get("eqpNm"));
        param2.put("eqpQty", detailArrFirst.get(0).get("ordrsQty"));
        param2.put("clntMngNm", "");
        param2.put("odrCd", "PCHORD03");
        param2.put("epctAmt", param.get("ordrsAmt"));
        param2.put("ordrsPlanAmt", param.get("ordrsAmt"));
        param2.put("winbdCd", "Y");
        param2.put("winbdRmk", param.get("ctrtNm"));
        param2.put("prjctNm", param.get("ctrtNm"));
        param2.put("ordrsPlanDt", param.get("ordrsDt"));
        param2.put("dsgnDt", param.get("ordrsDt"));
        param2.put("purchsDt", param.get("ordrsDt"));
        param2.put("prdctnDt", param.get("ordrsDt"));
        param2.put("acptncDt", param.get("ordrsDt"));
        param2.put("dlivyDt", param.get("ordrsDt"));
        param2.put("dudtPlanDt", dudtIntendDt);
        param2.put("prjctRmk", param.get("ordrsRmk"));
        param2.put("useYn", "Y");
        param2.put("ordrsPct", "100");
    
        bm16Mapper.insertPrjct(param2);
    }

    // 기존 프로젝트 금액 조정
    private void updateOldProject(Map<String, String> originPrjct, int oldOrdrsAmt, Map<String, String> param2) {

        int updatedAmt = Integer.parseInt(originPrjct.get("ordrsAmt")) - oldOrdrsAmt;
        
        param2.put("prjctSeq", originPrjct.get("prjctSeq"));
        param2.put("ordrsAmt", String.valueOf(updatedAmt));
        param2.put("epctAmt", String.valueOf(updatedAmt));
        param2.put("ordrsPlanAmt", String.valueOf(updatedAmt));
    
        List<Map<String, String>> selectPrdtList = bm16Mapper.selectPrdtList(param2);
        if (selectPrdtList.size() == 1) {
            bm16Mapper.deletePrjct(param2); 
        } else {
            bm16Mapper.updateOrdrsPrjct(param2);
        }
    }

    // 업데이트할 프로젝트 금액 조정
    private void updateNewProject(Map<String, String> updatePrjct, int updatedAmt, Map<String, String> param2) {

        param2.put("prjctSeq", updatePrjct.get("prjctSeq"));
        param2.put("ordrsAmt", String.valueOf(updatedAmt));
        param2.put("epctAmt", String.valueOf(updatedAmt));
        param2.put("ordrsPlanAmt", String.valueOf(updatedAmt));

        bm16Mapper.updateOrdrsPrjct(param2); // 프로젝트 수정
    }
}