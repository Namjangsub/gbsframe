package com.dksys.biz.user.dw.dw01.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DwgNameParser {



    // 1) “- 복사본(- (숫자))”가 여러 번 붙는 꼬리 처리 (반복 포함)
    private static final Pattern COPY_SUFFIX_MULTI =
        Pattern.compile("(?:\\s*-\\s*복사본(?:\\s*\\(\\d+\\))?)+$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);


    // 2) 끝의 괄호 주석(언어 무관) 제거용, 예: (접수시 …), (copy), (final) 등
    private static final Pattern TRAILING_PARENS =
        Pattern.compile("\\s*\\([^)]*\\)\\s*$", Pattern.UNICODE_CASE);

    // 확장자 제거 전 baseName 끝의 " - 복사본" 패턴 (선택)
//    private static final Pattern COPY_SUFFIX =
//        Pattern.compile("\\s*-\\s*복사본(?:\\s*\\(\\d+\\))?$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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

     // 느슨한 파싱을 위한 보조 패턴들
     private static final Pattern SALES_ONLY =
         Pattern.compile("^(?<salesCd>\\d{5}-\\d{2}[A-Za-z0-9]{4,12})", Pattern.CASE_INSENSITIVE);
     private static final Pattern PART_NO_AFTER =
         Pattern.compile("^(?:.*?)-(?<partNo>\\d{3,5})(?:[-_ ]+(?<tail>.*))?$", Pattern.CASE_INSENSITIVE);


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

        // 2) 끝의 “- 복사본 (n)” 들을 **전부** 제거
        base = COPY_SUFFIX_MULTI.matcher(base).replaceFirst("");

        // 3) 끝의 괄호 주석을 **여러 번** 제거 (…)(…)(…) 형태 모두 처리)
        String trimmed = base;
        while (true) {
            Matcher paren = TRAILING_PARENS.matcher(trimmed);
            if (paren.find()) {
                trimmed = trimmed.substring(0, paren.start()).trim();
            } else break;
        }
        base = trimmed.trim();

        // 4) 기존 HEAD로 본문 매칭
        Matcher m = HEAD_BASE.matcher(base);
        if (!m.matches()) return Optional.empty();
        if (m.matches()) {
            String salesCd = m.group("salesCd");
            String partNo  = m.group("partNo");
            String tail    = m.group("tail");

            String unitNo = tail;
            String revNo  = null;
            
            // 1) "-" 있는 경우 우선 처리
            String[] tokens = tail.split("-");
            if (tokens.length > 1) {
                String last = tokens[tokens.length - 1];
                if (last != null && last.toUpperCase().startsWith("R")) {
                    revNo = last.toUpperCase();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < tokens.length - 1; i++) {
                        if (i > 0) sb.append("-");
                        sb.append(tokens[i]);
                    }
                    unitNo = sb.toString();
                }
            } else {
                // 2) "-" 없는 경우, 끝부분에 R로 시작하는 Rev 패턴이 붙은 경우
                Matcher revMatcher = Pattern.compile("^(.*?)(R[0-9]{1,3})$", Pattern.CASE_INSENSITIVE)
                                            .matcher(tail);
                if (revMatcher.matches()) {
                    unitNo = revMatcher.group(1);   // 앞부분
                    revNo  = revMatcher.group(2).toUpperCase();  // 뒷부분 (예: R22)
                }
            }
            return Optional.of(new Result(salesCd, partNo, unitNo, revNo));
        }
        
        

        // 5) 느슨한 파싱 (fallback)
        // 5-1) 맨 앞에서 salesCd 먼저 뽑기
        Matcher so = SALES_ONLY.matcher(base);
        if (so.find()) {
            String salesCd = so.group("salesCd");

            // salesCd 다음의 나머지 문자열
            String rest = base.substring(so.end()).trim();

            // 보통은 바로 뒤에 "-{partNo}-..." 형태가 온다고 가정하고 추출
            Matcher pm = PART_NO_AFTER.matcher(base);
            String partNo = null, unitNo = null, revNo = null;

            if (pm.matches()) {
                partNo = pm.group("partNo");             // 1100, 1300 …
                String tail = Optional.ofNullable(pm.group("tail")).orElse("").trim();

                if (!tail.isEmpty()) {
                    // 기존 유닛/리비전 분해 로직 재사용
                    Matcher td = TAIL_WITH_DASH_REV.matcher(tail);
                    if (td.matches()) {
                        unitNo = td.group("unit");
                        revNo  = td.group("rev").toUpperCase();
                    } else {
                        Matcher tc = TAIL_CONCAT_REV.matcher(tail);
                        if (tc.matches()) {
                            unitNo = tc.group("unit");
                            revNo  = tc.group("rev").toUpperCase();
                        } else {
                            // 그래도 안되면 전체를 unit으로
                            unitNo = tail;
                        }
                    }
                }
            }

            // 최소한 salesCd는 확보 — part/unit/rev는 null일 수 있음
            return Optional.of(new Result(salesCd, partNo, unitNo, revNo));
        }

        // 6) 최후: “앞에서 13(또는 14)자” 자르기 옵션이 필요하면 여기서 처리
        //    보통 예: 23127-01NVFDT → 14자. 위험하니 패턴 없을 때만 사용.
        if (base.length() >= 14 && base.substring(0, 14).matches("\\d{5}-\\d{2}[A-Za-z0-9]{4,}")) {
            String salesCd = base.substring(0, 14);
            return Optional.of(new Result(salesCd, null, null, null));
        }

        return Optional.empty();
    }

    private static String upper(String s) { return s == null ? null : s.toUpperCase(); }
}
