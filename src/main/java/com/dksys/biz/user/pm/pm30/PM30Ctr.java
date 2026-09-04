package com.dksys.biz.user.pm.pm30;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.pm.pm30.service.PM30Svc;

@Controller
@RequestMapping("/user/pm/pm30")
public class PM30Ctr {

	@Autowired
	PM30Svc pm30Svc;

	@PostMapping(value = "/selectAttendanceList")
	public String selectAttendanceList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> resultList = pm30Svc.selectAttendanceList(paramMap);
			model.addAttribute("resultCode", "0000");
			model.addAttribute("resultMessage", "성공");
			model.addAttribute("resultList", resultList);
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/saveAttendanceList")
	public String saveAttendanceList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			Map<String, Object> result = pm30Svc.saveAttendanceList(paramMap);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
			model.addAttribute("resultCount", result.get("resultCount"));
			model.addAttribute("rawResultCount", result.get("rawResultCount"));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "저장 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectRelatedApplications")
	public String selectRelatedApplications(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			Map<String, Object> result = pm30Svc.selectRelatedApplications(paramMap);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
			model.addAttribute("dailyApplicationList", result.get("dailyApplicationList"));
			model.addAttribute("empSalesAreaList", result.get("empSalesAreaList"));
			model.addAttribute("savedWorkList", result.get("savedWorkList"));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAttendanceDailyMonthly")
	public String selectAttendanceDailyMonthly(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			model.addAttribute("resultCode", "0000");
			model.addAttribute("resultMessage", "성공");
			model.addAttribute("resultList", pm30Svc.selectAttendanceDailyMonthly(paramMap));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAttendanceEmployeeMonthly")
	public String selectAttendanceEmployeeMonthly(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			model.addAttribute("resultCode", "0000");
			model.addAttribute("resultMessage", "성공");
			model.addAttribute("resultList", pm30Svc.selectAttendanceEmployeeMonthly(paramMap));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectHourlyWorkerMonthly")
	public String selectHourlyWorkerMonthly(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			model.addAttribute("resultCode", "0000");
			model.addAttribute("resultMessage", "성공");
			model.addAttribute("resultList", pm30Svc.selectHourlyWorkerMonthly(paramMap));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패");
		}
		return "jsonView";
	}

	@PostMapping(value = "/selectAttendanceChangeList")
	public String selectAttendanceChangeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			model.addAttribute("resultCode", "0000");
			model.addAttribute("resultMessage", "성공");
			model.addAttribute("resultList", pm30Svc.selectAttendanceChangeList(paramMap));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "조회 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/saveAttendanceChange")
	public String saveAttendanceChange(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			Map<String, Object> result = pm30Svc.saveAttendanceChange(paramMap);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
			model.addAttribute("resultCount", result.get("resultCount"));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "저장 실패: " + e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/deleteAttendanceList")
	public String deleteAttendanceList(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			Map<String, Object> result = pm30Svc.deleteAttendanceList(paramMap);
			model.addAttribute("resultCode", result.get("resultCode"));
			model.addAttribute("resultMessage", result.get("resultMessage"));
			model.addAttribute("resultCount", result.get("resultCount"));
		} catch (Exception e) {
			model.addAttribute("resultCode", "9999");
			model.addAttribute("resultMessage", "삭제 실패: " + e.getMessage());
		}
		return "jsonView";
	}

}
