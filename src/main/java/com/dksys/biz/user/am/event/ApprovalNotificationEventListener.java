package com.dksys.biz.user.am.event;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.google.gson.Gson;

@Component
public class ApprovalNotificationEventListener {

    private final Logger logger = LoggerFactory.getLogger(ApprovalNotificationEventListener.class);

    @Value("${kakaoSend:false}")
    private boolean kakaoSend;

    // 알림 발송 트리거 방식: SCHEDULER(주기적 폴링, 기본) | EVENT(이벤트 발생 시 즉시 일괄 발송)
    @Value("${approval.notification.dispatch-mode:SCHEDULER}")
    private String dispatchMode;

    @Value("${GBS_TALK_API_URL:}") private String talkApiUrl;
    @Value("${GBS_TALK_AUTH_TOKEN:}") private String talkAuthToken;
    @Value("${GBS_TALK_SERVER_NAME:}") private String talkServerName;
    @Value("${GBS_TALK_PAYMENT_TYPE:}") private String talkPaymentType;
    @Value("${GBS_TALK_SERVICE:}") private String talkService;

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    @Autowired(required = false)
    private ApprovalQueueSvc approvalQueueSvc;

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
                    if ("PM51".equals(erpBizType)) {
                        sendPm51Notification(event);
                    } else if ("PM07".equals(erpBizType) || "PM08".equals(erpBizType)) {
                        sendRichNotification(event);
                    } else if ("CR02".equals(erpBizType)) {
                        if (!sendRichNotification(event)) {
                            sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                                    "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다",
                                    "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다: " + event.getDocTitle(),
                                    event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
                        }
                    } else {
                        sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다",
                                "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다: " + event.getDocTitle(),
                                event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
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
                            event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
                    break;
                case "REJECT":
                    // 기안자에게 반려 알림 발송
                    sendNotification(event.getDraUserId(), event.getDraUserNm(),
                            "[" + event.getDocNo() + "] 결재 반려",
                            "[" + event.getDocNo() + "] 상신하신 문서가 반려되었습니다: " + event.getDocTitle()
                                    + approvalOpinionSuffix(event),
                            event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
                    break;
                case "CANCEL":
                    // 취소 알림
                    sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                            "[" + event.getDocNo() + "] 결재 상신 취소",
                            "[" + event.getDocNo() + "] 기안자가 문서를 회수(상신 취소)하였습니다.",
                            event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("[ApprovalNotification] 알림 발송 이벤트 처리 중 예외 발생: eventType={}, docId={}",
                    event.getEventType(), event.getDocId(), e);
        }

        // 건별(EVENT) 발송 모드: 이 이벤트로 적재된 알림을 즉시 일괄 드레인한다(스케줄러 대기 없이 발송).
        // 리스너는 @Async(앰비언트 트랜잭션 없음)이고 enqueue는 각각 커밋되므로, 여기서 드레인하면
        // REQUIRES_NEW 실행기가 커밋된 큐 행을 정상 조회한다.
        if ("EVENT".equalsIgnoreCase(dispatchMode) && approvalQueueSvc != null) {
            try {
                approvalQueueSvc.retryPendingNotifications();
            } catch (Exception e) {
                logger.warn("[ApprovalNotification] EVENT 즉시 발송(드레인) 경고: docId={}", event.getDocId(), e);
            }
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
                event.getDocId(), approvalTodoNo(event), approvalTodoDiv2CodeId(event));
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

    private String approvalTodoNo(ApprovalEvent event) {
        if (event.getExtraInfo() != null) {
            String[] keys = {"todoNo", "wb20TodoNo", "erpBizKey"};
            for (String key : keys) {
                Object value = event.getExtraInfo().get(key);
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return String.valueOf(value).trim();
                }
            }
        }
        return null;
    }

