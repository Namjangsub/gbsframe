package com.dksys.biz.user.wb.wb24.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.sync.ErpAmSyncAdapter;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.google.gson.GsonBuilder;

/**
 * WB24 문제(TODODIV2060)·조치(TODODIV2090) 결재의 전자결재(AM) 연동 어댑터.
 * ERP_BIZ_KEY 는 ISS_NO 이며, 결재 단계(div2)별로 AM 문서 1건을 만든다.
 */
@Component
public class WbsIssueAmSyncAdapter implements ErpAmSyncAdapter {

    private static final String ERP_BIZ_TYPE = "WB24";
    private static final String FORM_CD = "WB24";
    private static final String PGM_ID = "WB2401P11";
    private static final String DIV2_ISSUE = "TODODIV2060";
    private static final String DIV2_ACTION = "TODODIV2090";
    private static final String DIV2_ISSUE_SHARE = "TODODIV1030";
    private static final String DIV2_ACTION_SHARE = "TODODIV1090";

    @Autowired
    private WB24Mapper wb24Mapper;

    @Override
    public String erpBizType() {
        return ERP_BIZ_TYPE;
    }

    @Override
    public String erpBizKey(Map<String, String> paramMap, String todoDiv2CodeId) {
        return trimToNull(paramMap.get("issNo"));
    }

    @Override
    public String formCd(String todoDiv2CodeId) {
        return FORM_CD;
    }

    @Override
    public String pgmId() {
        return PGM_ID;
    }

    @Override
    public String apprDiv2(String todoDiv2CodeId) {
        return (DIV2_ISSUE.equals(todoDiv2CodeId) || DIV2_ACTION.equals(todoDiv2CodeId)) ? todoDiv2CodeId : null;
    }

    @Override
    public String refDiv2(String todoDiv2CodeId) {
        if (DIV2_ISSUE.equals(todoDiv2CodeId)) {
            return DIV2_ISSUE_SHARE;
        }
        if (DIV2_ACTION.equals(todoDiv2CodeId)) {
            return DIV2_ACTION_SHARE;
        }
        return null;
    }

    @Override
    public String buildTitle(Map<String, String> paramMap, String todoDiv2CodeId) {
        Map<?, ?> dbInfo = selectIssueAmInfo(paramMap, todoDiv2CodeId);
        return pick(dbInfo, paramMap, "clntNm") + "-" + pick(dbInfo, paramMap, "clntPjtNm")
                + "(" + pick(dbInfo, paramMap, "salesCd") + ") "
                + pick(dbInfo, paramMap, "wbsPlanCodeNm")
                + (DIV2_ACTION.equals(todoDiv2CodeId) ? " 조치 결재" : " 이슈 결재");
    }

    @Override
    public String buildDocDataJson(Map<String, String> paramMap, String todoDiv2CodeId) {
        Map<?, ?> dbInfo = selectIssueAmInfo(paramMap, todoDiv2CodeId);
        Map<String, String> data = new LinkedHashMap<>();
        data.put("issNo", pick(dbInfo, paramMap, "issNo"));
        data.put("todoDiv2CodeId", nz(todoDiv2CodeId));
        data.put("coCd", pick(dbInfo, paramMap, "coCd"));
        data.put("salesCd", pick(dbInfo, paramMap, "salesCd"));
        data.put("clntNm", pick(dbInfo, paramMap, "clntNm"));
        data.put("clntPjtNm", pick(dbInfo, paramMap, "clntPjtNm"));
        data.put("wbsPlanCodeNm", pick(dbInfo, paramMap, "wbsPlanCodeNm"));
        data.put("issSj", pick(dbInfo, paramMap, "issSj"));
        data.put("actNm", pick(dbInfo, paramMap, "actNm"));
        data.put("issStsNm", pick(dbInfo, paramMap, "issStsNm"));
        data.put("subCdNm", pick(dbInfo, paramMap, "subCdNm"));
        data.put("mcPartNm", pick(dbInfo, paramMap, "mcPartNm"));
        data.put("importantNm", pick(dbInfo, paramMap, "importantNm"));
        data.put("impactNm", pick(dbInfo, paramMap, "impactNm"));
        data.put("reqDt", pick(dbInfo, paramMap, "reqDt"));
        data.put("custReqDt", pick(dbInfo, paramMap, "custReqDt"));
        data.put("vendNm", pick(dbInfo, paramMap, "vendNm"));
        String pgPath = pgPath(paramMap);
        if (pgPath != null) {
            data.put("pgPath", pgPath);
        }
        return new GsonBuilder().disableHtmlEscaping().create().toJson(data);
    }

