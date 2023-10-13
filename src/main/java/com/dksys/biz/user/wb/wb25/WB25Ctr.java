package com.dksys.biz.user.wb.wb25;

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
import com.dksys.biz.user.wb.wb25.service.WB25Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb25")
public class WB25Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB25Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB25Svc wb25Svc;
    
	@PostMapping(value = "/selectWbsTaskEvlList") 
	public String selectWbsTaskEvlList(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		int totalCnt = wb25Svc.selectWbsTaskEvlCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
		List<Map<String, String>> fileList = wb25Svc.selectWbsTaskEvlList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }		 
	
	@PostMapping(value = "/wbsTaskEvlInsert")
    public String wbsTaskEvlInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb25Svc.wbsTaskEvlInsert(paramMap, mRequest) != 0 ) {
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
	
	
	@PostMapping(value = "/wbsTaskEvlUpdate")
    public String wbsTaskEvlUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb25Svc.wbsTaskEvlUpdate(paramMap, mRequest) != 0 ) {
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
	
	
	@PutMapping(value = "/wbsTaskEvlCloseYnConfirm")
    public String wbsTaskEvlCloseYnConfirm(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb25Svc.wbsTaskEvlCloseYnConfirm(paramMap, mRequest) != 0 ) {
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
	
	
	@PostMapping(value = "/selectWbsTaskEvlResultList") 
	public String selectWbsTaskEvlResultList(@RequestBody Map<String, String> paramMap, ModelMap model) {	
		List<Map<String, String>> resultList = wb25Svc.selectWbsTaskEvlResultList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
    }	
	
	
}