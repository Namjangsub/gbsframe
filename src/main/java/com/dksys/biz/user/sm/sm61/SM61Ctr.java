package com.dksys.biz.user.sm.sm61;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.sm.sm61.service.SM61Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm61")
public class SM61Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM61Svc sm61Svc;

	// 거래처별 매입 확정 집계 조회
	@PostMapping(value = "/selectVendInitEstimateList")
	public String selectClntPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		
		List<Map<String, String>> result = sm61Svc.selectVendInitEstimateList(paramMap);
		model.addAttribute("result", result);

		List<Map<String, String>> resultMngId = sm61Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}	



	//평가점수 Update
	@PostMapping(value = "/updateVendInitEstimate")
	public String updateVendInitEstimate(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
			if (sm61Svc.updateVendInitEstimate(param) != 0) {
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


	@PutMapping(value = "/deleteVendInitEstimate")
	public String deleteVendInitEstimate(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
			if (sm61Svc.deleteVendInitEstimate(paramMap) != 0) {
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