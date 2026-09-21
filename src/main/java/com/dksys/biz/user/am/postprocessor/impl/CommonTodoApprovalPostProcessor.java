package com.dksys.biz.user.am.postprocessor.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.am11.mapper.AM11Mapper;
import com.dksys.biz.user.am.postprocessor.ApprovalPostProcessor;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.pm.pm07.service.PM07Svc;
import com.dksys.biz.user.pm.pm08.service.PM08Svc;

/**
 * 전자결재 완료/반려/취소 시 ERP 공통 업무 결재 마스터(TB_WB20M03) 및
 * 연계 업무(PM07, PM08, PM51 등)를 동기화하는 중앙 사후처리 구현체
 */
@Component
public class CommonTodoApprovalPostProcessor implements ApprovalPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(CommonTodoApprovalPostProcessor.class);

    @Autowired
    private AM11Mapper am11Mapper;

    @Autowired
    private ObjectProvider<PM07Svc> pm07SvcProvider;

    @Autowired
    private ObjectProvider<PM08Svc> pm08SvcProvider;

    @Override
    public String getBizType() {
        return "COMMON_TODO";
    }

    @Override
    public void onApprovalCompleted(Map<String, Object> docInfo, Map<String, Object> paramMap) {
        String erpBizKey = extractErpBizKey(docInfo, paramMap);
        if (erpBizKey == null || erpBizKey.trim().isEmpty()) {
            logger.info("[CommonTodoPostProcessor] erpBizKey가 없어 TB_WB20M03 동기화를 건너뜁니다. docId={}",
                    docInfo != null ? docInfo.get("docId") : null);
            return;
        }

        boolean linkedWb20 = Boolean.TRUE.equals(paramMap.get("linkedWb20"))
                || "Y".equals(String.valueOf(paramMap.get("linkedWb20")))
                || (docInfo != null && "Y".equals(String.valueOf(docInfo.get("linkedWb20"))));

        // WB20 결재가 연계되지 않은 순수 AM 결재 또는 큐 재실행 시 TB_WB20M03 결재선 일괄 완료 처리
        if (!linkedWb20) {
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("erpBizKey", erpBizKey);
            updateParam.put("userId", paramMap.get("userId"));
            updateParam.put("apprOpinion", ApprovalSecurityUtil.maskSensitiveData((String) paramMap.get("apprOpinion")));

            int rows = am11Mapper.updateCommonTodoApprovalCompleted(updateParam);
            logger.info("[CommonTodoPostProcessor] TB_WB20M03 결재 승인 완료 반영: erpBizKey={}, updatedRows={}", erpBizKey, rows);
        }

        // ERP 개별 업무(PM07 휴가신청서, PM08 대체근무, PM51 출장 등) 사후처리 일원화 위임
        dispatchBusinessApprovalCompleted(docInfo, paramMap, erpBizKey);
    }

    @Override
    public void onApprovalRejected(Map<String, Object> docInfo, Map<String, Object> paramMap) {
        String erpBizKey = extractErpBizKey(docInfo, paramMap);
        if (erpBizKey == null || erpBizKey.trim().isEmpty()) {
            logger.info("[CommonTodoPostProcessor] erpBizKey가 없어 TB_WB20M03 동기화를 건너뜁니다. docId={}",
                    docInfo != null ? docInfo.get("docId") : null);
            return;
        }

        Map<String, Object> updateParam = new HashMap<>();
        updateParam.put("erpBizKey", erpBizKey);
        updateParam.put("userId", paramMap.get("userId"));
        String rawRejectOpn = (String) paramMap.get("rejectOpinion");
        if (rawRejectOpn == null || rawRejectOpn.trim().isEmpty()) {
            rawRejectOpn = (String) paramMap.get("apprOpinion");
        }
        if (rawRejectOpn == null || rawRejectOpn.trim().isEmpty()) {
            rawRejectOpn = (String) paramMap.get("actOpinion");
        }
        updateParam.put("rejectOpinion", ApprovalSecurityUtil.maskSensitiveData(rawRejectOpn));

        int rows = am11Mapper.updateCommonTodoApprovalRejected(updateParam);
        logger.info("[CommonTodoPostProcessor] TB_WB20M03 결재 반려 반영: erpBizKey={}, updatedRows={}", erpBizKey, rows);
    }

    @Override
    public void onApprovalCancelled(Map<String, Object> docInfo, Map<String, Object> paramMap) {
        String erpBizKey = extractErpBizKey(docInfo, paramMap);
        if (erpBizKey == null || erpBizKey.trim().isEmpty()) {
            logger.info("[CommonTodoPostProcessor] erpBizKey가 없어 TB_WB20M03 동기화를 건너뜁니다. docId={}",
                    docInfo != null ? docInfo.get("docId") : null);
            return;
        }

        Map<String, Object> updateParam = new HashMap<>();
        updateParam.put("erpBizKey", erpBizKey);
        updateParam.put("userId", paramMap.get("userId"));

        int rows = am11Mapper.updateCommonTodoApprovalCancelled(updateParam);
        logger.info("[CommonTodoPostProcessor] TB_WB20M03 결재 취소 반영: erpBizKey={}, updatedRows={}", erpBizKey, rows);
    }

    private void dispatchBusinessApprovalCompleted(Map<String, Object> docInfo, Map<String, Object> paramMap, String erpBizKey) {
        String erpBizType = (docInfo != null && docInfo.get("erpBizType") != null)
                ? String.valueOf(docInfo.get("erpBizType"))
                : (paramMap.get("erpBizType") != null ? String.valueOf(paramMap.get("erpBizType")) : "");
        String div2CodeId = (docInfo != null && docInfo.get("todoDiv2CodeId") != null)
                ? String.valueOf(docInfo.get("todoDiv2CodeId"))
                : (paramMap.get("todoDiv2CodeId") != null ? String.valueOf(paramMap.get("todoDiv2CodeId")) : "");
        String coCd = paramMap.get("coCd") != null
                ? String.valueOf(paramMap.get("coCd"))
                : (docInfo != null && docInfo.get("coCd") != null ? String.valueOf(docInfo.get("coCd")) : "");
        String userId = paramMap.get("userId") != null ? String.valueOf(paramMap.get("userId")) : "";
        String apprOpinion = (String) paramMap.get("apprOpinion");

        // 1. PM07 휴가신청서 (TODODIV2300, VAC)
        if (pm07SvcProvider != null && pm07SvcProvider.getIfAvailable() != null
                && ("PM07".equals(erpBizType) || "TODODIV2300".equals(div2CodeId) || (erpBizKey != null && erpBizKey.startsWith("VAC")))) {
            try {
                Map<String, String> pm07Param = new HashMap<>();
                pm07Param.put("todoNo", erpBizKey);
                pm07Param.put("reqNo", erpBizKey);
                pm07Param.put("coCd", coCd);
                pm07Param.put("todoYn", "Y");
                pm07SvcProvider.getIfAvailable().applyVacationApproved(pm07Param);
                logger.info("[CommonTodoPostProcessor] PM07 휴가 승인완료 후처리 연동 성공: erpBizKey={}", erpBizKey);
            } catch (Exception e) {
                logger.error("[CommonTodoPostProcessor] PM07 휴가 승인완료 후처리 연동 실패: erpBizKey={}", erpBizKey, e);
                throw new RuntimeException("PM07 휴가 승인완료 연동 실패: " + e.getMessage(), e);
            }
        }

        // 2. PM08 휴일대체근무 (TODODIV2410 신청, TODODIV2420 결과, SWR/SWC)
        if (pm08SvcProvider != null && pm08SvcProvider.getIfAvailable() != null
                && ("PM08".equals(erpBizType) || (erpBizKey != null && (erpBizKey.startsWith("SWR") || erpBizKey.startsWith("SWC"))))) {
            try {
                Map<String, String> pm08Param = new HashMap<>();
                pm08Param.put("todoNo", erpBizKey);
                pm08Param.put("reqNo", erpBizKey);
                pm08Param.put("coCd", coCd);
                pm08Param.put("userId", userId);
                pm08Param.put("todoCfOpn", apprOpinion != null ? apprOpinion : "전자결재 승인");
                pm08Param.put("todoYn", "Y");
                if ("TODODIV2420".equals(div2CodeId)) {
                    pm08SvcProvider.getIfAvailable().applySubstituteWorkResultApproved(pm08Param);
                } else {
                    pm08SvcProvider.getIfAvailable().applySubstituteWorkApproved(pm08Param);
                }
                logger.info("[CommonTodoPostProcessor] PM08 대체근무 승인완료 후처리 연동 성공: erpBizKey={}", erpBizKey);
            } catch (Exception e) {
                logger.error("[CommonTodoPostProcessor] PM08 대체근무 승인완료 후처리 연동 실패: erpBizKey={}", erpBizKey, e);
                throw new RuntimeException("PM08 대체근무 승인완료 연동 실패: " + e.getMessage(), e);
            }
        }

        // 3. PM51 출장신청서 (TODODIV2190, TRQ)
        if ("PM51".equals(erpBizType) || "TODODIV2190".equals(div2CodeId) || (erpBizKey != null && erpBizKey.startsWith("TRQ"))) {
            try {
                Map<String, Object> pm51StsParam = new HashMap<>();
                pm51StsParam.put("tripReqNo", erpBizKey);
                pm51StsParam.put("aprvStsCd", "APRVSTS03");
                pm51StsParam.put("userId", userId);
                am11Mapper.updatePm51TripReqAprvSts(pm51StsParam);
                logger.info("[CommonTodoPostProcessor] PM51 출장신청서 승인완료 상태 갱신 성공: tripReqNo={}", erpBizKey);
            } catch (Exception e) {
                logger.error("[CommonTodoPostProcessor] PM51 출장신청서 상태 갱신 실패: tripReqNo={}", erpBizKey, e);
                throw new RuntimeException("PM51 출장신청서 상태 갱신 실패: " + e.getMessage(), e);
            }
        }
    }

    private String extractErpBizKey(Map<String, Object> docInfo, Map<String, Object> paramMap) {
        if (docInfo != null && docInfo.get("erpBizKey") != null) {
            return String.valueOf(docInfo.get("erpBizKey"));
        }
        if (paramMap != null && paramMap.get("erpBizKey") != null) {
            return String.valueOf(paramMap.get("erpBizKey"));
        }
        return null;
    }
}
