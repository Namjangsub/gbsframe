package com.dksys.biz.user.cr.cr16;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import com.dksys.biz.user.cr.cr16.service.CR16Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/cr/cr16")
public class CR16Ctr {
	
	private Logger logger = LoggerFactory.getLogger(CR16Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CR16Svc cr16Svc;

    @PostMapping(value = "/selectSalesYearPlanList") 
	public String selectSalesYearPlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {	
    	  int totalCnt = cr16Svc.selectSalesYearPlanListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanList(paramMap);
		  
		  
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
    @PutMapping(value = "/deleteSalesYearPlanList")
    public String deleteSalesYearPlanList(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
			if (cr16Svc.deleteSalesYearPlanList(paramMap) != 0 ) {
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
    
    @PostMapping(value = "/selectSalesYearPlanMC") 
	public String selectSalesYearPlanMC(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanMC(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
    @PostMapping(value = "/selectSalesYearPlanMU") 
	public String selectSalesYearPlanMU(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanMU(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
    @PostMapping(value = "/selectSalesYearPlanD") 
	public String selectSalesYearPlanD(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanD(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
}	  