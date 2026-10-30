package com.dksys.biz.user.am.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.bm.bm17.service.BM17Svc;
import com.google.gson.Gson;

/**
 * 카카오 알림톡 템플릿 10009('업무확인요청Ver0.2') 메시지 조립기.
 * - 본문: TB_BM17M01 의 TMPLAT_DIV = 'TMPLATDIV06' 행(없으면 확정 문구 폴백, 경고 로그)
 * - 제목: 발송 제목 = 변수 title + " 요청" (템플릿 '#{title} 요청')
 * - 버튼: 웹링크(통합시스템 바로가기) + 앱링크(건양ERP 결재하기). 값 규칙은 이 클래스의 buildButtons 한 곳에서만 만든다.
 * - 업체(LG CNS talkapi) 버튼 payload 규격은 미확인. 키 이름(url_mobile/url_pc, scheme_android/scheme_ios)은 프론트 global.js 와 같다.
 */
@Component
public class Kakao10009MessageBuilder {

    public static final String TMPLAT_DIV = "TMPLATDIV06";
    public static final String TEMPLATE_ID = "10009";

    /** 템플릿 DB 행이 없을 때 쓰는 확정 본문(카카오 승인본과 동일 문구). */
    public static final String FALLBACK_BODY = "내용 : #{cnts}\n"
            + "업무 : #{todoCodeNm}\n"
            + "요청일시 : #{sendDt}\n"
            + "요청자 : #{ordrgMngNm}\n"
            + "\n"
            + "#{sanctnDiv2} 바랍니다.\n"
            + "\n"
            + "(주)건양아이티티/051-312-2400";

