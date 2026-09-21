package com.dksys.biz.user.am.queue.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am11.mapper.AM11Mapper;
import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;
import com.dksys.biz.user.am.queue.service.impl.ApprovalQueueItemExecutor.ExecutionResult;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalQueueSvcImpl implements ApprovalQueueSvc {

    private final Logger logger = LoggerFactory.getLogger(ApprovalQueueSvcImpl.class);

    @Autowired
    private AM11Mapper am11Mapper;

    @Autowired
    private ApprovalQueueItemExecutor itemExecutor;

    @Override
    public Map<String, Object> enqueueNotification(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 민감정보 사전 마스킹
            if (paramMap.containsKey("notifTitle") && paramMap.get("notifTitle") != null) {
                paramMap.put("notifTitle", ApprovalSecurityUtil.maskSensitiveData(String.valueOf(paramMap.get("notifTitle"))));
            }
            if (paramMap.containsKey("notifMsg") && paramMap.get("notifMsg") != null) {
                paramMap.put("notifMsg", ApprovalSecurityUtil.maskSensitiveData(String.valueOf(paramMap.get("notifMsg"))));
            }
            am11Mapper.insertNotificationQueue(paramMap);
            resultMap.put("resultCode", "200");
            resultMap.put("notifId", paramMap.get("notifId"));
            resultMap.put("resultMessage", "알림이 큐에 성공적으로 적재되었습니다.");
        } catch (Exception e) {
            logger.error("알림 큐 적재 실패: docId={}", paramMap.get("docId"), e);
            throw new RuntimeException("알림 큐 적재 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
        return resultMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> retryPendingNotifications() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> pendingList = am11Mapper.selectPendingNotificationList(new HashMap<>());
        int successCnt = 0;
        int failCnt = 0;
        int skippedCnt = 0;

        for (Map<String, Object> item : pendingList) {
            // 독립 트랜잭션(REQUIRES_NEW)으로 단건 CAS 선점 및 실제 발송 위임
            ExecutionResult result = itemExecutor.acquireAndExecuteNotification(item);
            if (result == ExecutionResult.SUCCESS) {
                successCnt++;
            } else if (result == ExecutionResult.FAIL) {
                failCnt++;
            } else {
                skippedCnt++;
            }
        }

        resultMap.put("resultCode", "200");
        resultMap.put("totalTarget", pendingList.size());
        resultMap.put("successCnt", successCnt);
        resultMap.put("failCnt", failCnt);
        resultMap.put("skippedCnt", skippedCnt);
        resultMap.put("resultMessage", "알림 재처리 완료 (성공: " + successCnt + ", 실패: " + failCnt + ", 타 Worker 선점 건너뜀: " + skippedCnt + ")");
        return resultMap;
    }

    @Override
    public Map<String, Object> enqueueErpCompensation(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 에러 메시지 민감정보 마스킹
            if (paramMap.containsKey("errMsg") && paramMap.get("errMsg") != null) {
                paramMap.put("errMsg", ApprovalSecurityUtil.maskSensitiveData(String.valueOf(paramMap.get("errMsg"))));
            }
            am11Mapper.insertErpCompensation(paramMap);
            resultMap.put("resultCode", "200");
            resultMap.put("compId", paramMap.get("compId"));
            resultMap.put("resultMessage", "ERP 보상처리 항목이 큐에 적재되었습니다.");
        } catch (Exception e) {
            logger.error("ERP 보상처리 큐 적재 실패: docId={}", paramMap.get("docId"), e);
            throw new RuntimeException("ERP 보상처리 큐 적재 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
        return resultMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> retryPendingCompensations() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> pendingList = am11Mapper.selectPendingCompensationList(new HashMap<>());
        int successCnt = 0;
        int failCnt = 0;
        int skippedCnt = 0;

        for (Map<String, Object> item : pendingList) {
            // 독립 트랜잭션(REQUIRES_NEW)으로 단건 CAS 선점 및 실제 ERP 연동 위임
            ExecutionResult result = itemExecutor.acquireAndExecuteCompensation(item);
            if (result == ExecutionResult.SUCCESS) {
                successCnt++;
            } else if (result == ExecutionResult.FAIL) {
                failCnt++;
            } else {
                skippedCnt++;
            }
        }

        resultMap.put("resultCode", "200");
        resultMap.put("totalTarget", pendingList.size());
        resultMap.put("successCnt", successCnt);
        resultMap.put("failCnt", failCnt);
        resultMap.put("skippedCnt", skippedCnt);
        resultMap.put("resultMessage", "ERP 보상 재처리 완료 (성공: " + successCnt + ", 실패/수동전환: " + failCnt + ", 타 Worker 선점 건너뜀: " + skippedCnt + ")");
        return resultMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> selectPendingCompensationList(Map<String, Object> paramMap) {
        return am11Mapper.selectPendingCompensationList(paramMap);
    }

    @Override
    public Map<String, Object> resolveCompensationManually(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("status", "SUCCESS");
        String reason = ApprovalSecurityUtil.maskSensitiveData("관리자 수동 조치 완료: " + paramMap.get("manualComment"));
        paramMap.put("errMsg", reason);
        am11Mapper.updateErpCompensationStatus(paramMap);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "해당 ERP 보상 항목이 수동 조치 완료 처리되었습니다.");
        return resultMap;
    }
}
