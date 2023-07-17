package com.dksys.biz.user.cr.cr02.service.impl;

import com.dksys.biz.admin.cm.cm08.mapper.CM08Mapper;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CR02Svcmpl implements CR02Svc {

    @Autowired
    CR01Svc cr01Svc;
    @Autowired
    CR02Mapper cr02Mapper;


    @Autowired
    CM08Mapper cm08Mapper;

    @Autowired
    CM08Svc cm08Svc;

	@Autowired
	CM15Svc cm15Svc;
	
    @Override
    public int selectOrdrsCount(Map<String, String> param) {
        return cr02Mapper.selectOrdrsCount(param);
    }

    @Override
    public List<Map<String, Object>> selectOrdrsList(Map<String, String> param) {
        return cr02Mapper.selectOrdrsList(param);
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
    public void insertOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
        
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
        param.put("ordrsNo", selectMaxOrdrsNo(param));
        String fileTrgtKey;

        cr01Svc.updateEstConfirm(param);
        cr02Mapper.insertOrdrs(param);

      
        List<Map<String, String>> planArr = gson.fromJson(removeEmptyObjects(param.get("planArr")), mapList);
        for (Map<String, String> planMap : planArr) {
            try {
                planMap.put("coCd", param.get("coCd"));
                planMap.put("ordrsNo", param.get("ordrsNo"));
                planMap.put("estNo", param.get("estNo"));
                planMap.put("currCd", param.get("currCd"));
                planMap.put("userId", param.get("userId"));
                planMap.put("pgmId", param.get("pgmId"));
                planMap.put("udtId", param.get("userId"));
                planMap.put("udtPgm", "TB_CR02M01");

                
                cr02Mapper.insertClmnPlanHis(planMap);
                cr02Mapper.insertClmnPlan(planMap);

            } catch (Exception e) {
                System.out.println("error2" + e.getMessage());


            }
        }

        List<Map<String, String>> detailArr = gson.fromJson(removeEmptyObjects(param.get("detailArr")), mapList);

        for (Map<String, String> detailMap : detailArr) {
            try {
                detailMap.put("coCd", param.get("coCd"));
                detailMap.put("ordrsNo", param.get("ordrsNo"));
                detailMap.put("estNo", param.get("estNo"));
                detailMap.put("currCd", param.get("currCd"));
                detailMap.put("userId", param.get("userId"));
                detailMap.put("pgmId", param.get("pgmId"));
                detailMap.put("udtId", param.get("userId"));
                detailMap.put("udtPgm", "TB_CR02M01");


                if (detailMap.get("ordrsDtlDiv10").equals("설비")) {
                    System.out.println("시작;dtl");
                    String newSalesCode = param.get("ordrsNo") + detailMap.get("ordrsSeq") + detailMap.get("prdtCd") + detailMap.get("itemDiv");
                    detailMap.put("salesCd", newSalesCode);
                }
                System.out.println(detailMap + "총합2");
                cr02Mapper.insertOrdrsDetail(detailMap);
            } catch (Exception e) {
                System.out.println("error3" + e.getMessage());


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
    /*    try {
            cm08Svc.uploadTreeFile("TB_CR02M01", param, mRequest);
        } catch (Exception e) {
            System.out.println("error4" + e.getMessage());


        }*/


    }

    @Override
    public void updateOrdrs(Map<String, String> param, MultipartHttpServletRequest mRequest) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type mapList = new TypeToken<ArrayList<Map<String, String>>>() {
        }.getType();


        param.put("udtId", param.get("userId"));
        param.put("udtPgm", "TB_CR02M01");
        param.put("estNo", param.get("estNoOrdrs"));
        cr02Mapper.updateOrdrs(param);

        //데이터 처리 시작
        int clmnPlanDegKey = cr02Mapper.selectDegKey(param);
        param.put("clmnPlanDegKey", Integer.toString(clmnPlanDegKey));
        System.out.println(clmnPlanDegKey + "clmnPlanDegKey==============");
        
        
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
            if (found) {
                // 수주 상세 업데이트
                ordrsDetail.put("coCd", param.get("coCd"));
                ordrsDetail.put("ordrsNo", param.get("ordrsNo"));
                ordrsDetail.put("estNo", param.get("estNo"));
                ordrsDetail.put("currCd", param.get("currCd"));
                ordrsDetail.put("userId", param.get("userId"));
                ordrsDetail.put("pgmId", param.get("pgmId"));
                ordrsDetail.put("udtId", param.get("userId"));
                ordrsDetail.put("udtPgm", "TB_CR02M01");

//                if (ordrsDetail.get("ordrsDtlDiv10").equals("설비")) {
//                    String newSalesCode = param.get("ordrsNo") + ordrsDetail.get("ordrsSeq") + ordrsDetail.get("prdtCd") + ordrsDetail.get("itemDiv");
//                    ordrsDetail.put("salesCd", newSalesCode);
//                }
                System.out.println("23232" + ordrsDetail);
                cr02Mapper.updateOrdrsDetail(ordrsDetail);
            } else {
                // 수주 상세 삽입
                ordrsDetail.put("coCd", param.get("coCd"));
                ordrsDetail.put("ordrsNo", param.get("ordrsNo"));
                ordrsDetail.put("estNo", param.get("estNo"));
                ordrsDetail.put("currCd", param.get("currCd"));


                ordrsDetail.put("userId", param.get("userId"));
                ordrsDetail.put("pgmId", param.get("pgmId"));
                ordrsDetail.put("udtId", param.get("userId"));
                ordrsDetail.put("udtPgm", "TB_CR02M01");


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
//                    plan.put("coCd", param.get("coCd"));
//                    plan.put("ordrsNo", param.get("ordrsNo"));
//                    plan.put("estNo", param.get("estNo"));
//                    plan.put("userId", param.get("userId"));
//                    plan.put("pgmId", param.get("pgmId"));
//                    plan.put("currCd", param.get("currCd"));

                    cr02Mapper.updateClmnPlan(plan);
                    cr02Mapper.insertUpdatePlanHis(plan);
                } else {
                    // Insert new plan
//                    plan.put("coCd", param.get("coCd"));
//                    plan.put("ordrsNo", param.get("ordrsNo"));
//                    plan.put("estNo", param.get("estNo"));
//                    plan.put("userId", param.get("userId"));
//                    plan.put("pgmId", param.get("pgmId"));
//                    plan.put("currCd", param.get("currCd"));
                    cr02Mapper.updateClmnPlan(plan);
                    cr02Mapper.insertClmnPlan(plan);
                    cr02Mapper.insertUpdatePlanHis(plan);
                }
            }catch(Exception e){
                e.getStackTrace();
            }
        }
/*        String[] deleteFileArr = gson.fromJson(param.get("deleteFileArr"), String[].class);
        List<String> deleteFileList = Arrays.asList(deleteFileArr);*/


 /*       for (String fileKey : deleteFileList) {
            cm08Svc.deleteFile(fileKey);
        }*/


 /*       System.out.println(param.get("fileTrgtKey") + "해당위치");
        cm08Svc.uploadTreeFile("TB_CR02M01", param, mRequest);
*/


/*        for (int i = 0; i < mRequest.getFiles("files").size(); i++) {
            try {
                cm08Svc.uploadTreeFile("TB_CR02M01", param.get("ordrsNo"), mRequest);
            } catch (Exception e) {
                System.out.println("error4" + e.getMessage());


            }

        }*/


    }


    public static String removeEmptyObjects(String jsonArrayString) {
        if (jsonArrayString == null || !jsonArrayString.startsWith("[") || !jsonArrayString.endsWith("]")) {
            return jsonArrayString;
        }

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonArrayString).getAsJsonArray();
        JsonArray filteredJsonArray = new JsonArray();

        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.entrySet().isEmpty()) {
                    filteredJsonArray.add(jsonObject);
                }
            } else if (!jsonElement.isJsonNull()) {
                filteredJsonArray.add(jsonElement);
            }
        }

        Gson gson = new Gson();
        return gson.toJson(filteredJsonArray);
    }

    @Override
    public int deleteOrdrs(Map<String, String> paramMap) {
        int result = cr02Mapper.deleteOrdrs(paramMap);
        result += cr02Mapper.deleteOrdrsPlan(paramMap);
        result += cr02Mapper.deleteOrdrsDetail(paramMap);
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


}