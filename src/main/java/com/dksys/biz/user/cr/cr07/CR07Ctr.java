package com.dksys.biz.user.cr.cr07;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr07.service.CR07Svc;
import com.dksys.biz.util.MessageUtils;
import com.fasterxml.jackson.annotation.JacksonInject.Value;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/cr/cr07")
public class CR07Ctr {
	
	private Logger logger = LoggerFactory.getLogger(CR07Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	CR07Svc cr07Svc;
	
	// 수주마스터(확정 조회)
	@PostMapping(value = "/selectOrdrsDcsnList")
	public String selectOrdrsDcsnList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr07Svc.selectOrdrsDcsnCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = cr07Svc.selectOrdrsDcsnList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
		
	}
	
	// 매출확정리스트 조회
	@PostMapping(value = "/selectSellDcsnList")
	public String selectSellDcsnList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr07Svc.selectSellDcsnCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = cr07Svc.selectSellDcsnList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
		
	}
	
	// 매출확정등록 조회
	@PostMapping(value = "/addSellDscnList")
	public String addSellDscn(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr07Svc.addSellDscnCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = cr07Svc.addSellDscnList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 매출확정등록 입력
	@PostMapping(value = "/insertSellDscn")
	public String insertSellDscn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			cr07Svc.insertSellDscn(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}
	
	// 매출확정등록 수정
	
}
