package com.dksys.biz.user.am.am20;

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
import com.dksys.biz.user.am.am20.service.AM20Svc;

@Controller
@RequestMapping("/user/am/am20")
public class AM20Ctr {

    @Autowired
    private AM20Svc am20Svc;

    private void populatePagination(Map<String, Object> paramMap, int totalCount, ModelMap model) {
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            stringMap.put(entry.getKey(), entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
        }
        PaginationInfo paginationInfo = new PaginationInfo(stringMap, totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
    }

    // 1. 결재 대기함
    @PostMapping(value = "/selectInboxList")
    public String selectInboxList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectInboxCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectInboxList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 2. 진행 문서함
    @PostMapping(value = "/selectProgressList")
    public String selectProgressList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectProgressCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectProgressList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 3. 상신 문서함
    @PostMapping(value = "/selectDraftedList")
    public String selectDraftedList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectDraftedCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectDraftedList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 4. 완료 문서함
    @PostMapping(value = "/selectCompletedList")
    public String selectCompletedList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectCompletedCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectCompletedList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 5. 반려 문서함
    @PostMapping(value = "/selectRejectedList")
    public String selectRejectedList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectRejectedCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectRejectedList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 6. 참조 문서함
    @PostMapping(value = "/selectReferencedList")
    public String selectReferencedList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectReferencedCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectReferencedList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 7. 임시저장함
    @PostMapping(value = "/selectTempList")
    public String selectTempList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        int totalCount = am20Svc.selectTempCount(paramMap);
        populatePagination(paramMap, totalCount, model);
        List<Map<String, Object>> resultList = am20Svc.selectTempList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
}
