package com.dksys.biz.user.sm.sm18;

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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm18.service.SM18Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm18")
public class SM18Ctr {
	
	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM18Svc sm18Svc;	

	//리스트 조회
	@PostMapping(value = "/sm18_gridView_selectList")
	public String sm18_gridView_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm18Svc.sm18_gridView_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm18Svc.sm18_gridView_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//리스트 조회
	@PostMapping(value = "/sm18_gridView_selectListNew")
	public String sm18_gridView_selectListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm18Svc.sm18_gridView_selectCountNew(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm18Svc.sm18_gridView_selectListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	

}