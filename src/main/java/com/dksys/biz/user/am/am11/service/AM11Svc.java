package com.dksys.biz.user.am.am11.service;

import java.util.Map;

public interface AM11Svc {

    Map<String, Object> selectApprovalDocDetail(Map<String, Object> paramMap);

    Map<String, Object> saveApprovalDraft(Map<String, Object> paramMap);

    Map<String, Object> submitApproval(Map<String, Object> paramMap);

    Map<String, Object> approveDocument(Map<String, Object> paramMap);

    Map<String, Object> rejectDocument(Map<String, Object> paramMap);

    Map<String, Object> cancelApproval(Map<String, Object> paramMap);
    Map<String, Object> cancelArbitDocument(Map<String, Object> paramMap);

    Map<String, Object> arbitDocument(Map<String, Object> paramMap);

    // 1. 문서 열람 기록 및 열람 목록 조회
    Map<String, Object> recordDocumentRead(Map<String, Object> paramMap);

    java.util.List<Map<String, Object>> selectDocReadList(Map<String, Object> paramMap);

    // 2. 결재선 변경 (미결재 잔여 순번 대상)
    Map<String, Object> changeApprovalLines(Map<String, Object> paramMap);

    // 3. ERP 연계 비즈니스 키로 AM 전자결재 docId 조회
    String selectDocIdByBizKey(Map<String, Object> paramMap);

    // 4. 무알림 결재선 재동기화 (진행 전 전용, CR02 수주 삭제 시)
    Map<String, Object> resyncApprovalLinesPreApproval(Map<String, Object> paramMap);

    // 마이그레이션: CR02 문서 조회 및 업데이트
    java.util.List<Map<String, Object>> selectCr02DocsForRerender();

    int updateDocRenderHtmlById(Map<String, Object> paramMap);
}
