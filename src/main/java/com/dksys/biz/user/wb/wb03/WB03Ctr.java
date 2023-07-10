package com.dksys.biz.user.wb.wb03;

import java.util.HashMap;
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

import com.dksys.biz.user.wb.wb03.service.WB03Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/wb/wb03")
public class WB03Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  WB03Svc wb03Svc;

    //Paging 조회
	@PostMapping(value = "/selectWbsPlanTreeIssueList")
	public String selectWbsPlanTreeIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> fileList = wb03Svc.selectWbsPlanTreeIssueList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
	}
	
    @PostMapping(value = "/selectMaxWbsIssueNo") 
    public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb03Svc.selectMaxWbsIssueNo(paramMap);
	    model.addAttribute("result", result); 
	    return "jsonView"; 		 
    }
	   
	   
}