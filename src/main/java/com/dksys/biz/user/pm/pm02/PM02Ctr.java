package com.dksys.biz.user.pm.pm02;

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

import com.dksys.biz.user.pm.pm02.service.PM02Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm02")
public class PM02Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    PM02Svc pm02svc;

    //리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm02svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm02svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 창고 코드 검색
	@PostMapping(value = "/selectWhCd")
    public String selectWhCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> result = pm02svc.selectWhCd(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

	//팝업 재고 검색
	@PostMapping(value = "/select_stock_modal")
	public String select_stock_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm02svc.select_stock_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_pm02_Info")
	public String select_pm02_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = pm02svc.select_pm02_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//상세정보 조회
	@PostMapping(value = "/select_pm02_Info_Dtl")
	public String select_pm02_Info_Dtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm02svc.select_pm02_Info_Dtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//INSERT
	@PostMapping(value = "/insert_pm02")
	public String insert_pm02(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (pm02svc.insert_pm02(paramMap, mRequest) != 0 ) {
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
	@PostMapping(value = "/update_pm02")
	public String update_pm02(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (pm02svc.update_pm02(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_pm02")
	public String delete_pm02(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm02svc.delete_pm02(paramMap) != 0 ) {
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