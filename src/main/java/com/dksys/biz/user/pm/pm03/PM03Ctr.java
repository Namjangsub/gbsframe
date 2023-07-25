package com.dksys.biz.user.pm.pm03;

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
import com.dksys.biz.user.pm.pm03.service.PM03Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm03")
public class PM03Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  PM03Svc pm03Svc;

    //Paging 조회
	@PostMapping(value = "/selectDeliveryPageList")
	public String selectDeliveryPageList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm03Svc.selectDeliveryPageCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm03Svc.selectDeliveryPageList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectDeliveryList")
	public String selectDeliveryList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm03Svc.selectDeliveryList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectSelesCdList")
	public String selectSelesCdList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = pm03Svc.selectSelesCdList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectDeliveryMastInfo")
		public String selectDeliveryMastInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = pm03Svc.selectDeliveryMastInfo(paramMap);
		model.addAttribute("result", result);
//		List<Map<String, String>> resultList = pm03Svc.selectSelesCdList(paramMap);
//		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/insertDeliveryMast")
	public String insertDeliveryMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (pm03Svc.insertDeliveryMast(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/updateDeliveryMast")
	public String updateDeliveryMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (pm03Svc.updateDeliveryMast(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteDeliveryMast")
	public String deleteDeliveryMast(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (pm03Svc.deleteDeliveryMast(paramMap) != 0 ) {
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