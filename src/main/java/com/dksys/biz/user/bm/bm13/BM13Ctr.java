package com.dksys.biz.user.bm.bm13;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.exc.LogicException;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.user.bm.bm13.service.BM13Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm13")
public class BM13Ctr {
	
	private Logger logger = LoggerFactory.getLogger(BM13Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    BM13Svc bm13Svc;

    //<!-- WBS 결재선관리 메인 화면 조회 리스트  -->
    @PostMapping(value = "/selectApprovalList") 
    public String selectApplovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
    	int totalCnt = bm13Svc.selectApprovalListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
  		List<Map<String, String>> resultList = bm13Svc.selectApprovalList(paramMap);
		model.addAttribute("resultList", resultList); 
		return "jsonView"; 
    }
	  
	//<!-- WBS 결재선관리 엑셀 리스트  -->
    @PostMapping(value = "/selectApprovalExcelList") 
	public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = bm13Svc.selectApprovalExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }    

	  
}