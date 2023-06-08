package com.dksys.biz.admin.bm.bm16;

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

import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/bm/bm16")
public class BM16Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  BM16Svc bm16Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectPrjctList")
  public String selectPrjctList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = bm16Svc.selectPrjctCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> bm1601m01 = bm16Svc.selectPrjctList(paramMap);
    model.addAttribute("bm1601m01", bm1601m01);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectPrjctInfo")
  public String selectPrjctInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    Map<String, String> result = bm16Svc.selectPrjctInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 대상설비 리스트 조회
  @PostMapping("/selectPrdtList")
  public String selectPrdtList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> prdtList = bm16Svc.selectPrdtList(paramMap);
	  model.addAttribute("prdtList", prdtList);
	  return "jsonView";
  }

  // 아이템 정보조회
  @PostMapping("/selectItemList")
  public String selectItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> itemList = bm16Svc.selectItemList(paramMap);
    model.addAttribute("itemList", itemList);
    return "jsonView";
  }

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = bm16Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertPrjct")
  public String insertPrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm16Svc.insertPrjct(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updatePrjct")
  public String updatePrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (bm16Svc.updatePrjct(paramMap, mRequest) != 0 ) {
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

  @PutMapping(value = "/deletePrjct")
  public String deletePrjct(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (bm16Svc.deletePrjct(paramMap) != 0 ) {
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