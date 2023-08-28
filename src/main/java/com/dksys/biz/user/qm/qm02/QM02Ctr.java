package com.dksys.biz.user.qm.qm02;

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

import com.dksys.biz.user.qm.qm02.service.QM02Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/qm/qm02")
public class QM02Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    QM02Svc qm02svc;

    //리스트 조회
	@PostMapping(value = "/selectMainGridList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = qm02svc.select_grid_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = qm02svc.selectMainGridList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
//
//	// 창고 코드 검색
//	@PostMapping(value = "/selectWhCd")
//    public String selectWhCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
//        List<Map<String, Object>> result = qm02svc.selectWhCd(paramMap);
//        model.addAttribute("result", result);
//        return "jsonView";
//    }

	//팝업 그리드 검색
	@PostMapping(value = "/select_stock_modal")
	public String select_stock_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = qm02svc.select_stock_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//팝업 그리드 검색
	@PostMapping(value = "/select_all_modal")
	public String select_all_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = qm02svc.select_all_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//팝업 그리드 검색
	@PostMapping(value = "/select_zupiter_modal")
	public String select_zupiter_modal(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = qm02svc.select_zupiter_modal(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_qm02_Info")
	public String select_qm02_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = qm02svc.select_qm02_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//INSERT
	@PostMapping(value = "/insert_qm02")
	public String insert_qm02(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			int result = qm02svc.insert_qm02(paramMap, mRequest);
			if (result != 0 && result != 7 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			}
			else if (result != 0 && result == 7) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", "해당 월의 고찰은 등록 되어 있습니다.");
			}
			else {
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
	@PostMapping(value = "/update_qm02")
	public String update_qm02(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (qm02svc.update_qm02(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_qm02")
	public String delete_qm02(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (qm02svc.delete_qm02(paramMap) != 0 ) {
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
 
	//INSERT
	@PostMapping(value = "/insert_qm02_p02")
	public String insert_qm02_p02(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			int result = qm02svc.insert_qm02_p02(paramMap, mRequest);
			if (result != 0 && result != 7 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			}
			else if (result != 0 && result == 7) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", "해당 월의 고찰은 등록 되어 있습니다.");
			}
			else {
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
