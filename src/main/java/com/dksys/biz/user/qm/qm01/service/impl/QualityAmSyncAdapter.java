package com.dksys.biz.user.qm.qm01.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.sync.ErpAmSyncAdapter;
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * QM01 발주요청서(요청 TODODIV2020 / 결과 TODODIV2030) 전자결재(AM) 상신 동기화 어댑터.
 * 요청과 결과를 동시처리(sameTimeResult=Y)한 건은 요청 문서 1건으로 통합하므로 결과 문서를 만들지 않는다.
 */
@Component
public class QualityAmSyncAdapter implements ErpAmSyncAdapter {

    private static final String ERP_BIZ_TYPE = "QM01";
    private static final String FORM_CD = "QM01";
    private static final String PGM_ID = "QM0101P01";
    private static final String DIV2_REQ = "TODODIV2020";
    private static final String DIV2_RSLT = "TODODIV2030";
    private static final String DIV2_REQ_SHARE = "TODODIV1040";
    private static final String DIV2_RSLT_SHARE = "TODODIV1050";

    private static final String TH_STYLE = "background:#f5f5f5; border:1px solid #ddd; padding:8px; text-align:center;";
    private static final String TD_STYLE = "border:1px solid #ddd; padding:8px;";
    private static final String TD_WRAP_STYLE = "border:1px solid #ddd; padding:8px; white-space:pre-wrap;";

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private QM01Mapper qm01Mapper;

    @Override
    public String erpBizType() {
        return ERP_BIZ_TYPE;
    }

    @Override
    public String erpBizKey(Map<String, String> paramMap, String todoDiv2CodeId) {
        if (DIV2_REQ.equals(todoDiv2CodeId)) {
            return blankToNull(resolveReqNo(paramMap));
        }
        if (DIV2_RSLT.equals(todoDiv2CodeId)) {
            if (isSameTimeResult(paramMap)) {
                return null;
            }
            return blankToNull(resolveRsltNo(paramMap));
        }
        return null;
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
        if (DIV2_REQ.equals(todoDiv2CodeId) || DIV2_RSLT.equals(todoDiv2CodeId)) {
            return todoDiv2CodeId;
        }
        return null;
    }

    @Override
    public String refDiv2(String todoDiv2CodeId) {
        if (DIV2_REQ.equals(todoDiv2CodeId)) {
            return DIV2_REQ_SHARE;
        }
        if (DIV2_RSLT.equals(todoDiv2CodeId)) {
            return DIV2_RSLT_SHARE;
        }
        return null;
    }

    @Override
    public String buildTitle(Map<String, String> paramMap, String todoDiv2CodeId) {
        boolean isResult = DIV2_RSLT.equals(todoDiv2CodeId);
        Map<String, String> info = selectReqInfo(paramMap);
        String clntNm = nz(info.get("clntNm"));
        String key = isResult ? resolveRsltNo(paramMap) : resolveReqNo(paramMap);
        StringBuilder sb = new StringBuilder();
        sb.append(isResult ? "[발주결과서] " : "[발주요청서] ");
        if (!clntNm.isEmpty()) {
            sb.append(clntNm).append(" ");
        }
        sb.append("(").append(key).append(")");
        return sb.toString();
    }

    @Override
    public String buildDocDataJson(Map<String, String> paramMap, String todoDiv2CodeId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stage", DIV2_RSLT.equals(todoDiv2CodeId) ? "RESULT" : "REQUEST");
        data.put("coCd", nz(paramMap.get("coCd")));
        data.put("reqNo", resolveReqNo(paramMap));
        data.put("rsltNo", resolveRsltNo(paramMap));
        data.put("sameTimeResult", isSameTimeResult(paramMap) ? "Y" : "N");
        return gson.toJson(data);
    }

