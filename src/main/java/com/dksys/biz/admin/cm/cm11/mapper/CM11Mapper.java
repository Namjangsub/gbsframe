package com.dksys.biz.admin.cm.cm11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CM11Mapper {
	
	String selectSearchDttm();

	List<Map<String, String>> selectPrjectDashList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectClientTaxDashList(Map<String, String> paramMap);
	
	int selectClntSelpch1Count(Map<String, String> paramMap);

	List<Map<String, String>> selectClientPchsDashList(Map<String, String> paramMap);

	List<Map<String, String>> selectFacList(Map<String, String> paramMap);

	List<Map<String, String>> selectMonthlyStat(Map<String, String> paramMap);

	List<Map<String, String>> selectScheduleDelayList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectDisabilityList(Map<String, String> paramMap);
}