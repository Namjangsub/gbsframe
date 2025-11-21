package com.dksys.biz.user.cr.cr20;

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

import com.dksys.biz.user.cr.cr20.service.CR20Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr20")
public class CR20Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR20Svc cr20svc;


	//리스트 조회
	@PostMapping(value = "/select_cr20m01_List")
	public String select_cr20m01_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr20svc.select_cr20m01_List_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr20svc.select_cr20m01_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

}