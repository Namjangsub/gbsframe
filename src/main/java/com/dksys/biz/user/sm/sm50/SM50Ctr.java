package com.dksys.biz.user.sm.sm50;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.sm.sm50.service.SM50Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm50")
public class SM50Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    SM50Svc sm50Svc;

 // BOM 전체 정전개 트리 조회
    @PostMapping("/selectBomCostTreeList")
    public String selectBomCostTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	
    	List<Map<String, String>> result = sm50Svc.selectBomCostTreeList(paramMap);
    	model.addAttribute("result", result);
        return "jsonView";
    }
    
    // 한레벨 아래의 자식 트리 리스트 조회
    @PostMapping("/selectBomlevelList")
    public String selectBomlevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = sm50Svc.selectBomlevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllCostList")
    public String selectBomAllCostList(@RequestBody Map<String, String> paramMap, ModelMap model) {

    	List<Map<String, String>> result = sm50Svc.selectBomAllCostList(paramMap);
    	model.addAttribute("result", result);
    	Map<String, String> pcostInfo = sm50Svc.selectBomTrgtPchsPcostInfo(paramMap);
    	model.addAttribute("pcostInfo", pcostInfo);
    	return "jsonView";
    }
    
	
	
    // 엑셀업로드 점검용 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelTempList")
    public String selectBomAllLevelTempList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = sm50Svc.selectBomAllLevelTempList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
	
    
    // BOM비용 산정 프로시져 호출
    @PostMapping("/callBomTempUpd")
    public String callBomTempUpd(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = sm50Svc.callBomTempUpd(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
	
	// BOM원가의견
	@PostMapping(value = "/insertUpdateBomRemark")
	public String pm10_d01_update(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (sm50Svc.insertUpdateBomRemark(paramMap) != 0) {
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