package com.dksys.biz.user.am.queue.service;

import java.util.List;
import java.util.Map;

public interface ApprovalQueueSvc {

    // 1. 알림 큐
    Map<String, Object> enqueueNotification(Map<String, Object> paramMap);

    Map<String, Object> retryPendingNotifications();

    // 2. ERP 보상처리 큐
    Map<String, Object> enqueueErpCompensation(Map<String, Object> paramMap);

    Map<String, Object> retryPendingCompensations();

    List<Map<String, Object>> selectPendingCompensationList(Map<String, Object> paramMap);

    Map<String, Object> resolveCompensationManually(Map<String, Object> paramMap);
}
