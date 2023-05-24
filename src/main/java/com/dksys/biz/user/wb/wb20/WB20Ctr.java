package com.dksys.biz.user.wb.wb20;

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
import com.dksys.biz.user.wb.wb20.service.WB20Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb20")
public class WB20Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB20Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB20Svc wb20Svc;

    //<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectToDoList") 
	  public String selectToDoList(@RequestBody Map<String, String> paramMap, ModelMap model) {

		  int totalCnt = wb20Svc.selectToDoCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb20Svc.selectToDoList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	 
}