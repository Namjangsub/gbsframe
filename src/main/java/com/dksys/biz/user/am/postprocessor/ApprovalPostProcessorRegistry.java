package com.dksys.biz.user.am.postprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApprovalPostProcessorRegistry {

    private final Logger logger = LoggerFactory.getLogger(ApprovalPostProcessorRegistry.class);
    private final Map<String, ApprovalPostProcessor> processorMap = new HashMap<>();

    @Autowired(required = false)
    public ApprovalPostProcessorRegistry(List<ApprovalPostProcessor> processors) {
        if (processors != null) {
            for (ApprovalPostProcessor processor : processors) {
                processorMap.put(processor.getBizType(), processor);
                logger.info("[ApprovalPostProcessor] Registered processor for bizType: {}", processor.getBizType());
            }
        }
    }

    public void processCompleted(String bizType, Map<String, Object> docInfo, Map<String, Object> paramMap) {
        ApprovalPostProcessor processor = resolveProcessor(bizType);
        if (processor != null) {
            logger.info("[ApprovalPostProcessor] Executing onApprovalCompleted for bizType: {}, docId: {}", processor.getBizType(), docInfo != null ? docInfo.get("docId") : null);
            processor.onApprovalCompleted(docInfo, paramMap);
        } else {
            logger.debug("[ApprovalPostProcessor] No processor available for bizType: {}", bizType);
        }
    }

    public void processRejected(String bizType, Map<String, Object> docInfo, Map<String, Object> paramMap) {
        ApprovalPostProcessor processor = resolveProcessor(bizType);
        if (processor != null) {
            logger.info("[ApprovalPostProcessor] Executing onApprovalRejected for bizType: {}, docId: {}", processor.getBizType(), docInfo != null ? docInfo.get("docId") : null);
            processor.onApprovalRejected(docInfo, paramMap);
        }
    }

    public void processCancelled(String bizType, Map<String, Object> docInfo, Map<String, Object> paramMap) {
        ApprovalPostProcessor processor = resolveProcessor(bizType);
        if (processor != null) {
            logger.info("[ApprovalPostProcessor] Executing onApprovalCancelled for bizType: {}, docId: {}", processor.getBizType(), docInfo != null ? docInfo.get("docId") : null);
            processor.onApprovalCancelled(docInfo, paramMap);
        }
    }

    private ApprovalPostProcessor resolveProcessor(String bizType) {
        if (bizType != null && !bizType.trim().isEmpty()) {
            ApprovalPostProcessor processor = processorMap.get(bizType);
            if (processor != null) {
                return processor;
            }
        }
        // Fallback: 등록된 COMMON_TODO 기본 프로세서 반환
        return processorMap.get("COMMON_TODO");
    }
}
