package com.dksys.biz.user.am.queue.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am11.mapper.AM11Mapper;
import com.dksys.biz.user.am.postprocessor.ApprovalPostProcessorRegistry;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 다중 Worker 환경에서 단건 큐 레코드를 REQUIRES_NEW 독립 트랜잭션으로 원자적 CAS 선점 및 발송 실행하는 전담 실행기
 */
@Service
public class ApprovalQueueItemExecutor {

    private final Logger logger = LoggerFactory.getLogger(ApprovalQueueItemExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AM11Mapper am11Mapper;

    @Autowired
    private ApprovalPostProcessorRegistry postProcessorRegistry;

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    public enum ExecutionResult {
        SUCCESS, FAIL, SKIPPED
    }

    /**
     * 알림 큐 단건 CAS 선점 및 실제 발송 (REQUIRES_NEW 로 선점 즉시 커밋)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ExecutionResult acquireAndExecuteNotification(Map<String, Object> item) {
        String notifId = String.valueOf(item.get("notifId"));

        // 1. 원자적 CAS 선점 (PROCESSING 전이)
        int acquired = am11Mapper.updateNotificationProcessing(notifId);
        if (acquired == 0) {
            // 다른 Worker가 이미 선점하여 처리 중
            return ExecutionResult.SKIPPED;
        }

        int retryCnt = item.get("retryCnt") != null ? Integer.parseInt(String.valueOf(item.get("retryCnt"))) : 0;
        int maxRetry = item.get("maxRetry") != null ? Integer.parseInt(String.valueOf(item.get("maxRetry"))) : 3;

        try {
            // 2. 실제 알림 발송 (카카오/알림톡 인프라)
            boolean sendSuccess = attemptNotificationSend(item);
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("notifId", notifId);

            if (sendSuccess) {
                updateParam.put("notifStatus", "SUCCESS");
                updateParam.put("errMsg", null);
                am11Mapper.updateNotificationQueueStatus(updateParam);
                return ExecutionResult.SUCCESS;
            } else {
                retryCnt++;
                updateParam.put("retryCnt", retryCnt);
                updateParam.put("notifStatus", retryCnt >= maxRetry ? "EXHAUSTED" : "FAIL");
                updateParam.put("errMsg", ApprovalSecurityUtil.maskSensitiveData("발송 인프라 응답 오류 또는 실패 (회차: " + retryCnt + ")"));
                am11Mapper.updateNotificationQueueStatus(updateParam);
                return ExecutionResult.FAIL;
            }
        } catch (Exception e) {
            logger.error("[Notification Worker] 발송 처리 중 예외 발생: notifId={}", notifId, e);
            retryCnt++;
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("notifId", notifId);
            updateParam.put("retryCnt", retryCnt);
            updateParam.put("notifStatus", retryCnt >= maxRetry ? "EXHAUSTED" : "FAIL");
            updateParam.put("errMsg", ApprovalSecurityUtil.maskSensitiveData("발송 예외: " + e.getMessage()));
            am11Mapper.updateNotificationQueueStatus(updateParam);
            return ExecutionResult.FAIL;
        }
    }

    private boolean attemptNotificationSend(Map<String, Object> notif) {
        String channel = (String) notif.get("notifChannel");
        logger.info("[Notification Worker] 발송 실행: notifId={}, channel={}, receiverId={}",
                notif.get("notifId"), channel, notif.get("receiverId"));

        if (bm18Svc != null && ("KAKAO".equalsIgnoreCase(channel) || "TALK".equalsIgnoreCase(channel) || "MSG".equalsIgnoreCase(channel))) {
            try {
                Map<String, String> kakaoParam = new HashMap<>();
                kakaoParam.put("mssageId", "AM_" + notif.get("notifId"));
                kakaoParam.put("rcvId", String.valueOf(notif.get("receiverId")));
                kakaoParam.put("rcvNm", String.valueOf(notif.get("receiverNm")));
                kakaoParam.put("clntCd", "1");
                kakaoParam.put("tmplatDiv", "TMPLATDIV02");
                kakaoParam.put("sendgStatus", "READY");
                kakaoParam.put("title", ApprovalSecurityUtil.maskSensitiveData(String.valueOf(notif.get("notifTitle"))));
                kakaoParam.put("mssage", ApprovalSecurityUtil.maskSensitiveData(String.valueOf(notif.get("notifMsg"))));
                kakaoParam.put("mobile", "");
                kakaoParam.put("nameTo", String.valueOf(notif.get("receiverNm")));
                kakaoParam.put("creatId", "SYSTEM");
                kakaoParam.put("creatPgm", "AM_QUEUE");
                kakaoParam.put("todoNo", String.valueOf(notif.get("docId")));
                kakaoParam.put("todoDiv2CodeId", "AM1101P01");
                int result = bm18Svc.insertKakaoMessage(kakaoParam);
                return result >= 0;
            } catch (Exception e) {
                logger.warn("[Notification Worker] 카카오 발송 테이블 연동 실패: notifId={}, err={}", notif.get("notifId"), e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * ERP 보상 큐 단건 CAS 선점 및 실제 연동 재실행 (REQUIRES_NEW 로 선점 즉시 커밋)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ExecutionResult acquireAndExecuteCompensation(Map<String, Object> item) {
        String compId = String.valueOf(item.get("compId"));

        // 1. 원자적 CAS 선점 (PROCESSING 전이)
        int acquired = am11Mapper.updateCompensationProcessing(compId);
        if (acquired == 0) {
            return ExecutionResult.SKIPPED;
        }

        int retryCnt = item.get("retryCnt") != null ? Integer.parseInt(String.valueOf(item.get("retryCnt"))) : 0;
        int maxRetry = item.get("maxRetry") != null ? Integer.parseInt(String.valueOf(item.get("maxRetry"))) : 3;

        try {
            // 2. 실제 ERP PostProcessor 재호출
            boolean linkSuccess = attemptErpLink(item);
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("compId", compId);

            if (linkSuccess) {
                updateParam.put("status", "SUCCESS");
                updateParam.put("errMsg", null);
                am11Mapper.updateErpCompensationStatus(updateParam);
                return ExecutionResult.SUCCESS;
            } else {
                retryCnt++;
                updateParam.put("retryCnt", retryCnt);
                updateParam.put("status", retryCnt >= maxRetry ? "MANUAL_CHECK" : "FAIL");
                updateParam.put("errMsg", ApprovalSecurityUtil.maskSensitiveData("ERP 사후 연계 실패 (회차: " + retryCnt + ")"));
                am11Mapper.updateErpCompensationStatus(updateParam);
                return ExecutionResult.FAIL;
            }
        } catch (Exception e) {
            logger.error("[ERP Compensation Worker] 연계 재시도 중 예외: compId={}", compId, e);
            retryCnt++;
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("compId", compId);
            updateParam.put("retryCnt", retryCnt);
            updateParam.put("status", retryCnt >= maxRetry ? "MANUAL_CHECK" : "FAIL");
            updateParam.put("errMsg", ApprovalSecurityUtil.maskSensitiveData("재처리 예외: " + e.getMessage()));
            am11Mapper.updateErpCompensationStatus(updateParam);
            return ExecutionResult.FAIL;
        }
    }

    private boolean attemptErpLink(Map<String, Object> comp) {
        String erpBizType = (String) comp.get("erpBizType");
        String payloadJson = (String) comp.get("payloadJson");
        logger.info("[ERP Compensation Worker] Re-executing ERP PostProcessor: compId={}, erpBizType={}", comp.get("compId"), erpBizType);

        if (payloadJson == null || payloadJson.trim().isEmpty()) {
            return false;
        }

        try {
            Map<String, Object> docInfo = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> paramMap = new HashMap<>(docInfo);
            paramMap.put("isCompensationRetry", "Y");
            paramMap.put("userId", docInfo.get("draUserId"));

            // Registry를 통해 실제 PostProcessor 실행
            postProcessorRegistry.processCompleted(erpBizType, docInfo, paramMap);
            return true;
        } catch (Exception e) {
            logger.error("[ERP Compensation Worker] 연계 재실행 예외: compId={}, bizType={}", comp.get("compId"), erpBizType, e);
            return false;
        }
    }
}
