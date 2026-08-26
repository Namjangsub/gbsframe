package com.dksys.biz.user.pm.pm07;

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
import com.dksys.biz.user.pm.pm07.service.PM07Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm07")
public class PM07Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM07Svc pm07Svc;

	@PostMapping(value = "/selectVacationList")
	public String selectVacationList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm07Svc.selectVacationCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm07Svc.selectVacationList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectVacationCalendarList")
	public String selectVacationCalendarList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm07Svc.selectVacationCalendarList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectVacationDtl")
	public String selectVacationDtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = pm07Svc.selectVacationDtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/insertVacation")
	public String insertVacation(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			Map<String, String> result = pm07Svc.insertVacation(paramMap, mRequest);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert") + " 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/updateVacation")
	public String updateVacation(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			Map<String, String> result = pm07Svc.updateVacation(paramMap, mRequest);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("update") + " 실패");
		}
		return "jsonView";
	}

	@PutMapping(value = "/deleteVacation")
	public String deleteVacation(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm07Svc.deleteVacation(paramMap);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete") + " 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAnnualBalance")
	public String selectAnnualBalance(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = pm07Svc.selectAnnualBalance(paramMap);
		model.addAttribute("result", result);
		model.addAttribute("resultCode", result.get("resultCode"));
		return "jsonView";
	}

	@PostMapping(value = "/saveAnnualGrant")
	public String saveAnnualGrant(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm07Svc.saveAnnualGrant(paramMap);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("update") + " 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAnnualGrantList")
	public String selectAnnualGrantList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = pm07Svc.selectAnnualGrantList(paramMap);
		model.addAttribute("resultList", resultList);
		model.addAttribute("resultCode", 200);
		return "jsonView";
	}

	@PostMapping(value = "/saveAnnualGrantList")
	public String saveAnnualGrantList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm07Svc.saveAnnualGrantList(paramMap);
			model.addAttribute("result", result);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", "저장 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAutoCalcAnnualGrantList")
	public String selectAutoCalcAnnualGrantList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = pm07Svc.selectAutoCalcAnnualGrantList(paramMap);
		model.addAttribute("resultList", resultList);
		model.addAttribute("resultCode", 200);
		return "jsonView";
	}

	@PostMapping(value = "/selectVacationOverlapCheck")
	public String selectVacationOverlapCheck(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = pm07Svc.selectVacationOverlapCheck(paramMap);
		model.addAttribute("resultList", resultList);
		model.addAttribute("resultCode", 200);
		return "jsonView";
	}

	@PostMapping(value = "/selectAnnualUseStatusList")
	public String selectAnnualUseStatusList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm07Svc.selectAnnualUseStatusList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/updateMngRmk")
	public String updateMngRmk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			int count = pm07Svc.updateMngRmk(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", "근태담당자 사유가 저장되었습니다.");
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", "저장 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectMobileVacationFileList")
	public String selectMobileVacationFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = pm07Svc.selectMobileVacationFileList(paramMap);
		model.addAttribute("fileList", fileList);
		model.addAttribute("resultCode", 200);
		return "jsonView";
	}

}
