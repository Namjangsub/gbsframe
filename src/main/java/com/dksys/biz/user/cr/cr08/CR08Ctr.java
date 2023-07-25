package com.dksys.biz.user.cr.cr08;

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

import com.dksys.biz.user.cr.cr08.service.CR08Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr08")
public class CR08Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CR08Svc cr08svc;

    //리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr08svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr08svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//팝업 입력대상 검색
	@PostMapping(value = "/select_insert_target_modal")
	public String select_stock_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr08svc.select_insert_target_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_cr08_Info")
	public String select_cr08_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr08svc.select_cr08_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//상세정보 조회
	@PostMapping(value = "/select_cr08_Info_Dtl")
	public String select_cr08_Info_Dtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr08svc.select_cr08_Info_Dtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//INSERT
	@PostMapping(value = "/insert_cr08")
	public String insert_cr08(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr08svc.insert_cr08(paramMap, mRequest) != 0 ) {
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
	
	//UPDATE
	@PostMapping(value = "/update_cr08")
	public String update_cr08(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr08svc.update_cr08(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_cr08")
	public String delete_cr08(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (cr08svc.delete_cr08(paramMap) != 0 ) {
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