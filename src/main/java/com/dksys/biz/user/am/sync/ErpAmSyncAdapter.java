package com.dksys.biz.user.am.sync;

import java.util.Map;

/**
 * ERP 업무별 전자결재(AM) 상신 동기화 어댑터.
 * todoDiv2CodeId 는 결재 단계(예: 신청/결과)를 나타내며, 단계별로 문서 1건이 생성된다.
 */
public interface ErpAmSyncAdapter {

    /** TB_AM11M01.ERP_BIZ_TYPE 값 */
    String erpBizType();

    /** TB_AM11M01.ERP_BIZ_KEY 값이자 WB20 TODO_NO. 공란이면 동기화하지 않는다. */
    String erpBizKey(Map<String, String> paramMap, String todoDiv2CodeId);

    /** AM 양식 코드 */
    String formCd(String todoDiv2CodeId);

    /** AM 문서 생성 화면 pgmId */
    String pgmId();

    /** 승인 라인 WB20 TODO_DIV2_CODE_ID */
    String apprDiv2(String todoDiv2CodeId);

    /** 공유(참조) 라인 WB20 TODO_DIV2_CODE_ID. 없으면 null */
    String refDiv2(String todoDiv2CodeId);

    String buildTitle(Map<String, String> paramMap, String todoDiv2CodeId);

    String buildDocDataJson(Map<String, String> paramMap, String todoDiv2CodeId);

    String buildHtml(Map<String, String> paramMap, String todoDiv2CodeId);

    /** 단계(div2) 기준으로 활성 AM 문서 DOC_ID 를 조회한다. 없으면 null */
    String selectExistingDocId(Map<String, String> paramMap, String todoDiv2CodeId);

    /** erpBizType + erpBizKey 기준 AM 문서(결재선 → 마스터 순)를 삭제한다. */
    void deleteDocsByBizKey(Map<String, String> paramMap);
}
