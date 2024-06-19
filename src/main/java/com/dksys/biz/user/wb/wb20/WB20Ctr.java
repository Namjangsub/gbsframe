package com.dksys.biz.user.wb.wb20;

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
import com.dksys.biz.user.wb.wb20.service.WB20Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb20")
public class WB20Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB20Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB20Svc wb20Svc;

    //<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectToDoList") 
	  public String selectToDoList(@RequestBody Map<String, String> paramMap, ModelMap model) {

		  int totalCnt = wb20Svc.selectToDoCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb20Svc.selectToDoList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
		  
	  }
	 
	  @PostMapping(value = "/M08selectToDoList") 
	  public String M08selectToDoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int totalCnt = wb20Svc.M08selectToDoCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = wb20Svc.M08selectToDoList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
		  
	  }
	  
	  @PutMapping(value = "/toDoCfDtUpdate")
      public String toDoCfDtUpdate(@RequestParam Map<String, String> paramMap, ModelMap model) {
		wb20Svc.toDoCfDtUpdate(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
      }
	  
	  @PutMapping(value = "/updateRsltsApproval")
      public String updateRsltsApproval(@RequestParam Map<String, String> paramMap, ModelMap model) {
		wb20Svc.updateRsltsApproval(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
      }
	  
	  @PostMapping(value = "/selectApprovalChk") 
	  public String selectApprovalChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		  List<Map<String, String>> resultList = wb20Svc.selectApprovalChk(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  
	  @PostMapping(value = "/selectTodoDivList") 
	  public String selectTodoDivList(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		  List<Map<String, String>> resultList = wb20Svc.selectTodoDivList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  @PostMapping(value = "/selectApprovalYnList") 
	  public String selectApprovalYnList(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		  List<Map<String, String>> resultList = wb20Svc.selectApprovalYnList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  @PutMapping(value = "/updateRsltsQmApproval")
      public String updateRsltsQmApproval(@RequestParam Map<String, String> paramMap, ModelMap model) {
		wb20Svc.updateRsltsQmApproval(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
      }
	  	  
	  @PutMapping(value = "/updateQmMobileApproval")
      public String updateQmMobileApproval(@RequestParam Map<String, String> paramMap, ModelMap model) {
		wb20Svc.updateQmMobileApproval(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
      }
	  
	  @PostMapping(value = "/selectGetDeptList") 
	  public String selectGetDeptList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
  		  List<Map<String, String>> resultList = wb20Svc.selectGetDeptList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  } 
	  
	  // 공통 결재상태 불러오기 - OLD 사용안함
	  @PostMapping(value = "/selectGetApprovalList") 
	  public String selectApprovalGetList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
  		  List<Map<String, String>> resultList = wb20Svc.selectGetApprovalList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  } 
	  
	  //공통 결재승인(등록)
	  @PostMapping(value = "/insertApprovalLine")
	  public String insertApprovalLine(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    	try {
	    		Map<String, String> result = wb20Svc.insertApprovalLine(paramMap);
	    	    model.addAttribute("result", result);
	    	    
		    	model.addAttribute("resultCode", 200);
		    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
	    	}catch(Exception e) {
		    	 model.addAttribute("resultCode", 500);
		 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	    	}

	    	return "jsonView";		  		  
    }		  
	  
	  // 결재라인 싱글 셀렉트 read
	  @PostMapping(value = "/selectSignResUserlst")
	  public String selectSignResUserlst(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb20Svc.selectSignResUserlst(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }	 
	  
	  // 결재라인 default read
	  @PostMapping(value = "/selectSignResUserlstInit")
	  public String selectSignResUserlstInit(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb20Svc.selectSignResUserlstInit(paramMap);
	    model.addAttribute("result", result);
	    return "jsonView";
	  }	 	  
	  
	  //결재라인 부서명등 select 
	  @PostMapping(value = "/selectShareUserInfo")
	  public String selectShareUserInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb20Svc.selectShareUserInfo(paramMap);
	    model.addAttribute("resultList", resultList);
	    return "jsonView";
	  }	  
	  
		//todo 삭제    
		@DeleteMapping(value = "/deleteTodoMaster")
		public String deleteTodoMaster(@RequestBody Map<String, String> param, ModelMap model) {
	    	try {			
	    		wb20Svc.deleteTodoMaster(param);
				model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
	    	}catch(Exception e) {
		    	 model.addAttribute("resultCode", 500);
		 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	    	}	    	
			return "jsonView";
		}
		
		
		@PostMapping(value = "/selectMobileTodoSelect") 
		  public String selectMobileTodoSelect(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  		  List<Map<String, String>> result = wb20Svc.selectMobileTodoSelect(paramMap);
			  model.addAttribute("result", result); 
			  return "jsonView"; 
		  }
		
		//최종결재 완료여부
		@PostMapping(value = "/selectTodoFinalYn")
		public String selectTodoFinalYn(@RequestBody Map<String, String> paramMap, ModelMap model) {
			Map<String, String> result = wb20Svc.selectTodoFinalYn(paramMap);
		    model.addAttribute("result", result);
		    return "jsonView";
		}	 		
		  

		@PostMapping(value = "/updateApprovalCancle")
	  public String updateApprovalCancle(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    	try {
				wb20Svc.updateApprovalCancle(paramMap);
		    	model.addAttribute("resultCode", 200);
		    	model.addAttribute("resultMessage", messageUtils.getMessage("cancel"));
	    	}catch(Exception e) {
		    	 model.addAttribute("resultCode", 500);
		 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	    	}

	    	return "jsonView";		  		  
    }		  
}