package com.dksys.biz.user.am.event;

import java.util.HashMap;
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

@Component
public class ApprovalNotificationEventListener {

    private final Logger logger = LoggerFactory.getLogger(ApprovalNotificationEventListener.class);

    @Autowired(required = false)
    private BM18Svc bm18Svc;

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
                    // 다음 결재자에게 알림 발송
                    sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                            "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다",
                            "[" + event.getDocNo() + "] 결재 대기 문서가 도착했습니다: " + event.getDocTitle(),
                            event.getDocId());
                    break;
                case "COMPLETE":
                    // 기안자에게 최종 승인 완료 알림 발송
                    sendNotification(event.getDraUserId(), event.getDraUserNm(),
                            "[" + event.getDocNo() + "] 결재 승인 완료",
                            "[" + event.getDocNo() + "] 상신하신 문서가 최종 승인 완료되었습니다: " + event.getDocTitle(),
                            event.getDocId());
                    break;
                case "REJECT":
                    // 기안자에게 반려 알림 발송
                    sendNotification(event.getDraUserId(), event.getDraUserNm(),
                            "[" + event.getDocNo() + "] 결재 반려",
                            "[" + event.getDocNo() + "] 상신하신 문서가 반려되었습니다: " + event.getDocTitle(),
                            event.getDocId());
                    break;
                case "CANCEL":
                    // 취소 알림
                    sendNotification(event.getNextApproverId(), event.getNextApproverNm(),
                            "[" + event.getDocNo() + "] 결재 상신 취소",
                            "[" + event.getDocNo() + "] 기안자가 문서를 회수(상신 취소)하였습니다.",
                            event.getDocId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error("[ApprovalNotification] 알림 발송 이벤트 처리 중 예외 발생: eventType={}, docId={}",
                    event.getEventType(), event.getDocId(), e);
        }
    }

    private void sendNotification(String receiverId, String receiverNm, String title, String message, String docId) {
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
