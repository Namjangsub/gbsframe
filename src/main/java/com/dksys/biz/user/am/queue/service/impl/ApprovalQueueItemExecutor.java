package com.dksys.biz.user.am.queue.service.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am11.mapper.AM11Mapper;
import com.dksys.biz.user.am.postprocessor.ApprovalPostProcessorRegistry;
import com.dksys.biz.user.am.notification.Kakao10009MessageBuilder;
import com.dksys.biz.user.am.notification.Kakao10009Sender;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 다중 Worker 환경에서 단건 큐 레코드를 REQUIRES_NEW 독립 트랜잭션으로 원자적 CAS 선점 및 발송 실행하는 전담 실행기
 */
@Service
public class ApprovalQueueItemExecutor {

    private final Logger logger = LoggerFactory.getLogger(ApprovalQueueItemExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakaoSend:false}")
    private boolean kakaoSend;
    @Value("${GBS_TALK_API_URL:}") private String talkApiUrl;
    @Value("${GBS_TALK_AUTH_TOKEN:}") private String talkAuthToken;
    @Value("${GBS_TALK_SERVER_NAME:}") private String talkServerName;
    @Value("${GBS_TALK_PAYMENT_TYPE:}") private String talkPaymentType;
    @Value("${GBS_TALK_SERVICE:}") private String talkService;

    @Autowired
    private Kakao10009Sender kakao10009Sender;

    @Autowired
    private AM11Mapper am11Mapper;

    @Autowired
    private ApprovalPostProcessorRegistry postProcessorRegistry;

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    @Autowired(required = false)
    private WB24Svc wb24Svc;

    public enum ExecutionResult {
        SUCCESS, FAIL, SKIPPED
    }

