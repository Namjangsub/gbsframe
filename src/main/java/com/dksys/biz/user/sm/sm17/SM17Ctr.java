package com.dksys.biz.user.sm.sm17;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm17.service.SM17Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm17")
public class SM17Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM17Svc sm17Svc;	

	//리스트 조회
	@PostMapping(value = "/sm17_grid1_selectList")
	public String sm17_grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm17Svc.sm17_grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm17Svc.sm17_grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	
	
	// 창고 코드 검색
@PostMapping(value = "/selectWhCd")
    public String selectWhCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, Object>> result = sm17Svc.selectWhCd(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }
}