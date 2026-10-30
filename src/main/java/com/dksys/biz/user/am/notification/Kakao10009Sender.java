package com.dksys.biz.user.am.notification;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dksys.biz.user.am.util.ApprovalSecurityUtil;
import com.dksys.biz.user.bm.bm18.service.BM18Svc;
import com.google.gson.Gson;

/**
 * 템플릿 10009 알림톡 발송기. 결재 알림의 직접 발송(리스너)과 큐 재시도(실행기)는 모두 이 클래스의 send 만 호출한다.
 * messageId 채번, 본문 줄바꿈 정규화, 버튼, HTTP 호출, 응답 판정을 여기서 한 번만 구현한다. 예외는 던지지 않고 Result 로 돌려준다.
 */
@Component
public class Kakao10009Sender {

    private static final int STATUS_MAX = 30;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final Logger logger = LoggerFactory.getLogger(Kakao10009Sender.class);

    @Value("${GBS_TALK_API_URL:}") private String talkApiUrl;
    @Value("${GBS_TALK_AUTH_TOKEN:}") private String talkAuthToken;
    @Value("${GBS_TALK_SERVER_NAME:}") private String talkServerName;
    @Value("${GBS_TALK_PAYMENT_TYPE:}") private String talkPaymentType;
    @Value("${GBS_TALK_SERVICE:}") private String talkService;

    @Autowired(required = false)
    private BM18Svc bm18Svc;

    @Autowired
    private Kakao10009MessageBuilder messageBuilder;

