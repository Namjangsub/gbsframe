package com.dksys.biz.user.bm.bm17;

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
import com.dksys.biz.user.bm.bm17.service.BM17Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm17")
public class BM17Ctr {
	
	private Logger logger = LoggerFactory.getLogger(BM17Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    BM17Svc bm17Svc;

	//기준관리 결재선관리 메인 화면 조회 리스트  -->
    @PostMapping(value = "/selectMessageTemplList") 
    public String selectApplovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = bm17Svc.selectMessageTemplListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
  		List<Map<String, String>> resultList = bm17Svc.selectMessageTemplList(paramMap);
		model.addAttribute("resultList", resultList); 
		return "jsonView"; 
    }
	  
	//기준관리 결재선관리 엑셀 리스트  -->
    @PostMapping(value = "/selectMessageTemplExcelList") 
	public String selectMsExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = bm17Svc.selectMessageTemplExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }    

	//알림톡 등록
	//공통 결재승인(등록)
	@PostMapping(value = "/insertMessageTempl")
	public String insertApprovalLine(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    	try {
		    	int rtnInt = bm17Svc.selectMessageKey(paramMap);
		    	
		    	if(rtnInt == 0) {
			    	bm17Svc.insertMessageTempl(paramMap);
			    	model.addAttribute("resultCode", 200);
			    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		    	}else {
			    	model.addAttribute("resultCode", 900);
			    	model.addAttribute("resultMessage", "기존 템플릿이 저장되어있습니다.");
		    	}
	    	
	    	}catch(Exception e) {
		    	 model.addAttribute("resultCode", 500);
		 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	    	}

	    	return "jsonView";		  		  
    }

	//알림톡 수정    
    @PostMapping(value = "/updateMessageTempl")
    public String updateMessageTempl(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
    	try {
    		bm17Svc.updateMessageTempl(paramMap);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	}catch(Exception e) {
	    	 model.addAttribute("resultCode", 500);
	 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
  	  	return "jsonView";
    } 
    
	//알림톡 삭제    
    @PostMapping(value = "/deleteMessageTempl")
    public String deleteMessageTempl(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
    	try {
    		bm17Svc.deleteMessageTempl(paramMap);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	}catch(Exception e) {
	    	 model.addAttribute("resultCode", 500);
	 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}

  	  	return "jsonView";
    }   
        
	//조회 정보  -->
    @PostMapping(value = "/selectMessageTemplInfo") 
    public String selectMessageTemplInfo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {

  		Map<String, Object> resultInfo = bm17Svc.selectMessageTemplInfo(paramMap);
		model.addAttribute("resultInfo", resultInfo); 
		return "jsonView"; 
    }
    
    @PostMapping(value = "/selectTemplInfo") 
    public String selectTemplInfo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {

  		Map<String, Object> resultInfo = bm17Svc.selectTemplInfo(paramMap);
		model.addAttribute("resultInfo", resultInfo); 
		return "jsonView"; 
    }
}