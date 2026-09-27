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

    @Scheduled(fixedDelayString = "${approval.notification.queue.fixed-delay-ms:30000}")
    public void processNotificationQueue() {
        approvalQueueSvc.retryPendingNotifications();
    }
}
