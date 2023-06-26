package com.dksys.biz.user.cr.cr08;

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

import com.dksys.biz.user.cr.cr08.service.CR08Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr08")
public class CR08Ctr {	
	
	  @Autowired
	  MessageUtils messageUtils;

	  @Autowired
	  CR08Svc cr08Svc;

	//  수주 리스트 조회
	  @PostMapping(value = "/selectSalesStmtList")
	  public String selectSalesStmtList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    int totalCnt = cr08Svc.selectPchsCostCount(paramMap);
	    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	    model.addAttribute("paginationInfo", paginationInfo);
	    List<Map<String, String>> result = cr08Svc.selectSalesStmtList(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }
	  
	// 매출계산서 리스트 조회
	  @PostMapping(value = "/selectSalesStmtCalList")
	  public String selectSalesStmtCalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    int totalCnt = cr08Svc.selectSalesStmtCalCount(paramMap);
	    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	    model.addAttribute("paginationInfo", paginationInfo);
	    List<Map<String, String>> result = cr08Svc.selectSalesStmtCalList(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }
	  
	//  매출계산서 리스트 기본정보
	  @PostMapping(value = "/selectSalesStmtConList")
	  public String selectSalesStmtConList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    int totalCnt = cr08Svc.selectSalesStmtConCount(paramMap);
	    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	   model.addAttribute("paginationInfo", paginationInfo);
	    List<Map<String, String>> result = cr08Svc.selectSalesStmtConList(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }

	  // 프로젝트 정보 조회
	  @PostMapping(value = "/selectPchsCostInfo")
	  public String selectPchsCostInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr08Svc.selectPchsCostInfo(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }
	  

	  @PostMapping(value = "/selectConfirmCount")
	  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    int result = cr08Svc.selectConfirmCount(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }
	  
	  @PostMapping(value = "/insertSalesStmt")
	  public String insertSalesStmt(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
			try {
				if (cr08Svc.insertSalesStmt(paramMap, mRequest) != 0) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
					System.out.println("200");
				} else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
					System.out.println("500");
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
				if (cr08Svc.updatePchsCost(paramMap, mRequest) != 0 ) {
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
				if (cr08Svc.deletePchsCost(paramMap) != 0 ) {
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
	  
	//기준관리 결재선관리 엑셀 리스트  -->
    @PostMapping(value = "/selectSalesStmtExcelList") 
	public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = cr08Svc.selectSalesStmtExcelList(paramMap);
		  	  model.addAttribute("resultList", resultList); 
		     System.out.println("resultList==?"+resultList);
		  return "jsonView"; 
		  
	  }  
    
  //기준관리 결재선관리 엑셀 리스트  -->
    @PostMapping(value = "/selectSalesStmtCalExcelList") 
	public String selectSalesStmtCalExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = cr08Svc.selectSalesStmtCalExcelList(paramMap);
		  	  model.addAttribute("resultList", resultList); 
		     System.out.println("resultList==?"+resultList);
		  return "jsonView"; 
		  
	  }  
 
}
