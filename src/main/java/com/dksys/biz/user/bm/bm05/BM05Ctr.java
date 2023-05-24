package com.dksys.biz.user.bm.bm05;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.user.bm.bm05.service.BM05Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm05")
public class BM05Ctr {

	private Logger logger = LoggerFactory.getLogger(BM05Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
	@Autowired
	BM05Svc bm05Svc;
	
	@PostMapping(value = "/selectBmMstrList")
	public String selectBmMstrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm05Svc.selectBmMstrCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = bm05Svc.selectBmMstrList(paramMap);
		model.addAttribute("resultList", resultList);
		
		return "jsonView";
	}
	
	@PostMapping(value = "/insertBmMstr")
	public String insertBmMstr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			String newBmMstr = bm05Svc.insertBmMstr(paramMap, mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			model.addAttribute("newBmMstr", newBmMstr);
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";
	}
	
	@PutMapping(value = "/updateBmMstr")
	public String updateBmMstr(@RequestBody Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			bm05Svc.updateBmMstr(paramMap, mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}

}
