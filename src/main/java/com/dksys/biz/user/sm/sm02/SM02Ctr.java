package com.dksys.biz.user.sm.sm02;

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
import com.dksys.biz.user.sm.sm02.service.SM02Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm02")
public class SM02Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM02Svc sm02Svc;

	// 매입관리 발주 조회
	@PostMapping(value = "/selectOrderList")
	public String selectPchsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm02Svc.selectOrderListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm02Svc.selectOrderList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 매입관리 발주 조회 엑셀
	@PostMapping(value = "/selectOrderExcelList")
	public String selectOrderExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm02Svc.selectOrderExcelList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	

	// BOM내역상세 조회
	@PostMapping(value = "/selectBomDetailList")
	public String selectBomDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectBomDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	// 발주상세 조회
	@PostMapping(value = "/selectOrderDetailList")
	public String selectOrderDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectOrderDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
	
	// 발주상세 팝업 조회
	@PostMapping(value = "/selectOrderDetailView")
	public String selectOrderDetailView(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectOrderDetailView(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}		
	
	//발주 등록
    @PostMapping(value = "/insertOrderMaster")
    public String insertOrderMaster(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.insertOrderMaster(paramMap, mRequest) != 0 ) {
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
    
	//발주 수정
    @PostMapping(value = "/updateOrder")
    public String updateOrder(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateOrder(paramMap, mRequest) != 0 ) {
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

	//발주 수정
    @PostMapping(value = "/updateOrderDetail")
    public String updateOrderDetail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateOrderDetail(paramMap, mRequest) != 0 ) {
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

    
	//발주 detail 삭제    
	@DeleteMapping(value = "/deleteOrderDetail")
	public String deleteOrderDetail(@RequestBody Map<String, String> param, ModelMap model) {
		sm02Svc.deleteOrderDetail(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	}
	
	//발주 등록시 현재환율 조회
	@PostMapping(value = "/selectCurrToday")
	public String selectCurrToday(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectCurrToday(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
}