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
        // 신규 알림은 결재 이벤트 발생 즉시 리스너가 직접 발송한다(dispatch-mode 와 무관).
        // 큐에는 일시 오류로 실패한 재시도 건만 쌓이므로, 다음 결재 이벤트를 기다리지 않고 재시도 시각이 되면 모든 모드에서 폴링해 재발송한다.
        // (kakaoSend=false 이면 큐 적재 자체가 없고, 남은 행은 실행기가 SKIPPED 로 종료한다.)
        approvalQueueSvc.retryPendingNotifications();
    }
}
