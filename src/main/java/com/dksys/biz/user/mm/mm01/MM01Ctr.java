package com.dksys.biz.user.mm.mm01;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.mm.mm01.service.MM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/mm/mm01")
public class MM01Ctr {

	@Autowired
	private MessageUtils messageUtils;

	@Autowired
	private MM01Svc mm01Svc;

	@PostMapping(value = "/selectMindMapList")
	public String selectMindMapList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			List<Map<String, String>> resultList = mm01Svc.selectMindMapList(paramMap);
			model.addAttribute("resultList", resultList);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("select"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}
}
