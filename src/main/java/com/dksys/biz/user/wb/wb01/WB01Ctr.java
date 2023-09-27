package com.dksys.biz.user.wb.wb01;

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
import com.dksys.biz.user.wb.wb01.service.WB01Svc;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb01")
public class WB01Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB01Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB01Svc wb01Svc;

	//<!--   --> 
	@PostMapping(value = "/selectWbsLeftSalesCodeList") 
	public String selectWbsLeftSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {		
		int totalCnt = wb01Svc.selectWbsLeftSalesCodeListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		 
		List<Map<String, String>> fileList = wb01Svc.selectWbsLeftSalesCodeList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }	

	@PostMapping(value = "/selectNewWbsPlanTreeList") 
	public String selectNewWbsPlanTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
    	List<Map<String, String>> fileList = wb01Svc.selectNewWbsPlanTreeList(paramMap);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
	}
	
	@PostMapping(value = "/selectNewWbsPlanTreeListSelect") 
	public String selectNewWbsPlanTreeListSelect(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
    	List<Map<String, String>> fileList = wb01Svc.selectNewWbsPlanTreeListSelect(paramMap);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
	}
	
    @PutMapping(value = "/deleteWbsPlanlist")
    public String deleteWbsPlanlist(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb01Svc.deleteWbsPlanlist(paramMap) != 0 ) {
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

   @PutMapping(value = "/updateWbsPlanLockYn")
   public String updateWbsPlanLockYn(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb01Svc.updateWbsPlanLockYn(paramMap) != 0 ) {
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

   @PostMapping(value = "/selectPlanSharngList") 
   public String selectPlanSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb01Svc.selectPlanSharngList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
   }

   @PostMapping(value = "/selectMaxWbsPlanNo") 
   public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> result = wb01Svc.selectMaxWbsPlanNo(paramMap);
	  model.addAttribute("result", result); 
	  return "jsonView"; 		 
   }
   

  @PostMapping(value = "/selectWbsPlanChk")
  public String selectWbsPlanChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
	 int result = wb01Svc.selectWbsPlanChk(paramMap);
	 model.addAttribute("result", result);
     return "jsonView";
  }

  @PostMapping(value = "/insertWbsPlan")
  public String insertWbsPlan(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
			if (sharngChk.size() > 0) {
				wb01Svc.deleteWbsSharngList(paramMap);	
			}
			if (wb01Svc.insertWbsPlan(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updateWbsPlan")
  public String updateWbsPlan(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
	  		List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
			if (sharngChk.size() > 0) {
				wb01Svc.deleteWbsSharngList(paramMap);	
			}
			if (wb01Svc.updateWbsPlan(paramMap, mRequest) != 0 ) {
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
	
  @PutMapping(value = "/updateWbsRsltsCloseYn")
  public String updateWbsRsltsCloseYn(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (wb01Svc.updateWbsRsltsCloseYn(paramMap) != 0 ) {
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

  @PostMapping(value = "/selectWbsSalesCodeList") 
  public String selectWbsSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) { 
	  
	  int totalCnt = wb01Svc.selectWbsSalesCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
  	model.addAttribute("paginationInfo", paginationInfo);
  	
     List<Map<String, String>> codeInfoList = wb01Svc.selectWbsSalesCodeList(paramMap); 
     model.addAttribute("codeInfoList", codeInfoList); return "jsonView";
  }
  
  @PostMapping(value = "/selectWbsPlanNoList") 
  public String selectWbsPlanNoList(@RequestBody Map<String, String> paramMap, ModelMap model) {  
     List<Map<String, String>> codeInfoList =
     wb01Svc.selectWbsPlanNoList(paramMap); 
     model.addAttribute("codeInfoList",codeInfoList); return "jsonView";  
  }
  
  @PutMapping(value = "/deleteWbsRsltslist")
  public String deleteWbsRsltslist(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb01Svc.deleteWbsRsltslist(paramMap) != 0 ) {
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

  @PostMapping(value = "/selectWbsInfo") 
	public String selectWbsInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
  	List<Map<String, String>> result = wb01Svc.selectWbsInfo(paramMap);
  	model.addAttribute("result", result);
      return "jsonView";
	}

  @PostMapping(value = "/wbsPlanListInsert")
  public String wbsPlanListInsert(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb01Svc.wbsPlanListInsert(paramMap) != 0 ) {
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
  
  @PutMapping(value = "/deleteWbsPlanTempList")
  public String deleteWbsPlanTempList(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb01Svc.deleteWbsPlanTempList(paramMap) != 0 ) {
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
  
  @PostMapping(value = "/wbsCodeList") 
  public String wbsCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb01Svc.wbsCodeList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
  }
}	  