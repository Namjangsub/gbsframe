package com.dksys.biz.user.bm.bm13;

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
import com.dksys.biz.user.bm.bm13.service.BM13Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm13")
public class BM13Ctr {
	
	private Logger logger = LoggerFactory.getLogger(BM13Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    BM13Svc bm13Svc;

	//기준관리 결재선관리 메인 화면 조회 리스트  -->
    @PostMapping(value = "/selectApprovalList") 
    public String selectApplovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = bm13Svc.selectApprovalListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
  		List<Map<String, String>> resultList = bm13Svc.selectApprovalList(paramMap);
		model.addAttribute("resultList", resultList); 
		return "jsonView"; 
    }
	  
	//기준관리 결재선관리 엑셀 리스트  -->
    @PostMapping(value = "/selectApprovalExcelList") 
	public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = bm13Svc.selectApprovalExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }    

	//기준관리 결재선 등록
    @PostMapping(value = "/insertApproval")
    public String insertApproval(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (bm13Svc.insertApproval(paramMap, mRequest) != 0 ) {
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
    
	//기준관리 결재선 수정    
    @PostMapping(value = "/updateApproval")
    public String updateApproval(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  	  	try {
  			if (bm13Svc.updateApproval(paramMap, mRequest) != 0 ) {
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
    
	//기준관리 결재선 삭제    
    @PutMapping(value = "/deleteApproval")
    public String deleteApproval(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
  			if (bm13Svc.deleteApproval(paramMap) != 0 ) {
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
    
  //기준관리 결재선 삭제    
    @PutMapping(value = "/deleteMainGdApproval")
    public String deleteMainGdApproval(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
  			if (bm13Svc.deleteMainGdApproval(paramMap) != 0 ) {
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
    
    @PostMapping(value = "/selectRsltsMemberList") 
	public String selectRsltsMemberList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> resultList = bm13Svc.selectRsltsMemberList(paramMap);
	    model.addAttribute("resultList", resultList); 
	    return "jsonView"; 		 
    }
    
}