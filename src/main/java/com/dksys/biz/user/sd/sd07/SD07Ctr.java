package com.dksys.biz.user.sd.sd07;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.sd.sd07.service.SD07Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sd/sd07")

public class SD07Ctr {
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    SD07Svc sd07Svc;
    
    @PostMapping(value = "/selectClose")
    public String selectClose(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	return "jsonView";
	}
	
    @PostMapping(value = "/selectCloseYmList")
	public String selectCloseYmList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> resultList = sd07Svc.selectCloseYmList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
	}
    
    @PutMapping(value = "/saveClose")
	public String saveClose(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		sd07Svc.saveClose(paramMap);
        	model.addAttribute("resultCode", 200);
        	model.addAttribute("resultMessage", messageUtils.getMessage("save"));
    	}catch (Exception e){
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	return "jsonView";
	}

    @PostMapping(value = "/selectCloseLastYm")
	public String selectCloseLastYm(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> resultList = sd07Svc.selectCloseLastYm(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
	}
    
}