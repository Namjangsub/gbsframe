package com.dksys.biz.user.bm.bm10;

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

import com.dksys.biz.user.bm.bm10.service.BM10Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm10")
public class BM10Ctr {
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	BM10Svc bm10Svc;
	
	//리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm10Svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = bm10Svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//INSERT
	@PostMapping(value = "/insert_bm10")
	public String insertBM10M01(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm10Svc.insert_bm10(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	
	//정보 조회
	@PostMapping(value = "/select_bm10_Info")
	public String select_bm10_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = bm10Svc.select_bm10_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// @PostMapping(value = "/selectConfirmCount")
	// public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
	// 	int result = bm10Svc.selectConfirmCount(paramMap);
	// 	model.addAttribute("result", result);
	// 	return "jsonView";
	// }
	
	//UPDATE
	@PostMapping(value = "/update_bm10")
	public String update_bm10(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm10Svc.update_bm10(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
	  	return "jsonView";
	}
	
	//DELETE
	@PutMapping(value = "/delete_bm10")
	public String delete_bm10(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (bm10Svc.delete_bm10(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
	  	return "jsonView";
	}
	
}