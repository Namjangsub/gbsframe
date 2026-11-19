package com.dksys.biz.admin.cm.cm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM10Mapper {
	
	int selectShipNCount(Map<String, String> paramMap);

	List<Map<String, String>> selectShipNList(Map<String, String> paramMap);
	
	int selectOrdrgNCount(Map<String, String> paramMap);

	List<Map<String, String>> selectOrdrgNList(Map<String, String> paramMap);

	int selectReqNCount(Map<String, String> paramMap);

	List<Map<String, String>> selectReqNList(Map<String, String> paramMap);
	
	int selectTaxNCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectTaxNList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowProjectList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowProjectSummaryList(Map<String, String> paramMap);

	Map<String, Object> selectTaskFlowStepStatus(Map<String, String> paramMap);

	Map<String, Object> selectTaskFlowStepStatusCm1002(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowWbsSchedule(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowWbsIssueList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerSummaryList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerBaseList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerSummaryBatch(Map<String, Object> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerOrderList(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerWbsSchedule(Map<String, String> paramMap);

	List<Map<String, Object>> selectTaskFlowCustomerWbsIssueList(Map<String, String> paramMap);

}