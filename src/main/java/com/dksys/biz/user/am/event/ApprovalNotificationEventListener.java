package com.dksys.biz.user.am.event;

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

import com.dksys.biz.user.am.notification.Kakao10009MessageBuilder;
import com.dksys.biz.user.am.notification.Kakao10009Sender;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.dksys.biz.user.wb.wb24.service.WB24Svc;
import com.google.gson.Gson;

/**
 * 결재 알림 리스너. 모든 결재 알림은 템플릿 10009 로 발송하며, 발송 경로는 Kakao10009Sender 한 곳이다.
 * - 요청/완료/반려/취소/의견 모두 sendTalk10009 가 즉시 직접 발송한다. 상태별 차이는 vars(변수 값)에만 있다.
 * - 일시 오류(HTTP 비200·예외)만 TB_AM91M01 에 적재해 실행기가 재시도한다. 업체 거부 건은 재시도하지 않는다.
 */
@Component
public class ApprovalNotificationEventListener {

    private final Logger logger = LoggerFactory.getLogger(ApprovalNotificationEventListener.class);

    @Value("${kakaoSend:false}")
    private boolean kakaoSend;

    // 알림 발송 트리거 방식: SCHEDULER(주기적 폴링, 기본) | EVENT(이벤트 발생 시 즉시 일괄 발송)
    @Value("${approval.notification.dispatch-mode:SCHEDULER}")
    private String dispatchMode;

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    @Autowired(required = false)
    private ApprovalQueueSvc approvalQueueSvc;

    @Autowired(required = false)
    private WB24Svc wb24Svc;

    @Autowired
    private Kakao10009MessageBuilder messageBuilder;

    @Autowired
    private Kakao10009Sender kakao10009Sender;

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
                    sendRequestNotification(event);
                    sendApplicantOpinionNotification(event, "결재 처리", false);
                    break;
                case "ARBIT":
                    sendApplicantOpinionNotification(event, "전결 처리", true);
                    break;
                case "COMPLETE":
                    sendCompleteNotification(event);
                    break;
                case "REJECT":
                    sendRejectNotification(event);
                    break;
                case "CANCEL":
                    sendCancelNotification(event);
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

    /** 결재 요청(상신/다음 결재자). 직접 발송 대상 업무(PM07/PM08/PM51/CR02)는 업무별 번호·결재구분 규칙을 쓰고, 그 외는 ERP 원자료 값을 쓴다. */
    private void sendRequestNotification(ApprovalEvent event) {
        String docTitle = messageBuilder.docTitle(event.getDocTitle());
        Map<String, String> vars = messageBuilder.vars(
                docTitle + " 건으로 결재",
                docTitle + " 건으로 결재 요청",
                todoCodeNameOf(event),
                event.getDraUserNm(),
                "결재 처리");
        String erpBizType = extra(event, "erpBizType");
        String todoNo = approvalTodoNo(event);
        String todoDiv2CodeId = approvalTodoDiv2CodeId(event);
        if (isRichType(erpBizType) && !extra(event, "erpBizKey").isEmpty() && !extra(event, "coCd").isEmpty()) {
            String richDiv2 = resolveRichDiv2(event, erpBizType);
            if (richDiv2 != null) {
                todoNo = extra(event, "erpBizKey");
                todoDiv2CodeId = richDiv2;
            }
        }
        sendTalk10009(event, event.getNextApproverId(), event.getNextApproverNm(), vars, todoNo, todoDiv2CodeId);
    }

    /** 완료: 기안자에게 최종 승인 완료 알림. 결재의견이 있으면 추가. */
    private void sendCompleteNotification(ApprovalEvent event) {
        String docTitle = messageBuilder.docTitle(event.getDocTitle());
        String cnts = docTitle + " 결재 완료" + opinionLine(event);
        Map<String, String> vars = messageBuilder.vars(docTitle + " 결재 완료", cnts,
                todoCodeNameOf(event), event.getDraUserNm(), "결과 확인");
        sendTalk10009(event, event.getDraUserId(), event.getDraUserNm(), vars);
    }

