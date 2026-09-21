package com.dksys.biz.user.am.am02;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.am.am02.service.AM02Svc;

@Controller
@RequestMapping("/user/am/am02")
public class AM02Ctr {

    @Autowired
    private AM02Svc am02Svc;

    // 결재 양식 목록 조회
    @PostMapping(value = "/selectFormList")
    public String selectFormList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am02Svc.selectFormCount(paramMap);
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            stringMap.put(entry.getKey(), entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
        }
        PaginationInfo paginationInfo = new PaginationInfo(stringMap, totalCount);
        model.addAttribute("paginationInfo", paginationInfo);

        List<Map<String, Object>> resultList = am02Svc.selectFormList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 결재 양식 상세 및 템플릿 조회
    @PostMapping(value = "/selectFormDetail")
    public String selectFormDetail(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        Map<String, Object> result = am02Svc.selectFormDetail(paramMap);
        model.addAttribute("formInfo", result);
        return "jsonView";
    }

    // 결재 양식 저장 (신규/수정/버전개정)
    @PostMapping(value = "/saveForm")
    public String saveForm(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        paramMap.put("pgmId", "AM0201M01");
        Map<String, Object> result = am02Svc.saveForm(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("formCd", result.get("formCd"));
        return "jsonView";
    }
}
