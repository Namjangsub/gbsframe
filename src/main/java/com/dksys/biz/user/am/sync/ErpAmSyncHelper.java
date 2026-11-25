package com.dksys.biz.user.am.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.am11.service.AM11Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;

@Component
public class ErpAmSyncHelper {

    private static final Logger logger = LoggerFactory.getLogger(ErpAmSyncHelper.class);

    private static final String DEFAULT_CO_CD = "GUN";

    // 기존 AM 상신 호출부(PM07/PM08/PM51/CR02) 모두 formVer=1 을 사용한다.
    private static final int FORM_VER = 1;

    @Value("${erp.am.sync.enabled:true}")
    private boolean amSyncEnabled;

    @Autowired
    private WB20Svc wb20Svc;

    @Autowired
    private AM11Svc am11Svc;

    public void sync(ErpAmSyncAdapter adapter, Map<String, String> paramMap, String todoDiv2CodeId) {
        if (!amSyncEnabled) {
            return;
        }
        try {
            String erpBizKey = adapter.erpBizKey(paramMap, todoDiv2CodeId);
            if (isBlank(erpBizKey)) {
                return;
            }

            String apprDiv2 = adapter.apprDiv2(todoDiv2CodeId);
            if (isBlank(apprDiv2)) {
                return;
            }
            String refDiv2 = adapter.refDiv2(todoDiv2CodeId);

            String coCd = paramMap.get("coCd");
            if (isBlank(coCd)) {
                coCd = DEFAULT_CO_CD;
            }

            // todoDiv2CodeIds 를 함께 넘겨야 selectGetApprovalList 의 MAX(CREAT_DTTM) 부분이 이 단계로만 한정된다.
            // (todoNo 만 넘기면 같은 업무키의 최신 단계 행만 조회되어 이전 단계 문서가 생성되지 않는다)
            Map<String, String> wb20Query = new HashMap<>();
            wb20Query.put("todoNo", erpBizKey);
            wb20Query.put("coCd", coCd);
            wb20Query.put("todoDiv2CodeIds", isBlank(refDiv2) ? apprDiv2 : apprDiv2 + "," + refDiv2);
            List<Map<String, String>> wb20Lines = wb20Svc.selectGetApprovalList(wb20Query);
            if (wb20Lines == null || wb20Lines.isEmpty()) {
                return;
            }

            List<Map<String, String>> apprList = new ArrayList<>();
            List<Map<String, String>> refList = new ArrayList<>();
            for (Map<String, String> row : wb20Lines) {
                String div2 = row.get("todoDiv2CodeId");
                if (apprDiv2.equals(div2)) {
                    apprList.add(row);
                } else if (refDiv2 != null && refDiv2.equals(div2)) {
                    refList.add(row);
                }
            }

            // 해당 단계 결재 라인이 없으면 문서를 만들지 않는다 (전체 라인 APPR 폴백 없음).
            if (apprList.isEmpty()) {
                return;
            }

            Comparator<Map<String, String>> bySanctnSn = new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    return Integer.compare(parseIntSafe(o1.get("sanctnSn")), parseIntSafe(o2.get("sanctnSn")));
                }
            };
            Collections.sort(apprList, bySanctnSn);
            Collections.sort(refList, bySanctnSn);

            List<Map<String, Object>> amLineList = new ArrayList<>();
            for (Map<String, String> row : apprList) {
                amLineList.add(toAmLine(row, false));
            }
            for (Map<String, String> row : refList) {
                amLineList.add(toAmLine(row, true));
            }

            int autoApprovedCount = 0;
            for (Map<String, Object> line : amLineList) {
                if (!"Y".equals(line.get("sourceApproved"))) {
                    break;
                }
                autoApprovedCount++;
            }

            String applicantId = paramMap.get("reqId");
            if (isBlank(applicantId)) {
                applicantId = paramMap.get("userId");
            }
            String applicantNm = paramMap.get("reqNm");
            if (isBlank(applicantNm)) {
                applicantNm = paramMap.get("userNm");
            }

            String erpBizType = adapter.erpBizType();
            String existingDocId = adapter.selectExistingDocId(paramMap, todoDiv2CodeId);

            Map<String, Object> amParam = new HashMap<>();
            if (!isBlank(existingDocId)) {
                amParam.put("docId", existingDocId);
            }
            amParam.put("coCd", coCd);
            amParam.put("userId", applicantId);
            amParam.put("userNm", applicantNm);
            amParam.put("docTitle", adapter.buildTitle(paramMap, todoDiv2CodeId));
            amParam.put("formCd", adapter.formCd(todoDiv2CodeId));
            amParam.put("formVer", FORM_VER);
            amParam.put("erpBizType", erpBizType);
            amParam.put("erpBizKey", erpBizKey);
            amParam.put("docDataJson", adapter.buildDocDataJson(paramMap, todoDiv2CodeId));
            amParam.put("docRenderHtml", adapter.buildHtml(paramMap, todoDiv2CodeId));
            amParam.put("pgmId", adapter.pgmId());
            amParam.put("lineList", amLineList);
            amParam.put("autoApprovedCount", autoApprovedCount);

