package com.dksys.biz.user.cr.cr09;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.cr.cr09.service.CR09Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr09")
public class CR09Ctr {

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private CR09Svc cr09Svc;

    /**
     * 물류진행 예정정보 현황 목록 조회
     * @param paramMap 조회 조건
     * @param model 
     * @return
     */
    @PostMapping(value = "/selectLgistPlanList")
    public String selectLgistPlanList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = cr09Svc.selectLgistPlanList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    /**
     * 물류완료 처리
     * @param paramList 처리대상 리스트
     * @param model
     * @return
     */
    @PostMapping(value = "/updateLgistCompl")
    public String updateLgistCompl(@RequestBody List<Map<String, Object>> paramList, ModelMap model) {
        try {
            cr09Svc.updateLgistCompl(paramList);
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("update"));
        } catch (Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    /**
     * 물류완료 취소 처리
     * @param paramList 처리대상 리스트
     * @param model
     * @return
     */
    @PostMapping(value = "/cancelLgistCompl")
    public String cancelLgistCompl(@RequestBody List<Map<String, Object>> paramList, ModelMap model) {
        try {
            cr09Svc.cancelLgistCompl(paramList);
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("update"));
        } catch (Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }
}