    @Override
    public String buildHtml(Map<String, String> paramMap, String todoDiv2CodeId) {
        boolean isAction = DIV2_ACTION.equals(todoDiv2CodeId);
        Map<?, ?> dbInfo = selectIssueAmInfo(paramMap, todoDiv2CodeId);
        String issNo = pick(dbInfo, paramMap, "issNo");
        String clntNm = pick(dbInfo, paramMap, "clntNm");
        String clntPjtNm = pick(dbInfo, paramMap, "clntPjtNm");
        String salesCd = pick(dbInfo, paramMap, "salesCd");
        String wbsPlanCodeNm = pick(dbInfo, paramMap, "wbsPlanCodeNm");
        String issStsNm = pick(dbInfo, paramMap, "issStsNm");
        String actNm = pick(dbInfo, paramMap, "actNm");
        String vendNm = pick(dbInfo, paramMap, "vendNm");
        String reqDt = pick(dbInfo, paramMap, "reqDt");
        String custReqDt = pick(dbInfo, paramMap, "custReqDt");
        String subCdNm = pick(dbInfo, paramMap, "subCdNm");
        String etcInput = pick(dbInfo, paramMap, "etcInput");
        String subCdLabel = etcInput.isEmpty() ? subCdNm : (subCdNm.isEmpty() ? etcInput : subCdNm + " (" + etcInput + ")");
        String mcPartNm = pick(dbInfo, paramMap, "mcPartNm");
        String importantNm = pick(dbInfo, paramMap, "importantNm");
        String impactNm = pick(dbInfo, paramMap, "impactNm");
        String issSj = pick(dbInfo, paramMap, "issSj");
        String issCnts = pick(dbInfo, paramMap, "issCnts");

        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"table_input\" style=\"width:100%; border-collapse:collapse; border:1px solid #ddd; margin-bottom:10px;\">");
        sb.append("<colgroup><col style=\"width:18%;\"><col style=\"width:32%;\"><col style=\"width:18%;\"><col style=\"width:32%;\"></colgroup>");
        appendRow(sb, "이슈번호", issNo, "구분", isAction ? "WBS 조치 결재" : "WBS 문제 결재");
        appendRow(sb, "고객사", clntNm, "프로젝트", clntPjtNm + "(" + salesCd + ")");
        appendRow(sb, "계획구분", wbsPlanCodeNm, "진행상태", issStsNm);
        appendRow(sb, "조치담당자", actNm, "발생공급업체", vendNm);
        appendRow(sb, "사내 완료일", reqDt, "고객 조치완료일", custReqDt);
        appendRow(sb, "장애유형", subCdLabel, "기계분류", mcPartNm);
        appendRow(sb, "중요도", importantNm, "영향도", impactNm);
        appendWideRow(sb, "이슈제목", issSj);
        appendWideRow(sb, "이슈내용", issCnts);
        if (isAction) {
            String actsDt = pick(dbInfo, paramMap, "actsDt");
            String acteDt = pick(dbInfo, paramMap, "acteDt");
            String actMh = pick(dbInfo, paramMap, "actMh");
            String measRst = pick(dbInfo, paramMap, "measRst");
            String actCnts = pick(dbInfo, paramMap, "actCnts");
            String fdmtSolutNm = pick(dbInfo, paramMap, "fdmtSolutNm");
            String fdmtSolutCnt = pick(dbInfo, paramMap, "fdmtSolutCnt");
            appendRow(sb, "조치기간", actsDt + " ~ " + acteDt, "투입공수", actMh);
            appendWideRow(sb, "원인(측정결과)", measRst);
            appendWideRow(sb, "결과(조치내용)", actCnts);
            appendWideRow(sb, "근본원인", fdmtSolutNm);
            appendWideRow(sb, "근본예방대책", fdmtSolutCnt);
        }
        sb.append("</table>");
        return sb.toString();
    }

    private Map<?, ?> selectIssueAmInfo(Map<String, String> paramMap, String todoDiv2CodeId) {
        String issNo = erpBizKey(paramMap, todoDiv2CodeId);
        if (issNo == null) {
            return Collections.emptyMap();
        }
        Map<String, String> query = new HashMap<>();
        query.put("issNo", issNo);
        Map<String, String> info = wb24Mapper.selectWbsIssueAmInfo(query);
        return info == null ? Collections.emptyMap() : info;
    }

    private static String pick(Map<?, ?> primary, Map<String, String> fallback, String key) {
        if (primary != null && primary.get(key) != null) {
            String v = String.valueOf(primary.get(key));
            if (!v.trim().isEmpty()) {
                return v;
            }
        }
        if (fallback != null && fallback.get(key) != null) {
            return fallback.get(key);
        }
        return "";
    }

    @Override
    public String selectExistingDocId(Map<String, String> paramMap, String todoDiv2CodeId) {
        Map<String, String> query = new HashMap<>();
        query.put("issNo", erpBizKey(paramMap, todoDiv2CodeId));
        query.put("todoDiv2CodeId", todoDiv2CodeId);
        return trimToNull(wb24Mapper.selectAmDocIdByIssNo(query));
    }

    @Override
    public void deleteDocsByBizKey(Map<String, String> paramMap) {
        String issNo = erpBizKey(paramMap, null);
        if (issNo == null) {
            return;
        }
        Map<String, String> query = new HashMap<>();
        query.put("issNo", issNo);
        wb24Mapper.deleteAmD01ByIssNo(query);
        wb24Mapper.deleteAmM01ByIssNo(query);
    }

    private static String pgPath(Map<String, String> paramMap) {
        String pgmId = trimToNull(paramMap.get("pgmId"));
        if (pgmId == null || !pgmId.startsWith("WB24")) {
            return null;
        }
        return "/user/wb/wb24/" + pgmId + ".html";
    }

    private static void appendRow(StringBuilder sb, String label1, String value1, String label2, String value2) {
        sb.append("<tr>");
        sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">").append(escapeHtml(label1)).append("</th>");
        sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(value1)).append("</td>");
        sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">").append(escapeHtml(label2)).append("</th>");
        sb.append("<td style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(value2)).append("</td>");
        sb.append("</tr>");
    }

    private static void appendWideRow(StringBuilder sb, String label, String value) {
        sb.append("<tr>");
        sb.append("<th style=\"background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;\">").append(escapeHtml(label)).append("</th>");
        sb.append("<td colspan=\"3\" style=\"border:1px solid #ddd; padding:8px;\">").append(escapeHtml(value).replace("\n", "<br>")).append("</td>");
        sb.append("</tr>");
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    private static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
