package com.dksys.biz.user.gm.gm01;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.gm.gm01.service.GM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/gm/gm01")
public class GM01Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	GM01Svc gm01Svc;

	// 보안USB 불출/반입 목록 조회
	@PostMapping("/selectSecUsbList")
	public String selectSecUsbList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = gm01Svc.selectSecUsbCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = gm01Svc.selectSecUsbList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 보안USB 불출 등록
	@PostMapping("/insertSecUsbOut")
	public String insertSecUsbOut(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (gm01Svc.insertSecUsbOut(paramMap) != 0) {
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

	// 보안USB 불출 수정
	@PutMapping("/updateSecUsbOut")
	public String updateSecUsbOut(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (gm01Svc.updateSecUsbOut(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
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

	// 보안USB 반입 처리
	@PutMapping("/updateSecUsbIn")
	public String updateSecUsbIn(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (gm01Svc.updateSecUsbIn(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
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

	// 보안USB 반입 취소
	@PutMapping("/cancelSecUsbIn")
	public String cancelSecUsbIn(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (gm01Svc.cancelSecUsbIn(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", "반입이 취소되었습니다.");
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

	// 보안USB 불출 삭제
	@PutMapping("/deleteSecUsb")
	public String deleteSecUsb(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (gm01Svc.deleteSecUsb(paramMap) != 0) {
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
