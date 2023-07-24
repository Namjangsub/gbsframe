package com.dksys.biz.user.sm.sm12;

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
import com.dksys.biz.user.sm.sm12.service.SM12Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm12")
public class SM12Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM12Svc sm12Svc;	

	// 매입관리 입고 조회
	@PostMapping(value = "/selectPurchaseList")
	public String selectPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm12Svc.selectPurchaseListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm12Svc.selectPurchaseList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
		
	// 매입관리 발주 조회 엑셀
	@PostMapping(value = "/selectPurchaseExcelList")
	public String selectPurchaseExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm12Svc.selectPurchaseExcelList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	

	
	// 발주상세 조회
	@PostMapping(value = "/selectPurchaseDetailList")
	public String selectPurchaseDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm12Svc.selectPurchaseDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
	
	//매입확정관리 등록
    @PostMapping(value = "/insertPurchaseDetail")
    public String insertPurchase(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm12Svc.insertPurchaseDetail(paramMap) != 0 ) {
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
    @PostMapping(value = "/updatePurchaseDetail")
    public String updatePurchaseDetail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm12Svc.updatePurchaseDetail(paramMap) != 0 ) {
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
	@DeleteMapping(value = "/deletePurchaseDetail")
	public String deletePurchaseDetail(@RequestBody Map<String, String> param, ModelMap model) {
		sm12Svc.deletePurchaseDetail(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	} 
}