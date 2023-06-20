package com.dksys.biz.user.sm.sm01;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm01")
public class SM01Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	SM01Svc sm01Svc;
	
	// 구매BOM관리 Master 조회
	@PostMapping(value = "/selectBomSalesList")
	public String selectBomSalesList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm01Svc.selectBomSalesCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = sm01Svc.selectBomSalesList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	// BOM내역상세 조회
	@PostMapping(value = "/selectBomDetailList")
	public String selectBomDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm01Svc.selectBomDetailCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = sm01Svc.selectBomDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
		
	}
	
	// 매출확정등록 조회
	@PostMapping(value = "/addSellDscnList")
	public String addSellDscn(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm01Svc.addSellDscnCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = sm01Svc.addSellDscnList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 매출확정등록 입력
	@PostMapping(value = "/insertSellDscn")
	public String insertSellDscn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			sm01Svc.insertSellDscn(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}
	
}
