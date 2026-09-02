package com.dksys.biz.user.pm.pm08.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM08Svc {

	int selectSubstituteWorkCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSubstituteWorkList(Map<String, String> paramMap);

	Map<String, Object> selectSubstituteWorkDtl(Map<String, String> paramMap);

	Map<String, String> insertSubstituteWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	Map<String, String> updateSubstituteWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	Map<String, String> deleteSubstituteWork(Map<String, String> paramMap) throws Exception;

	Map<String, String> deleteSubstituteWorkResult(Map<String, String> paramMap) throws Exception;

	int applySubstituteWorkApproved(Map<String, String> paramMap);

	int applySubstituteWorkResultApproved(Map<String, String> paramMap);

	int selectApprovalCount(Map<String, String> paramMap);

	List<Map<String, String>> selectSubstituteVacationStatusList(Map<String, String> paramMap);

}
