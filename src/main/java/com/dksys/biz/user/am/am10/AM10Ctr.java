package com.dksys.biz.user.am.am10;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.am.am10.service.AM10Svc;

@Controller
@RequestMapping("/user/am/am10")
public class AM10Ctr {

    @Autowired
    private AM10Svc am10Svc;

    // 대시보드 요약 카운트 조회
    @PostMapping(value = "/selectApprovalDashboardCount")
    public String selectApprovalDashboardCount(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        Map<String, Object> result = am10Svc.selectApprovalDashboardCount(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    // 대시보드 최근 결재대기 목록 조회 (최대 5건)
    @PostMapping(value = "/selectDashboardWaitList")
    public String selectDashboardWaitList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = am10Svc.selectDashboardWaitList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
}
