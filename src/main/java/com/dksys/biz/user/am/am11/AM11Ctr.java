package com.dksys.biz.user.am.am11;

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
@RequestMapping("/user/am/am11")
public class AM11Ctr {

    @Autowired
    private AM11Svc am11Svc;

    // 결재문서 임시저장
    @PostMapping(value = "/saveApprovalDraft")
    public String saveApprovalDraft(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.saveApprovalDraft(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        model.addAttribute("docNo", result.get("docNo"));
        return "jsonView";
    }

    // 결재문서 상신
    @PostMapping(value = "/submitApproval")
    public String submitApproval(@RequestBody Map<String, Object> paramMap, HttpServletRequest request, ModelMap model) {
        enrichRequestInfo(paramMap, request);
        Map<String, Object> result = am11Svc.submitApproval(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("docId", result.get("docId"));
        model.addAttribute("docNo", result.get("docNo"));
        return "jsonView";
    }

    // 결재문서 상세 조회 (기안 작성 시 수정모드용)
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
        paramMap.put("pgmId", "AM1101M01");
    }
}