    /** 반려: 기안자에게 반려 알림. 반려자·결재의견 추가. */
    private void sendRejectNotification(ApprovalEvent event) {
        String docTitle = messageBuilder.docTitle(event.getDocTitle());
        String cnts = docTitle + " 결재 반려" + rejecterLine(event) + opinionLine(event);
        Map<String, String> vars = messageBuilder.vars(docTitle + " 결재 반려", cnts,
                todoCodeNameOf(event), event.getDraUserNm(), "반려 확인");
        sendTalk10009(event, event.getDraUserId(), event.getDraUserNm(), vars);
    }

    /** 취소(회수): 다음 결재자에게 상신 취소 알림. */
    private void sendCancelNotification(ApprovalEvent event) {
        String docTitle = messageBuilder.docTitle(event.getDocTitle());
        String cnts = docTitle + " 기안자가 문서를 회수(상신 취소)하였습니다.";
        Map<String, String> vars = messageBuilder.vars(docTitle + " 상신 취소", cnts,
                todoCodeNameOf(event), event.getDraUserNm(), "취소 확인");
        sendTalk10009(event, event.getNextApproverId(), event.getNextApproverNm(), vars);
    }

    /** 기안자에게 결재 의견 알림(결재 처리/전결). alwaysSend=false 면 의견이 없을 때 생략. */
    private void sendApplicantOpinionNotification(ApprovalEvent event, String action, boolean alwaysSend) {
        String opinion = approvalOpinion(event);
        if (!alwaysSend && opinion.isEmpty()) {
            return;
        }
        String docTitle = messageBuilder.docTitle(event.getDocTitle());
        String cnts = docTitle + "\n결재의견: " + (opinion.isEmpty() ? "(의견 없음)" : messageBuilder.value(opinion));
        Map<String, String> vars = messageBuilder.vars(docTitle + " " + action + " 의견", cnts,
                todoCodeNameOf(event), event.getDraUserNm(), "의견 확인");
        sendTalk10009(event, event.getDraUserId(), event.getDraUserNm(), vars);
    }

    private boolean isRichType(String erpBizType) {
        return "PM07".equals(erpBizType) || "PM08".equals(erpBizType)
                || "PM51".equals(erpBizType) || "CR02".equals(erpBizType);
    }

    /** 업무명 + (PM51 이면 출장기간). */
    private String todoCodeNameOf(ApprovalEvent event) {
        String erpBizType = extra(event, "erpBizType");
        String name = messageBuilder.todoCodeName(erpBizType, approvalTodoDiv2CodeId(event), vacTypeName(event));
        if ("PM51".equals(erpBizType)) {
            String period = resolvePm51TripPeriod(event.getExtraInfo() == null ? new HashMap<String, Object>() : event.getExtraInfo());
            if (!period.isEmpty()) {
                name = name + " [" + period + "]";
            }
        }
        return name;
    }

    /** 직접 발송 대상 업무의 결재구분(todoDiv2CodeId). 조건 미충족이면 null. */
    private String resolveRichDiv2(ApprovalEvent event, String erpBizType) {
        if ("PM07".equals(erpBizType)) {
            return "TODODIV2300";
        }
        if ("PM08".equals(erpBizType)) {
            String div2 = extra(event, "todoDiv2CodeId");
            return div2.isEmpty() ? "TODODIV2410" : div2;
        }
        if ("PM51".equals(erpBizType)) {
            String div2 = extra(event, "todoDiv2CodeId");
            return div2.isEmpty() ? null : div2;
        }
        if ("CR02".equals(erpBizType)) {
            return "TODODIV2100";
        }
        return null;
    }

    /** 4-인자 호출: 결재 업무 식별값(todoNo/todoDiv2CodeId)은 ERP 원자료 값을 쓴다. */
    private void sendTalk10009(ApprovalEvent event, String receiverId, String receiverNm, Map<String, String> vars) {
        sendTalk10009(event, receiverId, receiverNm, vars, approvalTodoNo(event), approvalTodoDiv2CodeId(event));
    }

