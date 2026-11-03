package com.dksys.biz.user.am.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** jsonView 응답에만 알림 발송 경고(notifyWarning)를 추가한다. 경고가 없으면 응답은 변하지 않는다. */
public class ApprovalNotificationWarningInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        Object warning = request.getAttribute(ApprovalNotificationWarning.ATTR_NAME);
        if (warning != null && modelAndView != null && "jsonView".equals(modelAndView.getViewName())) {
            modelAndView.addObject(ApprovalNotificationWarning.RESPONSE_KEY, warning);
        }
    }
}
