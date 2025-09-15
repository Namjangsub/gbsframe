package com.dksys.biz.user.dw.dw01.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DwgNameParser {


    // 확장자 제거 전 baseName 끝의 " - 복사본" 패턴 (선택)
    private static final Pattern COPY_SUFFIX =
        Pattern.compile("\\s*-\\s*복사본(?:\\s*\\(\\d+\\))?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // HEAD: salesCd(5숫자-2+영숫자) - partNo(숫자) - tail(영숫자/하이픈)
    // 예: 23127-01NVFDT-1300-149-2, 23127-01NVFDT-1300-149RRR1
    private static final Pattern HEAD_BASE = Pattern.compile(
        "^(?<salesCd>\\d{5}-\\d{2}[A-Za-z0-9]+)-(?<partNo>\\d+)-(?<tail>[A-Za-z0-9-]+)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );


    // rev가 하이픈으로 분리된 형태: 149- R1 / 110A- R10 / 105-6- R2 등
    private static final Pattern TAIL_WITH_DASH_REV = Pattern.compile(
        "^(?<unit>.+?)-(?<rev>(?:R\\d+|[A-Za-z]{1,4}\\d{0,3}))$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // rev가 끝에 붙어있는 형태: 149RRR1 / 110AR2 등 (앞은 주로 숫자 시작)
    private static final Pattern TAIL_CONCAT_REV = Pattern.compile(
        "^(?<unit>\\d[A-Za-z0-9-]*?)(?<rev>[A-Za-z]{1,4}\\d{1,3})$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

     // 전처리에 쓸 보조 패턴
     private static final Pattern EXT = Pattern.compile("(?i)\\.(dwg|dxf)$");

    public static final class Result {
        public final String salesCd; // 예: 23127-01NVFDT
        public final String partNo;  // 예: 1300
        public final String unitNo;  // 예: 149 / 149-2 / 110A / 105-6 ...
        public final String revNo;   // 예: R1 / R10 / RRR1 / null
        private Result(String s, String p, String u, String r) { salesCd=s; partNo=p; unitNo=u; revNo=r; }
        @Override public String toString() {
            return "Result{salesCd='" + salesCd + "', partNo='" + partNo + "', unitNo='" + unitNo + "', revNo='" + revNo + "'}";
        }
    }

    /** 경로/파일명 모두 허용, 매칭 실패 시 Optional.empty() */
    public static Optional<Result> parseFromPath(String fullPathOrName) {
        if (fullPathOrName == null || fullPathOrName.isEmpty()) return Optional.empty();

        // 파일명만 추출
        String fileName = fullPathOrName.trim();
        try {
            Path p = Paths.get(fullPathOrName);
            Path fn = p.getFileName();
            if (fn != null) fileName = fn.toString().trim();
        } catch (Exception ignore) {}


        // 1) 확장자 제거(.dwg/.dxf, 대소문자 무시)
        String base = EXT.matcher(fileName).replaceFirst("");
        

        // 2) " - 복사본" (또는 " - 복사본 (2)") 떼어내서 나중에 rev에 붙이기
        Matcher c = COPY_SUFFIX.matcher(base);
        String copySuffix = null;
        if (c.find()) {
            copySuffix = base.substring(c.start()).trim(); // "- 복사본" 그대로
            base = base.substring(0, c.start()).trim();
        }


        // 3) 기존 HEAD로 본문 매칭
        Matcher m = HEAD_BASE.matcher(base);
        if (!m.matches()) return Optional.empty();

        String salesCd = m.group("salesCd");
        String partNo  = m.group("partNo");
        String tail    = m.group("tail");

        // tail → unit / rev 분리
        String unitNo = tail;
        String revNo  = null;

        Matcher td = TAIL_WITH_DASH_REV.matcher(tail);

        if (td.matches()) {
            unitNo = td.group("unit");
            revNo  = td.group("rev").toUpperCase();
        } else {
            Matcher tc = TAIL_CONCAT_REV.matcher(tail);
            if (tc.matches()) {
                unitNo = tc.group("unit");
                revNo  = tc.group("rev").toUpperCase();
            }
        }
        
        // 4) 복사본 접미어를 rev에 반영
        if (copySuffix != null) {
            revNo = (revNo == null || revNo.isEmpty())
                    ? copySuffix
                    : (revNo + " " + copySuffix);
        }
        return Optional.of(new Result(salesCd, partNo, unitNo, revNo));
    }

    private static String upper(String s) { return s == null ? null : s.toUpperCase(); }
}
