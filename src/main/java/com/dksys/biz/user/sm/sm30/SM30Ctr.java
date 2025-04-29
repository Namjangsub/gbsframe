package com.dksys.biz.user.sm.sm30;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.sm.sm30.service.SM30Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm30")
public class SM30Ctr {

	@Autowired
    MessageUtils messageUtils;

	@Autowired
    SM30Svc sm30Svc;

	// PJT 정보 조회
	@PostMapping(value = "/selectPjtInfo")
	public String selectPjtInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm30Svc.selectPjtInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// PJT별 대금 입금/지급 정보 상세 조회
	@PostMapping(value = "/selectPjtInfoDetail")
	public String selectPjtInfoDetail(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> result = sm30Svc.selectPjtInfoDetail(paramMap);
	  model.addAttribute("result", result);
	  return "jsonView";
	}

	// 메입 대금 지급 실적 엑셀업로드 전 Check
	@PostMapping("/select_sm21_chk")
	public String select_sm21_chk(@RequestBody Map<String, Object> paramMap,ModelMap model) {
		try {
			List<Map<String, String>> result = sm30Svc.select_sm21_chk(paramMap);
			if (!result.isEmpty()) {
				model.addAttribute("result", result);
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", "적용 되었습니다.");
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail")); 
			}
		} catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}

		return "jsonView";
	}

	// 매입 대금 지급 실적 엑셀업로드
	@PostMapping("/insert_sm21_excelUpload")
    public String insert_sm21_excelUpload(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		int excelData = sm30Svc.insert_sm21_excelUpload(paramMap);
    		if (excelData > 0) {
    			model.addAttribute("rtnStr", excelData);
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", "적용 되었습니다.");
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
	
	// 
	@PostMapping(value = "/insert_sm30")
    public String insert_sm30(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if(sm30Svc.insert_sm30(paramMap, mRequest) !=0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
                model.addAttribute("fileTrgtKey", paramMap.get("fileTrgtKey"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }        
        return "jsonView";
    }
}
