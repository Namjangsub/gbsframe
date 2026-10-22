package com.dksys.biz.user.wb.wb30;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.wb.wb30.service.WB30Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/wb/wb30")
public class WB30Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	WB30Svc wb30svc;

	// WBS 기준 템플릿 마스터 목록 조회
	@PostMapping(value = "/selectWbsTmplatList")
	public String selectWbsTmplatList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = wb30svc.selectWbsTmplatList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// WBS 기준 템플릿 단계 상세 목록 조회
	@PostMapping(value = "/selectWbsTmplatDetailList")
	public String selectWbsTmplatDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = wb30svc.selectWbsTmplatDetailList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// WBS 기준 템플릿 저장 (마스터 및 단계 일괄 등록/수정)
	@PostMapping(value = "/saveWbsTmplat")
	public String saveWbsTmplat(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		try {
			wb30svc.saveWbsTmplat(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// WBS 기준 템플릿 삭제
	@PostMapping(value = "/deleteWbsTmplat")
	public String deleteWbsTmplat(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		try {
			wb30svc.deleteWbsTmplat(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

}
