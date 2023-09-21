package com.dksys.biz.admin.bm.bm99;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.bm.bm99.service.BM99Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/bm/bm99")
public class BM99Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	BM99Svc bm99Svc;
	
    // 메뉴얼 조회
    @PostMapping("/selectManualInfo")
    public String selectManualInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> info = bm99Svc.selectManualInfo(paramMap);
    	model.addAttribute("info", info);
        return "jsonView";
    }

    // 거래처 수정
	@PutMapping("/updateManualInfo")
    public String updateManualInfo(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
    		bm99Svc.updateManualInfo(paramMap, mRequest);
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	}catch(Exception e){
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	return "jsonView";
    }
	
}