package com.dksys.biz.user.cr.cr50;

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
import com.dksys.biz.user.cr.cr50.service.CR50Svc;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/cr/cr50")
public class CR50Ctr {
	
	private Logger logger = LoggerFactory.getLogger(CR50Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CR50Svc cr50Svc;
    

    
    @PostMapping(value = "/selectPfuList") 
	public String selectPfuList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  int totalCnt = cr50Svc.selectPfuListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
		  List<Map<String, String>> resultList = cr50Svc.selectPfuList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	}
    
    @PostMapping(value = "/selectPFUAreaItemList") 
	public String selectPFUAreaItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = cr50Svc.selectPFUAreaItemList(paramMap);
		model.addAttribute("result", result);
	    	
		return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUAreaRetriveList") 
    public String selectPFUAreaRetriveList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr50Svc.selectPFUAreaRetriveList(paramMap);
    	model.addAttribute("result", result);
    	
    	return "jsonView";
    }
    
    @PostMapping(value = "/selectPFUChangedList") 
    public String selectPFUChangedList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> result = cr50Svc.selectPFUChangedList(paramMap);
    	model.addAttribute("result", result);
    	
    	return "jsonView";
    }

    
    @PostMapping(value = "/selectStdPfuClobInfo") 
    public String selectStdPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultImg = cr50Svc.selectStdPfuClobInfo(paramMap);
		model.addAttribute("resultImg", resultImg);
    	
    	return "jsonView";
    }
    
    // salesCd 정보 조회
    @PostMapping(value = "/selectSalesCdInfo")
    public String selectSalesCdInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = cr50Svc.selectSalesCdInfo(paramMap);
    	model.addAttribute("result", result);

//		String pfuCode = result.get("pfuCode");
//		paramMap.put("prdtDiv", pfuCode);
//		List<Map<String, String>> resultImg = cr50Svc.selectStdPfuClobInfo(paramMap);
//		model.addAttribute("resultImg", resultImg);
      return "jsonView";
    }


    // PFU 정보 조회
    @PostMapping(value = "/selectPfuInfo")
    public String selectPfuInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
      Map<String, String> result = cr50Svc.selectPfuInfo(paramMap);
      model.addAttribute("result", result);
      return "jsonView";
    }


    // PFU 정보 조회
//    @PostMapping(value = "/selectPfuClobInfo")
//    public String selectPfuClobInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
//      Map<String, String> resultClob = cr50Svc.selectPfuClobInfo(paramMap);
//      model.addAttribute("resultClob", resultClob);
//      return "jsonView";
//    }



    @PostMapping(value = "/insertPfu")
    public String insertPfu(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (cr50Svc.insertPfu(paramMap, mRequest) != 0 ) {
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
  			if (cr50Svc.updatePfu(paramMap, mRequest) != 0 ) {
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
  			if (cr50Svc.deletePfuNo(paramMap) != 0 ) {
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