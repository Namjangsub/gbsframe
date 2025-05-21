package com.dksys.biz.user.pm.pm10.service;

import java.util.List;
import java.util.Map;

public interface PM10Svc {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap);

	int pm10_main_update(Map<String, String> param) throws Exception;

	int pm10_d01_update(Map<String, String> param) throws Exception;

	int pm10_d03_update(Map<String, String> param) throws Exception;

	int deleteMn(Map<String, String> paramMap) throws Exception;

}