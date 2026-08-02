package com.dksys.biz.user.pm.pm60;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.pm.pm60.service.PM60Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm60")
public class PM60Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM60Svc pm60Svc;

	@PostMapping("/selectTeamMemberList")
	public String selectTeamMemberList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectTeamMemberList(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectRsvCalendar")
	public String selectRsvCalendar(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectRsvCalendar(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectRsvActualCalendar")
	public String selectRsvActualCalendar(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectRsvActualCalendar(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectVacationCalendar")
	public String selectVacationCalendar(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectVacationCalendar(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectRsvDtl")
	public String selectRsvDtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			Map<String, String> result = pm60Svc.selectRsvDtl(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/insertRsv")
	public String insertRsv(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.insertRsv(paramMap) > 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
				model.addAttribute("rsvNo", paramMap.get("rsvNo"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (RuntimeException re) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", re.getMessage());
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/updateRsv")
	public String updateRsv(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.updateRsv(paramMap) > 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (RuntimeException re) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", re.getMessage());
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PutMapping("/deleteRsv")
	public String deleteRsv(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.deleteRsv(paramMap) >= 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectRsvDateOverlapList")
	public String selectRsvDateOverlapList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectRsvDateOverlapList(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectVndrList")
	public String selectVndrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectVndrList(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/insertVndr")
	public String insertVndr(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.insertVndr(paramMap) > 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
				model.addAttribute("rsvNo", paramMap.get("rsvNo"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/updateVndr")
	public String updateVndr(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.updateVndr(paramMap) > 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PutMapping("/deleteVndr")
	public String deleteVndr(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm60Svc.deleteVndr(paramMap) >= 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping("/selectRsvStatDaily")
	public String selectRsvStatDaily(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> result = pm60Svc.selectRsvStatDaily(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("result", result);
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

}
