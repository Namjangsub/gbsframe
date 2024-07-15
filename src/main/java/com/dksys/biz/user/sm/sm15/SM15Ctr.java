package com.dksys.biz.user.sm.sm15;

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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm15.service.SM15Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm15")
public class SM15Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM15Svc sm15Svc;	

	// 매입관리 입고 조회
	
	// 발주/입고 조회 NEW
	@PostMapping(value = "/selectSM15MainList")
	public String selectSM15MainList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm15Svc.selectSM15MainListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> result = sm15Svc.selectSM15MainList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}


	// 매입관리 입고 조회 NEW--Nam 거래처별 집계처리 하단그리드 세부내용
	@PostMapping(value = "/sm15selectPurchaseListNew")
	public String sm15selectPurchaseListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm15Svc.sm15selectPurchaseListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	
	// 매입관리 입고 조회 NEW--Nam 거래처별 집계처리 하단그리드 세부내용
	@PostMapping(value = "/sm15_2selectPurchaseListNew")
	public String sm15_2selectPurchaseListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm15Svc.sm15_2selectPurchaseListNewCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> result = sm15Svc.sm15_2selectPurchaseListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	

}