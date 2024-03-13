package com.dksys.biz.user.sm.sm21;

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
import com.dksys.biz.user.sm.sm21.service.SM21Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm21")
public class SM21Ctr {
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	SM21Svc sm21Svc;

	// 거래처별 매입 확정 집계 조회
	@PostMapping(value = "/sm21_main_grid1_selectList")
	public String sm21_main_grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm21Svc.sm21_main_grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 거래처별 매입 확정 상세 조회
	@PostMapping(value = "/sm21_main_grid2_selectList")
	public String sm21_main_grid2_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm21Svc.sm21_main_grid2_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm21Svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm21Svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//팝업 입력대상 검색
	@PostMapping(value = "/select_sm21_insert_target_modal")
	public String select_sm21_insert_target_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm21Svc.select_sm21_insert_target_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//세금계산서지급여부
	@PostMapping(value = "/update_sm21_payYn")
    public String update_sm21_payYn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm21Svc.update_sm21_payYn(paramMap) != 0 ) {
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
	
	//UPDATE
	@PostMapping(value = "/update_sm21")
	public String update_sm21(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm21Svc.update_sm21(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_sm21")
	public String delete_sm21(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (sm21Svc.delete_sm21(paramMap) != 0 ) {
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