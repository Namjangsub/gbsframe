package com.dksys.biz.user.am.util;

/**
 * 결재 알림 발송 방식(approval.notification.dispatch-mode) 판정 유틸리티
 */
public final class ApprovalNotificationDispatchMode {

    private ApprovalNotificationDispatchMode() {
        // 인스턴스화 방지
    }

    /**
     * dispatch-mode 값이 EVENT 인지 판정한다. 앞뒤 공백과 대소문자는 무시하며, null 이거나 그 외 값(SCHEDULER 포함)은 false 다.
     *
     * @param dispatchMode approval.notification.dispatch-mode 설정값
     * @return EVENT 모드이면 true
     */
    public static boolean isEventMode(String dispatchMode) {
        return dispatchMode != null && "EVENT".equalsIgnoreCase(dispatchMode.trim());
    }
}
