package com.dksys.biz.user.cr.cr21.service;

import java.util.List;
import java.util.Map;

public interface CR21Svc {
	
	List<Map<String, String>> selectPrjctVSResultChart(Map<String, String> paramMap);
}