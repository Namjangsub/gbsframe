package com.dksys.biz.user.wb.wb24;

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
import com.dksys.biz.user.wb.wb24.service.WB24Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb24")
public class WB24Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB24Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB24Svc wb24Svc;
    
	@PostMapping(value = "/selectWbsIssueList") 
	public String selectWbsIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		//int totalCnt = wb24Svc.selectWbsIssueListCount(paramMap); 
		//PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        //model.addAttribute("paginationInfo", paginationInfo);
		  
		List<Map<String, String>> fileList = wb24Svc.selectWbsIssueList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }	
    
	@PostMapping(value = "/selectWbsIssueListDashboard") 
	public String selectWbsIssueListDashboard(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		List<Map<String, String>> result = wb24Svc.selectWbsIssueListDashboard(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
    }	
	
    @PostMapping(value = "/selectMaxWbsIssueNo") 
    public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb24Svc.selectMaxWbsIssueNo(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
    
	@PostMapping(value = "/wbsIssueInsert")
    public String wbsIssInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb24Svc.wbsIssueInsert(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/wbsIssueUpdate")
    public String wbsIssueUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb24Svc.wbsIssueUpdate(paramMap, mRequest) != 0 ) {
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
	
	@PostMapping(value = "/wbsIssCloseYnConfirm")
    public String wbsIssCloseYnConfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb24Svc.wbsIssCloseYnConfirm(paramMap, mRequest) != 0 ) {
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
	
	@PostMapping(value = "/selectTodoRsltsView") 
    public String selectTodoRsltsView(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb24Svc.selectTodoRsltsView(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
	
	@PostMapping(value = "/selectRsltsMemberList") 
	public String selectRsltsMemberList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> resultList = wb24Svc.selectRsltsMemberList(paramMap);
	    model.addAttribute("resultList", resultList); 
	    return "jsonView"; 		 
    }
	
	@PostMapping(value = "/selectMemberTelNo") 
	public String selectMemberTelNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb24Svc.selectMemberTelNo(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }

	@PostMapping(value = "/select_wb2401p01_Info")
	public String select_wb2401p01_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = wb24Svc.select_wb2401p01_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	

	
	@PutMapping(value = "/wbsIssueDelete")
    public String wbsIssueDelete(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  		try {
  			if (wb24Svc.wbsIssueDelete(paramMap) != 0 ) {
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
	
	//조치담당자의 팀장 정보 가져오기
	@PostMapping(value = "/selectTeamManagerInfo")
	public String selectTeamManagerInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = wb24Svc.selectTeamManagerInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//팀장 이슈 조치결과 결재일경우 위험성 평가 기능 추가 하기위함   남장섭 240618
	@PostMapping(value = "/updateWbsIssueResultEvaluate")
	public String updateWbsIssueResultEvaluate(@RequestBody Map<String, String> paramMap, ModelMap model){
		int result = wb24Svc.updateWbsIssueResultEvaluate(paramMap);
		if ( result != 0 ) {
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", result);				
		} else {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}

}