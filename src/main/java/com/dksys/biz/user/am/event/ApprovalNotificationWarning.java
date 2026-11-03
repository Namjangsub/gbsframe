package com.dksys.biz.user.am.event;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * EVENT 모드 알림톡 발송 오류를 같은 HTTP 요청의 응답에 실어 보내기 위한 요청 범위 경고 홀더.
 * 요청 스레드가 아니면(스케줄러 등) 아무것도 하지 않는다. 응답 주입은 ApprovalNotificationWarningInterceptor 가 맡는다.
 */
public final class ApprovalNotificationWarning {

    public static final String ATTR_NAME = ApprovalNotificationWarning.class.getName() + ".WARNING";
    public static final String RESPONSE_KEY = "notifyWarning";
    public static final String MESSAGE = "알림톡 발송에 실패했습니다. 결재 처리는 완료되었으며 발송 이력에 실패가 기록되었습니다.";

    private ApprovalNotificationWarning() {
    }

    public static void record() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(ATTR_NAME, MESSAGE, RequestAttributes.SCOPE_REQUEST);
        }
    }
}