    /**
     * 알림 큐 단건 CAS 선점 및 실제 발송 (REQUIRES_NEW 로 선점 즉시 커밋)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ExecutionResult acquireAndExecuteNotification(Map<String, Object> item) {
        String notifId = String.valueOf(item.get("notifId"));

        if (!kakaoSend) {
            // 발송 비활성(kakaoSend=false) 환경: CAS 선점 후 종결 상태(SKIPPED)로 표시해
            // 행이 READY로 남아 스케줄러가 30초마다 무한 재선택/반복 로그하는 것을 방지한다.
            int skipAcquired = am11Mapper.updateNotificationProcessing(notifId);
            if (skipAcquired == 0) {
                return ExecutionResult.SKIPPED; // 다른 워커가 이미 선점
            }
            Map<String, Object> skipParam = new HashMap<>();
            skipParam.put("notifId", notifId);
            skipParam.put("notifStatus", "SKIPPED");
            skipParam.put("errMsg", "kakaoSend=false: 발송 비활성 환경으로 종결");
            am11Mapper.updateNotificationQueueStatus(skipParam);
            return ExecutionResult.SKIPPED;
        }

        // 1. 원자적 CAS 선점 (PROCESSING 전이)
        int acquired = am11Mapper.updateNotificationProcessing(notifId);
        if (acquired == 0) {
            // 다른 Worker가 이미 선점하여 처리 중
            return ExecutionResult.SKIPPED;
        }

        int retryCnt = intOf(item.get("retryCnt"), 0);
        int maxRetry = intOf(item.get("maxRetry"), 3);

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
                // 재시도 횟수 검증: 발송 시도 1회마다 retryCnt 를 정확히 1 증가시키고, maxRetry(기본 3) 번째 실패에서 종료(EXHAUSTED)한다.
                // 최초 발송 포함 총 maxRetry 회 시도한다(예: 3 → 최초 1회 + 재시도 2회). 선점 조회 조건(RETRY_CNT < MAX_RETRY)과 동일한 기준이다.
                retryCnt++;
                // 업체가 HTTP 200 으로 오류 status(!= OK, 예: 템플릿 불일치)를 돌려준 경우는 같은 내용을 다시 보내도 같은 사유로 거부되므로
                // 남은 재시도를 하지 않고 즉시 종료(EXHAUSTED)한다. HTTP 비200·네트워크 예외 등 일시 오류만 maxRetry 까지 재시도한다.
                boolean vendorRejected = Boolean.TRUE.equals(item.get("sendVendorRejected"));
                boolean exhausted = vendorRejected || retryCnt >= maxRetry;
                String detail = item.get("sendErrDetail") == null ? "" : String.valueOf(item.get("sendErrDetail"));
                updateParam.put("retryCnt", retryCnt);
                updateParam.put("notifStatus", exhausted ? "EXHAUSTED" : "FAIL");
                String errMsg = "발송 실패 (회차: " + retryCnt + "/" + maxRetry + (vendorRejected ? ", 재시도 불가로 즉시 종료" : "") + ")"
                        + (detail.isEmpty() ? "" : " - " + detail);
                errMsg = ApprovalSecurityUtil.maskSensitiveData(errMsg);
                updateParam.put("errMsg", errMsg.length() > 300 ? errMsg.substring(0, 300) : errMsg);
                am11Mapper.updateNotificationQueueStatus(updateParam);
                if (exhausted) {
                    logger.error("[Notification Worker] 재시도 한도 소진(EXHAUSTED): notifId={}, 시도={}/{}, 사유={}", notifId, retryCnt, maxRetry, detail);
                } else {
                    logger.warn("[Notification Worker] 발송 실패 - 재시도 예정: notifId={}, 시도={}/{}, 사유={}", notifId, retryCnt, maxRetry, detail);
                }
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
        String channel = String.valueOf(notif.get("notifChannel"));
        logger.info("[Notification Worker] 발송 실행: notifId={}, channel={}, receiverId={}",
                notif.get("notifId"), channel, notif.get("receiverId"));

        if (bm18Svc != null && ("KAKAO".equalsIgnoreCase(channel) || "TALK".equalsIgnoreCase(channel) || "MSG".equalsIgnoreCase(channel))) {
            try {
                boolean tmpl10009 = Kakao10009MessageBuilder.TMPLAT_DIV.equals(valueOrDefault(notif.get("tmplatDiv"), ""));
                Map<String, String> kakaoParam = new HashMap<>();
                kakaoParam.put("rcvId", String.valueOf(notif.get("receiverId")));
                kakaoParam.put("rcvNm", String.valueOf(notif.get("receiverNm")));
                kakaoParam.put("clntCd", "1");
                kakaoParam.put("tmplatDiv", valueOrDefault(notif.get("tmplatDiv"), "TMPLATDIV02"));
                // 10009 는 원문을 보낸다. 변수 값은 Kakao10009MessageBuilder.safe() 에서 이미 마스킹된다.
                kakaoParam.put("title", tmpl10009 ? String.valueOf(notif.get("notifTitle")) : ApprovalSecurityUtil.maskSensitiveData(String.valueOf(notif.get("notifTitle"))));
                kakaoParam.put("mssage", tmpl10009 ? String.valueOf(notif.get("notifMsg")) : ApprovalSecurityUtil.maskSensitiveData(String.valueOf(notif.get("notifMsg"))));
                String receiverMobile = notif.get("receiverMobile") == null
                        ? "" : String.valueOf(notif.get("receiverMobile"));
                if (isBlank(receiverMobile) && wb24Svc != null) {
                    Map<String, String> userParam = new HashMap<>();
                    userParam.put("userId", String.valueOf(notif.get("receiverId")));
                    java.util.List<Map<String, String>> telList = wb24Svc.selectMemberTelNo(userParam);
                    if (telList != null && !telList.isEmpty()) {
                        receiverMobile = telList.get(0).get("telNo");
                    }
                }
                kakaoParam.put("mobile", receiverMobile);
                kakaoParam.put("nameTo", String.valueOf(notif.get("receiverNm")));
                kakaoParam.put("creatId", "SYSTEM");
                kakaoParam.put("creatPgm", "AM_QUEUE");
                String todoNo = valueOrDefault(notif.get("todoNo"), "");
                String todoDiv2CodeId = valueOrDefault(notif.get("todoDiv2CodeId"), "");
                if (isBlank(todoNo) || isBlank(todoDiv2CodeId)) {
                    logger.warn("[Notification Worker] ERP 원자료 식별값 누락으로 카카오 발송 생략: notifId={}", notif.get("notifId"));
                    notif.put("sendErrDetail", "ERP 원자료 식별값(todoNo/todoDiv2CodeId) 누락");
                    return false;
                }
                kakaoParam.put("todoNo", todoNo);
                kakaoParam.put("todoDiv2CodeId", todoDiv2CodeId);

                if (tmpl10009) {
                    return sendTalk10009(notif, kakaoParam);
                }

                // 10003(레거시 템플릿) 직접 HTTP 경로: 기존 동작 유지.
                if (isBlank(talkApiUrl) || isBlank(talkAuthToken) || isBlank(talkServerName)
                        || isBlank(talkPaymentType) || isBlank(talkService) || isBlank(kakaoParam.get("mobile"))) {
                    notif.put("sendErrDetail", isBlank(kakaoParam.get("mobile")) ? "수신 휴대전화번호 없음" : "알림톡 접속 설정(GBS_TALK_*) 누락");
                    return false;
                }

                // 재시도 회차: 2회차 이후에는 messageId 끝에 _회차 를 붙인다(1회차는 기존 값 그대로).
                int attemptNo = intOf(notif.get("retryCnt"), 0) + 1;
                String attemptSuffix = attemptNo > 1 ? "_" + attemptNo : "";
                String sendMsgId = null;
                try {
                    sendMsgId = bm18Svc.selectKakaoSeqNext(new HashMap<String, String>());
                } catch (Exception seqEx) {
                    logger.warn("[Notification Worker] messageId 채번 실패(AM_ 형식으로 대체): notifId={}, err={}", notif.get("notifId"), seqEx.getMessage());
                }
                if (isBlank(sendMsgId)) {
                    sendMsgId = valueOrDefault(notif.get("messageId"), "AM_" + notif.get("notifId")) + attemptSuffix;
                }
                kakaoParam.put("mssageId", sendMsgId);
                Map<String, Object> talkBody = new HashMap<>();
                talkBody.put("service", talkService);
                talkBody.put("messageId", sendMsgId);
                talkBody.put("title", kakaoParam.get("title"));
                talkBody.put("message", kakaoParam.get("mssage"));
                talkBody.put("mobile", kakaoParam.get("mobile"));
                talkBody.put("template", "10003");

                HttpURLConnection conn = (HttpURLConnection) new URL(talkApiUrl).openConnection();
                conn.setRequestMethod("POST");
                // PM0701P01 직접 발송 경로와 동일한 카카오 API 인증 헤더명을 사용한다.
                conn.setRequestProperty("authToken", talkAuthToken);
                conn.setRequestProperty("serverName", talkServerName);
                conn.setRequestProperty("paymentType", talkPaymentType);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);
                byte[] body = new Gson().toJson(talkBody).getBytes("UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }
                int responseCode = conn.getResponseCode();
                boolean sendOk = responseCode == 200;
                kakaoParam.put("sendgStatus", sendOk ? "OK" : "FAIL");
                if (!sendOk) {
                    notif.put("sendErrDetail", "HTTP" + responseCode);
                    logger.warn("[Notification Worker] 알림톡 거부 요청 본문: notifId={}, request={}",
                            notif.get("notifId"), ApprovalSecurityUtil.maskSensitiveData(new Gson().toJson(talkBody)));
                }
                // 발송 결과(sendOk)와 이력 적재는 분리한다: 발송이 성공했는데 이력 INSERT 만 실패해 재시도되면 알림톡이 중복 발송된다.
                try {
                    bm18Svc.insertKakaoMessage(kakaoParam);
                } catch (Exception historyEx) {
                    logger.warn("[Notification Worker] 발송 이력 적재 실패(발송 결과는 유지): notifId={}, sendOk={}, err={}",
                            notif.get("notifId"), sendOk, historyEx.getMessage());
                }
                return sendOk;
            } catch (Exception e) {
                logger.warn("[Notification Worker] 카카오 발송 처리 중 예외: notifId={}", notif.get("notifId"), e);
                notif.put("sendErrDetail", e.getClass().getSimpleName() + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 10009 알림 발송. 결재요청 직접 발송과 같은 Kakao10009Sender 를 쓴다.
     * 결과는 큐 후처리(sendErrDetail/sendVendorRejected)와 발송 이력에 반영하고, 반환값으로 성공 여부를 돌려준다.
     */
    private boolean sendTalk10009(Map<String, Object> notif, Map<String, String> kakaoParam) {
        Kakao10009Sender.Result result = kakao10009Sender.send(kakaoParam.get("mobile"), kakaoParam.get("title"),
                kakaoParam.get("mssage"), kakaoParam.get("todoNo"), kakaoParam.get("todoDiv2CodeId"));
        if (!result.isOk()) {
            notif.put("sendErrDetail", result.getDetail());
        }
        // 재시도 불가(업체 거부, 설정·수신번호 누락 등)는 같은 입력이면 같은 결과이므로 즉시 종료(EXHAUSTED)하도록 표시한다.
        if (!result.isOk() && !result.isRetryable()) {
            notif.put("sendVendorRejected", Boolean.TRUE);
        }
        // 업체 호출이 없었으면(messageId=null) 이력도 남기지 않는다. 이력 INSERT 실패는 발송 결과에 영향을 주지 않는다.
        if (result.getMessageId() != null) {
            kakaoParam.put("mssageId", result.getMessageId());
            kakaoParam.put("sendgStatus", result.getStatus());
            try {
                bm18Svc.insertKakaoMessage(kakaoParam);
            } catch (Exception historyEx) {
                logger.warn("[Notification Worker] 발송 이력 적재 실패(발송 결과는 유지): notifId={}, sendOk={}, err={}",
                        notif.get("notifId"), result.isOk(), historyEx.getMessage());
            }
        }
        return result.isOk();
    }

    /** 큐 행의 숫자 값(DB 가 BigDecimal 등으로 줄 수 있음). 비어 있거나 숫자가 아니면 기본값. */
    private static int intOf(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String valueOrDefault(Object value, String defaultValue) {
        return value == null || String.valueOf(value).trim().isEmpty() ? defaultValue : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
