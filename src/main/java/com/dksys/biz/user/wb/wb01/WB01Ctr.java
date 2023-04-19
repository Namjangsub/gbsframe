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
	
	  @PostMapping(value = "/selectWbsList") public String
	  selectWbsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int totalCnt = wb01Svc.selectWbsCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsList(paramMap);
		  model.addAttribute("resultList", resultList); return "jsonView"; 
		  
	  }
	 
	  @PostMapping(value = "/selectWbsList01") public String
	  selectWbsList01(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsList01(paramMap);
		  model.addAttribute("resultList", resultList); return "jsonView"; 
		  
	  }
    
}