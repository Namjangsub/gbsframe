package com.dksys.biz.user.bm.bm11;

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
import com.dksys.biz.user.bm.bm11.service.BM11Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm11")
public class BM11Ctr {

	private Logger logger = LoggerFactory.getLogger(BM11Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
	@Autowired
	BM11Svc bm11Svc;
	
	@PostMapping(value = "/selectBmUprMstrList")
	public String selectBmUprMstrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		
		  int totalCnt = bm11Svc.selectBmUprMstrCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = bm11Svc.selectBmUprMstrList(paramMap);
		  model.addAttribute("resultList", resultList);
		 
		
		return "jsonView";
	}

	@PostMapping(value = "/selectUprHsMstrList")
	public String selectUprHsMstrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm11Svc.selectBmUprMstrCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> hsresultList = bm11Svc.selectUprHsMstrList(paramMap);
		model.addAttribute("hsresultList", hsresultList);
		
		return "jsonView";
	}
	
	@PostMapping(value = "/insertBmUprMstr")
	public String insertBmUprMstr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			int newBmMstr = bm11Svc.insertBmUprMstr(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			model.addAttribute("newBmMstr", newBmMstr);
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
//		bm11Svc.insertBmUprMstr(paramMap, mRequest);
//    	model.addAttribute("resultCode", 200);
//    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
		
	}
	
	@PutMapping(value = "/UpdateBmUprMstr")
	public String UpdateBmUprMstr(@RequestParam Map<String, String> paramMap,  ModelMap model) {
		try {
			bm11Svc.UpdateBmUprMstr(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		} catch (Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}
	
	//<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectMsExcelList") 
	  public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = bm11Svc.selectMsExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }

}
