package com.dksys.biz.user.cr.cr10;

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
import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr10")
public class CR10Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  CR10Svc cr10Svc;

    //Paging 조회
	@PostMapping(value = "/selectLgistReqPageList")
	public String selectLgistReqPageList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr10Svc.selectLgistReqPageCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr10Svc.selectLgistReqPageList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistReqList")
	public String selectLgistReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr10Svc.selectLgistReqList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectSelesCdList")
	public String selectSelesCdList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectSelesCdList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectSelesCdViewList")
	public String selectSelesCdViewList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectSelesCdViewList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistMastInfo")
		public String selectLgistMastInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr10Svc.selectLgistMastInfo(paramMap);
		model.addAttribute("result", result);
		List<Map<String, String>> resultList = cr10Svc.selectLgistAppList(paramMap);
		model.addAttribute("resultAppList", resultList);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistAppCount")
	public String selectLgistAppCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		String result = cr10Svc.selectLgistAppCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/selectLgistAppList")
	public String selectLgistAppList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectLgistAppList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}


	@PostMapping(value = "/insertLgistMast")
	public String insertLgistMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			Map<String, String> rtnResult = cr10Svc.insertLgistMast(paramMap, mRequest);
			
			String lgistNo =  rtnResult.get("lgistNo").toString();
			int result =  Integer.parseInt(rtnResult.get("result"));
			
			if (result !=0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				model.addAttribute("lgistNo", lgistNo);
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

	@PostMapping(value = "/updateLgistMast")
	public String updateLgistMast(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.updateLgistMast(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteLgistMast")
	public String deleteLgistMast(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.deleteLgistMast(paramMap) != 0 ) {
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

	@PostMapping(value = "/updateTodoAppConfirm")
	public String updateTodoAppConfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.updateTodoAppConfirm(paramMap) != 0 ) {
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

}