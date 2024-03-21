package com.dksys.biz.user.bm.bm18;

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
import com.dksys.biz.user.bm.bm18.service.BM18Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/bm/bm18")
public class BM18Ctr {
	
	private Logger logger = LoggerFactory.getLogger(BM18Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    BM18Svc bm18Svc;

	//알림톡수신 조회 리스트
    @PostMapping(value = "/selectReceptionMessageList") 
    public String selectApplovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = bm18Svc.selectReceptionMessageListCount(paramMap); 
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		  
  		List<Map<String, String>> resultList = bm18Svc.selectReceptionMessageList(paramMap);
		model.addAttribute("resultList", resultList); 
		return "jsonView"; 
    }
 
	//알림톡 MESSAGE ID 
	@PostMapping(value = "/selectMaxMessageId")
	public String selectMaxMessageId(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = bm18Svc.selectMaxMessageId(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	//알림톡 MESSAGE ID 
	@PostMapping(value = "/selectMaxMessageIdTodo")
	public String selectMaxMessageTodo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = bm18Svc.selectMaxMessageIdTodo(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	//알림톡 MESSAGE ID 
	@PostMapping(value = "/selectMaxMessageIdTodoNew")
	public String selectMaxMessageIdTodoNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = bm18Svc.selectMaxMessageIdTodoNew(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	//알림톡 MESSAGE ID 
	@PostMapping(value = "/selectMaxMessageIdTodo_kakao")
	public String selectMaxMessageIdTodo_kakao(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = bm18Svc.selectMaxMessageIdTodo_kakao(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
    //알림톡 로그 등록
    @PostMapping(value = "/insertKakaoMessage")
	public String insertKakaoMessage(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		bm18Svc.insertKakaoMessage(paramMap);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	}catch(Exception e) {
	    	 model.addAttribute("resultCode", 500);
	 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    	}
    	return "jsonView";		  		  
    }	
    
    
    //알림톡 MESSAGE ID 
  	@PostMapping(value = "/selectPmMaxMessageIdTodo")
  	public String selectPmMaxMessageIdTodo(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		List<Map<String, String>> resultList = bm18Svc.selectPmMaxMessageIdTodo(paramMap);
  		model.addAttribute("resultList", resultList);
  		return "jsonView";
  	}
  	
  	
  	
    //알림톡 MESSAGE ID 
  	@PostMapping(value = "/selectWbsPlanMaxMessageIdTodo")
  	public String selectWbsPlanMaxMessageIdTodo(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		List<Map<String, String>> resultList = bm18Svc.selectWbsPlanMaxMessageIdTodo(paramMap);
  		model.addAttribute("resultList", resultList);
  		return "jsonView";
  	}
  	
  	
    //변경 시 알림톡 MESSAGE ID 
  	@PostMapping(value = "/selectWbsPlanUpdateMaxMessageIdTodo")
  	public String selectWbsPlanUpdateMaxMessageIdTodo(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		List<Map<String, String>> resultList = bm18Svc.selectWbsPlanUpdateMaxMessageIdTodo(paramMap);
  		model.addAttribute("resultList", resultList);
  		return "jsonView";
  	}
  	
  	
  	
  	
}