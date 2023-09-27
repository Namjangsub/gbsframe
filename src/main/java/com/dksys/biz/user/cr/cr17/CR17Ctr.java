package com.dksys.biz.user.cr.cr17;

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

import com.dksys.biz.user.cr.cr17.service.CR17Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr17")
public class CR17Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR17Svc cr17svc;

    //리스트 조회
	@PostMapping(value = "/select_cr17_List")
	public String select_cr17_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr17svc.select_cr17_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr17svc.select_cr17_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// // 수금유형 조회
	// @PostMapping(value = "/selectPmntmtdCd") 
	// public String selectPmntmtdCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
	// 	List<Map<String, Object>> result = cr14svc.selectPmntmtdCd(paramMap);
	// 	model.addAttribute("result", result);
	// 	return "jsonView";
	// }
}