package com.dksys.biz.user.cr.cr18;

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

import com.dksys.biz.user.cr.cr18.service.CR18Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr18")
public class CR18Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR18Svc cr18svc;

    //리스트 조회
	@PostMapping(value = "/select_cr18_List")
	public String select_cr18_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr18svc.select_cr18_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr18svc.select_cr18_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
}