            Map<String, Object> amResult = am11Svc.submitApproval(amParam);
            String submitResultCode = amResult == null ? null : String.valueOf(amResult.get("resultCode"));
            // 기존 AM 문서가 진행 중이라 재상신이 거절된 경우에는 WB20 결재선 및 문서 본문/제목을 AM에 동기화한다.
            if (!isBlank(existingDocId) && amResult != null && !"200".equals(submitResultCode)) {
                Map<String, Object> lineChangeParam = new HashMap<>(amParam);
                lineChangeParam.put("changeReason", erpBizType + " 결재선 수정 동기화");
                lineChangeParam.put("lineList", amLineList);
                lineChangeParam.put("todoDiv2CodeIds", isBlank(refDiv2) ? apprDiv2 : apprDiv2 + "," + refDiv2);
                Map<String, Object> changeResult = am11Svc.changeApprovalLines(lineChangeParam);
                String changeCode = changeResult == null ? null : String.valueOf(changeResult.get("resultCode"));
                if ("200".equals(changeCode)) {
                    am11Svc.updateApprovalDocContent(amParam);
                } else {
                    logger.warn("전자결재(AM) 수정 동기화 건너뜀: erpBizType={}, erpBizKey={}, docId={}, changeCode={}, msg={}",
                            erpBizType, erpBizKey, existingDocId, changeCode,
                            changeResult == null ? null : changeResult.get("resultMessage"));
                }
            }

            logger.info("전자결재(AM) 상신 동기화: erpBizType={}, erpBizKey={}, todoDiv2CodeId={}, lineCount={}, submitResultCode={}, reSubmit={}",
                    erpBizType, erpBizKey, todoDiv2CodeId, amLineList.size(), submitResultCode, !isBlank(existingDocId));
        } catch (Exception e) {
            throw new RuntimeException("전자결재(AM) 연동 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public void deleteAmDocs(ErpAmSyncAdapter adapter, Map<String, String> paramMap) {
        if (!amSyncEnabled) {
            return;
        }
        try {
            adapter.deleteDocsByBizKey(paramMap);
        } catch (Exception e) {
            throw new RuntimeException("전자결재 문서 삭제 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /** 원천 업무 코멘트/내용 수정 시 기존 AM 문서의 DOC_RENDER_HTML 만 최신 DB 기준으로 재렌더링한다. */
    public void rerenderDocHtml(ErpAmSyncAdapter adapter, Map<String, String> paramMap, String todoDiv2CodeId) {
        if (!amSyncEnabled || adapter == null || paramMap == null) {
            return;
        }
        String existingDocId = adapter.selectExistingDocId(paramMap, todoDiv2CodeId);
        if (isBlank(existingDocId)) {
            return;
        }
        Map<String, Object> updateParam = new HashMap<>();
        updateParam.put("docId", existingDocId);
        updateParam.put("docRenderHtml", adapter.buildHtml(paramMap, todoDiv2CodeId));
        updateParam.put("userId", paramMap.get("userId"));
        updateParam.put("pgmId", isBlank(paramMap.get("pgmId")) ? adapter.pgmId() : paramMap.get("pgmId"));
        am11Svc.updateDocRenderHtmlById(updateParam);
    }

    private Map<String, Object> toAmLine(Map<String, String> row, boolean isRef) {
        String raw = row.get("lineType");
        String lineType;
        if (isRef) {
            lineType = ("REF".equals(raw) || "REFERENCE".equals(raw)) ? raw : "REF";
        } else {
            lineType = isBlank(raw) ? "APPR" : raw;
        }

        String todoNm = row.get("todoNm");
        Map<String, Object> amLine = new HashMap<>();
        amLine.put("approverId", row.get("todoId"));
        amLine.put("approverNm", isBlank(todoNm) ? row.get("name") : todoNm);
        amLine.put("deptId", row.get("deptId"));
        amLine.put("lineSeq", row.get("sanctnSn"));
        amLine.put("wb20TodoKey", row.get("todoKey"));
        amLine.put("wb20CoCd", row.get("coCd"));
        amLine.put("wb20TodoNo", row.get("todoNo"));
        amLine.put("wb20SanctnSn", row.get("sanctnSn"));
        amLine.put("wb20Div1CodeId", row.get("todoDiv1CodeId"));
        amLine.put("wb20Div2CodeId", row.get("todoDiv2CodeId"));
        amLine.put("lineType", lineType);
        amLine.put("sourceApproved", !isRef && "Y".equalsIgnoreCase(row.get("sanctnSttus")) ? "Y" : "N");
        return amLine;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static int parseIntSafe(String value) {
        if (isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
