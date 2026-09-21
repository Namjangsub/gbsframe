package com.dksys.biz.user.am.am03;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.am.am03.service.AM03Svc;

@Controller
@RequestMapping("/user/am/am03")
public class AM03Ctr {

    @Autowired
    private AM03Svc am03Svc;

    // 개인 결재선 프리셋 목록 조회
    @PostMapping(value = "/selectLinePresetList")
    public String selectLinePresetList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = am03Svc.selectLinePresetList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 개인 결재선 상세 조회
    @PostMapping(value = "/selectLinePresetDetail")
    public String selectLinePresetDetail(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = am03Svc.selectLinePresetDetail(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 개인 결재선 저장
    @PostMapping(value = "/saveLinePreset")
    public String saveLinePreset(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        paramMap.put("pgmId", "AM0301M01");
        Map<String, Object> result = am03Svc.saveLinePreset(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        model.addAttribute("linePresetId", result.get("linePresetId"));
        return "jsonView";
    }

    // 개인 결재선 삭제
    @PostMapping(value = "/deleteLinePreset")
    public String deleteLinePreset(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        Map<String, Object> result = am03Svc.deleteLinePreset(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        return "jsonView";
    }

    // 대결(위임) 설정 목록 조회
    @PostMapping(value = "/selectDelegateList")
    public String selectDelegateList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        List<Map<String, Object>> resultList = am03Svc.selectDelegateList(paramMap);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    // 대결(위임) 설정 저장
    @PostMapping(value = "/saveDelegate")
    public String saveDelegate(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        paramMap.put("pgmId", "AM0301M01");
        Map<String, Object> result = am03Svc.saveDelegate(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        return "jsonView";
    }

    // 대결(위임) 상태 변경(해제)
    @PostMapping(value = "/updateDelegateStatus")
    public String updateDelegateStatus(@RequestBody Map<String, Object> paramMap, ModelMap model) {
        paramMap.put("pgmId", "AM0301M01");
        Map<String, Object> result = am03Svc.updateDelegateStatus(paramMap);
        model.addAttribute("resultCode", result.get("resultCode"));
        model.addAttribute("resultMessage", result.get("resultMessage"));
        return "jsonView";
    }
}
