package com.dksys.biz.user.pm.pm10;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.pm.pm10.service.PM10Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm10")
public class PM10Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM10Svc pm10Svc;

	// 임팀장회의록 현황 조회
	@PostMapping(value = "/selectMnList")
	public String selectClntPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		
		List<Map<String, String>> result = pm10Svc.selectMnList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 메인 등록
    @PostMapping("/pm10_main_insert")
    public String pm10_main_insert(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			pm10Svc.pm10_main_insert(param);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
    }

	// 주제 등록
    @PostMapping("/pm10_d01_insert")
    public String pm10_d01_insert(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			pm10Svc.pm10_d01_insert(param);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
    }

	// 주제 수정
	@PostMapping(value = "/pm10_d01_update")
	public String pm10_d01_update(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
			if (pm10Svc.pm10_d01_update(param) != 0) {
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

	// 팀별내용 등록
    @PostMapping("/pm10_d03_insert")
    public String pm10_d03_insert(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			pm10Svc.pm10_d03_insert(param);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
    }

	// 팀별내용 수정
	@PostMapping(value = "/pm10_d03_update")
	public String pm10_d03_update(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
			if (pm10Svc.pm10_d03_update(param) != 0) {
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




	// 등록
    @PostMapping("/insertMn")
    public String insertMn(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
    	try {
    		pm10Svc.insertMn(param);
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	} catch(Exception e) {
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	return "jsonView";
    }


	// 임팀장회의록 Update
	@PostMapping(value = "/updateMn")
	public String updateMn(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
			if (pm10Svc.updateMn(param) != 0) {
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

	// 임팀장회의록 삭제
	@PutMapping(value = "/deleteMn")
	public String deleteMn(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
			if (pm10Svc.deleteMn(paramMap) != 0) {
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

}