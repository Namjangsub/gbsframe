package com.dksys.biz.user.cr.cr19;

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

import com.dksys.biz.user.cr.cr19.service.CR19Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr19")
public class CR19Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR19Svc cr19svc;

    //리스트 조회
	@PostMapping(value = "/select_cr19_List")
	public String select_cr19_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr19svc.select_cr19_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr19svc.select_cr19_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 수주버전 조회
	@PostMapping(value = "/select_ordrsHistNo_List") 
	public String select_ordrsHistNo_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = cr19svc.select_ordrsHistNo_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/save_cr19")
	public String save_cr19(@RequestParam Map<String, String> paramMap,MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			cr19svc.save_cr19(paramMap,mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		} catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";
	}
}