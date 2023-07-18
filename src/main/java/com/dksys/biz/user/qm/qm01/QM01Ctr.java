package com.dksys.biz.user.qm.qm01;

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

import com.dksys.biz.user.qm.qm01.service.QM01Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/qm/qm01")
public class QM01Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  QM01Svc qm01Svc;

// 발주 및 출장 요청서 리스트 조회
  @PostMapping(value = "/selectQualityReqList")
  public String selectQualityReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = qm01Svc.selectQualityReqCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm01Svc.selectQualityReqList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
   
//발주 및 출장 요청서 리스트엑셀 리스트  -->
  @PostMapping(value = "/selectQualityReqExList") 
	public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
		  List<Map<String, String>> resultList = qm01Svc.selectQualityReqExList(paramMap);
		  	  model.addAttribute("resultList", resultList); 
		   //  System.out.println("resultList==?"+resultList);
		  return "jsonView"; 
		  
	}  

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectQtyReqInfo")
  public String selectPchsCostInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	Map<String, String> result = qm01Svc.selectQtyReqInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = qm01Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertQualityReq")
  public String insertQualityReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (qm01Svc.insertQualityReq(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updateQualityReq")
  public String updateQualityReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (qm01Svc.updateQualityReq(paramMap, mRequest) != 0 ) {
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

  @PutMapping(value = "/deleteQualityReq")
  public String deleteQualityReq(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (qm01Svc.deleteQualityReq(paramMap) != 0 ) {
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