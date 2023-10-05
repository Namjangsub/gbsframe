package com.dksys.biz.user.wb.wb22;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.wb.wb22.service.WB22Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb22")
public class WB22Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB22Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB22Svc wb22Svc;
    
	@PostMapping(value = "/selectWbsSjList") 
	public String selectWbsSjList(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		int totalCnt = wb22Svc.selectWbsSjListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
		List<Map<String, String>> fileList = wb22Svc.selectWbsSjList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }	
	
	@PostMapping(value = "/selectSjInfo") 
   	public String selectSjInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
   		  Map<String, String> resultList = wb22Svc.selectSjInfo(paramMap);
   		  model.addAttribute("resultList", resultList); 
   		  return "jsonView"; 
   	}
	
	@PostMapping(value = "/selectWBS1Level") 
	public String selectWBS1Level(@RequestBody Map<String, String> paramMap, ModelMap model) {		
		List<Map<String, String>> fileList = wb22Svc.selectWBS1Level(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }	
	
	@PostMapping(value = "/wbsLevel1Insert")
    public String wbsLevel1Insert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsLevel1Insert(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/wbsLevel1Update")
    public String wbsLevel1Update(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsLevel1Update(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/selectWBS2Level") 
	public String selectWBS2Level(@RequestBody Map<String, String> paramMap, ModelMap model) {		
		List<Map<String, String>> fileList = wb22Svc.selectWBS2Level(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }	
	
	@PostMapping(value = "/wbsLevel2Insert")
    public String wbsLevel2Insert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsLevel2Insert(paramMap, mRequest) != 0 ) {
  			model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/selectVerNoNext") 
    public String selectSjVerNoNext(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb22Svc.selectVerNoNext(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
	
	@PostMapping(value = "/wbsVerUpInsert")
    public String sjVerUpInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsVerUpInsert(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/wbsLevel1confirm")
    public String wbsLevel1confirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsLevel1confirm(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/wbsLevel2confirm")
    public String wbsLevel2confirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsLevel2confirm(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/selectRsltsSharngList") 
    public String selectRsltsSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {	  
	  List<Map<String, String>> resultList = wb22Svc.selectRsltsSharngList(paramMap);
	  model.addAttribute("resultList", resultList); 
	  return "jsonView"; 		 
    }
	
	@PostMapping(value = "/selectRsltsApprovalList") 
    public String selectRsltsApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {	  
	  List<Map<String, String>> resultList = wb22Svc.selectRsltsApprovalList(paramMap);
	  model.addAttribute("resultList", resultList); 
	  return "jsonView"; 		 
    }
	
	@PostMapping(value = "/wbsRsltsInsert")
    public String wbsRsltsInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {		
		try {
			if (wb22Svc.wbsRsltsInsert(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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

	@PostMapping(value = "/wbsRsltsUpdate")
    public String wbsRsltsUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {		
		try {
			if (wb22Svc.wbsRsltsUpdate(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	
	@PostMapping(value = "/wbsRsltsconfirm")
    public String wbsRsltsconfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb22Svc.wbsRsltsconfirm(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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
	  List<Map<String, String>> resultList = wb22Svc.selectTodoRsltsView(paramMap);
	  model.addAttribute("resultList", resultList); 
	  return "jsonView"; 		 
    }
	  
	  
	//TODO 미완료 현황 대쉬보드 오른쪽 하단 WBS 계획정보
	@PostMapping(value = "/selectIncompleteJob") 
	public String selectIncompleteJob(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = wb22Svc.selectIncompleteJob(paramMap);
		model.addAttribute("resultList", resultList); 
		return "jsonView"; 
	}
}