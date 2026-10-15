package com.dksys.biz.user.am.util;

import java.util.List;
import java.util.Map;

/**
 * 결재선 라인 규칙 공용 유틸 (순수 함수, 상태 없음).
 *
 * 사양 사본 주석: 기준 원본은 AM 전자결재 AM11SvcImpl 이다.
 *  - lineType 정규화: AM11SvcImpl.lineTypeOf (L1185 부근)
 *  - 단계 구성: AM11SvcImpl.openStepAt (헤드 + 바로 뒤 연속 AGREE, AGREE 가 아니면 종료, L1246~1260 부근)
 *  - 비구속 제외(REF/COOP): AM11SvcImpl.cancelApproval 의 REF/COOP 처리 (L1041 부근)
 * AM 쪽 코드는 이 유틸로 위임하지 않았다(AM lineTypeOf 는 REFERENCE→REF 정규화를 하지 않음, openStepAt 은 DB 부작용 포함).
 * WB20SvcImpl 의 PM51 순차결재 취소/승인 검증이 이 유틸을 사용한다.
 *
 * 단계 정의
 *  - 헤드: APPR 또는 AGREE 라인. 직전 라인이 APPR/AGREE 가 아니면 새 단계가 시작된다.
 *  - 멤버: 헤드 바로 뒤에 이어지는 연속 AGREE 라인.
 *  - 경계: AGREE 가 아닌 라인(APPR, COOP, REF, POST 등)을 만나면 단계가 끝난다.
 */
public final class ApprovalLineRules {

    private ApprovalLineRules() {
    }

    /** lineType 정규화: null/공백 → 'APPR', 'REFERENCE' → 'REF', 그 외 trim */
    public static String lineType(Object raw) {
        if (raw == null) return "APPR";
        String t = String.valueOf(raw).trim();
        if (t.isEmpty()) return "APPR";
        return "REFERENCE".equals(t) ? "REF" : t;
    }

    /** 구속 라인(APPR/AGREE) 여부 */
    public static boolean isBinding(String type) {
        return "APPR".equals(type) || "AGREE".equals(type);
    }

    /** 비구속 라인(COOP/REF/POST) 여부. 순차 차단 판정에서 제외한다. */
    public static boolean isNonBinding(String type) {
        return "COOP".equals(type) || "REF".equals(type) || "POST".equals(type);
    }

    /**
     * 두 라인(순번 snA, snB)이 같은 단계(병렬 AGREE 런)에 속하는지 판정.
     * @param lines 순번(snKey) 오름차순으로 정렬된 전체 라인 목록. 각 라인의 "lineType" 을 사용한다.
     * @param snKey 순번 키 (WB20: "sanctnSn", AM: "lineSeq")
     * @param snA 비교 대상 라인 순번
     * @param snB 비교 대상 라인 순번
     * @return 두 라인이 목록에 모두 존재하고 같은 단계이면 true, 아니면 false
     */
    public static boolean isSameStep(List<? extends Map<String, ?>> lines, String snKey, int snA, int snB) {
        if (lines == null) return false;
        int idxA = -1;
        int idxB = -1;
        for (int i = 0; i < lines.size(); i++) {
            int sn = parseSn(lines.get(i).get(snKey));
            if (idxA < 0 && sn == snA) idxA = i;
            if (idxB < 0 && sn == snB) idxB = i;
        }
        if (idxA < 0 || idxB < 0) return false;
        int[] steps = stepIds(lines);
        return steps[idxA] == steps[idxB];
    }

    /** 목록 각 위치의 단계 번호. 같은 번호끼리 같은 단계이다. */
    private static int[] stepIds(List<? extends Map<String, ?>> lines) {
        int[] ids = new int[lines.size()];
        int step = -1;
        String prev = null;
        for (int i = 0; i < lines.size(); i++) {
            String t = lineType(lines.get(i).get("lineType"));
            boolean joinsPrevStep = "AGREE".equals(t) && prev != null && isBinding(prev);
            if (!joinsPrevStep) {
                step++;
            }
            ids[i] = step;
            prev = t;
        }
        return ids;
    }

    private static int parseSn(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }
}
