package com.dksys.biz.user.am.queue.service.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;
import com.dksys.biz.user.am.util.ApprovalNotificationDispatchMode;

/** SCHEDULER 모드에서만 AM 알림 큐를 주기적으로 폴링해 발송한다. EVENT 모드에서는 폴링하지 않는다(리스너가 커밋 직후 드레인). */
@Component
public class ApprovalNotificationScheduler {

    private final Logger logger = LoggerFactory.getLogger(ApprovalNotificationScheduler.class);

    @Autowired
    private ApprovalQueueSvc approvalQueueSvc;

    // 알림 발송 트리거 방식: SCHEDULER(주기적 폴링, 기본) | EVENT(스케줄러 폴링 OFF, 결재 이벤트 커밋 직후 즉시 발송)
    @Value("${approval.notification.dispatch-mode:SCHEDULER}")
    private String dispatchMode;

    @PostConstruct
    public void logDispatchMode() {
        if (ApprovalNotificationDispatchMode.isEventMode(dispatchMode)) {
            logger.info("[ApprovalNotification] dispatch-mode=EVENT: 알림 스케줄러 폴링 비활성");
        }
    }

    @Scheduled(fixedDelayString = "${approval.notification.queue.fixed-delay-ms:30000}")
    public void processNotificationQueue() {
        if (ApprovalNotificationDispatchMode.isEventMode(dispatchMode)) {
            return;
        }
        approvalQueueSvc.retryPendingNotifications();
    }
}
