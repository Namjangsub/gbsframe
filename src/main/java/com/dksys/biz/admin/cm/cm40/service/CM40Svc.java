package com.dksys.biz.admin.cm.cm40.service;

import java.util.Map;

public interface CM40Svc {

    Map<String, Object> getFileOpenStatus();

    // 현재 로그인 사용자가 점유해제 허용 목록에 있는지(조회 응답의 canClose 용)
    boolean isCloseAuthorizedForCurrentUser();

    // 점유해제(열린 핸들 강제 닫기) 요청 처리. resultCode/resultMessage/(선택)requestId 반환
    Map<String, Object> closeOpenFile(Map<String, String> param);
}
