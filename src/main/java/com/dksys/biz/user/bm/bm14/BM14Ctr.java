package com.dksys.biz.user.bm.bm14;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.bm.bm14.service.BM14Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm14")
public class BM14Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    BM14Svc bm14svc;

 // BOM 전체 정전개 트리 조회
    @PostMapping("/selectBomTreeList")
    public String selectBomTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomTreeList(paramMap);
    	model.addAttribute("result", result);
        return "jsonView";
    }
    
    // 한레벨 아래의 자식 트리 리스트 조회
    @PostMapping("/selectBomlevelList")
    public String selectBomlevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomlevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelList")
    public String selectBomAllLevelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomAllLevelList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
    
    // BOM 조회
    @PostMapping("/selectBomTreInfo")
    public String selectBomTreInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> deptInfo = bm14svc.selectBomTreInfo(paramMap);
    	model.addAttribute("deptInfo", deptInfo);
    	return "jsonView";
    }
    
    // BOM 중복확인
    @PostMapping("/checkBomId")
    public String checkBomId(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int deptCount = bm14svc.checkBomId(paramMap);
    	model.addAttribute("deptCount", deptCount);
        return "jsonView";
    }
    
    @PostMapping(value = "/insertBomTree")
    public String insertBomTree(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  	  try {
  		  if (bm14svc.insertBomTree(paramMap, mRequest) != 0 ) {
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
    		if (bm14svc.moveBom(paramList) != 0 ) {
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
    		if (bm14svc.deleteBom(paramMap) != 0 ) {
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
    // BOM Node 복사
    @PostMapping("/copyBomTree")
    public String copyBomTree(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    	try {
    		if (bm14svc.copyBomTree(paramList) != 0 ) {
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
    
    // BOM Sales Code 단위 복사
    @PostMapping("/copyBomRootSalesCdTree")
    public String copyBomRootSalesCdTree(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	try {
    		if (bm14svc.copyBomRootSalesCdTree(paramMap) != 0 ) {
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
    
	@PostMapping("/checkBomInfo")
    public String checkBomInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
		    int resultCount = bm14svc.checkBomInfo(paramMap);
		    model.addAttribute("resultCount", resultCount);
		    model.addAttribute("resultCode", 200);
		    model.addAttribute("resultMessage", messageUtils.getMessage("check"));
		} catch (Exception e) {
			model.addAttribute("resultCount", 0);
		    model.addAttribute("resultCode", 900);
		    model.addAttribute("resultMessage", e.getMessage());
		}
        return "jsonView";
    }
	
    // 엑셀업로드 점검용 자식 트리 전체 리스트 조회
    @PostMapping("/selectBomAllLevelTempList")
    public String selectBomAllLevelTempList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = bm14svc.selectBomAllLevelTempList(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }
}