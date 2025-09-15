package com.dksys.biz.user.dw.dw02;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.dw.dw02.service.DW02Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/dw/dw02/")
public class DW02Ctr {

	private Logger logger = LoggerFactory.getLogger(DW02Ctr.class);

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	DW02Svc dw02Svc;
	
	@PostMapping(value = "/drawingAuditsList")
	public String selectWbsSjList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw02Svc.searchAuditsCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, String>> resultList = dw02Svc.searchAuditsList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	

}