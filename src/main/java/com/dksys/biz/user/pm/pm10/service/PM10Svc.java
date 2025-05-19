package com.dksys.biz.user.pm.pm10.service;

import java.util.List;
import java.util.Map;

public interface PM10Svc {

	List<Map<String, String>> selectMnList(Map<String, String> paramMap);

	int updateMn(Map<String, String> param);

	int deleteMn(Map<String, String> paramMap);

}