package com.dksys.biz.user.bm.bm15;

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

import com.dksys.biz.user.bm.bm15.service.BM15Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm15")
public class BM15Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    BM15Svc bm15svc;

    //리스트 조회
	@PostMapping(value = "/select_bm15_List")
	public String select_bm15_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm15svc.select_bm15_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = bm15svc.select_bm15_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// // 수금유형 조회
	// @PostMapping(value = "/selectPmntmtdCd") 
	// public String selectPmntmtdCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
	// 	List<Map<String, Object>> result = bm15svc.selectPmntmtdCd(paramMap);
	// 	model.addAttribute("result", result);
	// 	return "jsonView";
	// }
}