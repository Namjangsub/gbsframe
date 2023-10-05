package com.dksys.biz.admin.cm.cm11.service;

import java.util.List;
import java.util.Map;

public interface CM11Svc {
	
	public String selectSearchDttm();

	public List<Map<String, String>> selectPrjectDashList(Map<String, String> param);
	
	public List<Map<String, String>> selectClientTaxDashList(Map<String, String> param);
	
	public List<Map<String, String>> selectClientPchsDashList(Map<String, String> param);
	
	public List<Map<String, String>> selectFacList(Map<String, String> param);

	public List<Map<String, String>> selectMonthlyStat(Map<String, String> param);
	
	public List<Map<String, String>> selectScheduleDelayList(Map<String, String> param);
	
	public List<Map<String, String>> selectDisabilityList(Map<String, String> param);
	
}