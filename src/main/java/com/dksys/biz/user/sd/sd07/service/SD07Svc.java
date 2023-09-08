package com.dksys.biz.user.sd.sd07.service;

import java.util.List;
import java.util.Map;

public interface SD07Svc {

	List<Map<String, String>> selectCloseYmList(Map<String, String> paramMap);

	void saveClose(Map<String, String> paramMap);

	List<Map<String, String>> selectCloseLastYm(Map<String, String> paramMap);

}