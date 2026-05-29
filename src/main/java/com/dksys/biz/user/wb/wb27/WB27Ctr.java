package com.dksys.biz.user.wb.wb27;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.wb.wb27.service.WB27Svc;

@Controller
@RequestMapping("/user/wb/wb27")
public class WB27Ctr {

    private Logger logger = LoggerFactory.getLogger(WB27Ctr.class);

    @Autowired
    WB27Svc wb27Svc;

    /* WBS 계획/실적 공수 파이 차트 */
    @PostMapping(value = "/selectResourceKpi")
    public String selectResourceKpi(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = wb27Svc.selectResourceKpi(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    /* 게이지 4개 (진행현황/일정준수/공수일치/리스크) */
    @PostMapping(value = "/selectGaugeKpi")
    public String selectGaugeKpi(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, Object> resultMap = wb27Svc.selectGaugeKpi(paramMap);
        model.addAttribute("resultMap", resultMap);
        return "jsonView";
    }

    /* 이슈 스택 바 차트 (중요도별) */
    @PostMapping(value = "/selectIssueKpi")
    public String selectIssueKpi(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = wb27Svc.selectIssueKpi(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    /* 하단 브레이크다운 바 차트 4개 */
    @PostMapping(value = "/selectBreakdownKpi")
    public String selectBreakdownKpi(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = wb27Svc.selectBreakdownKpi(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    /* 제품그룹 동적 조회 (제품그룹을 제외한 타 검색조건 필터링 결과 기준) */
    @PostMapping(value = "/selectPrdtGrpList")
    public String selectPrdtGrpList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = wb27Svc.selectPrdtGrpList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

}
