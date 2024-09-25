package com.dksys.biz.user.sm.sm60;

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
import com.dksys.biz.user.sm.sm60.service.SM60Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm60")
public class SM60Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM60Svc sm60Svc;	

	// 거래처별 매입 확정 집계 조회
	@PostMapping(value = "/selectVendEstimateList")
	public String selectClntPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		
		List<Map<String, String>> result = sm60Svc.selectVendEstimateList(paramMap);
		model.addAttribute("result", result);

	   	List<Map<String, String>> resultMngId = sm60Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}	



	//평가점수 Update
    @PostMapping(value = "/updateVendEstimate")
    public String updateVendEstimate(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
  			if (sm60Svc.updateVendEstimate(param) != 0 ) {
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


    @PutMapping(value = "/deleteVendEstimate")
    public String deleteVendEstimate(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
  			if (sm60Svc.deleteVendEstimate(paramMap) != 0 ) {
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