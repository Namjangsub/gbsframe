package com.dksys.biz.user.cr.cr02;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/user/cr/cr02")
public class CR02Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	CR02Svc cr02Svc;

	@PostMapping("/selectOrdrsList")
	public String selectOrdrsList(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, Object>> ordrsList = cr02Svc.selectOrdrsList(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}

	@PostMapping(value = "/selectOrdrsInfo")
	public String selectOrdrsInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {

		Map<String, Object> ordrsInfo = cr02Svc.selectOrdrsInfo(paramMap);
		model.addAttribute("ordrsInfo", ordrsInfo);
		return "jsonView";
	}
	// ...
}