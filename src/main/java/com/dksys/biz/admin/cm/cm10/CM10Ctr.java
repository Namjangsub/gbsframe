package com.dksys.biz.admin.cm.cm10;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm10.service.CM10Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cm/cm10")
public class CM10Ctr {
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CM10Svc cm10Svc;
	
    @PostMapping(value = "/selectShipNList")
	public String selectShipNList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm10Svc.selectShipNCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> resultList = cm10Svc.selectShipNList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
	}
	
    @PostMapping(value = "/selectOrdrgNList")
	public String selectOrdrgNList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm10Svc.selectOrdrgNCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> resultList = cm10Svc.selectOrdrgNList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
	}
 
    @PostMapping(value = "/selectReqNList")
	public String selectReqNList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm10Svc.selectReqNCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> resultList = cm10Svc.selectReqNList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
	}
    
    @PostMapping(value = "/selectTaxNList")
    public String selectTaxNList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm10Svc.selectTaxNCount(paramMap);
    	PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> resultList = cm10Svc.selectTaxNList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }
    
    @PostMapping(value = "/selectTaskFlowProjectList")
    public String selectTaskFlowProjectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowProjectList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowStepStatus")
    public String selectTaskFlowStepStatus(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = cm10Svc.selectTaskFlowStepStatus(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowWbsSchedule")
    public String selectTaskFlowWbsSchedule(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowWbsSchedule(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowWbsIssueList")
    public String selectTaskFlowWbsIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowWbsIssueList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }
    
}