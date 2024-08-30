package com.dksys.biz.user.cr.cr51;

import java.sql.SQLException;
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
import com.dksys.biz.user.cr.cr51.service.CR51Svc;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/cr/cr51")
public class CR51Ctr {
	
	private Logger logger = LoggerFactory.getLogger(CR51Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CR51Svc cr51Svc;
    

    
    @PostMapping(value = "/selectPfuList") 
	public String selectPfuList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  int totalCnt = cr51Svc.selectPfuListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = cr51Svc.selectPfuList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
    
    @PostMapping(value = "/selectPFUAreaItemList") 
	public String selectPFUAreaItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr51Svc.selectPFUAreaItemList(paramMap);
		model.addAttribute("result", result);
	    	
		return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUAreaRetriveList") 
    public String selectPFUAreaRetriveList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr51Svc.selectPFUAreaRetriveList(paramMap);
    	model.addAttribute("result", result);
    	
    	return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUChangedList") 
    public String selectPFUChangedList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr51Svc.selectPFUChangedList(paramMap);
    	model.addAttribute("result", result);
    	
    	return "jsonView";
    }


    @PostMapping(value = "/selectPfuIsThereListCount") 
	public String selectPfuIsThereListCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  int totalCnt = cr51Svc.selectPfuIsThereListCount(paramMap); 
		  model.addAttribute("resultList", totalCnt);
		  return "jsonView"; 
	}
    
    
    @PostMapping(value = "/selectStdPfuClobInfo") 
    public String selectStdPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultImg = cr51Svc.selectStdPfuClobInfo(paramMap);
		model.addAttribute("resultImg", resultImg);
    	
    	return "jsonView";
    }
    
    // salesCd 정보 조회
    @PostMapping(value = "/selectSalesCdInfo")
    public String selectSalesCdInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = cr51Svc.selectSalesCdInfo(paramMap);
    	model.addAttribute("result", result);

//		String pfuCode = result.get("pfuCode");
//		paramMap.put("prdtDiv", pfuCode);
//		List<Map<String, String>> resultImg = cr51Svc.selectStdPfuClobInfo(paramMap);
//		model.addAttribute("resultImg", resultImg);
      return "jsonView";
    }


    // PFU 정보 조회
    @PostMapping(value = "/selectPfuInfo")
    public String selectPfuInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
      Map<String, String> result = cr51Svc.selectPfuInfo(paramMap);
      model.addAttribute("result", result);
      return "jsonView";
    }

    
    @PostMapping(value = "/selectPfuInfoSalesCdList") 
    public String selectPfuInfoSalesCdList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr51Svc.selectPfuInfoSalesCdList(paramMap);
    	model.addAttribute("result", result);
    	
    	return "jsonView";
    }

    // PFU 정보 조회
//    @PostMapping(value = "/selectPfuClobInfo")
//    public String selectPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
//      Map<String, String> resultClob = cr51Svc.selectPfuClobInfo(paramMap);
//      model.addAttribute("resultClob", resultClob);
//      return "jsonView";
//    }



    @PostMapping(value = "/insertPfu")
    public String insertPfu(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr51Svc.insertPfu(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultDBError", ((SQLException) e.getCause()).getSQLState());
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }

    @PostMapping(value = "/updatePfu")
    public String updatePfu(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  	  	try {
  			if (cr51Svc.updatePfu(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
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

    @PutMapping(value = "/deletePfuNo")
    public String deletePfuNo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
  	  	try {
  			if (cr51Svc.deletePfuNo(paramMap) != 0 ) {
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



	@PostMapping(value = "/copy_cr51")
	public String copy_cr51(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr51Svc.copy_cr51(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
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
	

    
    @PostMapping(value = "/selectPfuCopyTargetList") 
	public String selectPfuCopyTargetList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  int totalCnt = cr51Svc.selectPfuCopyTargetListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = cr51Svc.selectPfuCopyTargetList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
	

    
    @PostMapping(value = "/selectTagetSalesCodeList") 
	public String selectTagetSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> result = cr51Svc.selectTagetSalesCodeList(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
	}
    
    
    @PostMapping(value = "/selectPfuReferenceTargetList") 
    public String selectPfuReferenceTargetList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> resultList = cr51Svc.selectPfuReferenceTargetList(paramMap);
    	model.addAttribute("resultList", resultList); 
    	return "jsonView"; 
    }
    
    @PostMapping(value = "/selectIssueReferenceList") 
    public String selectIssueReferenceList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr51Svc.selectIssueReferenceList(paramMap);
    	model.addAttribute("result", result); 
    	return "jsonView"; 
    }
    
}	  