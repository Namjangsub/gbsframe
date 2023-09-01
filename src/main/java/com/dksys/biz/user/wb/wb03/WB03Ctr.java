package com.dksys.biz.user.wb.wb03;

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

import com.dksys.biz.user.wb.wb03.service.WB03Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/wb/wb03")
public class WB03Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  WB03Svc wb03Svc;

    //Paging 조회
	@PostMapping(value = "/selectWbsPlanTreeIssueList")
	public String selectWbsPlanTreeIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = wb03Svc.selectWbsPlanTreeIssueList(paramMap);
		
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}
		
    @PostMapping(value = "/selectMaxWbsIssueNo") 
    public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb03Svc.selectMaxWbsIssueNo(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
	   
    @PostMapping(value = "/insertWbsPlanIssue")
    public String insertWbsPlanIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			List<Map<String, String>> sharngChk = wb03Svc.deleteIssueSharngListChk(paramMap);
			if (sharngChk.size() > 0) {
				wb03Svc.deleteIssueSharngList(paramMap);	
			}
			
  			if (wb03Svc.insertWbsPlanIssue(paramMap, mRequest) != 0 ) {
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
    
    @PutMapping(value = "/updateWbsPlanIssue")
    public String updateWbsPlanIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
 		try {
 			List<Map<String, String>> sharngChk = wb03Svc.deleteIssueSharngListChk(paramMap);
			if (sharngChk.size() > 0) {
				wb03Svc.deleteIssueSharngList(paramMap);	
			}
			
 			if (wb03Svc.updateWbsPlanIssue(paramMap, mRequest) != 0 ) {
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
    
    @PutMapping(value = "/deleteWbsPlanIssue")
    public String deleteWbsPlanIssue(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
			if (wb03Svc.deleteWbsPlanIssue(paramMap) != 0 ) {
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
    
    
    @PostMapping(value = "/selectIssueSharngList") 
    public String selectPlanSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
 		  List<Map<String, String>> resultList = wb03Svc.selectIssueSharngList(paramMap);
 		  model.addAttribute("resultList", resultList); 
 		  return "jsonView"; 
    }
}