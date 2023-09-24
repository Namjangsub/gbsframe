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
    
    @PostMapping(value = "/selectSalesYearPlanListExcel") 
	public String selectSalesYearPlanListExcel(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanList(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }    
    
    @PostMapping(value = "/selectSalesYearPlanListHist") 
	public String selectSalesYearPlanListHist(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanListHist(paramMap);
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
    
    @PostMapping(value = "/salesPlanYearInsert")
    public String salesPlanYearInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr16Svc.salesPlanYearInsert(paramMap, mRequest) != 0 ) {
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

    @PostMapping(value = "/salesPlanYearUpdate")
    public String salesPlanYearUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr16Svc.salesPlanYearUpdate(paramMap, mRequest) != 0 ) {
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
    
    @PostMapping(value = "/salesYearPlanCloseY")
    public String salesYearPlanCloseY(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr16Svc.salesYearPlanCloseY(paramMap, mRequest) != 0 ) {
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
    
    @PostMapping(value = "/salesYearPlanCloseN")
    public String salesYearPlanCloseN(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr16Svc.salesYearPlanCloseN(paramMap, mRequest) != 0 ) {
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
    
    
    @PostMapping(value = "/selectSalesYearPlanMC2") 
	public String selectSalesYearPlanMC2(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanMC2(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
    @PostMapping(value = "/selectSalesYearPlanD2") 
	public String selectSalesYearPlanD2(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		  List<Map<String, String>> fileList = cr16Svc.selectSalesYearPlanD2(paramMap);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
    }
    
    @PostMapping(value = "/selectSalesDeptList") 
	public String selectSalesDeptList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> list = cr16Svc.selectSalesDeptList(paramMap);
		  model.addAttribute("list", list);
		  return "jsonView";
    }
    
    @PostMapping(value = "/callPlanClose") 
	public String callPlanClose(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		cr16Svc.callPlanClose(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", "확정되었습니다.");
			model.addAttribute("errMsg", paramMap.get("errMsg"));
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  			model.addAttribute("errMsg", paramMap.get("errMsg"));
  		}
		return "jsonView";
    }
    
    @PostMapping(value = "/selectSalesPlanHistList") 
	public String selectSalesPlanHistList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> list = cr16Svc.selectSalesPlanHistList(paramMap);
		  model.addAttribute("list", list);
		  return "jsonView";
    }
    
}	  