    /**
     * 결재 알림 발송(템플릿 10009). 요청/완료/반려/취소/의견 모두 이 한 경로를 탄다. 상태별 차이는 vars 값에만 있다.
     * 발송은 Kakao10009Sender 로 즉시 수행한다. 일시 오류(재시도 가능)만 큐에 적재해 실행기가 재시도한다.
     */
    private void sendTalk10009(ApprovalEvent event, String receiverId, String receiverNm,
                               Map<String, String> vars, String todoNo, String todoDiv2CodeId) {
        if (!kakaoSend) {
            logger.info("[ApprovalNotification] kakaoSend=false 로 발송 대상에서 제외: docId={}, receiverId={}", event.getDocId(), receiverId);
            return;
        }
        if (isBlank(receiverId) || isBlank(todoNo) || isBlank(todoDiv2CodeId)) {
            logger.warn("[ApprovalNotification] 필수 알림 식별값 누락으로 발송 생략: receiverId={}, todoNo={}, todoDiv2CodeId={}",
                    receiverId, todoNo, todoDiv2CodeId);
            return;
        }
        String mobile = selectApproverMobile(receiverId, extra(event, "coCd"));
        if (isBlank(mobile)) {
            logger.warn("[ApprovalNotification] 수신번호 없음: receiverId={}", receiverId);
            return;
        }
        String title = messageBuilder.talkTitle(vars);
        String message = renderBody(event, vars);
        logger.info("[ApprovalNotification -> Receiver({}): {}] Title: {}", receiverId, receiverNm,
                ApprovalSecurityUtil.maskSensitiveData(title));

        Kakao10009Sender.Result result = kakao10009Sender.send(mobile, title, message, todoNo, todoDiv2CodeId);
        if (result.getMessageId() != null) {
            recordHistory(event, receiverId, receiverNm, mobile, title, message, todoNo, todoDiv2CodeId, result);
        }
        if (result.isOk()) {
            return;
        }
        if (!result.isRetryable()) {
            logger.error("[ApprovalNotification] 알림 발송 실패(재시도 없음): receiverId={}, status={}, detail={}",
                    receiverId, result.getStatus(), result.getDetail());
            return;
        }
        enqueueRetry(event, receiverId, receiverNm, mobile, title, message, todoNo, todoDiv2CodeId, result);
    }

    /** 발송 이력 적재. 이력 INSERT 실패는 격리하며 발송 결과에는 영향을 주지 않는다. */
    private void recordHistory(ApprovalEvent event, String receiverId, String receiverNm, String mobile,
                               String title, String message, String todoNo, String todoDiv2CodeId,
                               Kakao10009Sender.Result result) {
        if (bm18Svc == null) {
            return;
        }
        try {
            Map<String, String> logParam = new HashMap<>();
            logParam.put("mssageId", result.getMessageId());
            logParam.put("rcvId", receiverId);
            logParam.put("rcvNm", receiverNm);
            logParam.put("clntCd", "1");
            logParam.put("tmplatDiv", Kakao10009MessageBuilder.TMPLAT_DIV);
            logParam.put("sendgStatus", result.getStatus());
            logParam.put("title", title);
            logParam.put("mssage", message);
            logParam.put("mobile", mobile);
            logParam.put("nameTo", receiverNm);
            logParam.put("creatId", event.getDraUserId());
            logParam.put("creatPgm", creatPgmOf(extra(event, "erpBizType")));
            logParam.put("todoNo", todoNo);
            logParam.put("todoDiv2CodeId", todoDiv2CodeId);
            bm18Svc.insertKakaoMessage(logParam);
        } catch (Exception e) {
            logger.warn("[ApprovalNotification] 발송 이력 적재 실패(발송 결과는 유지): receiverId={}, err={}", receiverId, e.getMessage());
        }
    }

    /** 발송 이력의 생성 프로그램(업무별 화면 코드). 매핑이 없으면 AM_ALIM. */
    private String creatPgmOf(String erpBizType) {
        switch (erpBizType) {
            case "PM07":
                return "PM0701P01";
            case "PM08":
                return "PM0801P01";
            case "PM51":
                return "PM5101P01";
            case "PM52":
                return "PM5102P01";
            case "CR02":
                return "CR0202P01";
            default:
                return "AM_ALIM";
        }
    }

