package com.dksys.biz.user.cr.cr12;

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

import com.dksys.biz.user.cr.cr12.service.CR12Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr12")
public class CR12Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR12Svc cr12svc;

    //리스트 조회
	@PostMapping(value = "/select_cr12_List")
	public String select_cr12_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr12svc.select_cr12_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr12svc.select_cr12_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}