package com.dksys.biz.user.sm.sm19;

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

import com.dksys.biz.user.sm.sm19.service.SM19Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm19")
public class SM19Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    SM19Svc sm19svc;

    //리스트 조회
	@PostMapping(value = "/select_sm19_List")
	public String select_sm19_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm19svc.select_sm19_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm19svc.select_sm19_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 창고 조회
	@PostMapping(value = "/selectWhCd") 
	public String selectWhCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = sm19svc.selectWhCd(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 수불유형 조회
	@PostMapping(value = "/selectinoutDiv") 
	public String selectinoutDiv(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = sm19svc.selectinoutDiv(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}