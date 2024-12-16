package com.dksys.biz.user.pm.pm01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM01Svc {

  int selectDailyWorkCount(Map<String, String> paramMap);

  List<Map<String, String>> selectDailyWorkList(Map<String, String> paramMap);

  Map<String, String> selectDailyWorkInfo(Map<String, String> paramMap);

  int selectConfirmCount(Map<String, String> paramMap);

  int insertDailyWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int updateDailyWork(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

  int deleteDailyWork(Map<String, String> paramMap) throws Exception;
  
  List<Map<String, String>> selectMonthlyWorkList(Map<String, String> paramMap);

	int selectWorkPrtCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWorkPrtList(Map<String, String> paramMap);

	int selectWorkOrdrsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWorkOrdrsList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectUploadFileList(Map<String, String> paramMap);

	int selectIssueWorkCount(Map<String, String> paramMap);

	List<Map<String, String>> selectIssueWorkList(Map<String, String> paramMap);

	int selectAllIssueWorkListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectAllIssueWorkList(Map<String, String> paramMap);

	List<Map<String, String>> selectDailyWorkPrductList(Map<String, String> paramMap);

	List<Map<String, String>> selectDailyWorkTripRows(Map<String, String> paramMap);

	Map<String, String> dailyWorkConstrain(Map<String, String> paramMap);

}