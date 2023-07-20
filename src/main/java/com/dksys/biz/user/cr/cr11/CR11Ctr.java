package com.dksys.biz.user.cr.cr11;

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

import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.user.cr.cr11.service.CR11Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr11")
public class CR11Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	CR11Svc cr11Svc;
  
  	//그리드 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr11Svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr11Svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//모달 그리드 조회
	@PostMapping(value = "/select_cr11_Info")
	public String select_cr11_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr11Svc.select_cr11_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	
	//모달창 안에 그리드 grid-modal
	@PostMapping(value = "/grid_Modal_selectList")
	public String grid_Modal_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr11Svc.grid_Modal_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	//UPDATE
	@PostMapping(value = "/update_cr11_Modal")
	public String update_cr11_Modal(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr11Svc.update_cr11_Modal(paramMap, mRequest) != 0 ) {
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
  
	
	
}