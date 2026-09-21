package com.dksys.biz.user.am.am12;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.config.RequestUtils;
import com.dksys.biz.user.am.am11.service.AM11Svc;

@Controller
@RequestMapping("/user/am/am12")
public class AM12Ctr {

    @Autowired
    private AM11Svc am11Svc;

    // 결재문서 상세 및 결재선/이력 조회
    @PostMapping(value = "/selectApprovalDocDetail")
    public String selectApprovalDocDetail(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.selectApprovalDocDetail(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docInfo", result.get("docInfo"));
        model.addAttribute("lineList", result.get("lineList"));
        model.addAttribute("histList", result.get("histList"));
        return "jsonView";
    }

    // 승인 처리
    @PostMapping(value = "/approveDocument")
    public String approveDocument(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.approveDocument(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        model.addAttribute("nextStatus", result.get("nextStatus"));
        return "jsonView";
    }

    // 반려 처리
    @PostMapping(value = "/rejectDocument")
    public String rejectDocument(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.rejectDocument(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        return "jsonView";
    }

    // 상신 취소(회수) 처리
    @PostMapping(value = "/cancelApproval")
    public String cancelApproval(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.cancelApproval(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        return "jsonView";
    }

    @PostMapping(value = "/cancelArbitDocument")
    public String cancelArbitDocument(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.cancelArbitDocument(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        return "jsonView";
    }

    // 전결 처리
    @PostMapping(value = "/arbitDocument")
    public String arbitDocument(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.arbitDocument(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        return "jsonView";
    }

    // 문서 열람(읽음) 처리
    @PostMapping(value = "/recordDocumentRead")
    public String recordDocumentRead(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.recordDocumentRead(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        return "jsonView";
    }

    // 문서 열람 이력 목록 조회 (서버 권한 검증 필수)
    @PostMapping(value = "/selectDocReadList")
    public String selectDocReadList(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        try {
            model.addAttribute("resultCode", "200");
            model.addAttribute("readList", am11Svc.selectDocReadList(paramMap));
        } catch (Exception e) {
            model.addAttribute("resultCode", "403");
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    // 결재선 동적 변경 (잔여 순번 대상)
    @PostMapping(value = "/changeApprovalLines")
    public String changeApprovalLines(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.changeApprovalLines(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        return "jsonView";
    }

    // ERP 연계 업무(PM51, PM07, PM08 등)의 비즈니스키(todoNo/erpBizKey/todoKey)로 AM 전자결재 docId 조회
    @PostMapping(value = "/selectDocIdByBizKey")
    public String selectDocIdByBizKey(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        String docId = am11Svc.selectDocIdByBizKey(paramMap);
        model.addAttribute("resultCode", "200");
        model.addAttribute("docId", docId != null ? docId : "");
        return "jsonView";
    }

    private void enrichRequestInfo(Map<String, Object> paramMap, HttpServletRequest request) {
        // 서버 사이드 인증 사용자 식별 (클라이언트 userId/authCd 위조 원천 방지)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String loginUserId = auth.getName();
            if (loginUserId != null && !loginUserId.trim().isEmpty()) {
                paramMap.put("userId", loginUserId);
            }
        }

        // 클라이언트 전송 권한 플래그 강제 무효화 (DB selectUserAuthInfo 기반 서버 직접 검증)
        paramMap.remove("authCd");
        paramMap.remove("isAdmin");
        paramMap.remove("isSystemAdmin");

        paramMap.put("clientIp", RequestUtils.getClientIp(request));
        paramMap.put("userAgent", RequestUtils.getUserAgent());
        paramMap.put("pgmId", "AM1201M01");
    }
}
