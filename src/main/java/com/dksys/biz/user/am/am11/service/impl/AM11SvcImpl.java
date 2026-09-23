package com.dksys.biz.user.am.am11.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Reader;
import java.sql.Clob;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.user.am.am11.mapper.AM11Mapper;
import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.user.am.event.ApprovalEvent;
import com.dksys.biz.user.am.postprocessor.ApprovalPostProcessorRegistry;
import com.dksys.biz.user.am.queue.service.ApprovalQueueSvc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional(rollbackFor = Exception.class)
public class AM11SvcImpl implements AM11Svc {

    private final Logger logger = LoggerFactory.getLogger(AM11SvcImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AM11Mapper am11Mapper;

    @Autowired
    private ApprovalPostProcessorRegistry postProcessorRegistry;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApprovalQueueSvc approvalQueueSvc;

    @Autowired
    private WB20Svc wb20Svc;

    @Autowired(required = false)
    private com.dksys.biz.user.pm.pm07.service.PM07Svc pm07Svc;

    @Autowired(required = false)
    private com.dksys.biz.user.pm.pm08.service.PM08Svc pm08Svc;

    @Override
    public Map<String, Object> selectApprovalDocDetail(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        if (docInfo == null || docInfo.isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "해당 결재문서가 존재하지 않습니다.");
            return resultMap;
        }

        normalizeClobValues(docInfo);

        List<Map<String, Object>> lineList = am11Mapper.selectApprovalLineList(paramMap);
        String userId = (String) paramMap.get("userId");
        if ("PM08".equals(String.valueOf(docInfo.get("erpBizType")))) {
            Map<String, Object> managerParam = new HashMap<>();
            managerParam.put("approverId", userId);
            managerParam.put("draUserId", docInfo.get("draUserId"));
            docInfo.put("pm08TeamManagerOpinionRequired", "Y".equals(am11Mapper.selectPm08TeamManagerYn(managerParam)) ? "Y" : "N");
        }

        // 1. 문서 열람 권한 검증 (기안자, 결재선/참조선 포함자, 대결자, 관리자)
        if (!isAuthorizedDocViewer(userId, docInfo, lineList)) {
            resultMap.put("resultCode", "403");
            resultMap.put("resultMessage", "해당 결재문서에 대한 열람 권한이 없습니다.");
            return resultMap;
        }

        // 2. 문서 열람(읽음) 이력 자동 기록 (권한이 검증된 경우만)
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                recordDocumentRead(paramMap);
            } catch (Exception e) {
                logger.warn("문서 열람 이력 기록 예외: docId={}, userId={}", paramMap.get("docId"), userId, e);
            }
        }

        List<Map<String, Object>> histList = am11Mapper.selectApprovalHistList(paramMap);

        resultMap.put("docInfo", docInfo);
        resultMap.put("lineList", lineList);
        resultMap.put("histList", histList);
        writeAudit(paramMap, docInfo, "DOC_VIEW", "문서 상세 조회");
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "조회에 성공하였습니다.");
        return resultMap;
    }

    /** Tibero CLOB 객체를 jsonView가 직렬화할 수 있는 문자열로 변환한다. */
    private void normalizeClobValues(Map<String, Object> docInfo) {
        for (Map.Entry<String, Object> entry : docInfo.entrySet()) {
            Object value = entry.getValue();
            if (value == null || !value.getClass().getName().toLowerCase().contains("clob")) continue;
            try {
                Method method = value instanceof Clob
                        ? Clob.class.getMethod("getCharacterStream")
                        : value.getClass().getDeclaredMethod("getCharacterStream");
                method.setAccessible(true);
                StringBuilder text = new StringBuilder();
                char[] buffer = new char[4096];
                try (Reader reader = (Reader) method.invoke(value)) {
                    int read;
                    while ((read = reader.read(buffer)) != -1) text.append(buffer, 0, read);
                }
                entry.setValue(text.toString());
            } catch (Exception e) {
                throw new IllegalStateException("결재문서 CLOB 응답 변환 실패: " + entry.getKey(), e);
            }
        }
    }

    @Override
    public Map<String, Object> saveApprovalDraft(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String userId = (String) paramMap.get("userId");
        String docId = (String) paramMap.get("docId");

        // 기안자 스냅샷 정보 보강
        enrichDrafterSnapshot(paramMap, userId);
        paramMap.put("docStatus", "DRAFT");

        if (docId == null || docId.trim().isEmpty()) {
            docId = am11Mapper.selectNextDocId();
            paramMap.put("docId", docId);
            String docNo = am11Mapper.selectNextDocNo(paramMap);
            paramMap.put("docNo", docNo);

            am11Mapper.insertApprovalDoc(paramMap);
        } else {
            // 기존 문서 상태 확인
            Map<String, Object> existDoc = am11Mapper.selectApprovalDocForUpdate(paramMap);
            if (existDoc == null) {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "수정 대상 문서가 존재하지 않습니다.");
                return resultMap;
            }
            if (!"DRAFT".equals(existDoc.get("docStatus"))) {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "임시저장 상태인 문서만 수정할 수 있습니다.");
                return resultMap;
            }
            am11Mapper.updateApprovalDoc(paramMap);
        }

        // 결재선 저장 (기존 라인 삭제 후 재등록)
        saveApprovalLines(docId, paramMap, "READY");

        resultMap.put("docId", docId);
        resultMap.put("docNo", paramMap.get("docNo"));
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "임시저장되었습니다.");
        return resultMap;
    }

    @Override
    public Map<String, Object> submitApproval(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String userId = (String) paramMap.get("userId");
        String docId = (String) paramMap.get("docId");

        // 기안자 스냅샷 정보 보강
        enrichDrafterSnapshot(paramMap, userId);

        boolean isNew = (docId == null || docId.trim().isEmpty());
        if (isNew) {
            docId = am11Mapper.selectNextDocId();
            paramMap.put("docId", docId);
            String docNo = am11Mapper.selectNextDocNo(paramMap);
            paramMap.put("docNo", docNo);
        } else {
            Map<String, Object> existDoc = am11Mapper.selectApprovalDocForUpdate(paramMap);
            if (existDoc == null) {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "상신 대상 문서가 존재하지 않습니다.");
                return resultMap;
            }
            String currStatus = (String) existDoc.get("docStatus");
            if (!"DRAFT".equals(currStatus) && !"REJECTED".equals(currStatus)) {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "임시저장 또는 반려 상태의 문서만 상신할 수 있습니다.");
                return resultMap;
            }
        }

        // 결재선 파싱 및 검증
        List<Map<String, Object>> lineList = parseLineList(paramMap.get("lineList"));
        if (lineList == null || lineList.isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "지정된 결재선이 없습니다. 최소 1명 이상의 결재자를 지정하십시오.");
            return resultMap;
        }
        // ERP/WB20 기존 결재선 명칭을 AM 표준 명칭으로 정규화한다.
        for (Map<String, Object> line : lineList) {
            if (line == null) continue;
            if (line.get("approverId") == null) line.put("approverId", line.get("todoId"));
            if (line.get("lineSeq") == null) line.put("lineSeq", line.get("sanctnSn"));
            if (line.get("lineType") == null) {
                Object gb = line.get("gb");
                line.put("lineType", "공유".equals(String.valueOf(gb)) || "REF".equals(String.valueOf(gb)) ? "REF" : "APPR");
            }
        }
        // 이후 saveApprovalLines()가 동일한 정규화 결과를 사용하도록 원본 파라미터에도 반영한다.
        paramMap.put("lineList", lineList);
        for (Map<String, Object> line : lineList) {
            if (line == null || line.get("approverId") == null
                    || String.valueOf(line.get("approverId")).trim().isEmpty()
                    || line.get("lineSeq") == null || line.get("lineType") == null
                    || String.valueOf(line.get("lineType")).trim().isEmpty()) {
                throw new IllegalArgumentException("AM 결재선 필수 매핑값(approverId, lineSeq, lineType)이 누락되었습니다.");
            }
        }
        String erpBizType = String.valueOf(paramMap.get("erpBizType"));
        boolean wb20Linked = "WB20".equals(erpBizType) || "PM07".equals(erpBizType)
                || "PM08".equals(erpBizType) || "PM51".equals(erpBizType)
                || "PM52".equals(erpBizType);
        if (wb20Linked) {
            for (Map<String, Object> line : lineList) {
                if (line.get("wb20TodoNo") == null || String.valueOf(line.get("wb20TodoNo")).trim().isEmpty()
                        || line.get("wb20Div2CodeId") == null || String.valueOf(line.get("wb20Div2CodeId")).trim().isEmpty()) {
                    throw new IllegalArgumentException("WB20 연계 필수 매핑값(wb20TodoNo, wb20Div2CodeId)이 누락되어 결재선을 저장할 수 없습니다.");
                }
            }
        }

        // 1차 결재자 정보 추출
        int autoApprovedCount = 0;
        Object autoCountValue = paramMap.get("autoApprovedCount");
        if (autoCountValue != null) {
            autoApprovedCount = Math.max(0, Math.min(Integer.parseInt(String.valueOf(autoCountValue)), lineList.size()));
        }
        boolean completedBySource = autoApprovedCount >= lineList.size();
        Map<String, Object> firstApprover = completedBySource ? lineList.get(lineList.size() - 1) : lineList.get(autoApprovedCount);
        paramMap.put("currLineSeq", completedBySource ? lineList.size() : autoApprovedCount + 1);
        paramMap.put("currApproverId", completedBySource ? null : firstApprover.get("approverId"));
        paramMap.put("currApproverNm", completedBySource ? null : firstApprover.get("approverNm"));
        paramMap.put("docStatus", completedBySource ? "COMPLETED" : "REQUEST");
        if (!completedBySource && firstApprover.get("wb20Div2CodeId") != null) {
            paramMap.put("todoDiv2CodeId", firstApprover.get("wb20Div2CodeId"));
        }

        if (isNew) {
            am11Mapper.insertApprovalDoc(paramMap);
        } else {
            am11Mapper.updateApprovalDoc(paramMap);
        }

        // 결재선 스냅샷 저장 (1차 결재자는 PENDING, 나머지는 READY)
        saveApprovalLines(docId, paramMap, completedBySource ? "APPROVED" : "PENDING");

        // 행위 이력 기록
        paramMap.put("actType", "SUBMIT");
        paramMap.put("prevStatus", isNew ? "NEW" : "DRAFT");
        paramMap.put("nextStatus", "REQUEST");
        paramMap.put("actOpinion", "결재 상신");
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, paramMap, "SUBMIT", "결재 상신");

        // 1차 결재자 알림 큐(TB_AM91M01) 영속 적재
        try {
            Map<String, Object> notifParam = new HashMap<>();
            notifParam.put("docId", docId);
            notifParam.put("eventType", "SUBMIT");
            notifParam.put("receiverId", paramMap.get("currApproverId"));
            notifParam.put("receiverNm", paramMap.get("currApproverNm"));
            notifParam.put("notifChannel", "KAKAO");
            notifParam.put("notifTitle", "[" + paramMap.get("docNo") + "] 결재 대기 문서가 도착했습니다");
            notifParam.put("notifMsg", paramMap.get("draUserNm") + "님이 결재를 상신하였습니다: " + paramMap.get("docTitle"));
            approvalQueueSvc.enqueueNotification(notifParam);
        } catch (Exception ne) {
            logger.warn("상신 알림 큐 적재 경고: docId={}", docId, ne);
        }

        // 비동기 알림 이벤트 발행 (Commit 후 실행)
        eventPublisher.publishEvent(new ApprovalEvent(
            "SUBMIT", docId, (String) paramMap.get("docNo"), (String) paramMap.get("docTitle"),
            (String) paramMap.get("draUserId"), (String) paramMap.get("draUserNm"),
            (String) paramMap.get("currApproverId"), (String) paramMap.get("currApproverNm"),
            paramMap
        ));

        resultMap.put("docId", docId);
        resultMap.put("docNo", paramMap.get("docNo"));
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "결재가 성공적으로 상신되었습니다.");
        return resultMap;
    }

    @Override
    public Map<String, Object> approveDocument(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");
        String userNm = (String) paramMap.get("userNm");
        String apprOpinion = (String) paramMap.get("apprOpinion");
        Object pgmIdObj = paramMap.get("pgmId");
        if (pgmIdObj == null || String.valueOf(pgmIdObj).trim().isEmpty()) {
            paramMap.put("pgmId", "AM11_APPR");
        }

        // 1. 동시성 비관적 Lock (FOR UPDATE NOWAIT)
        Map<String, Object> docLock;
        try {
            docLock = am11Mapper.selectApprovalDocForUpdate(paramMap);
        } catch (Exception e) {
            logger.warn("동시성 충돌 감지 (Lock 실패): docId={}", docId, e);
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 다른 사용자에 의해 결재 처리 중인 문서입니다. 잠시 후 다시 확인하십시오.");
            return resultMap;
        }

        if (docLock == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재 대상 문서를 찾을 수 없습니다.");
            return resultMap;
        }

        if (("PM07".equals(docLock.get("erpBizType")) || "PM51".equals(docLock.get("erpBizType")) || "PM52".equals(docLock.get("erpBizType")) || "PM08".equals(docLock.get("erpBizType")))
                && am11Mapper.selectLinkedSourceCount(docLock) == 0) {
            resultMap.put("resultCode", "409");
            resultMap.put("resultMessage", "원본 ERP 결재문서가 삭제되어 승인할 수 없습니다.");
            return resultMap;
        }

        String docStatus = (String) docLock.get("docStatus");
        if (!"REQUEST".equals(docStatus) && !"PROGRESS".equals(docStatus) && !"POST_PROGRESS".equals(docStatus)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "진행 중인 결재 문서가 아닙니다. (현재 상태: " + docStatus + ")");
            return resultMap;
        }

        if (!isCurrentApprovalActor(paramMap, docLock)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 결재자 또는 대결자만 승인할 수 있습니다.");
            return resultMap;
        }

        // docInfo 호이스트 (알림 이벤트에 실제 docTitle/docDataJson 등 필요)
        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        if (docInfo != null) {
            paramMap.put("docTitle", docInfo.get("docTitle"));
            paramMap.put("docDataJson", docInfo.get("docDataJson"));
        }
        // PM07/PM08 리치 발송용 필드 추가
        paramMap.put("erpBizType", docLock.get("erpBizType"));
        paramMap.put("erpBizKey", docLock.get("erpBizKey"));
        paramMap.put("coCd", docLock.get("coCd"));

        // 2. 현재 결재선 목록 확인
        List<Map<String, Object>> lineList = am11Mapper.selectApprovalLineList(paramMap);
        int currLineSeq = Integer.parseInt(String.valueOf(docLock.get("currLineSeq")));

        Map<String, Object> currentLine = null;
        for (Map<String, Object> line : lineList) {
            int seq = Integer.parseInt(String.valueOf(line.get("lineSeq")));
            if (seq == currLineSeq) {
                currentLine = line;
                break;
            }
        }

        if (currentLine == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 결재 순번의 정보를 찾을 수 없습니다.");
            return resultMap;
        }

        String approverId = (String) currentLine.get("approverId");
        boolean isDelegate = false;
        if ("PM08".equals(valueOf(docLock.get("erpBizType")))) {
            Map<String, Object> managerParam = new HashMap<>();
            managerParam.put("approverId", approverId);
            managerParam.put("draUserId", docLock.get("draUserId"));
            if ("Y".equals(am11Mapper.selectPm08TeamManagerYn(managerParam))
                    && (apprOpinion == null || apprOpinion.trim().isEmpty())) {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "신청자의 팀장 결재는 결재처리 의견을 입력해야 합니다.");
                return resultMap;
            }
        }

        // 결재 권한 검증: 본인인지 확인, 본인이 아니면 활성 대결(위임) 설정 검증
        if (!userId.equals(approverId)) {
            Map<String, Object> delegParam = new HashMap<>();
            delegParam.put("originUserId", approverId);
            delegParam.put("delegateUserId", userId);
            Map<String, Object> activeDeleg = am11Mapper.selectActiveDelegation(delegParam);
            if (activeDeleg != null) {
                isDelegate = true;
            } else {
                resultMap.put("resultCode", "500");
                resultMap.put("resultMessage", "결재 처리 권한이 없습니다. (현재 결재자: " + currentLine.get("approverNm") + ")");
                return resultMap;
            }
        }

        // 3. 현재 결재선 승인 처리
        Map<String, Object> updateLineParam = new HashMap<>(paramMap);
        updateLineParam.put("lineSeq", currLineSeq);
        updateLineParam.put("lineStatus", "APPROVED");
        updateLineParam.put("apprOpinion", apprOpinion);
        if (isDelegate) {
            updateLineParam.put("delegateYn", "Y");
            updateLineParam.put("actualUserId", userId);
            updateLineParam.put("actualUserNm", userNm);
            Map<String, Object> actualOrg = am11Mapper.selectUserOrgSnapshot(userId);
            if (actualOrg != null) {
                updateLineParam.put("actualDeptId", actualOrg.get("deptId"));
                updateLineParam.put("actualDeptNm", actualOrg.get("deptNm"));
                updateLineParam.put("actualLevelNm", actualOrg.get("levelNm"));
            }
        }
        am11Mapper.updateApprovalLineStatus(updateLineParam);

        // AM에서 결재한 동일 결재자 행만 WB20 원본문서에 즉시 반영한다.
        if ("WB20".equals(docLock.get("erpBizType")) || "PM07".equals(docLock.get("erpBizType")) || "PM51".equals(docLock.get("erpBizType")) || "PM52".equals(docLock.get("erpBizType")) || "PM08".equals(docLock.get("erpBizType"))) {
            Map<String, Object> wb20Param = new HashMap<>(paramMap);
            wb20Param.put("erpBizKey", docLock.get("erpBizKey"));
            wb20Param.put("todoId", approverId);
            wb20Param.put("apprOpinion", apprOpinion);
            executeLinkedWb20Approval(docLock, currentLine, approverId, currLineSeq, userId, apprOpinion);
        }

        // 4. 다음 결재자 탐색 (일반 결재선 vs 후결 결재선 분기)
        Map<String, Object> nextLine = null;
        for (Map<String, Object> line : lineList) {
            int seq = Integer.parseInt(String.valueOf(line.get("lineSeq")));
            String lType = (String) line.get("lineType");
            if (seq > currLineSeq && !"REF".equals(lType) && !"POST".equals(lType)) {
                nextLine = line;
                break;
            }
        }

        String nextDocStatus;
        Map<String, Object> nextPostLine = null;

        if (nextLine != null) {
            // 일반 결재 진행
            nextDocStatus = "PROGRESS";
            int nextSeq = Integer.parseInt(String.valueOf(nextLine.get("lineSeq")));
            Map<String, Object> nextParam = new HashMap<>(paramMap);
            nextParam.put("nextLineSeq", nextSeq);
            am11Mapper.updateNextLinePending(nextParam);

            Map<String, Object> docStatusParam = new HashMap<>(paramMap);
            docStatusParam.put("nextStatus", nextDocStatus);
            docStatusParam.put("currLineSeq", nextSeq);
            docStatusParam.put("currApproverId", nextLine.get("approverId"));
            docStatusParam.put("currApproverNm", nextLine.get("approverNm"));
            am11Mapper.updateApprovalDocStatus(docStatusParam);
        } else {
            // 일반 결재 완료 -> 잔여 후결(POST) 결재선 존재 여부 확인
            for (Map<String, Object> line : lineList) {
                int seq = Integer.parseInt(String.valueOf(line.get("lineSeq")));
                String lType = (String) line.get("lineType");
                String lStatus = (String) line.get("lineStatus");
                if ("POST".equals(lType) && ("READY".equals(lStatus) || (seq > currLineSeq && "PENDING".equals(lStatus)))) {
                    nextPostLine = line;
                    break;
                }
            }

            if (nextPostLine != null) {
                // 후결(POST) 진행 단계 진입
                nextDocStatus = "POST_PROGRESS";
                int postSeq = Integer.parseInt(String.valueOf(nextPostLine.get("lineSeq")));
                Map<String, Object> nextParam = new HashMap<>(paramMap);
                nextParam.put("nextLineSeq", postSeq);
                am11Mapper.updateNextLinePending(nextParam);

                Map<String, Object> docStatusParam = new HashMap<>(paramMap);
                docStatusParam.put("nextStatus", nextDocStatus);
                docStatusParam.put("currLineSeq", postSeq);
                docStatusParam.put("currApproverId", nextPostLine.get("approverId"));
                docStatusParam.put("currApproverNm", nextPostLine.get("approverNm"));
                am11Mapper.updateApprovalDocStatus(docStatusParam);
            } else {
                // 최종 결재 완료
                nextDocStatus = "COMPLETED";
                Map<String, Object> docStatusParam = new HashMap<>(paramMap);
                docStatusParam.put("nextStatus", nextDocStatus);
                docStatusParam.put("currLineSeq", currLineSeq);
                docStatusParam.put("currApproverId", "");
                docStatusParam.put("currApproverNm", "");
                am11Mapper.updateApprovalDocStatus(docStatusParam);
            }
        }

        // 5. 결재 이력 기록
        paramMap.put("actType", isDelegate ? "DELEGATE" : "APPROVE");
        paramMap.put("prevStatus", docStatus);
        paramMap.put("nextStatus", nextDocStatus);
        paramMap.put("actOpinion", apprOpinion);
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, docLock, isDelegate ? "DELEGATE" : "APPROVE", isDelegate ? "대결 승인" : "결재 승인");

        // 6. ERP 사후처리 및 알림 큐/이벤트 발행
        // 본문 승인 완료 시점(일반 결재 완료 시) 또는 최종 완료 시점에 ERP 사후처리 실행
        if (!"POST_PROGRESS".equals(docStatus) && (nextLine == null)) {
            String erpBizType = (docInfo != null) ? (String) docInfo.get("erpBizType") : null;
            try {
                postProcessorRegistry.processCompleted(erpBizType, docInfo, paramMap);
            } catch (Exception e) {
                logger.error("ERP 사후 연동 실패 (보상 큐 Enqueue): docId={}, erpBizType={}", docId, erpBizType, e);
                try {
                    String payloadJson = objectMapper.writeValueAsString(docInfo);
                    Map<String, Object> compParam = new HashMap<>(paramMap);
                    compParam.put("docId", docId);
                    compParam.put("erpBizType", erpBizType);
                    compParam.put("erpBizKey", docInfo != null ? (String) docInfo.get("erpBizKey") : "");
                    compParam.put("execType", "APPROVE_COMPLETE");
                    compParam.put("payloadJson", payloadJson);
                    compParam.put("errMsg", e.getMessage());
                    approvalQueueSvc.enqueueErpCompensation(compParam);
                } catch (Exception ce) {
                    logger.error("ERP 보상 큐 등록 실패 (원자적 롤백 트리거): docId={}", docId, ce);
                    throw new RuntimeException("ERP 보상 큐 등록 실패로 인해 결재 처리가 롤백되었습니다: " + ce.getMessage(), ce);
                }
            }

            // 기안자 최종 완료 알림 큐 적재
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "COMPLETE");
                notifParam.put("receiverId", docLock.get("draUserId"));
                notifParam.put("receiverNm", docInfo != null ? docInfo.get("draUserNm") : "");
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 결재가 최종 승인되었습니다");
                notifParam.put("notifMsg", "상신하신 문서가 최종 승인 완료되었습니다: " + (docInfo != null ? docInfo.get("docTitle") : ""));
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("최종 승인 알림 큐 적재 경고: docId={}", docId, ne);
            }

            eventPublisher.publishEvent(new ApprovalEvent(
                "COMPLETE", docId, (String) docLock.get("docNo"), (String) paramMap.get("docTitle"),
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : "", "", "", paramMap
            ));
        } else if (nextLine != null) {
            // 다음 순번 결재자 알림 큐 적재
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "APPROVE_NEXT");
                notifParam.put("receiverId", nextLine.get("approverId"));
                notifParam.put("receiverNm", nextLine.get("approverNm"));
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 결재 대기 문서가 도착했습니다");
                notifParam.put("notifMsg", "결재 순번이 도래하였습니다. 문서를 확인하십시오.");
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("다음 결재자 알림 큐 적재 경고: docId={}", docId, ne);
            }

            paramMap.put("currLineSeq", nextLine.get("lineSeq"));
            if (nextLine.get("wb20Div2CodeId") != null) {
                paramMap.put("todoDiv2CodeId", nextLine.get("wb20Div2CodeId"));
            }
            eventPublisher.publishEvent(new ApprovalEvent(
                "APPROVE_NEXT", docId, (String) docLock.get("docNo"), (String) paramMap.get("docTitle"),
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : (String) paramMap.get("draUserNm"),
                (String) nextLine.get("approverId"), (String) nextLine.get("approverNm"), paramMap
            ));
        } else if (nextPostLine != null) {
            // 사후결재(후결) 결재자 알림 큐 적재
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "APPROVE_NEXT");
                notifParam.put("receiverId", nextPostLine.get("approverId"));
                notifParam.put("receiverNm", nextPostLine.get("approverNm"));
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 사후결재(후결) 요청 문서가 도착했습니다");
                notifParam.put("notifMsg", "전결 후 사후결재(후결) 순번이 도래하였습니다.");
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("후결 결재자 알림 큐 적재 경고: docId={}", docId, ne);
            }

            paramMap.put("currLineSeq", nextPostLine.get("lineSeq"));
            if (nextPostLine.get("wb20Div2CodeId") != null) {
                paramMap.put("todoDiv2CodeId", nextPostLine.get("wb20Div2CodeId"));
            }
            eventPublisher.publishEvent(new ApprovalEvent(
                "APPROVE_NEXT", docId, (String) docLock.get("docNo"), (String) paramMap.get("docTitle"),
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : (String) paramMap.get("draUserNm"),
                (String) nextPostLine.get("approverId"), (String) nextPostLine.get("approverNm"), paramMap
            ));
        }

        resultMap.put("docId", docId);
        resultMap.put("nextStatus", nextDocStatus);
        resultMap.put("resultCode", "200");
        if (nextPostLine != null) {
            resultMap.put("resultMessage", "승인 완료되었습니다. 사후결재(후결) 단계로 전환되었습니다.");
        } else if (nextLine != null) {
            resultMap.put("resultMessage", "승인 처리되었습니다. 다음 결재자에게 전달되었습니다.");
        } else {
            resultMap.put("resultMessage", "최종 승인 완료되었습니다.");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> rejectDocument(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");
        String apprOpinion = (String) paramMap.get("apprOpinion");

        if (apprOpinion == null || apprOpinion.trim().isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "반려 사유(의견)를 반드시 입력해야 합니다.");
            return resultMap;
        }

        // 문서 Lock
        Map<String, Object> docLock = am11Mapper.selectApprovalDocForUpdate(paramMap);
        if (docLock == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재 대상 문서를 찾을 수 없습니다.");
            return resultMap;
        }

        String docStatus = (String) docLock.get("docStatus");
        if (!"REQUEST".equals(docStatus) && !"PROGRESS".equals(docStatus)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "진행 중인 결재 문서만 반려할 수 있습니다.");
            return resultMap;
        }

        if (!isCurrentApprovalActor(paramMap, docLock)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 결재자 또는 대결자만 반려할 수 있습니다.");
            return resultMap;
        }

        int currLineSeq = Integer.parseInt(String.valueOf(docLock.get("currLineSeq")));

        // 결재선 반려 업데이트
        Map<String, Object> updateLineParam = new HashMap<>(paramMap);
        updateLineParam.put("lineSeq", currLineSeq);
        updateLineParam.put("lineStatus", "REJECTED");
        am11Mapper.updateApprovalLineStatus(updateLineParam);

        // 문서 마스터 상태 REJECTED 업데이트
        Map<String, Object> docStatusParam = new HashMap<>(paramMap);
        docStatusParam.put("nextStatus", "REJECTED");
        docStatusParam.put("currLineSeq", currLineSeq);
        docStatusParam.put("currApproverId", "");
        docStatusParam.put("currApproverNm", "");
        am11Mapper.updateApprovalDocStatus(docStatusParam);

        // AM 반려도 원본 WB20의 동일 결재행과 업무 후처리를 반드시 같은 트랜잭션으로 처리한다.
        String erpBizKey = valueOf(docLock.get("erpBizKey"));
        String coCd = valueOf(docLock.get("coCd"));
        if (!erpBizKey.isEmpty() && !coCd.isEmpty()) {
            Map<String, String> wbQuery = new HashMap<>();
            wbQuery.put("todoNo", erpBizKey);
            wbQuery.put("coCd", coCd);
            List<Map<String, String>> wbLines = wb20Svc.selectGetApprovalList(wbQuery);
            Map<String, String> currentWbLine = null;
            String originalApproverId = valueOf(docLock.get("currApproverId"));
            for (Map<String, String> row : wbLines) {
                if ((originalApproverId.equals(row.get("todoId")) || userId.equals(row.get("todoId")))
                        && !"Y".equals(row.get("sanctnSttus"))) {
                    currentWbLine = row;
                    break;
                }
            }
            if (currentWbLine == null) {
                throw new IllegalStateException("원본 WB20 결재행을 찾을 수 없어 반려를 취소했습니다.");
            }
            Map<String, String> wbReject = new HashMap<>(currentWbLine);
            wbReject.put("coCd", coCd);
            wbReject.put("todoId", currentWbLine.get("todoId"));
            wbReject.put("rejectOpinion", apprOpinion);
            wbReject.put("userId", userId);
            wbReject.put("pgmId", "AM1201P01");
            Map<String, String> wbResult = wb20Svc.rejectApprovalLine(wbReject);
            String rejectCnt = wbResult != null
                    ? (wbResult.get("resultCount") != null ? wbResult.get("resultCount") : wbResult.get("RESULT_COUNT"))
                    : null;
            if (!"1".equals(rejectCnt)) {
                throw new IllegalStateException("원본 WB20 반려 처리에 실패하여 롤백했습니다.");
            }
        }

        // 이력 기록
        paramMap.put("actType", "REJECT");
        paramMap.put("prevStatus", docStatus);
        paramMap.put("nextStatus", "REJECTED");
        paramMap.put("actOpinion", apprOpinion);
        paramMap.put("rejectOpinion", apprOpinion);
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, docLock, "REJECT", "결재 반려: " + ApprovalSecurityUtil.maskSensitiveData(apprOpinion));

        // ERP 사후처리 및 알림 이벤트
        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        String erpBizType = (docInfo != null) ? (String) docInfo.get("erpBizType") : null;
        postProcessorRegistry.processRejected(erpBizType, docInfo, paramMap);

        // 기안자 반려 알림 큐 적재
        try {
            Map<String, Object> notifParam = new HashMap<>();
            notifParam.put("docId", docId);
            notifParam.put("eventType", "REJECT");
            notifParam.put("receiverId", docLock.get("draUserId"));
            notifParam.put("receiverNm", docInfo != null ? docInfo.get("draUserNm") : "");
            notifParam.put("notifChannel", "KAKAO");
            notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 결재가 반려되었습니다");
            notifParam.put("notifMsg", "상신하신 문서가 반려되었습니다: " + ApprovalSecurityUtil.maskSensitiveData(apprOpinion));
            approvalQueueSvc.enqueueNotification(notifParam);
        } catch (Exception ne) {
            logger.warn("반려 알림 큐 적재 경고: docId={}", docId, ne);
        }

        eventPublisher.publishEvent(new ApprovalEvent(
            "REJECT", docId, (String) docLock.get("docNo"), docInfo != null ? (String) docInfo.get("docTitle") : "",
            (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : "", "", "", paramMap
        ));

        resultMap.put("docId", docId);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "문서가 반려 처리되었습니다.");
        return resultMap;
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    @Override
    public Map<String, Object> cancelApproval(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");

        Map<String, Object> docLock = am11Mapper.selectApprovalDocForUpdate(paramMap);
        if (docLock == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "대상 문서를 찾을 수 없습니다.");
            return resultMap;
        }

        // 기안자 본인 확인
        String draUserId = (String) docLock.get("draUserId");
        if (!userId.equals(draUserId)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "기안자 본인만 상신 취소(회수)할 수 있습니다.");
            return resultMap;
        }

        // 1차 결재자 승인 여부 확인
        int currLineSeq = Integer.parseInt(String.valueOf(docLock.get("currLineSeq")));
        if (currLineSeq > 1) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "이미 결재가 진행된 문서는 상신 취소할 수 없습니다.");
            return resultMap;
        }

        Map<String, Object> docStatusParam = new HashMap<>(paramMap);
        docStatusParam.put("nextStatus", "CANCELLED");
        docStatusParam.put("currLineSeq", 0);
        docStatusParam.put("currApproverId", "");
        docStatusParam.put("currApproverNm", "");
        am11Mapper.updateApprovalDocStatus(docStatusParam);

        // 이력 기록
        paramMap.put("actType", "CANCEL");
        paramMap.put("prevStatus", docLock.get("docStatus"));
        paramMap.put("nextStatus", "CANCELLED");
        paramMap.put("actOpinion", "기안자 상신 취소");
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, docLock, "CANCEL", "기안자 상신 취소(회수)");

        // ERP 사후처리 및 이벤트
        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        String erpBizType = (docInfo != null) ? (String) docInfo.get("erpBizType") : null;
        postProcessorRegistry.processCancelled(erpBizType, docInfo, paramMap);

        // 현재 결재자 취소 알림 큐 적재
        try {
            String currApproverId = (String) docLock.get("currApproverId");
            if (currApproverId != null && !currApproverId.trim().isEmpty()) {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "CANCEL");
                notifParam.put("receiverId", currApproverId);
                notifParam.put("receiverNm", (String) docLock.get("currApproverNm"));
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 결재 문서가 회수되었습니다");
                notifParam.put("notifMsg", "기안자(" + docLock.get("draUserNm") + ")가 상신 문서를 회수(취소)하였습니다.");
                approvalQueueSvc.enqueueNotification(notifParam);
            }
        } catch (Exception ne) {
            logger.warn("취소 알림 큐 적재 경고: docId={}", docId, ne);
        }

        eventPublisher.publishEvent(new ApprovalEvent(
            "CANCEL", docId, (String) docLock.get("docNo"), docInfo != null ? (String) docInfo.get("docTitle") : "",
            draUserId, "", "", "", paramMap
        ));

        resultMap.put("docId", docId);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "상신이 취소(회수)되었습니다.");
        return resultMap;
    }

    @Override
    public Map<String, Object> cancelArbitDocument(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        String userId = valueOf(paramMap.get("userId"));
        Map<String, Object> doc = am11Mapper.selectApprovalDocForUpdate(paramMap);
        if (doc == null) throw new IllegalStateException("결재문서를 찾을 수 없습니다.");
        Map<String, Object> arbitLine = am11Mapper.selectArbitLineForCancel(paramMap);
        if (arbitLine == null) {
            result.put("docId", paramMap.get("docId"));
            result.put("resultCode", "200");
            result.put("resultMessage", "이미 전결 취소되었거나 취소할 전결 행이 없습니다.");
            return result;
        }
        int lineSeq = Integer.parseInt(String.valueOf(arbitLine.get("lineSeq")));
        paramMap.put("lineSeq", lineSeq);
        paramMap.put("pgmId", "AM1201P01");

        if (valueOf(paramMap.get("userNm")).isEmpty()
                || valueOf(paramMap.get("deptNm")).isEmpty()
                || valueOf(paramMap.get("levelNm")).isEmpty()) {
            throw new IllegalStateException("전결 취소 이력의 처리자 Snapshot 정보가 없어 전체 처리를 롤백했습니다.");
        }

        int current = am11Mapper.cancelArbitCurrentLine(paramMap);
        if (current != 1) throw new IllegalStateException("전결 처리자 본인만 전결을 취소할 수 있습니다.");
        am11Mapper.resetArbitRemainingLines(paramMap);

        Map<String, Object> info = am11Mapper.selectApprovalDocInfo(paramMap);
        String erpKey = valueOf(info == null ? null : info.get("erpBizKey"));
        if (!erpKey.isEmpty()) {
            Map<String, Object> wb = new HashMap<>(paramMap);
            wb.put("coCd", valueOf(doc.get("coCd")));
            wb.put("erpBizKey", erpKey);
            wb.put("wb20SanctnSn", lineSeq);
            am11Mapper.resetWb20ArbitRemainingLines(wb);
        }

        Map<String, Object> status = new HashMap<>(paramMap);
        status.put("nextStatus", "PROGRESS");
        status.put("currApproverId", arbitLine.get("approverId"));
        status.put("currApproverNm", arbitLine.get("approverNm"));
        status.put("currLineSeq", lineSeq);
        am11Mapper.updateApprovalDocStatus(status);

        paramMap.put("actType", "ARBIT_CANCEL");
        paramMap.put("prevStatus", doc.get("docStatus"));
        paramMap.put("nextStatus", "PROGRESS");
        paramMap.put("actOpinion", "전결 취소");
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, doc, "ARBIT_CANCEL", "전결 취소");
        result.put("docId", paramMap.get("docId"));
        result.put("resultCode", "200");
        result.put("resultMessage", "전결이 취소되고 이후 결재선이 대기 상태로 복구되었습니다.");
        return result;
    }

    @Override
    public Map<String, Object> arbitDocument(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");
        String apprOpinion = (String) paramMap.get("apprOpinion");

        Map<String, Object> docLock = am11Mapper.selectApprovalDocForUpdate(paramMap);
        if (docLock == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재 대상 문서를 찾을 수 없습니다.");
            return resultMap;
        }

        String docStatus = (String) docLock.get("docStatus");
        if (!"REQUEST".equals(docStatus) && !"PROGRESS".equals(docStatus)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "진행 중인 문서만 전결 처리할 수 있습니다.");
            return resultMap;
        }
        if (!isCurrentApprovalActor(paramMap, docLock)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 결재자 또는 대결자만 전결 처리할 수 있습니다.");
            return resultMap;
        }

        int currLineSeq = Integer.parseInt(String.valueOf(docLock.get("currLineSeq")));

        // 1. 현재 결재선 전결 승인 처리
        Map<String, Object> updateLineParam = new HashMap<>(paramMap);
        updateLineParam.put("lineSeq", currLineSeq);
        updateLineParam.put("lineStatus", "APPROVED");
        updateLineParam.put("arbitYn", "Y");
        updateLineParam.put("apprOpinion", apprOpinion != null ? apprOpinion : "전결 승인");
        am11Mapper.updateApprovalLineStatus(updateLineParam);

        // 2. 결재선 목록 조회 및 현재 전결자 행 식별
        List<Map<String, Object>> lineList = am11Mapper.selectApprovalLineList(paramMap);
        Map<String, Object> currentLine = null;
        if (lineList != null) {
            for (Map<String, Object> line : lineList) {
                if (currLineSeq == Integer.parseInt(String.valueOf(line.get("lineSeq")))) {
                    currentLine = line;
                    break;
                }
            }
        }

        // WB20 원본문서는 현재 전결자 행만 기존 승인 서비스로 처리한다.
        // 전체 TODO_NO를 일괄 갱신하면 이미 승인된 이전 행의 승인시각이 훼손된다.
        Map<String, Object> linkedDocInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        boolean linkedWb20 = linkedDocInfo != null
                && linkedDocInfo.get("erpBizKey") != null
                && !String.valueOf(linkedDocInfo.get("erpBizKey")).trim().isEmpty();
        if (linkedWb20) {
            executeLinkedWb20Approval(docLock, currentLine, String.valueOf(docLock.get("currApproverId")),
                    Integer.parseInt(String.valueOf(updateLineParam.get("lineSeq"))),
                    userId, apprOpinion != null ? apprOpinion : "전결 승인");
        }

        String currentDiv2CodeId = (currentLine != null && currentLine.get("wb20Div2CodeId") != null)
                ? String.valueOf(currentLine.get("wb20Div2CodeId")).trim() : "";

        // PM51 출장신청서(TODODIV2190: 신청부서 / TODODIV2191: 관리부서), 출장복명서(TODODIV2200: 신청부서 / TODODIV2201: 관리부서)
        boolean isStage1 = "TODODIV2190".equals(currentDiv2CodeId) || "TODODIV2200".equals(currentDiv2CodeId);
        boolean isStage2 = "TODODIV2191".equals(currentDiv2CodeId) || "TODODIV2201".equals(currentDiv2CodeId);
        boolean isTwoStageDiv2 = isStage1 || isStage2;

        if (isTwoStageDiv2) {
            // 3. 해당 결재구분(TODO_DIV2_CODE_ID) 내의 잔여 결재라인만 전결 종결 처리
            Map<String, Object> arbitDiv2Param = new HashMap<>(updateLineParam);
            arbitDiv2Param.put("wb20Div2CodeId", currentDiv2CodeId);
            am11Mapper.updateRemainingLinesArbitByDiv2(arbitDiv2Param);

            // ERP(TB_WB20M03)에서도 해당 결재구분 내 잔여 결재선 일괄 완료 처리
            if (linkedWb20) {
                Map<String, Object> wb20ArbitParam = new HashMap<>(paramMap);
                wb20ArbitParam.put("erpBizKey", linkedDocInfo.get("erpBizKey"));
                wb20ArbitParam.put("todoDiv2CodeId", currentDiv2CodeId);
                wb20ArbitParam.put("wb20SanctnSn", currentLine != null ? currentLine.get("wb20SanctnSn") : null);
                wb20ArbitParam.put("apprOpinion", apprOpinion != null ? apprOpinion : "상위 결재자 전결로 인한 종결");
                am11Mapper.updateWb20RemainingLinesArbit(wb20ArbitParam);

                // 출장신청서(TODODIV2190) 신청부서 전결 완료 시 상태를 APRVSTS03(신청부서 결재완료)으로 갱신
                if ("TODODIV2190".equals(currentDiv2CodeId)) {
                    Map<String, Object> pm51StsParam = new HashMap<>();
                    pm51StsParam.put("tripReqNo", linkedDocInfo.get("erpBizKey"));
                    pm51StsParam.put("aprvStsCd", "APRVSTS03");
                    pm51StsParam.put("userId", userId);
                    am11Mapper.updatePm51TripReqAprvSts(pm51StsParam);
                }
            }
        } else {
            // 3. 일반 단일 단계 결재문서: 이후 결재선 일괄 종결(ARBIT - 후결(POST) 제외)
            am11Mapper.updateRemainingLinesArbit(updateLineParam);

            // ERP(TB_WB20M03)에서도 잔여 결재선 일괄 완료 처리
            if (linkedWb20) {
                Map<String, Object> wb20ArbitParam = new HashMap<>(paramMap);
                wb20ArbitParam.put("erpBizKey", linkedDocInfo.get("erpBizKey"));
                wb20ArbitParam.put("todoDiv2CodeId", currentDiv2CodeId);
                wb20ArbitParam.put("wb20SanctnSn", currentLine != null ? currentLine.get("wb20SanctnSn") : null);
                wb20ArbitParam.put("apprOpinion", apprOpinion != null ? apprOpinion : "상위 결재자 전결로 인한 종결");
                am11Mapper.updateWb20RemainingLinesArbit(wb20ArbitParam);
            }
        }

        // 4. 다음 결재자 탐색 (1단계 신청부서 전결 시 2단계 관리부서 결재선 탐색)
        Map<String, Object> nextLine = null;
        if (isStage1 && lineList != null) {
            for (Map<String, Object> line : lineList) {
                int seq = Integer.parseInt(String.valueOf(line.get("lineSeq")));
                String lType = (String) line.get("lineType");
                String lStatus = (String) line.get("lineStatus");
                String lineDiv2 = line.get("wb20Div2CodeId") != null ? String.valueOf(line.get("wb20Div2CodeId")).trim() : "";
                if (seq > currLineSeq && !"REF".equals(lType) && !"POST".equals(lType)
                        && !currentDiv2CodeId.equals(lineDiv2)
                        && ("READY".equals(lStatus) || "PENDING".equals(lStatus))) {
                    nextLine = line;
                    break;
                }
            }
        }

        // 잔여 후결(POST) 결재선 존재 여부 확인 (다음 일반 결재선이 없을 때만)
        Map<String, Object> nextPostLine = null;
        if (nextLine == null && lineList != null) {
            for (Map<String, Object> line : lineList) {
                int seq = Integer.parseInt(String.valueOf(line.get("lineSeq")));
                String lType = (String) line.get("lineType");
                String lStatus = (String) line.get("lineStatus");
                if (seq > currLineSeq && "POST".equals(lType) && ("READY".equals(lStatus) || "PENDING".equals(lStatus))) {
                    nextPostLine = line;
                    break;
                }
            }
        }

        String nextDocStatus;
        if (nextLine != null) {
            // 2단계(관리부서) 결재선 PENDING 상태로 전환
            nextDocStatus = "PROGRESS";
            int nextSeq = Integer.parseInt(String.valueOf(nextLine.get("lineSeq")));
            Map<String, Object> nextParam = new HashMap<>(paramMap);
            nextParam.put("nextLineSeq", nextSeq);
            am11Mapper.updateNextLinePending(nextParam);

            Map<String, Object> docStatusParam = new HashMap<>(paramMap);
            docStatusParam.put("nextStatus", nextDocStatus);
            docStatusParam.put("currLineSeq", nextSeq);
            docStatusParam.put("currApproverId", nextLine.get("approverId"));
            docStatusParam.put("currApproverNm", nextLine.get("approverNm"));
            am11Mapper.updateApprovalDocStatus(docStatusParam);
        } else if (nextPostLine != null) {
            // 잔여 후결자가 존재하므로 POST_PROGRESS 상태로 전환
            nextDocStatus = "POST_PROGRESS";
            int postSeq = Integer.parseInt(String.valueOf(nextPostLine.get("lineSeq")));
            Map<String, Object> nextParam = new HashMap<>(paramMap);
            nextParam.put("nextLineSeq", postSeq);
            am11Mapper.updateNextLinePending(nextParam);

            Map<String, Object> docStatusParam = new HashMap<>(paramMap);
            docStatusParam.put("nextStatus", nextDocStatus);
            docStatusParam.put("currLineSeq", postSeq);
            docStatusParam.put("currApproverId", nextPostLine.get("approverId"));
            docStatusParam.put("currApproverNm", nextPostLine.get("approverNm"));
            am11Mapper.updateApprovalDocStatus(docStatusParam);
        } else {
            // 후결자가 없으면 최종 종결 COMPLETED
            nextDocStatus = "COMPLETED";
            Map<String, Object> docStatusParam = new HashMap<>(paramMap);
            docStatusParam.put("nextStatus", nextDocStatus);
            docStatusParam.put("currLineSeq", currLineSeq);
            docStatusParam.put("currApproverId", "");
            docStatusParam.put("currApproverNm", "");
            am11Mapper.updateApprovalDocStatus(docStatusParam);
        }

        // 5. 이력 기록
        paramMap.put("actType", "ARBIT");
        paramMap.put("prevStatus", docLock.get("docStatus"));
        paramMap.put("nextStatus", nextDocStatus);
        paramMap.put("actOpinion", apprOpinion != null ? apprOpinion : (isStage1 ? "신청부서 전결 처리 완료" : "전결 처리 완료"));
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, docLock, "ARBIT", "전결 처리: " + (apprOpinion != null ? apprOpinion : "전결 승인"));

        Map<String, Object> docInfo = linkedDocInfo != null ? linkedDocInfo
                : am11Mapper.selectApprovalDocInfo(paramMap);

        // 6. ERP 사후처리 (최종 COMPLETED 완료 시점에만 트리거)
        if (nextLine == null) {
            String erpBizType = (docInfo != null) ? (String) docInfo.get("erpBizType") : null;
            try {
                paramMap.put("linkedWb20", linkedWb20);
                postProcessorRegistry.processCompleted(erpBizType, docInfo, paramMap);
            } catch (Exception e) {
                logger.error("ERP 사후 연동 실패 (보상 큐 Enqueue): docId={}, erpBizType={}", docId, erpBizType, e);
                try {
                    String payloadJson = objectMapper.writeValueAsString(docInfo);
                    Map<String, Object> compParam = new HashMap<>(paramMap);
                    compParam.put("docId", docId);
                    compParam.put("erpBizType", erpBizType);
                    compParam.put("erpBizKey", docInfo != null ? (String) docInfo.get("erpBizKey") : "");
                    compParam.put("execType", "ARBIT_COMPLETE");
                    compParam.put("payloadJson", payloadJson);
                    compParam.put("errMsg", e.getMessage());
                    approvalQueueSvc.enqueueErpCompensation(compParam);
                } catch (Exception ce) {
                    logger.error("ERP 보상 큐 등록 실패 (원자적 롤백 트리거): docId={}", docId, ce);
                    throw new RuntimeException("전결 ERP 보상 큐 등록 실패로 인해 전체 처리가 롤백되었습니다: " + ce.getMessage(), ce);
                }
            }
        }

        // 7. 알림 큐 적재 및 이벤트 발행
        if (nextLine != null) {
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "ARBIT");
                notifParam.put("receiverId", docLock.get("draUserId"));
                notifParam.put("receiverNm", docInfo != null ? docInfo.get("draUserNm") : "");
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 전결 처리");
                notifParam.put("notifMsg", "전결 처리되었습니다. 결재의견: " + (apprOpinion != null ? apprOpinion : "전결 승인"));
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("다음 단계 결재자 알림 큐 적재 경고: docId={}", docId, ne);
            }

            eventPublisher.publishEvent(new ApprovalEvent(
                "ARBIT", docId, (String) docLock.get("docNo"), "전결 처리",
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : "",
                (String) nextLine.get("approverId"), (String) nextLine.get("approverNm"), paramMap
            ));
        } else if (nextPostLine != null) {
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "ARBIT");
                notifParam.put("receiverId", docLock.get("draUserId"));
                notifParam.put("receiverNm", docInfo != null ? docInfo.get("draUserNm") : "");
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 전결 처리");
                notifParam.put("notifMsg", "전결 처리되었습니다. 결재의견: " + (apprOpinion != null ? apprOpinion : "전결 승인"));
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("전결 후 후결 알림 큐 적재 경고: docId={}", docId, ne);
            }

            eventPublisher.publishEvent(new ApprovalEvent(
                "ARBIT", docId, (String) docLock.get("docNo"), "전결 처리",
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : "",
                (String) nextPostLine.get("approverId"), (String) nextPostLine.get("approverNm"), paramMap
            ));
        } else {
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "COMPLETE");
                notifParam.put("receiverId", docLock.get("draUserId"));
                notifParam.put("receiverNm", docInfo != null ? docInfo.get("draUserNm") : "");
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 전결로 최종 승인 완료되었습니다");
                notifParam.put("notifMsg", "상신하신 문서가 전결로 최종 승인 완료되었습니다: " + (docInfo != null ? docInfo.get("docTitle") : ""));
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("전결 완료 알림 큐 적재 경고: docId={}", docId, ne);
            }

            eventPublisher.publishEvent(new ApprovalEvent(
                "COMPLETE", docId, (String) docLock.get("docNo"), docInfo != null ? (String) docInfo.get("docTitle") : "",
                (String) docLock.get("draUserId"), docInfo != null ? (String) docInfo.get("draUserNm") : "", "", "", paramMap
            ));
        }

        String resultMsg;
        if (nextLine != null) {
            resultMsg = "전결 처리되었습니다. 관리부서 결재 단계로 전환되었습니다.";
        } else if (nextPostLine != null) {
            resultMsg = "전결 처리되었습니다. 사후결재(후결) 단계로 전환되었습니다.";
        } else {
            resultMsg = nextPostLine != null ? "전결 처리되었습니다. 사후결재(후결) 단계로 전환되었습니다." : "전결 처리되어 최종 승인 종결되었습니다.";
        }

        resultMap.put("docId", docId);
        resultMap.put("nextStatus", nextDocStatus);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", resultMsg);
        return resultMap;
    }

    private boolean isCurrentApprovalActor(Map<String, Object> paramMap, Map<String, Object> docLock) {
        String userId = (String) paramMap.get("userId");
        if (userId == null) return false;
        Map<String, Object> lineParam = new HashMap<>(paramMap);
        lineParam.put("docId", docLock.get("docId"));
        List<Map<String, Object>> lines = am11Mapper.selectApprovalLineList(lineParam);
        Object currentSeq = docLock.get("currLineSeq");
        if (currentSeq == null) return false;
        for (Map<String, Object> line : lines) {
            if (String.valueOf(currentSeq).equals(String.valueOf(line.get("lineSeq")))) {
                if (userId.equals(line.get("approverId"))) return true;
                Map<String, Object> delegParam = new HashMap<>();
                delegParam.put("originUserId", line.get("approverId"));
                delegParam.put("delegateUserId", userId);
                return am11Mapper.selectActiveDelegation(delegParam) != null;
            }
        }
        return false;
    }

    private void writeAudit(Map<String, Object> paramMap, Map<String, Object> docInfo,
                            String eventType, String detail) {
        if (paramMap.get("userId") == null || docInfo == null || docInfo.get("docId") == null) {
            logger.warn("감사로그 기록 생략: 사용자 또는 문서 식별정보가 없습니다.");
            return;
        }
        Map<String, Object> audit = new HashMap<>();
        audit.put("docId", docInfo.get("docId"));
        audit.put("docNo", docInfo.get("docNo"));
        audit.put("eventType", eventType);
        audit.put("userId", paramMap.get("userId"));
        audit.put("userNm", paramMap.get("userNm"));
        audit.put("deptNm", paramMap.get("deptNm"));
        audit.put("clientIp", ApprovalSecurityUtil.maskIp((String) paramMap.get("clientIp")));
        audit.put("eventDetail", ApprovalSecurityUtil.maskSensitiveData(detail));
        am11Mapper.insertAuditLog(audit);
    }

    // 문서 열람 권한 검증 (기안자, 결재선 포함자, 대결자, 시스템 관리자)
    private boolean isAuthorizedDocViewer(String userId, Map<String, Object> docInfo, List<Map<String, Object>> lineList) {
        if (userId == null || userId.trim().isEmpty() || docInfo == null) return false;

        // 1. 기안자 본인
        String draUserId = (String) docInfo.get("draUserId");
        if (userId.equals(draUserId)) return true;

        // 2. 결재선에 포함된 자 (결재, 합의, 협조, 참조)
        if (lineList != null) {
            for (Map<String, Object> line : lineList) {
                if (userId.equals(line.get("approverId"))) return true;

                // 3. 대결자 권한 확인
                Map<String, Object> delegParam = new HashMap<>();
                delegParam.put("originUserId", line.get("approverId"));
                delegParam.put("delegateUserId", userId);
                if (am11Mapper.selectActiveDelegation(delegParam) != null) {
                    return true;
                }
            }
        }

        // 4. 시스템 관리자 (AUTH001)
        return isSystemAdmin(userId);
    }

    // 서버 사이드 시스템 관리자(AUTH001) 권한 검증
    private boolean isSystemAdmin(String userId) {
        if (userId == null || userId.trim().isEmpty()) return false;
        try {
            String authInfo = am11Mapper.selectUserAuthInfo(userId);
            if (authInfo != null && (authInfo.contains("AUTH001") || authInfo.contains("ADMIN"))) {
                return true;
            }
        } catch (Exception e) {
            logger.warn("사용자 시스템 관리자 권한 조회 오류: userId={}", userId, e);
        }
        return false;
    }

    // 헬퍼 메소드: 기안자 조직정보 Snapshot 채우기
    private void enrichDrafterSnapshot(Map<String, Object> paramMap, String userId) {
        Map<String, Object> orgInfo = am11Mapper.selectUserOrgSnapshot(userId);
        if (orgInfo != null) {
            paramMap.put("draUserId", orgInfo.get("userId"));
            paramMap.put("draUserNm", orgInfo.get("userNm"));
            paramMap.put("draDeptId", orgInfo.get("deptId"));
            paramMap.put("draDeptNm", orgInfo.get("deptNm"));
            paramMap.put("draLevelCd", orgInfo.get("levelCd"));
            paramMap.put("draLevelNm", orgInfo.get("levelNm"));
            paramMap.put("draDutyCd", orgInfo.get("dutyCd"));
            paramMap.put("draDutyNm", orgInfo.get("dutyNm"));
            if (!paramMap.containsKey("coCd") || paramMap.get("coCd") == null) {
                paramMap.put("coCd", orgInfo.get("coCd"));
            }
            if (!paramMap.containsKey("userNm") || paramMap.get("userNm") == null || String.valueOf(paramMap.get("userNm")).trim().isEmpty()) {
                paramMap.put("userNm", orgInfo.get("userNm"));
            }
            if (!paramMap.containsKey("deptNm") || paramMap.get("deptNm") == null || String.valueOf(paramMap.get("deptNm")).trim().isEmpty()) {
                paramMap.put("deptNm", orgInfo.get("deptNm"));
            }
            if (!paramMap.containsKey("levelNm") || paramMap.get("levelNm") == null || String.valueOf(paramMap.get("levelNm")).trim().isEmpty()) {
                paramMap.put("levelNm", orgInfo.get("levelNm"));
            }
        }
    }

    // 헬퍼 메소드: 결재선 Snapshot 일괄 등록
    private void saveApprovalLines(String docId, Map<String, Object> paramMap, String firstStatus) {
        am11Mapper.deleteApprovalLines(paramMap);

        List<Map<String, Object>> lineList = parseLineList(paramMap.get("lineList"));
        if (lineList != null && !lineList.isEmpty()) {
            int seq = 1;
            for (Map<String, Object> line : lineList) {
                String approverId = (String) line.get("approverId");
                Map<String, Object> approverOrg = am11Mapper.selectUserOrgSnapshot(approverId);

                Map<String, Object> lineParam = new HashMap<>(paramMap);
                lineParam.put("docId", docId);
                lineParam.put("lineSeq", seq);
                lineParam.put("lineType", line.get("lineType") != null ? line.get("lineType") : "APPR");
                lineParam.put("wb20TodoKey", line.get("wb20TodoKey"));
                lineParam.put("wb20CoCd", line.get("wb20CoCd"));
                lineParam.put("wb20TodoNo", line.get("wb20TodoNo"));
                lineParam.put("wb20SanctnSn", line.get("wb20SanctnSn"));
                lineParam.put("wb20Div1CodeId", line.get("wb20Div1CodeId"));
                lineParam.put("wb20Div2CodeId", line.get("wb20Div2CodeId"));

                // 첫 번째 순번 결재자 상태 설정
                if ("Y".equals(line.get("sourceApproved"))) {
                    lineParam.put("lineStatus", "APPROVED");
                } else if (seq == (paramMap.get("autoApprovedCount") == null ? 1 : Integer.parseInt(String.valueOf(paramMap.get("autoApprovedCount"))) + 1)
                        && "PENDING".equals(firstStatus)) {
                    lineParam.put("lineStatus", "PENDING");
                } else {
                    lineParam.put("lineStatus", "READY");
                }

                if (approverOrg != null) {
                    lineParam.put("approverId", approverOrg.get("userId"));
                    lineParam.put("approverNm", approverOrg.get("userNm"));
                    lineParam.put("deptId", approverOrg.get("deptId"));
                    lineParam.put("deptNm", approverOrg.get("deptNm"));
                    lineParam.put("levelCd", approverOrg.get("levelCd"));
                    lineParam.put("levelNm", approverOrg.get("levelNm"));
                    lineParam.put("dutyCd", approverOrg.get("dutyCd"));
                    lineParam.put("dutyNm", approverOrg.get("dutyNm"));
                } else {
                    lineParam.put("approverId", approverId);
                    lineParam.put("approverNm", line.get("approverNm"));
                    lineParam.put("deptId", line.get("deptId"));
                    lineParam.put("deptNm", line.get("deptNm"));
                }

                am11Mapper.insertApprovalLine(lineParam);
                seq++;
            }
        }
    }

    // 헬퍼 메소드: Object -> List<Map<String, Object>> 안전 변환
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseLineList(Object raw) {
        if (raw == null) return null;
        if (raw instanceof List) {
            return (List<Map<String, Object>>) raw;
        }
        if (raw instanceof String) {
            String str = ((String) raw).trim();
            if (str.isEmpty()) return null;
            try {
                return objectMapper.readValue(str, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                logger.error("결재선 JSON 파싱 오류: {}", str, e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> recordDocumentRead(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");
        if (docId == null || docId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "문서 ID 또는 사용자 ID가 유효하지 않습니다.");
            return resultMap;
        }

        // 문서 열람 권한 검증 (기안자, 결재선/참조선 포함자, 대결자, 관리자)
        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        List<Map<String, Object>> lineList = am11Mapper.selectApprovalLineList(paramMap);
        if (!isAuthorizedDocViewer(userId, docInfo, lineList)) {
            resultMap.put("resultCode", "403");
            resultMap.put("resultMessage", "해당 결재문서에 대한 열람 권한이 없습니다.");
            return resultMap;
        }

        // 사용자 조직정보 Snapshot 채우기 (deptNm 등)
        Map<String, Object> userOrg = am11Mapper.selectUserOrgSnapshot(userId);
        if (userOrg != null) {
            paramMap.put("userNm", userOrg.get("userNm"));
            paramMap.put("deptNm", userOrg.get("deptNm"));
        }

        am11Mapper.mergeDocReadStatus(paramMap);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "문서 열람 상태가 정상 반영되었습니다.");
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> selectDocReadList(Map<String, Object> paramMap) {
        String userId = (String) paramMap.get("userId");
        Map<String, Object> docInfo = am11Mapper.selectApprovalDocInfo(paramMap);
        if (docInfo == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> lineList = am11Mapper.selectApprovalLineList(paramMap);
        if (!isAuthorizedDocViewer(userId, docInfo, lineList)) {
            logger.warn("문서 열람 이력 목록 조회 권한 없음: docId={}, userId={}", paramMap.get("docId"), userId);
            throw new RuntimeException("해당 결재문서의 열람 이력을 조회할 권한이 없습니다.");
        }
        return am11Mapper.selectDocReadList(paramMap);
    }

    @Override
    public Map<String, Object> changeApprovalLines(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String docId = (String) paramMap.get("docId");
        String userId = (String) paramMap.get("userId");
        String changeReason = (String) paramMap.get("changeReason");

        if (changeReason == null || changeReason.trim().isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재선 변경 사유를 반드시 입력해야 합니다.");
            return resultMap;
        }

        // 1. 비관적 Lock (동시 결재 진행 차단)
        Map<String, Object> docLock;
        try {
            docLock = am11Mapper.selectApprovalDocForUpdate(paramMap);
        } catch (Exception e) {
            logger.warn("동시성 충돌 감지 (Lock 실패): docId={}", docId, e);
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "현재 결재 처리 중인 문서입니다. 잠시 후 다시 시도하십시오.");
            return resultMap;
        }

        if (docLock == null) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "결재 대상을 찾을 수 없습니다.");
            return resultMap;
        }

        String docStatus = (String) docLock.get("docStatus");
        if (!"REQUEST".equals(docStatus) && !"PROGRESS".equals(docStatus) && !"POST_PROGRESS".equals(docStatus)) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "진행 중인 결재 문서만 결재선을 변경할 수 있습니다. (현재 상태: " + docStatus + ")");
            return resultMap;
        }

        // 2. 서버 사이드 권한 엄격 검증: 기안자 본인, 현재 결재 순번자, 시스템 관리자(AUTH001)만 변경 가능
        String draUserId = (String) docLock.get("draUserId");
        String currApproverId = (String) docLock.get("currApproverId");
        boolean isAdmin = isSystemAdmin(userId);
        boolean isAuthorized = userId.equals(draUserId) || userId.equals(currApproverId) || isAdmin;
        if (!isAuthorized) {
            resultMap.put("resultCode", "403");
            resultMap.put("resultMessage", "기안자 또는 현재 결재자만 결재선을 변경할 수 있습니다.");
            return resultMap;
        }

        // 3. 변경 전 결재선 목록 조회 (Snapshot용)
        List<Map<String, Object>> beforeLineList = am11Mapper.selectApprovalLineList(paramMap);
        int currLineSeq = Integer.parseInt(String.valueOf(docLock.get("currLineSeq")));

        // 신규 잔여 결재선 파싱
        List<Map<String, Object>> rawNewLines = parseLineList(paramMap.get("lineList"));
        if (rawNewLines == null || rawNewLines.isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "변경할 결재선 목록이 비어있습니다. 최소 1명 이상의 잔여 결재자를 지정하십시오.");
            return resultMap;
        }

        // 4. 기승인 순번 불변성 강제 보장 및 중복 지정 검증
        Set<String> approvedUserIds = new HashSet<>();
        for (Map<String, Object> prevLine : beforeLineList) {
            int prevSeq = Integer.parseInt(String.valueOf(prevLine.get("lineSeq")));
            if (prevSeq < currLineSeq || "APPROVED".equals(prevLine.get("lineStatus"))) {
                approvedUserIds.add((String) prevLine.get("approverId"));
            }
        }

        List<Map<String, Object>> newLines = new ArrayList<>();
        for (Map<String, Object> newLine : rawNewLines) {
            String apprvId = (String) newLine.get("approverId");
            if (approvedUserIds.contains(apprvId)) {
                // 기승인자가 전체 목록에 포함되어 넘어온 경우, 기승인 상태 보존을 위해 건너뜀
                logger.info("기승인 완료 결재자({}): 잔여 결재선 등록 대상에서 제외하여 기존 승인 상태 불변 보존", apprvId);
                continue;
            }
            newLines.add(newLine);
        }

        if (newLines.isEmpty()) {
            resultMap.put("resultCode", "500");
            resultMap.put("resultMessage", "변경할 잔여 결재선이 비어있거나 모두 기승인된 결재자입니다. 최소 1명 이상의 신규 잔여 결재자를 지정하십시오.");
            return resultMap;
        }

        // 5. 잔여 미결재선만 원자적 삭제 (LINE_SEQ >= currLineSeq 인 READY/PENDING 대상만 삭제)
        paramMap.put("currLineSeq", currLineSeq);
        am11Mapper.deleteRemainingApprovalLines(paramMap);

        // 6. 신규 잔여 결재선 등록 (currLineSeq부터 순차 번호 강제 부여하여 번호 왜곡 차단)
        int nextSeq = currLineSeq;
        String nextFirstApproverId = null;
        String nextFirstApproverNm = null;

        for (Map<String, Object> line : newLines) {
            String approverId = (String) line.get("approverId");
            Map<String, Object> approverOrg = am11Mapper.selectUserOrgSnapshot(approverId);

            Map<String, Object> lineParam = new HashMap<>(paramMap);
            lineParam.put("docId", docId);
            lineParam.put("lineSeq", nextSeq);
            lineParam.put("lineType", line.get("lineType") != null ? line.get("lineType") : "APPR");
            lineParam.put("wb20TodoKey", line.get("wb20TodoKey"));
            lineParam.put("wb20CoCd", line.get("wb20CoCd"));
            lineParam.put("wb20TodoNo", line.get("wb20TodoNo"));
            lineParam.put("wb20SanctnSn", line.get("wb20SanctnSn"));
            lineParam.put("wb20Div1CodeId", line.get("wb20Div1CodeId"));
            lineParam.put("wb20Div2CodeId", line.get("wb20Div2CodeId"));

            if (nextSeq == currLineSeq) {
                lineParam.put("lineStatus", "PENDING");
                nextFirstApproverId = (approverOrg != null) ? (String) approverOrg.get("userId") : approverId;
                nextFirstApproverNm = (approverOrg != null) ? (String) approverOrg.get("userNm") : (String) line.get("approverNm");
            } else {
                lineParam.put("lineStatus", "READY");
            }

            if (approverOrg != null) {
                lineParam.put("approverId", approverOrg.get("userId"));
                lineParam.put("approverNm", approverOrg.get("userNm"));
                lineParam.put("deptId", approverOrg.get("deptId"));
                lineParam.put("deptNm", approverOrg.get("deptNm"));
                lineParam.put("levelCd", approverOrg.get("levelCd"));
                lineParam.put("levelNm", approverOrg.get("levelNm"));
                lineParam.put("dutyCd", approverOrg.get("dutyCd"));
                lineParam.put("dutyNm", approverOrg.get("dutyNm"));
            } else {
                lineParam.put("approverId", approverId);
                lineParam.put("approverNm", line.get("approverNm"));
                lineParam.put("deptId", line.get("deptId"));
                lineParam.put("deptNm", line.get("deptNm"));
            }

            am11Mapper.insertApprovalLine(lineParam);
            nextSeq++;
        }

        // AM 변경 결재선을 WB20 원본문서에도 동일 트랜잭션으로 반영한다.
        Object erpBizKey = docLock.get("erpBizKey");
        if (erpBizKey != null && !String.valueOf(erpBizKey).trim().isEmpty() && wb20Svc != null) {
            Map<String, Object> wb20SyncParam = new HashMap<>();
            wb20SyncParam.put("todoNo", String.valueOf(erpBizKey));
            wb20SyncParam.put("coCd", String.valueOf(docLock.get("coCd")));
            wb20SyncParam.put("userId", paramMap.get("userId"));
            wb20SyncParam.put("lineList", newLines);
            wb20Svc.syncApprovalLinesFromAm(wb20SyncParam);
        }

        // 7. 문서 마스터 현재 결재자 정보 동기화
        if (nextFirstApproverId != null) {
            Map<String, Object> docUpdate = new HashMap<>(paramMap);
            docUpdate.put("nextStatus", docStatus);
            docUpdate.put("currLineSeq", currLineSeq);
            docUpdate.put("currApproverId", nextFirstApproverId);
            docUpdate.put("currApproverNm", nextFirstApproverNm);
            am11Mapper.updateApprovalDocStatus(docUpdate);
        }

        // 8. 변경 후 결재선 조회 및 Snapshot 저장 (실패 시 원자적 롤백 보장)
        if (paramMap.get("docDataJson") != null && paramMap.get("docRenderHtml") != null) {
            am11Mapper.updateApprovalDocContent(paramMap);
        }
        List<Map<String, Object>> afterLineList = am11Mapper.selectApprovalLineList(paramMap);
        try {
            String beforeJson = objectMapper.writeValueAsString(beforeLineList);
            String afterJson = objectMapper.writeValueAsString(afterLineList);

            Map<String, Object> histParam = new HashMap<>(paramMap);
            histParam.put("docId", docId);
            histParam.put("chgRsn", ApprovalSecurityUtil.maskSensitiveData(changeReason));
            histParam.put("prevLineJson", beforeJson);
            histParam.put("nextLineJson", afterJson);
            am11Mapper.insertLineChangeHist(histParam);
        } catch (Exception je) {
            logger.error("결재선 변경 스냅샷 생성 실패 (원자적 롤백 트리거): docId={}", docId, je);
            throw new RuntimeException("결재선 변경 스냅샷 생성 실패로 인해 전체 변경 처리가 롤백되었습니다: " + je.getMessage(), je);
        }

        // 9. 결재 행위 이력 및 감사로그 기록 (민감정보 마스킹 적용)
        paramMap.put("actType", "LINE_CHG");
        paramMap.put("prevStatus", docStatus);
        paramMap.put("nextStatus", docStatus);
        paramMap.put("actOpinion", "결재선 변경: " + ApprovalSecurityUtil.maskSensitiveData(changeReason));
        am11Mapper.insertApprovalHist(paramMap);
        writeAudit(paramMap, docLock, "LINE_CHG", "결재선 변경: " + ApprovalSecurityUtil.maskSensitiveData(changeReason));

        // 10. 변경된 새 결재자에게 알림 큐 적재 및 이벤트 발행
        if (nextFirstApproverId != null) {
            try {
                Map<String, Object> notifParam = new HashMap<>();
                notifParam.put("docId", docId);
                notifParam.put("eventType", "APPROVE_NEXT");
                notifParam.put("receiverId", nextFirstApproverId);
                notifParam.put("receiverNm", nextFirstApproverNm);
                notifParam.put("notifChannel", "KAKAO");
                notifParam.put("notifTitle", "[" + docLock.get("docNo") + "] 결재선 변경에 따른 결재 대기 요청");
                notifParam.put("notifMsg", "결재선이 변경되어 결재 대기 순번이 도래하였습니다: " + docLock.get("docTitle"));
                approvalQueueSvc.enqueueNotification(notifParam);
            } catch (Exception ne) {
                logger.warn("결재선 변경 알림 큐 적재 경고: docId={}", docId, ne);
            }

            eventPublisher.publishEvent(new ApprovalEvent(
                "APPROVE_NEXT", docId, (String) docLock.get("docNo"), "결재선 변경에 따른 결재 요청",
                draUserId, "", nextFirstApproverId, nextFirstApproverNm, paramMap
            ));
        }

        resultMap.put("docId", docId);
        resultMap.put("resultCode", "200");
        resultMap.put("resultMessage", "결재선이 성공적으로 변경되었습니다.");
        return resultMap;
    }
    private void executeLinkedWb20Approval(Map<String, Object> docLock, Map<String, Object> amLine, String approverId,
            int amLineSeq, String userId, String apprOpinion) {
        String erpBizKey = docLock.get("erpBizKey") == null ? null : String.valueOf(docLock.get("erpBizKey")).trim();
        if (erpBizKey == null || erpBizKey.isEmpty() || wb20Svc == null || amLine == null) return;

        // 1. 유일키 (wb20Div2CodeId + wb20TodoNo) 및 wb20TodoKey 추출
        String todoNo = amLine.get("wb20TodoNo") != null ? amLine.get("wb20TodoNo").toString().trim() : erpBizKey;
        String div2CodeId = amLine.get("wb20Div2CodeId") != null ? amLine.get("wb20Div2CodeId").toString().trim() : null;
        String todoKey = amLine.get("wb20TodoKey") != null ? amLine.get("wb20TodoKey").toString().trim() : null;

        Map<String, String> targetLine = null;

        // 2. wb20TodoKey가 있는 경우, selectCurrentUserApprovalDataListFromTodoKey API를 통해 결재자료 확인
        if (todoKey != null && !todoKey.isEmpty()) {
            Map<String, String> chkParam = new HashMap<>();
            chkParam.put("todoKey", todoKey);
            chkParam.put("userId", approverId != null ? approverId : userId);
            List<Map<String, String>> curApprovalList = wb20Svc.selectCurrentUserApprovalDataListFromTodoKey(chkParam);
            if ((curApprovalList == null || curApprovalList.isEmpty()) && userId != null && !userId.equals(approverId)) {
                chkParam.put("userId", userId);
                curApprovalList = wb20Svc.selectCurrentUserApprovalDataListFromTodoKey(chkParam);
            }
            if (curApprovalList != null && !curApprovalList.isEmpty()) {
                targetLine = curApprovalList.get(0);
            }
        }

        // 3. todoKey로 확인되지 않는 경우, 유일키(todoDiv2CodeId + todoNo) 기반으로 WB20 결재행 조회
        if (targetLine == null) {
            Map<String, String> findParam = new HashMap<>();
            findParam.put("todoNo", todoNo);
            if (div2CodeId != null && !div2CodeId.isEmpty()) findParam.put("todoDiv2CodeId", div2CodeId);

            List<Map<String, String>> wb20Lines = wb20Svc.selectGetApprovalList(findParam);
            if (wb20Lines == null || wb20Lines.isEmpty()) {
                throw new IllegalStateException("연결된 WB20 결재행을 찾을 수 없습니다. (TODO_NO=" + todoNo + "). 결재를 롤백합니다.");
            }

            for (Map<String, String> row : wb20Lines) {
                if (todoKey != null && todoKey.equals(row.get("todoKey"))) {
                    targetLine = row;
                    break;
                }
                if (targetLine == null && approverId != null && approverId.equalsIgnoreCase(row.get("todoId"))) {
                    targetLine = row;
                } else if (targetLine == null && String.valueOf(amLineSeq).equals(row.get("sanctnSn"))) {
                    targetLine = row;
                }
            }
            if (targetLine == null) targetLine = wb20Lines.get(0);
        }

        // 4. WB20 승인 실행
        Map<String, String> approvalParam = new HashMap<>(targetLine);
        approvalParam.put("userId", userId);
        approvalParam.put("todoCfOpn", apprOpinion);
        approvalParam.put("pgmId", "AM1201M01");
        approvalParam.put("amLinkedApproval", "Y");

        Map<String, String> approvalResult = wb20Svc.insertApprovalLine(approvalParam);
        int approvalResultCount = 0;
        try {
            Object cntObj = approvalResult != null
                    ? (approvalResult.get("resultCount") != null ? approvalResult.get("resultCount") : approvalResult.get("RESULT_COUNT"))
                    : null;
            approvalResultCount = cntObj == null ? 0 : Integer.parseInt(String.valueOf(cntObj));
        } catch (Exception e) {
            approvalResultCount = 0;
        }
        if (approvalResultCount < 1) {
            throw new IllegalStateException("WB20 기존 결재 API가 결재행을 완료 처리하지 못했습니다. 결재를 롤백합니다.");
        }
    }

    @Override
    public String selectDocIdByBizKey(Map<String, Object> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        return am11Mapper.selectDocIdByBizKey(paramMap);
    }
}
