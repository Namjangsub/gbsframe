package com.dksys.biz.admin.cm.cm14;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm14.service.CM14Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/cm/cm14")
public class CM14Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	CM14Svc cm14Svc;
	
    @PostMapping("/selectBoardList")
    public String selectBoardList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm14Svc.selectBoardCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> boardList = cm14Svc.selectBoardList(paramMap);
    	model.addAttribute("boardList", boardList);
    	return "jsonView";
    }
    
    @PostMapping("/selectBoardInfo")
    public String selectBoardInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = cm14Svc.selectBoardInfo(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    @PostMapping("/selectBoardPopList")
    public String selectBoardPopList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<String> result = cm14Svc.selectBoardPopList();
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    @PostMapping("/insertBoard")
    public String insertBoard(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		if (cm14Svc.insertBoard(paramMap, mRequest) != 0 ) {
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    		};
    	}catch(Exception e){
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	
    	return "jsonView";
    }
    
    @PutMapping("/updateBoard")
    public String updateBoard(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		if (cm14Svc.updateBoard(paramMap, mRequest) != 0 ) {
	    		model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("save"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    		}
    	}catch(Exception e){
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	
    	return "jsonView";
    }
    
    @PutMapping("/uploadFile")
    public String uploadFile(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		cm14Svc.uploadFile(paramMap, mRequest);
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", messageUtils.getMessage("save"));
    	}catch(Exception e){
    		model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	
    	return "jsonView";
    }

    @PutMapping(value = "/deleteBoard")
    public String deleteBoard(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  try {
  		  if (cm14Svc.deleteBoard(paramMap) != 0 ) {
  			  model.addAttribute("resultCode", 200);
  			  model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
  		  } else {
  			  model.addAttribute("resultCode", 500);
  			  model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  		  };
  	  }catch(Exception e){
  		  model.addAttribute("resultCode", 900);
  		  model.addAttribute("resultMessage", e.getMessage());
  	  }
  	  return "jsonView";
    }
}