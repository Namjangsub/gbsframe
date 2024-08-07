package com.dksys.biz.user.wb.wb04;

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
import com.dksys.biz.user.wb.wb04.service.WB04Svc;


@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb04")
public class WB04Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB04Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB04Svc wb04Svc;
    
    @PostMapping(value = "/selectWbsLeftSalesCodeTreeList") 
	public String selectWbsLeftSalesCodeTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectWbsLeftSalesCodeTreeList(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }
    
    
    @PostMapping(value = "/selectWbsPlanTreeList") 
	public String selectWbsPlanTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectWbsPlanTreeList(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }
    
	@PostMapping(value = "/selectGanttList") 
	public String selectGanttList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttList(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }
	
    @PutMapping(value = "/updateWbsPlanDate")
    public String updateWbsPlanDate(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb04Svc.updateWbsPlanDate(paramMap) != 0 ) {
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
	  
    @PostMapping(value = "/selectGanttAllList") 
	public String selectGanttAllList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttAllList(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }  
	  
    @PostMapping(value = "/selectGanttInfo") 
	public String selectGanttInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttInfo(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }    
	  
    @PostMapping(value = "/selectGanttAllList2") 
	public String selectGanttAllList2(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttAllList2(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
	  
    }    
	  
    @PostMapping(value = "/selectGanttAllList3") 
	public String selectGanttAllList3(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttAllList3(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
    }

	@PostMapping(value = "/selectGanttAllList4") 
	public String selectGanttAllList4(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> fileList = wb04Svc.selectGanttAllList4(paramMap); 
	  model.addAttribute("fileList", fileList); 
	  return "jsonView";
    }
}