    private static final int TITLE_MAX = 50;
    private static final int DOC_TITLE_MAX = 60;
    private static final int VALUE_MAX = 200;
    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([A-Za-z0-9_]+)\\}");
    private static final Pattern NO_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    private final Logger logger = LoggerFactory.getLogger(Kakao10009MessageBuilder.class);

    @Value("${gbs.mobile.base-url:https://gbs.gunyangitt.co.kr}")
    private String mobileBaseUrl;

    @Autowired(required = false)
    private BM17Svc bm17Svc;

    /** TMPLATDIV06 본문 조회. 행이 없거나 오류면 확정 문구 폴백. */
    public String loadBody(String coCd) {
        if (bm17Svc == null || coCd == null || coCd.trim().isEmpty()) {
            logger.warn("[Kakao10009] 본문 템플릿 조회 불가(coCd 없음 또는 서비스 없음): 확정 문구로 폴백");
            return FALLBACK_BODY;
        }
        try {
            Map<String, String> param = new HashMap<>();
            param.put("coCd", coCd);
            param.put("tmplatDiv", TMPLAT_DIV);
            Map<String, Object> info = bm17Svc.selectTemplInfo(param);
            Object desc = info == null ? null : info.get("mssageDesc");
            if (desc == null || String.valueOf(desc).trim().isEmpty()) {
                logger.warn("[Kakao10009] TB_BM17M01 {} 행 없음(coCd={}): 확정 문구로 폴백", TMPLAT_DIV, coCd);
                return FALLBACK_BODY;
            }
            return String.valueOf(desc);
        } catch (Exception e) {
            logger.warn("[Kakao10009] 본문 템플릿 조회 실패(coCd={}): 확정 문구로 폴백, err={}", coCd, e.getMessage());
            return FALLBACK_BODY;
        }
    }

    /** #{키} 를 vars 값으로 치환. 값이 없는 키는 빈 문자열. */
    public String render(String body, Map<String, String> vars) {
        // 템플릿 본문 줄바꿈은 LF 로 통일한다(DB 에 CRLF 로 저장돼 있어도 카카오 승인 문구의 줄바꿈과 어긋나지 않게).
        body = body.replace("\r\n", "\n").replace('\r', '\n');
        Matcher m = PLACEHOLDER.matcher(body);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String value = vars.get(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 템플릿 변수 생성. 값은 민감정보 마스킹 후 길이를 제한한다(제목 50자 기준은 ' 요청' 자리 3자를 뺀 값).
     * titleVar 는 ' 요청' 을 뺀 제목, cnts 는 본문 '내용' 값(최종 문자열).
     */
    public Map<String, String> vars(String titleVar, String cnts, String todoCodeNm, String ordrgMngNm, String sanctnDiv2) {
        Map<String, String> v = new HashMap<>();
        v.put("title", safe(titleVar, TITLE_MAX - 3));
        v.put("cnts", safe(cnts, VALUE_MAX * 2));
        v.put("todoCodeNm", safe(todoCodeNm, VALUE_MAX));
        v.put("sendDt", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        v.put("ordrgMngNm", safe(ordrgMngNm, VALUE_MAX));
        v.put("sanctnDiv2", safe(sanctnDiv2, VALUE_MAX));
        return v;
    }

    /** 발송 제목 = title 변수 + " 요청" (템플릿 '#{title} 요청' 과 동일). */
    public String talkTitle(Map<String, String> vars) {
        return vars.get("title") + " 요청";
    }

    /** 결재 문서 제목을 변수용으로 자른다(DOC_TITLE_MAX). */
    public String docTitle(String docTitle) {
        return safe(docTitle, DOC_TITLE_MAX);
    }

    /** 결재의견 등 추가 문구 값 자르기. */
    public String value(String text) {
        return safe(text, VALUE_MAX);
    }

    /** 결재구분(todoDiv2CodeId) → approvalLink.html 업무 키. 매핑 없으면 null. */
    public String goOf(String todoDiv2CodeId) {
        if (todoDiv2CodeId == null) {
            return null;
        }
        switch (todoDiv2CodeId) {
            case "TODODIV2300":
                return "PM07";
            case "TODODIV2410":
            case "TODODIV2420":
                return "PM08";
            case "TODODIV2190":
            case "TODODIV2191":
                return "PM5101";
            case "TODODIV2200":
            case "TODODIV2201":
                return "PM5102";
            default:
                return null;
        }
    }

    /**
     * 버튼 payload. 문서 매핑과 번호 검증이 통과하면 문서별 URL, 아니면 모바일 메인 + 앱 go=HOME 으로 폴백.
     * 앱 go 는 '업무&reqNo=문서번호' 이며 & 와 = 는 인코딩하지 않는다.
     */
    public List<Map<String, Object>> buildButtons(String todoNo, String todoDiv2CodeId) {
        String host = mobileBaseUrl.replaceFirst("^https?://", "");
        String go = goOf(todoDiv2CodeId);
        String webUrl;
        String appGo;
        if (go != null && todoNo != null && NO_PATTERN.matcher(todoNo).matches()) {
            webUrl = host + "/static/mobile/index.html?go=" + go + "&reqNo=" + todoNo;
            appGo = go + "&reqNo=" + todoNo;
        } else {
            webUrl = host + "/static/mobile/index.html";
            appGo = "HOME";
        }
        Map<String, Object> wl = new HashMap<>();
        wl.put("name", "통합시스템 바로가기");
        wl.put("type", "WL");
        wl.put("url_mobile", "https://" + webUrl);
        wl.put("url_pc", "https://" + webUrl);

        Map<String, Object> al = new HashMap<>();
        al.put("name", "건양ERP 결재하기");
        al.put("type", "AL");
        al.put("scheme_android", "gbserp://approval?go=" + appGo);
        al.put("scheme_ios", "gbserp://approval?go=" + appGo);

        List<Map<String, Object>> buttons = new ArrayList<>();
        buttons.add(wl);
        buttons.add(al);
        return buttons;
    }

    /** 업무명(#{todoCodeNm}). PM07 은 휴가유형명(vacTypeNm), 없으면 '휴가'. */
    public String todoCodeName(String erpBizType, String todoDiv2CodeId, String vacTypeNm) {
        if ("PM07".equals(erpBizType)) {
            return vacTypeNm == null || vacTypeNm.trim().isEmpty() ? "휴가" : vacTypeNm.trim();
        }
        if ("PM08".equals(erpBizType)) {
            return "TODODIV2420".equals(todoDiv2CodeId) ? "휴일대체근무 결과보고" : "휴일대체근무 신청서";
        }
        if ("PM51".equals(erpBizType)) {
            return "출장신청서";
        }
        if ("PM52".equals(erpBizType)) {
            return "출장복명서";
        }
        if ("CR02".equals(erpBizType)) {
            return "수주목표원가";
        }
        return "결재 문서";
    }

    /** 응답 JSON 의 status 값(성공 'OK'). 파싱 불가면 null. */
    public static String parseStatus(String body) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            Object status = new Gson().fromJson(body, Map.class).get("status");
            return status == null ? null : String.valueOf(status);
        } catch (Exception e) {
            return null;
        }
    }

    /** 응답 본문 읽기(성공/오류 스트림 공용). 로그용으로만 쓰며 마스킹 후 300자까지. */
    public static String readResponse(java.net.HttpURLConnection conn, int responseCode) {
        try (InputStream in = responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()) {
            if (in == null) {
                return "";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

    /** 로그용 응답 요약(마스킹, 300자 제한). */
    public static String logSafe(String text) {
        if (text == null) {
            return "";
        }
        String cut = text.length() > 300 ? text.substring(0, 300) : text;
        return ApprovalSecurityUtil.maskSensitiveData(cut);
    }

    private static String safe(String text, int max) {
        // 변수 값 안의 줄바꿈은 공백으로 바꾼다: 카카오 템플릿 검증은 변수 자리를 한 줄 값으로 보고 비교하므로
        // 값에 줄바꿈이 있으면(예: 결재의견 여러 줄) 템플릿 불일치로 거부될 수 있다.
        String value = text == null ? "" : ApprovalSecurityUtil.maskSensitiveData(text.replaceAll("[\\r\\n]+", " ").trim());
        return value.length() > max ? value.substring(0, max) : value;
    }
}