    @Override
    public String buildHtml(Map<String, String> paramMap, String todoDiv2CodeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"table_input\" style=\"width:100%; border-collapse:collapse; border:1px solid #ddd; margin-bottom:10px;\">");
        sb.append("<colgroup><col style=\"width:18%;\"><col style=\"width:32%;\"><col style=\"width:18%;\"><col style=\"width:32%;\"></colgroup>");
        if (DIV2_RSLT.equals(todoDiv2CodeId)) {
            appendResultRows(sb, selectRespInfo(resolveRsltNo(paramMap)));
        } else {
            appendRequestRows(sb, selectReqInfo(paramMap));
            if (isSameTimeResult(paramMap)) {
                appendResultRows(sb, selectRespInfo(rsltNoOf(resolveReqNo(paramMap))));
            }
        }
        sb.append("</table>");
        return sb.toString();
    }

    @Override
    public String selectExistingDocId(Map<String, String> paramMap, String todoDiv2CodeId) {
        String erpBizKey = erpBizKey(paramMap, todoDiv2CodeId);
        if (blank(erpBizKey)) {
            return null;
        }
        Map<String, String> param = new HashMap<>();
        param.put("erpBizKey", erpBizKey);
        param.put("todoDiv2CodeId", apprDiv2(todoDiv2CodeId));
        return blankToNull(qm01Mapper.selectAmDocIdByQmBizKey(param));
    }

    @Override
    public void deleteDocsByBizKey(Map<String, String> paramMap) {
        Map<String, String> param = new HashMap<>();
        param.put("reqNo", blankToNull(resolveReqNo(paramMap)));
        param.put("rsltNo", blankToNull(resolveRsltNo(paramMap)));
        if (param.get("reqNo") == null && param.get("rsltNo") == null) {
            return;
        }
        qm01Mapper.deleteAmD01ByQmBizKey(param);
        qm01Mapper.deleteAmM01ByQmBizKey(param);
    }

    private void appendRequestRows(StringBuilder sb, Map<String, String> info) {
        pair(sb, "발주요청번호", str(info, "reqNo"), "발행일자", str(info, "ordrgDt"));
        pair(sb, "발행자", str(info, "ordrgNm"), "작성자", str(info, "regNm"));
        pair(sb, "고객사", str(info, "clntNm"), "Project명", str(info, "projNm"));
        pair(sb, "고객사PJT", str(info, "clntPjtNm"), "요청구분", str(info, "reqCdNm"));
        pair(sb, "요구일정", str(info, "reqDt"), "문제등록번호", str(info, "workRptNo"));
        wide(sb, "요청 등록 사유", str(info, "reqRmk"));
    }

    private void appendResultRows(StringBuilder sb, Map<String, String> info) {
        pair(sb, "실적번호", str(info, "rsltNo"), "발주요청번호", str(info, "reqNo"));
        pair(sb, "결과 등록자", str(info, "resRegNm"), "등록일자", str(info, "resDt"));
        pair(sb, "조치자 투입공수", str(info, "actMh"), "근본원인", str(info, "fdmtSolutNm"));
        wide(sb, "원인", str(info, "measRst"));
        wide(sb, "결과사유", str(info, "resltRst"));
        wide(sb, "근본예방대책", str(info, "fdmtSolutCnt"));
    }

    private static String str(Map<?, ?> map, String key) {
        if (map == null) {
            return "";
        }
        Object v = map.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private void pair(StringBuilder sb, String label1, String value1, String label2, String value2) {
        sb.append("<tr>")
          .append("<th style=\"").append(TH_STYLE).append("\">").append(escapeHtml(label1)).append("</th>")
          .append("<td style=\"").append(TD_STYLE).append("\">").append(escapeHtml(value1)).append("</td>")
          .append("<th style=\"").append(TH_STYLE).append("\">").append(escapeHtml(label2)).append("</th>")
          .append("<td style=\"").append(TD_STYLE).append("\">").append(escapeHtml(value2)).append("</td>")
          .append("</tr>");
    }

    private void wide(StringBuilder sb, String label, String value) {
        sb.append("<tr>")
          .append("<th style=\"").append(TH_STYLE).append("\">").append(escapeHtml(label)).append("</th>")
          .append("<td colspan=\"3\" style=\"").append(TD_WRAP_STYLE).append("\">").append(escapeHtml(value)).append("</td>")
          .append("</tr>");
    }

    private Map<String, String> selectReqInfo(Map<String, String> paramMap) {
        String reqNo = resolveReqNo(paramMap);
        if (blank(reqNo)) {
            return new HashMap<>();
        }
        Map<String, String> param = new HashMap<>();
        param.put("reqNo", reqNo);
        param.put("userId", nz(paramMap.get("userId")));
        param.put("onlyLgistQM", "N");
        Map<String, String> info = qm01Mapper.selectQtyReqInfo(param);
        return info == null ? new HashMap<String, String>() : info;
    }

    private Map<String, String> selectRespInfo(String rsltNo) {
        if (blank(rsltNo)) {
            return new HashMap<>();
        }
        Map<String, String> param = new HashMap<>();
        param.put("rsltNo", rsltNo);
        Map<String, String> info = qm01Mapper.selectQtyReqRespInfo(param);
        return info == null ? new HashMap<String, String>() : info;
    }

    private boolean isSameTimeResult(Map<String, String> paramMap) {
        String reqNo = resolveReqNo(paramMap);
        if (blank(reqNo)) {
            return false;
        }
        Map<String, String> param = new HashMap<>();
        param.put("reqNo", reqNo);
        Map<String, String> status = qm01Mapper.selectReqStCurrentStatus(param);
        return status != null && "Y".equals(status.get("sameTimeResult"));
    }

    /** 결과 화면(P03)에서 reqNo 자리에 RES 번호가 들어올 수 있으므로(updateQualityResp) RES/REQ 어느 쪽이든 요청번호로 정규화한다. */
    private String resolveReqNo(Map<String, String> paramMap) {
        String reqNo = nz(paramMap.get("reqNo"));
        if (reqNo.startsWith("REQ")) {
            return reqNo;
        }
        String rsltNo = nz(paramMap.get("rsltNo"));
        if (rsltNo.startsWith("RES")) {
            return "REQ" + rsltNo.substring(3);
        }
        if (reqNo.startsWith("RES")) {
            return "REQ" + reqNo.substring(3);
        }
        return reqNo;
    }

    private String resolveRsltNo(Map<String, String> paramMap) {
        String rsltNo = nz(paramMap.get("rsltNo"));
        if (!rsltNo.isEmpty()) {
            return rsltNo;
        }
        return rsltNoOf(resolveReqNo(paramMap));
    }

    /** 결과번호 규칙: "RES" + reqNo.substring(3,10) (QM01SvcImpl 결과 생성부와 동일) */
    private String rsltNoOf(String reqNo) {
        if (reqNo.startsWith("REQ") && reqNo.length() >= 10) {
            return "RES" + reqNo.substring(3, 10);
        }
        return "";
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String blankToNull(String value) {
        return blank(value) ? null : value;
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }

    private static String escapeHtml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
