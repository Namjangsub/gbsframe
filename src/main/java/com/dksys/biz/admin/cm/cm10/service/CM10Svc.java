package com.dksys.biz.admin.cm.cm10.service;

import java.util.List;
import java.util.Map;

public interface CM10Svc {

	public List<Map<String, String>> selectShipNList(Map<String, String> param);
	
	public int selectShipNCount(Map<String, String> param);

	public List<Map<String, String>> selectOrdrgNList(Map<String, String> param);
	
	public int selectOrdrgNCount(Map<String, String> param);

	public int selectReqNCount(Map<String, String> param);

	public List<Map<String, String>> selectReqNList(Map<String, String> param);
	
	public int selectTaxNCount(Map<String, String> param);
	
	public List<Map<String, String>> selectTaxNList(Map<String, String> param);
	
	public List<Map<String, Object>> selectTaskFlowProjectList(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowProjectSummaryList(Map<String, String> param);

	public Map<String, Object> selectTaskFlowStepStatus(Map<String, String> param);

	public Map<String, Object> selectTaskFlowStepStatusCm1002(Map<String, String> param);
	
	public List<Map<String, Object>> selectTaskFlowWbsSchedule(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowWbsIssueList(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowCustomerSummaryList(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowCustomerBaseList(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowCustomerSummaryBatch(Map<String, Object> param);

	public List<Map<String, Object>> selectTaskFlowCustomerOrderList(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowCustomerWbsSchedule(Map<String, String> param);

	public List<Map<String, Object>> selectTaskFlowCustomerWbsIssueList(Map<String, String> param);
	
}