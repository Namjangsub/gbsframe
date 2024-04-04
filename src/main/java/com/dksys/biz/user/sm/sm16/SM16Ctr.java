package com.dksys.biz.user.sm.sm16;

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
import com.dksys.biz.user.sm.sm16.service.SM16Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm16")
public class SM16Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM16Svc sm16Svc;	

	// 매입관리 입고 조회
	
	// 발주/입고 조회 NEW
	@PostMapping(value = "/selectSM16MainList")
	public String selectSM16MainList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm16Svc.selectSM16MainList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}


	// 매입관리 입고 조회 NEW--Nam 거래처별 집계처리 하단그리드 세부내용
	@PostMapping(value = "/sm16selectPurchaseListNew")
	public String sm16selectPurchaseListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm16Svc.sm16selectPurchaseListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	

}