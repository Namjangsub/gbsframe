package com.dksys.biz.admin.cm.cm10.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dksys.biz.admin.cm.cm10.mapper.CM10Mapper;
import com.dksys.biz.admin.cm.cm10.service.CM10Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class CM10SvcImpl implements CM10Svc {
	
    @Autowired
    CM10Mapper cm10Mapper;
    
	@Override
	public int selectShipNCount(Map<String, String> paramMap) {
		return cm10Mapper.selectShipNCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectShipNList(Map<String, String> paramMap) {
		return cm10Mapper.selectShipNList(paramMap);
	}
    
	@Override
	public int selectOrdrgNCount(Map<String, String> paramMap) {
		return cm10Mapper.selectOrdrgNCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectOrdrgNList(Map<String, String> paramMap) {
		return cm10Mapper.selectOrdrgNList(paramMap);
	}

	@Override
	public int selectReqNCount(Map<String, String> paramMap) {
		// TODO Auto-generated method stub
		return cm10Mapper.selectReqNCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectReqNList(Map<String, String> paramMap) {
		return cm10Mapper.selectReqNList(paramMap);
	}
	
	@Override
	public int selectTaxNCount(Map<String, String> paramMap) {
		return cm10Mapper.selectTaxNCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectTaxNList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaxNList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowProjectList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowProjectList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowProjectSummaryList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowProjectSummaryList(paramMap);
	}

	@Override
	public Map<String, Object> selectTaskFlowStepStatus(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowStepStatus(paramMap);
	}

	@Override
	public Map<String, Object> selectTaskFlowStepStatusCm1002(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowStepStatusCm1002(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowWbsSchedule(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowWbsSchedule(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowWbsIssueList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowWbsIssueList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerSummaryList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowCustomerSummaryList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerBaseList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowCustomerBaseList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerSummaryBatch(Map<String, Object> paramMap) {
		if (paramMap == null) {
			return java.util.Collections.emptyList();
		}
		Object rawList = paramMap.get("clntCdList");
		if (!(rawList instanceof List) || ((List<?>) rawList).isEmpty()) {
			return java.util.Collections.emptyList();
		}
		return cm10Mapper.selectTaskFlowCustomerSummaryBatch(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerOrderList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowCustomerOrderList(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerWbsSchedule(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowCustomerWbsSchedule(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectTaskFlowCustomerWbsIssueList(Map<String, String> paramMap) {
		return cm10Mapper.selectTaskFlowCustomerWbsIssueList(paramMap);
	}
}