package com.dksys.biz.user.pm.pm50;

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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.pm.pm50.service.PM50Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm50")
public class PM50Ctr {
	
	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM50Svc pm50Svc;

	// 출장자 사진파일 리스트 조회
	@PostMapping(value = "/select_pm50_List")
    public String select_pm50_List(@RequestBody Map<String, String> paramMap, ModelMap model) {

        int totalCnt = pm50Svc.select_pm50_ListCount(paramMap); 
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> result = pm50Svc.select_pm50_List(paramMap);
        model.addAttribute("result", result);
        
        return "jsonView";  
    }

	// 출장자 사진파일 작업정보
	@PostMapping("/select_pm50_Info")
	public String select_pm50_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = pm50Svc.select_pm50_Info(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }

	// 파일업로드
	@PostMapping("/upLoadFiles")
	public String upLoadFiles(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm50Svc.upLoadFiles(paramMap, mRequest) >= 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 출장자 사진파일 리스트 삭제
	@PostMapping("/delete_pm50")
	public String delete_pm50(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int deleted = pm50Svc.delete_pm50(paramMap);
		model.addAttribute("deletedCount", deleted);
		return "jsonView";
	}
}