    /** 알림 1건 발송. 어떤 경우에도 예외를 던지지 않고 Result 로 돌려준다. title/message 는 호출자가 만든 최종 값이다(변수 값 마스킹은 builder 에서 이미 끝난 상태). */
    public Result send(String mobile, String title, String message, String todoNo, String todoDiv2CodeId) {
        try {
            return doSend(mobile, title, message, todoNo, todoDiv2CodeId);
        } catch (Exception e) {
            // 업체 호출 이전 단계(요청 본문 생성 등)의 예외: 업체를 호출하지 않았으므로 messageId 없이 단일 실패로 종결한다(재시도 없음).
            String detail = ApprovalSecurityUtil.maskSensitiveData("발송 준비 실패: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            logger.error("[Kakao10009] 발송 준비 실패(업체 미호출, 재시도 없음): todoNo={}, detail={}", todoNo, detail, e);
            return Result.notSent(detail);
        }
    }

    private Result doSend(String mobile, String title, String message, String todoNo, String todoDiv2CodeId) {
        if (isBlank(mobile)) {
            return Result.notSent("수신 휴대전화번호 없음");
        }
        if (isBlank(talkApiUrl) || isBlank(talkAuthToken) || isBlank(talkServerName)
                || isBlank(talkPaymentType) || isBlank(talkService)) {
            return Result.notSent("알림톡 접속 설정(GBS_TALK_*) 누락");
        }
        String messageId = nextMessageId();
        Map<String, Object> talkBody = new HashMap<>();
        talkBody.put("service", talkService);
        talkBody.put("messageId", messageId);
        talkBody.put("title", title);
        // 큐 왕복·저장 과정에서 CR 이 섞여도 카카오 승인 문구(LF)와 어긋나지 않도록 본문 줄바꿈을 LF 로 통일한다.
        talkBody.put("message", normalizeLf(message));
        talkBody.put("mobile", mobile);
        talkBody.put("template", Kakao10009MessageBuilder.TEMPLATE_ID);
        talkBody.put("buttons", messageBuilder.buildButtons(todoNo, todoDiv2CodeId));
        String requestJson = new Gson().toJson(talkBody);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(talkApiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("authToken", talkAuthToken);
            conn.setRequestProperty("serverName", talkServerName);
            conn.setRequestProperty("paymentType", talkPaymentType);
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            String resp = Kakao10009MessageBuilder.readResponse(conn, code);
            return judge(messageId, code, resp, requestJson);
        } catch (Exception e) {
            // 연결·타임아웃·응답 읽기 실패: 업체 처리 여부가 불확실하므로 재시도 대상으로 둔다.
            String detail = ApprovalSecurityUtil.maskSensitiveData("발송 예외: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            logger.warn("[Kakao10009] 발송 예외(재시도 대상): messageId={}, detail={}", messageId, detail, e);
            return new Result(messageId, false, 0, "EXCEPTION", false, true, detail);
        }
    }

    /** 응답 판정. 성공은 HTTP 200 + status OK. HTTP 200 에서 OK 가 아닌 status 는 업체 거부로 보고 재시도하지 않는다. */
    private Result judge(String messageId, int code, String resp, String requestJson) {
        String parsed = Kakao10009MessageBuilder.parseStatus(resp);
        if (code == 200 && "OK".equals(parsed)) {
            return new Result(messageId, true, code, "OK", false, false, "");
        }
        String status = code != 200 ? "HTTP" + code : (parsed == null ? "NORESP" : parsed);
        boolean vendorRejected = code == 200 && parsed != null;
        String detail = "HTTP" + code + (parsed == null ? "" : ", status=" + parsed)
                + (resp == null || resp.isEmpty() ? "" : ", resp=" + Kakao10009MessageBuilder.logSafe(resp));
        logger.warn("[Kakao10009] 알림톡 발송 실패: messageId={}, status={}, detail={}", messageId, status, detail);
        // 템플릿 불일치(예: KKO_3016) 원인 비교용: 거부된 요청 본문 전체(버튼 포함)를 마스킹 후 남긴다.
        logger.warn("[Kakao10009] 알림톡 거부 요청 본문: messageId={}, request={}", messageId,
                ApprovalSecurityUtil.maskSensitiveData(requestJson));
        return new Result(messageId, false, code, cap(status), vendorRejected, !vendorRejected, detail);
    }

    private String nextMessageId() {
        if (bm18Svc != null) {
            try {
                String seq = bm18Svc.selectKakaoSeqNext(new HashMap<String, String>());
                if (!isBlank(seq)) {
                    return seq.trim();
                }
            } catch (Exception e) {
                logger.warn("[Kakao10009] messageId 채번 실패: err={}", e.getMessage());
            }
        }
        String fallback = "AM_" + System.currentTimeMillis();
        logger.warn("[Kakao10009] messageId 채번 결과 없음, 대체 값 사용: messageId={}", fallback);
        return fallback;
    }

    private static String normalizeLf(String text) {
        return text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static String cap(String text) {
        return text.length() > STATUS_MAX ? text.substring(0, STATUS_MAX) : text;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** 발송 결과. messageId 가 null 이면 업체를 호출하지 않은 것(설정·수신번호 누락)이다. */
    public static final class Result {
        private final String messageId;
        private final boolean ok;
        private final int httpCode;
        private final String status;
        private final boolean vendorRejected;
        private final boolean retryable;
        private final String detail;

        Result(String messageId, boolean ok, int httpCode, String status,
                boolean vendorRejected, boolean retryable, String detail) {
            this.messageId = messageId;
            this.ok = ok;
            this.httpCode = httpCode;
            this.status = status;
            this.vendorRejected = vendorRejected;
            this.retryable = retryable;
            this.detail = detail;
        }

        static Result notSent(String detail) {
            return new Result(null, false, 0, "NOT_SENT", false, false, detail);
        }

        public String getMessageId() {
            return messageId;
        }

        public boolean isOk() {
            return ok;
        }

        public int getHttpCode() {
            return httpCode;
        }

        public String getStatus() {
            return status;
        }

        public boolean isVendorRejected() {
            return vendorRejected;
        }

        /** 일시 오류(HTTP 비200·예외)만 true. 업체 거부·설정 누락은 false. */
        public boolean isRetryable() {
            return retryable;
        }

        public String getDetail() {
            return detail;
        }
    }
}
