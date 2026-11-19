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

    @PostMapping(value = "/selectTaskFlowProjectSummaryList")
    public String selectTaskFlowProjectSummaryList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowProjectSummaryList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowStepStatus")
    public String selectTaskFlowStepStatus(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = cm10Svc.selectTaskFlowStepStatus(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowStepStatusCm1002")
    public String selectTaskFlowStepStatusCm1002(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, Object> result = cm10Svc.selectTaskFlowStepStatusCm1002(paramMap);
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

    @PostMapping(value = "/selectTaskFlowCustomerSummaryList")
    public String selectTaskFlowCustomerSummaryList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerSummaryList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowCustomerBaseList")
    public String selectTaskFlowCustomerBaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerBaseList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowCustomerSummaryBatch")
    public String selectTaskFlowCustomerSummaryBatch(@RequestBody Map<String, Object> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerSummaryBatch(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowCustomerOrderList")
    public String selectTaskFlowCustomerOrderList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerOrderList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowCustomerWbsSchedule")
    public String selectTaskFlowCustomerWbsSchedule(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerWbsSchedule(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }

    @PostMapping(value = "/selectTaskFlowCustomerWbsIssueList")
    public String selectTaskFlowCustomerWbsIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, Object>> resultList = cm10Svc.selectTaskFlowCustomerWbsIssueList(paramMap);
    	model.addAttribute("resultList", resultList);
    	return "jsonView";
    }
    
}