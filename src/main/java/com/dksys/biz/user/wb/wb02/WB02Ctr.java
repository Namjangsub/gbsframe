package com.dksys.biz.user.wb.wb02;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;
import com.dksys.biz.user.wb.wb01.service.WB01Svc;
@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb02")
public class WB02Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB02Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;

	@Autowired
    WB01Svc wb01Svc;
	
    @Autowired
    WB02Svc wb02Svc;

    @PutMapping(value = "/deleteWbsRsltslist")
    public String deleteWbsRsltslist(@RequestParam Map<String, String> paramMap, ModelMap model) {
		try {
				if (wb02Svc.deleteWbsRsltslist(paramMap) != 0 ) {
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
    
    @PutMapping(value = "/updateWbsRsltsCloseYn")
    public String updateWbsRsltsCloseYn(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (wb02Svc.updateWbsRsltsCloseYn(paramMap) != 0 ) {
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

	@PostMapping(value = "/selectRsltsMemberList") 
	public String selectRsltsMemberList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> resultList = wb02Svc.selectRsltsMemberList(paramMap);
	    model.addAttribute("resultList", resultList); 
	    return "jsonView"; 		 
    }

	@PostMapping(value = "/selectWbsRsltsDetailList") 
	public String selectWbsRsltsDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsDetailList(paramMap);
	    model.addAttribute("resultList", resultList); 
	    return "jsonView";   
    }

	  @PostMapping(value = "/selectRsltsSharngList") 
	  public String selectRsltsSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectRsltsSharngList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }


	  //<!-- 결재 테이블 조회  -->  
	  @PostMapping(value = "/selectRsltsApprovalList") 
	  public String selectRsltsApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectRsltsApprovalList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }

   
	  @PostMapping(value = "/selectWbsRsltsInfo") 
	  public String selectWbsRsltsInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  Map<String, String> result= wb02Svc.selectWbsRsltsInfo(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
		  
	  } 


	  @PostMapping(value = "/wbsLevel1RsltsInsert")
      public String wbsLevel1RsltsInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {		
		try {
			List<Map<String, String>> detailChk = wb02Svc.deleteWbsRsltsDetailChk(paramMap);
			if (detailChk.size() > 0) {
				wb02Svc.deleteWbsRsltsDetail(paramMap);	
			}
			
			List<Map<String, String>> sharngChk = wb02Svc.deleteWbsSharngListChk(paramMap);
			if (sharngChk.size() > 0) {
				wb02Svc.deleteWbsSharngList(paramMap);	
			}
			
			List<Map<String, String>> approvalChk = wb02Svc.deleteWbsApprovalListChk(paramMap);
			if (approvalChk.size() > 0) {
				wb02Svc.deleteWbsApprovalList(paramMap);	
			}
			if (wb02Svc.wbsLevel1RsltsInsert(paramMap, mRequest) != 0 ) {
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


     @PutMapping(value = "/wbsPlanStsCodeUpdate")
     public String wbsPlanStsCodeUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.wbsPlanStsCodeUpdate(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }

	  @PutMapping(value = "/wbsLevel1RsltsUpdate")
      public String wbsLevel1RsltsUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	    List<Map<String, String>> detailChk = wb02Svc.deleteWbsRsltsDetailChk(paramMap);
	    if (detailChk.size() > 0) {
			wb02Svc.deleteWbsRsltsDetail(paramMap);	
	    }
		
	    List<Map<String, String>> sharngChk = wb02Svc.deleteWbsSharngListChk(paramMap);
	    if (sharngChk.size() > 0) {
			wb02Svc.deleteWbsSharngList(paramMap);	
	    }
		
	    List<Map<String, String>> approvalChk = wb02Svc.deleteWbsApprovalListChk(paramMap);
	    if (approvalChk.size() > 0) {
			wb02Svc.deleteWbsApprovalList(paramMap);	
	    }		
		  
		wb02Svc.wbsLevel1RsltsUpdate(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
     }



      @PostMapping(value = "/selectApprovalList") 
	  public String selectApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectApprovalList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }


	  
}


