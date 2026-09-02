package com.dksys.biz.user.pm.pm08;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.pm.pm08.service.PM08Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm08")
public class PM08Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM08Svc pm08Svc;

	@PostMapping(value = "/selectSubstituteWorkList")
	public String selectSubstituteWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm08Svc.selectSubstituteWorkCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm08Svc.selectSubstituteWorkList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectSubstituteWorkDtl")
	public String selectSubstituteWorkDtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> result = pm08Svc.selectSubstituteWorkDtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/insertSubstituteWork")
	public String insertSubstituteWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			Map<String, String> result = pm08Svc.insertSubstituteWork(paramMap, mRequest);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert") + " 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/updateSubstituteWork")
	public String updateSubstituteWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			Map<String, String> result = pm08Svc.updateSubstituteWork(paramMap, mRequest);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("update") + " 실패");
		}
		return "jsonView";
	}

	@PutMapping(value = "/deleteSubstituteWork")
	public String deleteSubstituteWork(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm08Svc.deleteSubstituteWork(paramMap);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete") + " 실패");
		}
		return "jsonView";
	}

	@PutMapping(value = "/deleteSubstituteWorkResult")
	public String deleteSubstituteWorkResult(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm08Svc.deleteSubstituteWorkResult(paramMap);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", "근무결과 삭제 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectSubstituteVacationStatusList")
	public String selectSubstituteVacationStatusList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm08Svc.selectSubstituteVacationStatusList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

}
