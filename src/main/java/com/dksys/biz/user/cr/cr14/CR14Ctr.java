package com.dksys.biz.user.cr.cr14;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.cr.cr14.service.CR14Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr14")
public class CR14Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR14Svc cr14svc;

    //리스트 조회
	@PostMapping(value = "/select_cr14_List")
	public String select_cr14_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_cr14_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_cr14_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//POPUP 리스트 조회
	@PostMapping(value = "/select_modal_List1")
	public String select_modal_List1(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List1_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List1(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/select_modal_List2")
	public String select_modal_List2(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List2_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List2(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/select_modal_List3")
	public String select_modal_List3(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List3_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List3(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/select_modal_List4")
	public String select_modal_List4(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List4_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List4(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/select_modal_List5")
	public String select_modal_List5(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List5_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List5(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/select_modal_List6")
	public String select_modal_List6(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr14svc.select_modal_List6_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr14svc.select_modal_List6(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 수주버전 조회
	@PostMapping(value = "/select_ordrsHistNo_List") 
	public String select_ordrsHistNo_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = cr14svc.select_ordrsHistNo_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}