package com.dksys.biz.user.sm.sm10;

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

import com.dksys.biz.user.sm.sm10.service.SM10Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm10")
public class SM10Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  SM10Svc sm10Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectPchsCostList")
  public String selectPchsCostList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = sm10Svc.selectPchsCostCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = sm10Svc.selectPchsCostList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectPchsCostInfo")
  public String selectPchsCostInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	Map<String, String> result = sm10Svc.selectPchsCostInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = sm10Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertPchsCost")
  public String insertPchsCost(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm10Svc.insertPchsCost(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updatePchsCost")
  public String updatePchsCost(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (sm10Svc.updatePchsCost(paramMap, mRequest) != 0 ) {
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

  @PutMapping(value = "/deletePchsCost")
  public String deletePchsCost(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (sm10Svc.deletePchsCost(paramMap) != 0 ) {
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