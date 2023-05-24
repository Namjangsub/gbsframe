package com.dksys.biz.user.cr.cr01;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/cr/cr01")
public class CR01Ctr {
 
	@Autowired
	MessageUtils messageUtils;
	
    @Autowired
	CR01Svc cr01svc;


    @PostMapping("/maxEst")
    public String showEstimationForm(@RequestBody Map<String, String> param, ModelMap model) {
        String maxEstNo = cr01svc.selectMaxEstNo(param);
        model.addAttribute("maxEstNo", maxEstNo);
        return "jsonView";
    }
    
    
    @PostMapping("/selectEstList")
    public String selectEstList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = cr01svc.selectEstCount(param);
    	PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    
    	List<Map<String, Object>> estList = cr01svc.selectEstList(param);
    	model.addAttribute("estList", estList);
        return "jsonView";
    }
    
    @PostMapping(value = "/selectEstInfo")
    public String selectEstInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	  	Map<String, Object> estInfo = cr01svc.selectEstInfo(paramMap);
    	    model.addAttribute("estInfo", estInfo);
    	return "jsonView";
    }
    
    @PostMapping(value = "/insertEst")
	public String insertEst(@RequestParam Map<String, String> paramMap , MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		System.out.println(paramMap+"여기0");
			String newEstNo = cr01svc.insertEst(paramMap, mRequest);

			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			model.addAttribute("newEstNo", newEstNo);
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", e.getLocalizedMessage());
    	}
		return "jsonView";
	}
	@PostMapping(value = "/insertEstDeg")
	public String insertEstDeg(@RequestParam Map<String, String> paramMap , MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			System.out.println(paramMap+"여기0");
			String newEstNo = cr01svc.insertEstDeg(paramMap, mRequest);

			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			model.addAttribute("newEstNo", newEstNo);
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";
	}



	
	@PutMapping(value = "/updateEst")
	public String updateEst(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			cr01svc.updateEst(paramMap, mRequest);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
    	return "jsonView";
    }
	
	@DeleteMapping(value = "/deleteEst")
    public String deleteEst(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	cr01svc.deleteEst(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
    }
    
}