    /** 일시 오류 재시도 큐 적재. retryCnt=1 은 직접 발송 1회를 이미 센 값이다(MAX_RETRY 기본 3 → 총 3회). */
    private void enqueueRetry(ApprovalEvent event, String receiverId, String receiverNm, String mobile,
                              String title, String message, String todoNo, String todoDiv2CodeId,
                              Kakao10009Sender.Result result) {
        if (approvalQueueSvc == null) {
            logger.warn("[ApprovalNotification] 재시도 큐 없음(일시 오류 재시도 생략): receiverId={}, status={}", receiverId, result.getStatus());
            return;
        }
        try {
            Map<String, Object> queueParam = new HashMap<>();
            queueParam.put("docId", event.getDocId());
            queueParam.put("eventType", "APPROVAL_NOTIFICATION");
            queueParam.put("receiverId", receiverId);
            queueParam.put("receiverNm", receiverNm);
            queueParam.put("receiverMobile", mobile);
            queueParam.put("notifChannel", "KAKAO");
            queueParam.put("notifTitle", title);
            queueParam.put("notifMsg", message);
            queueParam.put("messageId", "AM_" + System.currentTimeMillis());
            queueParam.put("tmplatDiv", Kakao10009MessageBuilder.TMPLAT_DIV);
            queueParam.put("todoNo", todoNo);
            queueParam.put("todoDiv2CodeId", todoDiv2CodeId);
            queueParam.put("creatPgm", "AM_ALIM");
            queueParam.put("retryCnt", 1);
            approvalQueueSvc.enqueueNotification(queueParam);
        } catch (Exception e) {
            logger.warn("[ApprovalNotification] AM 알림 재시도 큐 적재 실패: receiverId={}, err={}", receiverId, e.getMessage());
        }
    }

    /** 공용 본문 조립: TMPLATDIV06 본문 로드 후 vars 로 치환. */
    private String renderBody(ApprovalEvent event, Map<String, String> vars) {
        return messageBuilder.render(messageBuilder.loadBody(extra(event, "coCd")), vars);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String extra(ApprovalEvent event, String key) {
        if (event.getExtraInfo() == null || event.getExtraInfo().get(key) == null) {
            return "";
        }
        return String.valueOf(event.getExtraInfo().get(key)).trim();
    }

    /** 결재의견 줄(없으면 빈 문자열). */
    private String opinionLine(ApprovalEvent event) {
        String opinion = approvalOpinion(event);
        return opinion.isEmpty() ? "" : "\n결재의견: " + messageBuilder.value(opinion);
    }

    private String rejecterLine(ApprovalEvent event) {
        String rejecterNm = extra(event, "rejectUserNm");
        return rejecterNm.isEmpty() ? "" : "\n반려자: " + messageBuilder.value(rejecterNm);
    }

    private String vacTypeName(ApprovalEvent event) {
        String vacTypeNm = extra(event, "vacTypeNm");
        if (!vacTypeNm.isEmpty()) {
            return vacTypeNm;
        }
        String docDataJson = extra(event, "docDataJson");
        if (docDataJson.isEmpty()) {
            return "";
        }
        try {
            Map<String, Object> docData = new Gson().fromJson(docDataJson, Map.class);
            Object value = docData == null ? null : docData.get("vacTypeNm");
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception parseException) {
            logger.warn("[ApprovalNotification] 원천 휴가유형 파싱 실패: {}", parseException.getMessage());
            return "";
        }
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
        List<Map<String, String>> memberList;
        try {
            memberList = wb24Svc.selectMemberTelNo(param);
        } catch (Exception e) {
            logger.warn("[ApprovalNotification] 수신번호 조회 실패: receiverId={}, err={}", approverId, e.getMessage());
            return null;
        }
        if (memberList == null || memberList.isEmpty()) {
            return null;
        }
        String mobile = memberList.get(0).get("telNo");
        return mobile == null || mobile.trim().isEmpty() ? null : mobile;
    }
}
