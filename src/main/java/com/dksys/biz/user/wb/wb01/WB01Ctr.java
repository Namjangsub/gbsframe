package com.dksys.biz.user.wb.wb01;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.dksys.biz.exc.LogicException;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.user.wb.wb01.service.WB01Svc;

@Controller
@RequestMapping("/user/wb/wb01")
public class WB01Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB01Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB01Svc wb01Svc;
	
	  @PostMapping(value = "/selectWbsPlanList") public String
	  selectWbsPlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int totalCnt = wb01Svc.selectWbsPlanCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb01Svc.selectWbsPlanList(paramMap);
		  model.addAttribute("resultList", resultList); return "jsonView"; 
		  
	  }
	 
	  @PostMapping(value = "/selectWbsPlanLvl1List") public String
	  selectWbsPlanLvl1List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsPlanLvl1List(paramMap);
		  model.addAttribute("resultList", resultList); return "jsonView"; 
		  
	  }
	  
	  @PostMapping(value = "/selectWbsPlanLvlCboList") public String
	  selectWbsPlanLvlCboList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanLvlCboList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); return "jsonView"; 
		  
	  }
	  
	  @PostMapping(value = "/selectWbsPlanSubLvlCboList") public String
	  selectWbsPlanSubLvlCboList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanSubLvlCboList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); return "jsonView"; 
		  
	  }
	  
	  @PostMapping(value = "/selectWbsPlanNoList") public String
	  selectWbsPlanNoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanNoList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); return "jsonView"; 
		  
	  }
	  
	  @PostMapping(value = "/selectWbsSalesCodeList") public String
	  selectWbsSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsSalesCodeList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); return "jsonView"; 
		  
	  }
    
}