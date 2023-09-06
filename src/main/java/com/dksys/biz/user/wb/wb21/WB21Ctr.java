package com.dksys.biz.user.wb.wb21;

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
import com.dksys.biz.user.wb.wb21.service.WB21Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb21")
public class WB21Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB21Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB21Svc wb21Svc;

    @PostMapping(value = "/selectMaxSjNo") 
    public String selectMaxSjNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb21Svc.selectMaxSjNo(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
    
    @PostMapping(value = "/selectCodeList") 
	public String selectCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb21Svc.selectCodeList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
    
    @PostMapping(value = "/selectSjList") 
	public String selectSjList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  int totalCnt = wb21Svc.selectSjListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = wb21Svc.selectSjList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
    
    @PostMapping(value = "/sjInsert")
    public String sjInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			List<Map<String, String>> sharngChk = wb21Svc.deleteSjListChk(paramMap);
  			if (sharngChk.size() > 0) {
  				wb21Svc.deleteSjList(paramMap);	
  			}
  			if (wb21Svc.sjInsert(paramMap, mRequest) != 0 ) {
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
    
    
    @PostMapping(value = "/sjUpdate")
    public String sjUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			List<Map<String, String>> sharngChk = wb21Svc.deleteSjListChk(paramMap);
  			if (sharngChk.size() > 0) {
  				wb21Svc.deleteSjList(paramMap);	
  			}
  			if (wb21Svc.sjUpdate(paramMap, mRequest) != 0 ) {
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
    
    
    @PostMapping(value = "/selectSjDtlList") 
	public String selectSjDtlList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb21Svc.selectSjDtlList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
    
    @PostMapping(value = "/selectSjInfo") 
   	public String selectSjInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
   		  Map<String, String> resultList = wb21Svc.selectSjInfo(paramMap);
   		  model.addAttribute("resultList", resultList); 
   		  return "jsonView"; 
   	}
    
    @PutMapping(value = "/sjConfirmY")
    public String sjConfirmY(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb21Svc.sjConfirmY(paramMap, mRequest) != 0 ) {
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
    
    @PutMapping(value = "/sjConfirmN")
    public String sjConfirmN(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb21Svc.sjConfirmN(paramMap, mRequest) != 0 ) {
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
}