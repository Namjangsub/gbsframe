package com.dksys.biz.user.am.postprocessor;

import java.util.Map;

/**
 * 전자결재 완료/반려/취소 시 ERP 업무 상태를 동기화하기 위한 사후처리 인터페이스
 */
public interface ApprovalPostProcessor {

    /**
     * 지원하는 ERP 업무 구분 코드 (TB_AM11M01.ERP_BIZ_TYPE)
     * 예: "PM51_TRIP" (출장), "CR09_EST" (견적), "FI01_EXP" (지출결의) 등
     */
    String getBizType();

    /**
     * 최종 승인 완료 시 ERP 업무 반영 (단일 트랜잭션 내에서 실행)
     */
    void onApprovalCompleted(Map<String, Object> docInfo, Map<String, Object> paramMap);

    /**
     * 반려 시 ERP 업무 반영
     */
    void onApprovalRejected(Map<String, Object> docInfo, Map<String, Object> paramMap);

    /**
     * 상신 취소(회수) 시 ERP 업무 반영
     */
    void onApprovalCancelled(Map<String, Object> docInfo, Map<String, Object> paramMap);
}