    private String approvalTodoDiv2CodeId(ApprovalEvent event) {
        if (event.getExtraInfo() != null) {
            String[] keys = {"todoDiv2CodeId", "wb20Div2CodeId"};
            for (String key : keys) {
                Object value = event.getExtraInfo().get(key);
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return String.valueOf(value).trim();
                }
            }
        }
        return null;
    }

    /** PM51은 WB20에 신청/관리부서 결재선을 일괄 생성하므로 실제 현재 결재선 코드를 사용한다. */
    private void sendPm51Notification(ApprovalEvent event) {
        sendRichNotification(event, resolvePm51TodoDiv2CodeId(event));
    }

    private String resolvePm51TodoDiv2CodeId(ApprovalEvent event) {
        if (event.getExtraInfo() == null || event.getExtraInfo().get("todoDiv2CodeId") == null) {
            return null;
        }
        return String.valueOf(event.getExtraInfo().get("todoDiv2CodeId"));
    }

    private boolean sendRichNotification(ApprovalEvent event) {
        return sendRichNotification(event, null);
    }

    private boolean sendRichNotification(ApprovalEvent event, String resolvedTodoDiv2CodeId) {
        if (event.getNextApproverId() == null || event.getNextApproverId().trim().isEmpty() || bm18Svc == null) {
            return false;
        }

        try {
            Map<String, Object> extraInfo = event.getExtraInfo();
            String erpBizKey = (String) extraInfo.get("erpBizKey");
            String coCd = (String) extraInfo.get("coCd");
            String erpBizType = (String) extraInfo.get("erpBizType");
            Map<String, Object> paramMap = extraInfo;

            if (erpBizType == null || erpBizType.isEmpty() || erpBizKey == null || erpBizKey.isEmpty()
                    || coCd == null || coCd.isEmpty()) {
                logger.warn("[ApprovalNotification] PM07/PM08/PM51 리치 발송 조건 미충족: erpBizType={}, erpBizKey={}, coCd={}",
                        erpBizType, erpBizKey, coCd);
                return false;
            }

            String todoDiv2CodeId = null;
            if ("PM07".equals(erpBizType)) {
                todoDiv2CodeId = "TODODIV2300";
            } else if ("PM08".equals(erpBizType)) {
                todoDiv2CodeId = (String) event.getExtraInfo().get("todoDiv2CodeId");
                if (todoDiv2CodeId == null || todoDiv2CodeId.isEmpty()) {
                    todoDiv2CodeId = "TODODIV2410";
                }
            } else if ("PM51".equals(erpBizType)) {
                todoDiv2CodeId = resolvedTodoDiv2CodeId;
            } else if ("CR02".equals(erpBizType)) {
                todoDiv2CodeId = "TODODIV2100";
            }
            if (todoDiv2CodeId == null) {
                return false;
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
                return false;
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
                return false;
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
                // #{formNm}: 업무별 양식명(제목 표기용). 공통 템플릿 1개로도 업무마다 올바른 양식명이 채워진다.
                finalMessage = finalMessage.replace("#{formNm}", resolveFormNm(erpBizType, todoDiv2CodeId));

                if ("PM51".equals(erpBizType)) {
                    String tripPeriod = resolvePm51TripPeriod(paramMap);
                    finalMessage = finalMessage.replace("#{tripPeriod}", tripPeriod);
                    if (!tripPeriod.isEmpty() && finalMessage.indexOf("#{tripPeriod}") < 0) {
                        int requestWorkIndex = finalMessage.indexOf("요청업무 :");
                        if (requestWorkIndex >= 0) {
                            int lineEnd = finalMessage.indexOf('\n', requestWorkIndex);
                            if (lineEnd < 0) lineEnd = finalMessage.length();
                            finalMessage = finalMessage.substring(0, lineEnd) + " [" + tripPeriod + "]"
                                    + finalMessage.substring(lineEnd);
                        }
                    }
                }
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
            logParam.put("creatPgm", "CR02".equals(erpBizType) ? "CR0202P01" : "PM0701P01");
            logParam.put("todoNo", erpBizKey);
            logParam.put("todoDiv2CodeId", todoDiv2CodeId);

            String sendgStatus = "READY";
            if (!kakaoSend) {
                logParam.put("sendgStatus", "READY");
                bm18Svc.insertKakaoMessage(logParam);
                return true;
            }
            try {
                if (talkApiUrl == null || talkApiUrl.trim().isEmpty()
                        || talkAuthToken == null || talkAuthToken.trim().isEmpty()
                        || talkServerName == null || talkServerName.trim().isEmpty()
                        || talkPaymentType == null || talkPaymentType.trim().isEmpty()
                        || talkService == null || talkService.trim().isEmpty()) {
                    logger.warn("[ApprovalNotification] 알림톡 환경변수 미설정으로 실발송을 건너뜁니다: messageId={}", maxMessageId);
                    logParam.put("sendgStatus", "READY");
                    bm18Svc.insertKakaoMessage(logParam);
                    return true;
                }
                URL url = new URL(talkApiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("authToken", talkAuthToken);
                conn.setRequestProperty("serverName", talkServerName);
                conn.setRequestProperty("paymentType", talkPaymentType);
                conn.setDoOutput(true);

                Map<String, String> talkBody = new HashMap<>();
                talkBody.put("service", talkService);
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
                    logger.info("[ApprovalNotification] PM07/PM08/CR02 리치 실발송 성공: messageId={}, receiver={}", maxMessageId, event.getNextApproverId());
                } else {
                    logger.warn("[ApprovalNotification] talkapi 발송 실패 (responseCode={}): messageId={}", responseCode, maxMessageId);
                }
            } catch (Exception kakaoEx) {
                logger.warn("[ApprovalNotification] talkapi 발송 예외: {}", kakaoEx.getMessage());
            }

            logParam.put("sendgStatus", sendgStatus);
            bm18Svc.insertKakaoMessage(logParam);
            return true;

        } catch (Exception e) {
            logger.warn("[ApprovalNotification] PM07/PM08/CR02 리치 발송 중 예외로 발송하지 않습니다: {}", e.getMessage());
            return false;
        }
    }

    private String resolvePm51TripPeriod(Map<String, Object> paramMap) {
        String start = textValue(paramMap.get("tripStDtm"));
        String end = textValue(paramMap.get("tripEdDtm"));
        if ((start.isEmpty() || end.isEmpty()) && paramMap.get("docDataJson") != null) {
            try {
                Map<String, Object> docData = new Gson().fromJson(String.valueOf(paramMap.get("docDataJson")), Map.class);
                if (start.isEmpty()) start = textValue(docData.get("tripStDtm"));
                if (end.isEmpty()) end = textValue(docData.get("tripEdDtm"));
            } catch (Exception e) {
                logger.warn("[ApprovalNotification] PM51 출장기간 정보 파싱 실패: docId={}", paramMap.get("docId"));
            }
        }
        if (start.length() < 8 || end.length() < 8) return "";
        try {
            LocalDate startDate = LocalDate.parse(start.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);
            LocalDate endDate = LocalDate.parse(end.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);
            long nights = Math.max(0, ChronoUnit.DAYS.between(startDate, endDate));
            long days = nights + 1;
            return startDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "~"
                    + endDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " "
                    + nights + "박" + days + "일";
        } catch (Exception e) {
            return "";
        }
    }

    private String textValue(Object value) {
        return value == null ? "" : String.valueOf(value).replaceAll("[^0-9]", "");
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

    /** 알림 메시지 제목 표기용 업무 양식명. 공통 템플릿의 #{formNm} 치환값. */
    private String resolveFormNm(String erpBizType, String todoDiv2CodeId) {
        if ("PM08".equals(erpBizType)) {
            return "TODODIV2420".equals(todoDiv2CodeId) ? "[휴일대체근무 결과보고]" : "[휴일대체근무 신청서]";
        }
        if ("PM51".equals(erpBizType)) {
            return "[출장신청서]";
        }
        if ("PM07".equals(erpBizType)) {
            return "[휴가신청서]";
        }
        if ("CR02".equals(erpBizType)) {
            return "[수주목표원가]";
        }
        return "";
    }

    private void sendNotification(String receiverId, String receiverNm, String title, String message, String docId,
            String todoNo, String todoDiv2CodeId) {
        if (receiverId == null || receiverId.trim().isEmpty()
                || todoNo == null || todoNo.trim().isEmpty()
                || todoDiv2CodeId == null || todoDiv2CodeId.trim().isEmpty()) {
            logger.warn("[ApprovalNotification] 필수 알림 식별값 누락으로 발송 생략: receiverId={}, todoNo={}, todoDiv2CodeId={}",
                    receiverId, todoNo, todoDiv2CodeId);
            return;
        }

        String maskedTitle = ApprovalSecurityUtil.maskSensitiveData(title);
        String maskedMsg = ApprovalSecurityUtil.maskSensitiveData(message);

        logger.info("[ApprovalNotification -> Receiver({}): {}] Title: {}, Message: {}",
                receiverId, receiverNm, maskedTitle, maskedMsg);

        if (approvalQueueSvc != null) {
            try {
                String receiverMobile = selectApproverMobile(receiverId, null);
                Map<String, Object> queueParam = new HashMap<>();
                queueParam.put("docId", docId);
                queueParam.put("eventType", "APPROVAL_NOTIFICATION");
                queueParam.put("receiverId", receiverId);
                queueParam.put("receiverNm", receiverNm);
                queueParam.put("receiverMobile", receiverMobile);
                queueParam.put("notifChannel", "KAKAO");
                queueParam.put("notifTitle", maskedTitle);
                queueParam.put("notifMsg", maskedMsg);
                queueParam.put("messageId", "AM_" + System.currentTimeMillis());
                queueParam.put("tmplatDiv", "TMPLATDIV02");
                queueParam.put("todoNo", todoNo);
                queueParam.put("todoDiv2CodeId", todoDiv2CodeId);
                queueParam.put("creatPgm", "AM_ALIM");
                approvalQueueSvc.enqueueNotification(queueParam);
                return;
            } catch (Exception e) {
                logger.warn("[ApprovalNotification] AM 알림 큐 적재 실패: receiverId={}, err={}", receiverId, e.getMessage());
            }
        }

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
                kakaoParam.put("mobile", selectApproverMobile(receiverId, null));
                kakaoParam.put("nameTo", receiverNm);
                kakaoParam.put("creatId", "SYSTEM");
                kakaoParam.put("creatPgm", "AM_ALIM");
                kakaoParam.put("todoNo", todoNo);
                kakaoParam.put("todoDiv2CodeId", todoDiv2CodeId);
                bm18Svc.insertKakaoMessage(kakaoParam);
                logger.info("[ApprovalNotification] TB_BM18M01 알림 메시지 발송 테이블 INSERT 성공: receiverId={}", receiverId);
            } catch (Exception e) {
                logger.warn("[ApprovalNotification] 카카오 발송 테이블 등록 실패 (재처리 큐가 후속 처리): receiverId={}, err={}", receiverId, e.getMessage());
            }
        }
    }
}
