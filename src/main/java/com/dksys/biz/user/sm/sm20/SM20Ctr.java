package com.dksys.biz.user.sm.sm20;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm20.service.SM20Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm20")
public class SM20Ctr {
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	SM20Svc sm20Svc;

	// 거래처별 매입 확정 집계 조회
	@PostMapping(value = "/sm20_main_grid1_selectList")
	public String sm20_main_grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm20Svc.sm20_main_grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 거래처별 매입 확정 상세 조회
	@PostMapping(value = "/sm20_main_grid2_selectList")
	public String sm20_main_grid2_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		model.addAttribute("result", sm20Svc.sm20_main_grid2_selectList(paramMap));
		model.addAttribute("grid3", sm20Svc.sm20_main_grid3_selectList(paramMap));
		return "jsonView";
	}

	//리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm20Svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm20Svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//팝업 입력대상 검색
	@PostMapping(value = "/select_sm20_insert_target_modal")
	public String select_sm20_insert_target_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm20Svc.select_sm20_insert_target_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_sm20_Info")
	public String select_sm20_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm20Svc.select_sm20_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//상세정보 조회
	@PostMapping(value = "/select_sm20_Info_Dtl")
	public String select_sm20_Info_Dtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm20Svc.select_sm20_Info_Dtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//세금계산서지급여부
	@PostMapping(value = "/update_sm20_payYn")
    public String update_sm20_payYn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm20Svc.update_sm20_payYn(paramMap) != 0 ) {
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

	//INSERT
	@PostMapping(value = "/insert_sm20")
	public String insert_sm20(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm20Svc.insert_sm20(paramMap, mRequest) != 0 ) {
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
	@PostMapping(value = "/update_sm20")
	public String update_sm20(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm20Svc.update_sm20(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_sm20")
	public String delete_sm20(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (sm20Svc.delete_sm20(paramMap) != 0 ) {
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