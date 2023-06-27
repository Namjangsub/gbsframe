package com.dksys.biz.user.wb.wb04;

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
import com.dksys.biz.user.wb.wb04.service.WB04Svc;


@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb04")
public class WB04Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB04Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB04Svc wb04Svc;
    
    //<!-- 관리번호(WBS계획번호) 조회 조건 팝업 리스트  -->
	@PostMapping(value = "/selectWbsPlanNoList") 
	public String selectWbsPlanNoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> codeInfoList = wb04Svc.selectWbsPlanNoList(paramMap); 
	  model.addAttribute("codeInfoList", codeInfoList); 
	  return "jsonView";
	  
    }
	  
	//<!-- 수주번호 조회 조건 팝업 리스트  -->
	@PostMapping(value = "/selectWbsSalesCodeList") 
	public String selectWbsSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> codeInfoList = wb04Svc.selectWbsSalesCodeList(paramMap); 
	  model.addAttribute("codeInfoList",
	  codeInfoList); 
	  return "jsonView";  
	}
	  
    //<!-- 일정 미마감 조회 리스트   --> 
	@PostMapping(value = "/selectWbsPlanCloseYnNTreeList") 
	public String selectWbsPlanCloseYnNTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
	  int totalCnt = wb04Svc.selectWbsPlanCloseYnNTreeListCount(paramMap);
      PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
  	  model.addAttribute("paginationInfo", paginationInfo);
  	  List<Map<String, String>> fileList = wb04Svc.selectWbsPlanCloseYnNTreeList(paramMap);
  	  model.addAttribute("fileList", fileList);
      return "jsonView";
	}  
	  
	@PostMapping(value = "/selectWbsPlanCloseYnNTreeListSub") 
	public String selectWbsPlanCloseYnNTreeListSub(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
  	  List<Map<String, String>> fileList = wb04Svc.selectWbsPlanCloseYnNTreeListSub(paramMap);
  	  model.addAttribute("fileList", fileList);
      return "jsonView";
	}   
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
}