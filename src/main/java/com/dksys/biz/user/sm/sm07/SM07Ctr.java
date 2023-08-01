package com.dksys.biz.user.sm.sm07;

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
import com.dksys.biz.user.sm.sm07.service.SM07Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm07")
public class SM07Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM07Svc sm07Svc;

	// 매입관리 발주 조회
	@PostMapping(value = "/selectOrderList")
	public String selectPchsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm07Svc.selectOrderListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm07Svc.selectOrderList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
		
	@PostMapping(value = "/selectOrderDetailList")
	public String selectOrderDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm07Svc.selectOrderDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	
  //발주 수정
	@PostMapping(value = "/updateOrderDetail")
    public String updateOrderDetail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm07Svc.updateOrderDetail(paramMap, mRequest) != 0 ) {
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
    
	/*
	 * // 마스터 페이지 조회
	 * 
	 * @PostMapping(value = "/master_grid_selectList") public String
	 * master_grid_selectList(@RequestBody Map<String, String> paramMap, ModelMap
	 * model) { int totalCnt = sm07Svc.master_grid_selectCount(paramMap);
	 * PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	 * model.addAttribute("paginationInfo", paginationInfo); List<Map<String,
	 * String>> result = sm07Svc.master_grid_selectList(paramMap);
	 * model.addAttribute("result", result); return "jsonView"; }
	 * 
	 * //모달 그리드 검색 조회
	 * 
	 * @PostMapping(value = "/select_sm07_Info") public String
	 * select_sm07_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
	 * Map<String, String> result = sm07Svc.select_sm07_Info(paramMap);
	 * model.addAttribute("result", result); return "jsonView"; }
	 * 
	 * //모달창 안에 그리드 grid-modal
	 * 
	 * @PostMapping(value = "/grid_Modal_selectList") public String
	 * grid_Modal_selectList(@RequestBody Map<String, String> paramMap, ModelMap
	 * model) { List<Map<String, String>> result =
	 * sm07Svc.grid_Modal_selectList(paramMap); model.addAttribute("result",
	 * result); return "jsonView"; }
	 */
	
	
}