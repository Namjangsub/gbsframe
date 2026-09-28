package com.dksys.biz.user.am.queue.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;

/** AM 알림 큐를 서비스의 JDK 프록시와 분리해 주기적으로 실행한다. */
@Component
public class ApprovalNotificationScheduler {

    @Autowired
    private ApprovalQueueSvc approvalQueueSvc;

    // 알림 발송 트리거 방식: SCHEDULER(주기적 폴링, 기본) | EVENT(결재 이벤트 발생 시에만 일괄 발송)
    @org.springframework.beans.factory.annotation.Value("${approval.notification.dispatch-mode:SCHEDULER}")
    private String dispatchMode;

    @Scheduled(fixedDelayString = "${approval.notification.queue.fixed-delay-ms:30000}")
    public void processNotificationQueue() {
        // EVENT 모드에서는 스케줄러 폴링을 비활성화한다(이벤트 커밋 후 리스너가 즉시 큐를 드레인).
        if (!"SCHEDULER".equalsIgnoreCase(dispatchMode)) {
            return;
        }
        approvalQueueSvc.retryPendingNotifications();
    }
}
