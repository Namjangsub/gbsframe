package com.dksys.biz.user.pm.pm05;

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
import com.dksys.biz.user.pm.pm05.service.PM05Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm05")
public class PM05Ctr {
	
	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM05Svc pm05Svc;	

	//리스트 조회
	@PostMapping(value = "/pm05_grid1_selectList")
	public String pm05_grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm05Svc.pm05_grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm05Svc.pm05_grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 창고 코드 검색
	@PostMapping(value = "/selectWhCd")
	public String selectWhCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, Object>> result = pm05Svc.selectWhCd(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	
	//모바일 리스트 조회
		@PostMapping(value = "/pm05_grid1_selectList_m")
		public String pm05_grid1_selectList_m(@RequestBody Map<String, String> paramMap, ModelMap model) {
			List<Map<String, String>> result = pm05Svc.pm05_grid1_selectList_m(paramMap);
			model.addAttribute("result", result);
			return "jsonView";
		}
		
		
}