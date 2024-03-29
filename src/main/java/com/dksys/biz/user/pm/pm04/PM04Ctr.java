package com.dksys.biz.user.pm.pm04;

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

import com.dksys.biz.user.pm.pm04.service.PM04Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm04")
public class PM04Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  PM04Svc pm04Svc;

  // 작업일보 리스트 조회
  @PostMapping(value = "/selectDailyWorkMainList")
  public String selectDailyWorkMainList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm04Svc.selectDailyWorkCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm04Svc.selectDailyWorkMainList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

//  // 작업일보 정보 조회
//  @PostMapping(value = "/selectDailyWorkInfo")
//  public String selectDailyWorkInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
//	Map<String, String> result = pm04Svc.selectDailyWorkInfo(paramMap);
//    model.addAttribute("result", result);
//    return "jsonView";
//  }
  

//  @PostMapping(value = "/selectConfirmCount")
//  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
//    int result = pm04Svc.selectConfirmCount(paramMap);
//    model.addAttribute("result", result);
//    return "jsonView";
//  }
//
//  @PostMapping(value = "/insertDailyWork")
//  public String insertDailyWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
//		try {
//			if (pm04Svc.insertDailyWork(paramMap, mRequest) != 0 ) {
//				model.addAttribute("resultCode", 200);
//				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
//			} else {
//				model.addAttribute("resultCode", 500);
//				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
//			};
//		}catch(Exception e){
//			model.addAttribute("resultCode", 900);
//			model.addAttribute("resultMessage", e.getMessage());
//		}
//		return "jsonView";
//  }
//
//  @PostMapping(value = "/updateDailyWork")
//  public String updateDailyWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
//	  	try {
//			if (pm04Svc.updateDailyWork(paramMap, mRequest) != 0 ) {
//				model.addAttribute("resultCode", 200);
//				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
//			} else {
//				model.addAttribute("resultCode", 500);
//				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
//			};
//		}catch(Exception e){
//			model.addAttribute("resultCode", 900);
//			model.addAttribute("resultMessage", e.getMessage());
//		}
//	  	return "jsonView";
//  }
//
//  @PutMapping(value = "/deleteDailyWork")
//  public String deleteDailyWork(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
//	  	try {
//			if (pm04Svc.deleteDailyWork(paramMap) != 0 ) {
//				model.addAttribute("resultCode", 200);
//				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
//			} else {
//				model.addAttribute("resultCode", 500);
//				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
//			};
//		}catch(Exception e){
//			model.addAttribute("resultCode", 900);
//			model.addAttribute("resultMessage", e.getMessage());
//		}
//	  	return "jsonView";
//  }
//
//
  	//팀원 검색
	@PostMapping(value = "/select_all_name")
	public String select_all_name(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm04Svc.select_all_name(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}