package com.dksys.biz.user.zz.zz01;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.zz.zz01.service.ZZ01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/zz/zz01")
public class ZZ01Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
    @Autowired
    ZZ01Svc zz01Svc;
    
    // BOM 전체 정전개 트리 조회
    @PostMapping("/selectBomTreeList")
    public String selectBomTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = zz01Svc.selectBomTreeList(paramMap);
    	model.addAttribute("result", result);
        return "jsonView";
    }
    
    // 한레벨 아래의 자식 트리 리스트 조회
    @PostMapping("/selectBomlevelList")
    public String selectBomlevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = zz01Svc.selectBomlevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelList")
    public String selectBomAllLevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = zz01Svc.selectBomAllLevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // 부서정보 조회
    @PostMapping("/selectBomTreInfo")
    public String selectBomTreInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> deptInfo = zz01Svc.selectBomTreInfo(paramMap);
    	model.addAttribute("deptInfo", deptInfo);
    	return "jsonView";
    }
    
    // 부서아이디 중복확인
    @PostMapping("/checkBomId")
    public String checkBomId(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int deptCount = zz01Svc.checkBomId(paramMap);
    	model.addAttribute("deptCount", deptCount);
        return "jsonView";
    }
    
    @PostMapping(value = "/insertBomTree")
    public String insertBomTree(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  	  try {
  		  if (zz01Svc.insertBomTree(paramMap, mRequest) != 0 ) {
  			  model.addAttribute("resultCode", 200);
  			  model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
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
    
    // BOM이동
    @PostMapping("/moveBom")
    public String moveBom(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (zz01Svc.moveBom(paramList) != 0 ) {
	    		model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("move"));
    		} else {
    			  model.addAttribute("resultCode", 500);
      			  model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		  model.addAttribute("resultCode", 900);
      		  model.addAttribute("resultMessage", e.getMessage());
    	}
        return "jsonView";
    }
    // BOM 삭제
    @PostMapping("/deleteBom")
    public String deleteBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		if (zz01Svc.deleteBom(paramMap) != 0 ) {
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
    // BOM 복사
    @PostMapping("/copyBomTree")
    public String copyBomTree(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (zz01Svc.copyBomTree(paramList) != 0 ) {
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", messageUtils.getMessage("copy"));
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
}