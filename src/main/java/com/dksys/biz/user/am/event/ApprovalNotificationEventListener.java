package com.dksys.biz.user.am.event;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.google.gson.Gson;

@Component
public class ApprovalNotificationEventListener {

    private final Logger logger = LoggerFactory.getLogger(ApprovalNotificationEventListener.class);

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    @Autowired(required = false)
    private WB24Svc wb24Svc;

    /**
     * DB 트랜잭션이 성공적으로 COMMIT된 이후에만 안전하게 비동기 알림을 전송합니다.
     * 알림 발송 중 실패(네트워크 단절 등)가 발생해도 결재 트랜잭션은 롤백되지 않습니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalEvent(ApprovalEvent event) {
        logger.info("[ApprovalNotification] Processing event: {} for Doc: {} ({})",
                event.getEventType(), event.getDocNo(), event.getDocId());

        try {
            switch (event.getEventType()) {
                case "SUBMIT":
                case "APPROVE_NEXT":
                    String erpBizType = event.getExtraInfo() != null ? (String) event.getExtraInfo().get("erpBizType") : null;
                    if ("PM07".equals(erpBizType) || "PM08".equals(erpBizType)) {
                        sendRichNotification(event);
                    } else {
                        sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다",
                                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다: " + event.getDocTitle(),
                                event.getDocId(), null, null);
                    }
                    sendApplicantOpinionNotification(event, "결재 처리", false);
                    break;
                case "ARBIT":
                    sendApplicantOpinionNotification(event, "전결 처리", true);
                    break;
                case "COMPLETE":
                    // 기안자에게 최종 승인 완료 알림 발송
                    sendNotification(event.getDraUserId(), event.getDraUserNm(),
                            "[" + event.getDocNo() + "] 결재 승인 완료",
                            "[" + event.getDocNo() + "] 상신하신 문서가 최종 승인 완료되었습니다: " + event.getDocTitle()
                                    + approvalOpinionSuffix(event),
                            event.getDocId(), null, null);
                    break;
                case "REJECT":
                    // 기안자에게 반려 알림 발송
                    sendNotification(event.getDraUserId(), event.getDraUserNm(),
                            "[" + event.getDocNo() + "] 결재 반려",
                            "[" + event.getDocNo() + "] 상신하신 문서가 반려되었습니다: " + event.getDocTitle()
                                    + approvalOpinionSuffix(event),
                            event.getDocId(), null, null);
                    break;
                case "CANCEL":
                    // 취소 알림
                    sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                            "[" + event.getDocNo() + "] 결재 상신 취소",
                            "[" + event.getDocNo() + "] 기안자가 문서를 회수(상신 취소)하였습니다.",
                            event.getDocId(), null, null);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("[ApprovalNotification] 알림 발송 이벤트 처리 중 예외 발생: eventType={}, docId={}",
                    event.getEventType(), event.getDocId(), e);
        }
    }

    private void sendApplicantOpinionNotification(ApprovalEvent event, String action, boolean alwaysSend) {
        String opinion = approvalOpinion(event);
        if (!alwaysSend && opinion.isEmpty()) {
            return;
        }
        sendNotification(event.getDraUserId(), event.getDraUserNm(),
                "[" + event.getDocNo() + "] " + action + " 의견",
                "[" + event.getDocNo() + "] " + event.getDocTitle()
                        + "\n결재의견: " + (opinion.isEmpty() ? "(의견 없음)" : opinion),
                event.getDocId(), null, null);
    }

    private String approvalOpinionSuffix(ApprovalEvent event) {
        String opinion = approvalOpinion(event);
        return opinion.isEmpty() ? "" : "\n결재의견: " + opinion;
    }

    private String approvalOpinion(ApprovalEvent event) {
        if (event.getExtraInfo() == null) {
            return "";
        }
        Object opinion = event.getExtraInfo().get("apprOpinion");
        if (opinion == null || String.valueOf(opinion).trim().isEmpty()) {
            opinion = event.getExtraInfo().get("rejectOpinion");
        }
        return opinion == null ? "" : String.valueOf(opinion).trim();
    }

    private void sendRichNotification(ApprovalEvent event) {
        if (event.getNextApproverId() == null || event.getNextApproverId().trim().isEmpty() || bm18Svc == null) {
            return;
        }

        try {
            Map<String, Object> extraInfo = event.getExtraInfo();
            String erpBizKey = (String) extraInfo.get("erpBizKey");
            String coCd = (String) extraInfo.get("coCd");
            String erpBizType = (String) extraInfo.get("erpBizType");
            Map<String, Object> paramMap = extraInfo;

            if (erpBizType == null || erpBizType.isEmpty() || erpBizKey == null || erpBizKey.isEmpty()
                    || coCd == null || coCd.isEmpty()) {
                logger.warn("[ApprovalNotification] PM07/PM08 리치 발송 조건 미충족: erpBizType={}, erpBizKey={}, coCd={}",
                        erpBizType, erpBizKey, coCd);
                return;
            }

            String todoDiv2CodeId = null;
            if ("PM07".equals(erpBizType)) {
                todoDiv2CodeId = "TODODIV2300";
            } else if ("PM08".equals(erpBizType)) {
                todoDiv2CodeId = (String) event.getExtraInfo().get("todoDiv2CodeId");
                if (todoDiv2CodeId == null || todoDiv2CodeId.isEmpty()) {
                    todoDiv2CodeId = "TODODIV2410";
                }
            }
            if (todoDiv2CodeId == null) {
                return;
            }

            Map<String, String> selectParam = new HashMap<>();
            selectParam.put("coCd", coCd);
            selectParam.put("todoDiv1CodeId", "TODODIV20");
            selectParam.put("todoDiv2CodeId", todoDiv2CodeId);
            selectParam.put("todoNo", erpBizKey);
            selectParam.put("tmplatDiv", "TMPLATDIV02");
            selectParam.put("sanctnSn", event.getExtraInfo().get("currLineSeq") != null
                    ? String.valueOf(event.getExtraInfo().get("currLineSeq")) : "1");

            List<Map<String, String>> messageList = bm18Svc.selectMaxMessageIdTodo(selectParam);
            if (messageList == null || messageList.isEmpty()) {
                logger.warn("[ApprovalNotification] selectMaxMessageIdTodo 결과 없음: todoNo={}", erpBizKey);
                return;
            }

            Map<String, String> msgInfo = messageList.get(0);
            String maxMessageId = msgInfo.get("maxMessageId");
            String messageDesc = msgInfo.get("messageDesc");
            String mobile = selectApproverMobile(event.getNextApproverId(), coCd);
            // 사용자 정보 조회가 누락된 경우에 한해 BM18 조회 결과의 수신번호를 보조 사용한다.
            // 이 값은 요청담당자 전화번호에는 사용하지 않는다.
            if (mobile == null || mobile.trim().isEmpty()) {
                mobile = msgInfo.get("telNo");
            }

            if (mobile == null || mobile.isEmpty() || messageDesc == null || messageDesc.isEmpty()) {
                logger.warn("[ApprovalNotification] 필수 정보 부족: mobile={}, messageDesc 있음={}", mobile, messageDesc != null);
                return;
            }

            String finalMessage = messageDesc;

            if (finalMessage != null && !finalMessage.isEmpty()) {
                finalMessage = finalMessage.replace("#{todoTitl}", event.getDocTitle() != null ? event.getDocTitle() : "");
                finalMessage = finalMessage.replace("#{title}", event.getDocTitle() != null ? event.getDocTitle() : "");
                finalMessage = finalMessage.replace("#{todoDiv1CodeNm}", "결재");
                String todoDiv2Name;
                if ("PM07".equals(erpBizType)) {
                    Object vacTypeNm = paramMap.get("vacTypeNm");
                    if (vacTypeNm == null || String.valueOf(vacTypeNm).trim().isEmpty()) {
                        String docDataJson = (String) paramMap.get("docDataJson");
                        if (docDataJson != null && !docDataJson.trim().isEmpty()) {
                            try {
                                Map<String, Object> docData = new Gson().fromJson(docDataJson, Map.class);
                                if (docData != null) {
                                    vacTypeNm = docData.get("vacTypeNm");
                                }
                            } catch (Exception parseException) {
                                logger.warn("[ApprovalNotification] PM07 원천 휴가유형 파싱 실패: {}", parseException.getMessage());
                            }
                        }
                    }
                    todoDiv2Name = vacTypeNm != null && !String.valueOf(vacTypeNm).trim().isEmpty()
                            ? String.valueOf(vacTypeNm) : "휴가";
                } else {
                    todoDiv2Name = msgInfo.get("todoTitl") != null ? msgInfo.get("todoTitl") : "";
                }
                finalMessage = finalMessage.replace("#{todoDiv2CodeNm}", todoDiv2Name);
                finalMessage = finalMessage.replace("#{sanctnDiv2}", "결재");
                finalMessage = finalMessage.replace("#{sendDt}", msgInfo.get("sendDt") != null ? msgInfo.get("sendDt") : "");
                finalMessage = finalMessage.replace("#{ordrgMngNm}", event.getDraUserNm() != null ? event.getDraUserNm() : "");
                // 요청담당자 전화번호는 수신자(msgInfo.telNo)나 원천 JSON의 업무별 telNo가
                // 아니라, 항상 기안자(draUserId) 기준으로 조회한다.
                String ordrgMngTelNo = selectApproverMobile(event.getDraUserId(), coCd);
                if (ordrgMngTelNo == null) {
                    ordrgMngTelNo = "";
                }
                finalMessage = finalMessage.replace("#{ordrgMngTelNo}", ordrgMngTelNo);
                finalMessage = finalMessage.replace("#{nameTo}", msgInfo.get("name") != null ? msgInfo.get("name") : "");
                finalMessage = finalMessage.replace("#{rcvNm}", msgInfo.get("name") != null ? msgInfo.get("name") : "");
            }

            Map<String, String> logParam = new HashMap<>();
            logParam.put("mssageId", maxMessageId);
            logParam.put("rcvId", event.getNextApproverId());
            logParam.put("rcvNm", event.getNextApproverNm());
            logParam.put("clntCd", "1");
            logParam.put("tmplatDiv", "TMPLATDIV02");
            String notificationTitle = event.getDocTitle() != null && !event.getDocTitle().trim().isEmpty()
                    ? event.getDocTitle() : "결재 대기";
            logParam.put("title", event.getDocNo() != null
                    ? "[" + event.getDocNo() + "] " + notificationTitle : notificationTitle);
            logParam.put("mssage", finalMessage);
            logParam.put("mobile", mobile);
            logParam.put("nameTo", event.getNextApproverNm());
            logParam.put("creatId", event.getDraUserId());
            logParam.put("creatPgm", "PM0701P01");
            logParam.put("todoNo", erpBizKey);
            logParam.put("todoDiv2CodeId", todoDiv2CodeId);

            String sendgStatus = "READY";
            try {
                String talkApiUrl = System.getenv("GBS_TALK_API_URL");
                String authToken = System.getenv("GBS_TALK_AUTH_TOKEN");
                String serverName = System.getenv("GBS_TALK_SERVER_NAME");
                String paymentType = System.getenv("GBS_TALK_PAYMENT_TYPE");
                String service = System.getenv("GBS_TALK_SERVICE");
                if (talkApiUrl == null || talkApiUrl.trim().isEmpty()
                        || authToken == null || authToken.trim().isEmpty()
                        || serverName == null || serverName.trim().isEmpty()
                        || paymentType == null || paymentType.trim().isEmpty()
                        || service == null || service.trim().isEmpty()) {
                    logger.warn("[ApprovalNotification] 알림톡 환경변수 미설정으로 실발송을 건너뜁니다: messageId={}", maxMessageId);
                    logParam.put("sendgStatus", "READY");
                    bm18Svc.insertKakaoMessage(logParam);
                    return;
                }
                URL url = new URL(talkApiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("authToken", authToken);
                conn.setRequestProperty("serverName", serverName);
                conn.setRequestProperty("paymentType", paymentType);
                conn.setDoOutput(true);

                Map<String, String> talkBody = new HashMap<>();
                talkBody.put("service", service);
                talkBody.put("messageId", maxMessageId);
                talkBody.put("title", logParam.get("title"));
                talkBody.put("message", finalMessage);
                talkBody.put("mobile", mobile);
                talkBody.put("template", "10003");

                String jsonInputString = new Gson().toJson(talkBody);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    sendgStatus = "OK";
                    logger.info("[ApprovalNotification] PM07/PM08 리치 실발송 성공: messageId={}, receiver={}", maxMessageId, event.getNextApproverId());
                } else {
                    logger.warn("[ApprovalNotification] talkapi 발송 실패 (responseCode={}): messageId={}", responseCode, maxMessageId);
                }
            } catch (Exception kakaoEx) {
                logger.warn("[ApprovalNotification] talkapi 발송 예외: {}", kakaoEx.getMessage());
            }

            logParam.put("sendgStatus", sendgStatus);
            bm18Svc.insertKakaoMessage(logParam);

        } catch (Exception e) {
            logger.warn("[ApprovalNotification] PM07/PM08 리치 발송 중 예외로 발송하지 않습니다: {}", e.getMessage());
        }
    }

    private String selectApproverMobile(String approverId, String coCd) {
        if (wb24Svc == null || approverId == null || approverId.trim().isEmpty()) {
            return null;
        }
        Map<String, String> param = new HashMap<>();
        param.put("coCd", coCd);
        param.put("userId", approverId);
        List<Map<String, String>> memberList = wb24Svc.selectMemberTelNo(param);
        if (memberList == null || memberList.isEmpty()) {
            return null;
        }
        String mobile = memberList.get(0).get("telNo");
        return mobile == null || mobile.trim().isEmpty() ? null : mobile;
    }

    private void sendBasicNotification(ApprovalEvent event) {
        sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다",
                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다: " + event.getDocTitle(),
                event.getDocId(), null, null);
    }

    private void sendNotification(String receiverId, String receiverNm, String title, String message, String docId,
            String todoNo, String todoDiv2CodeId) {
        if (receiverId == null || receiverId.trim().isEmpty()) {
            return;
        }

        String maskedTitle = ApprovalSecurityUtil.maskSensitiveData(title);
        String maskedMsg = ApprovalSecurityUtil.maskSensitiveData(message);

        logger.info("[ApprovalNotification -> Receiver({}): {}] Title: {}, Message: {}",
                receiverId, receiverNm, maskedTitle, maskedMsg);

        if (bm18Svc != null) {
            try {
                Map<String, String> kakaoParam = new HashMap<>();
                kakaoParam.put("mssageId", "AM_" + System.currentTimeMillis());
                kakaoParam.put("rcvId", receiverId);
                kakaoParam.put("rcvNm", receiverNm);
                kakaoParam.put("clntCd", "1");
                kakaoParam.put("tmplatDiv", "TMPLATDIV02");
                kakaoParam.put("sendgStatus", "READY");
                kakaoParam.put("title", maskedTitle);
                kakaoParam.put("mssage", maskedMsg);
                kakaoParam.put("mobile", "");
                kakaoParam.put("nameTo", receiverNm);
                kakaoParam.put("creatId", "SYSTEM");
                kakaoParam.put("creatPgm", "AM_ALIM");
                kakaoParam.put("todoNo", docId);
                kakaoParam.put("todoDiv2CodeId", "AM1101P01");
                bm18Svc.insertKakaoMessage(kakaoParam);
                logger.info("[ApprovalNotification] TB_BM18M01 알림 메시지 발송 테이블 INSERT 성공: receiverId={}", receiverId);
            } catch (Exception e) {
                logger.warn("[ApprovalNotification] 카카오 발송 테이블 등록 실패 (재처리 큐가 후속 처리): receiverId={}, err={}", receiverId, e.getMessage());
            }
        }
    }
}
