package com.dksys.biz.user.fi.fi30;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.fi.fi30.service.FI30Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/fi/fi30")
public class FI30Ctr {

	@Autowired
    MessageUtils messageUtils;

    @Autowired
    FI30Svc fi30svc;
	
	 //리스트 조회
	@PostMapping(value = "/select_fi30_List")
	public String select_fi30_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = fi30svc.select_fi30_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = fi30svc.select_fi30_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}
