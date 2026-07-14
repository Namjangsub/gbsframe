package com.dksys.biz.user.pm.pm51;

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
import com.dksys.biz.user.pm.pm51.service.PM51Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm51")
public class PM51Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM51Svc pm51Svc;

	@PostMapping("/selectTripReqList")
	public String selectTripReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm51Svc.selectTripReqListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm51Svc.selectTripReqList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/selectTripReqDtl")
	public String selectTripReqDtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> result = pm51Svc.selectTripReqDtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/insertTripReq")
	public String insertTripReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm51Svc.insertTripReq(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
				model.addAttribute("tripReqNo", paramMap.get("tripReqNo"));
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

	@PostMapping("/updateTripReq")
	public String updateTripReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm51Svc.updateTripReq(paramMap, mRequest) != 0) {
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

	@PutMapping("/deleteTripReq")
	public String deleteTripReq(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm51Svc.deleteTripReq(paramMap) >= 0) {
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

	@PutMapping("/updateTripReqPayDone")
	public String updateTripReqPayDone(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm51Svc.updateTripReqPayDone(paramMap) != 0) {
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

	@PutMapping("/updateTripReqPayCancel")
	public String updateTripReqPayCancel(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm51Svc.updateTripReqPayCancel(paramMap) != 0) {
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

	@PostMapping("/selectSignResUserlstInit")
	public String selectSignResUserlstInit(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm51Svc.selectSignResUserlstInit(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/selectTripRptList")
	public String selectTripRptList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm51Svc.selectTripRptListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm51Svc.selectTripRptList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/selectTripReqForRpt")
	public String selectTripReqForRpt(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm51Svc.selectTripReqForRpt(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/selectTripRptDtl")
	public String selectTripRptDtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> result = pm51Svc.selectTripRptDtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping("/insertTripRpt")
	public String insertTripRpt(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm51Svc.insertTripRpt(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
				model.addAttribute("tripRptNo", paramMap.get("tripRptNo"));
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

	@PostMapping("/updateTripRpt")
	public String updateTripRpt(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm51Svc.updateTripRpt(paramMap, mRequest) != 0) {
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

	@PutMapping("/deleteTripRpt")
	public String deleteTripRpt(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm51Svc.deleteTripRpt(paramMap) >= 0) {
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

}
