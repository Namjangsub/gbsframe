package com.dksys.biz.user.am.am11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AM11Mapper {

    String selectNextDocId();

    String selectNextDocNo(Map<String, Object> paramMap);

    Map<String, Object> selectApprovalDocForUpdate(Map<String, Object> paramMap);
    int selectLinkedSourceCount(Map<String, Object> paramMap);

    Map<String, Object> selectApprovalDocInfo(Map<String, Object> paramMap);
    String selectPm08TeamManagerYn(Map<String, Object> paramMap);

    List<Map<String, Object>> selectApprovalLineList(Map<String, Object> paramMap);

    List<Map<String, Object>> selectApprovalHistList(Map<String, Object> paramMap);

    int insertApprovalDoc(Map<String, Object> paramMap);

    int updateApprovalDoc(Map<String, Object> paramMap);

    int updateApprovalDocContent(Map<String, Object> paramMap);

    int updateApprovalDocStatus(Map<String, Object> paramMap);

    int insertApprovalLine(Map<String, Object> paramMap);

    int updateApprovalLineStatus(Map<String, Object> paramMap);

    int updateNextLinePending(Map<String, Object> paramMap);

    int cancelArbitCurrentLine(Map<String, Object> paramMap);

    Map<String, Object> selectArbitLineForCancel(Map<String, Object> paramMap);

    int resetArbitRemainingLines(Map<String, Object> paramMap);

    int resetWb20ArbitRemainingLines(Map<String, Object> paramMap);

    int updateRemainingLinesArbit(Map<String, Object> paramMap);

    int updateRemainingLinesArbitByDiv2(Map<String, Object> paramMap);

    int updateWb20RemainingLinesArbit(Map<String, Object> paramMap);

    int updatePm51TripReqAprvSts(Map<String, Object> paramMap);

    int insertApprovalHist(Map<String, Object> paramMap);

    int deleteApprovalLines(Map<String, Object> paramMap);

    Map<String, Object> selectActiveDelegation(Map<String, Object> paramMap);

    Map<String, Object> selectUserOrgSnapshot(String userId);

    int insertAuditLog(Map<String, Object> paramMap);

    // 1. 문서 열람(읽음) 처리
    int mergeDocReadStatus(Map<String, Object> paramMap);

    List<Map<String, Object>> selectDocReadList(Map<String, Object> paramMap);

    // 2. 결재선 변경 이력
    int insertLineChangeHist(Map<String, Object> paramMap);

    int deleteRemainingApprovalLines(Map<String, Object> paramMap);

    // 3. 알림 재처리 큐
    int insertNotificationQueue(Map<String, Object> paramMap);

    int updateNotificationQueueStatus(Map<String, Object> paramMap);

    List<Map<String, Object>> selectPendingNotificationList(Map<String, Object> paramMap);

    // 4. ERP 보상처리 큐
    int insertErpCompensation(Map<String, Object> paramMap);

    int updateErpCompensationStatus(Map<String, Object> paramMap);

    List<Map<String, Object>> selectPendingCompensationList(Map<String, Object> paramMap);

    // 5. 서버 권한 검증 및 CAS 선점
    String selectUserAuthInfo(String userId);

    int updateNotificationProcessing(String notifId);

    int updateCompensationProcessing(String compId);

    // 6. ERP 결재/공유(TB_WB20M03) 실시간 연동
    int updateCommonTodoApprovalCompleted(Map<String, Object> paramMap);


    int updateCommonTodoApprovalRejected(Map<String, Object> paramMap);

    int updateCommonTodoApprovalCancelled(Map<String, Object> paramMap);

    // 7. ERP 연계 비즈니스 키(todoNo, todoKey 등)로 AM 전자결재 docId 조회
    String selectDocIdByBizKey(Map<String, Object> paramMap);
}
