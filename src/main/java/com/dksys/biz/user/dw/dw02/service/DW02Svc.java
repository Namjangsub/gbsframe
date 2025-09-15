package com.dksys.biz.user.dw.dw02.service;

import java.util.List;
import java.util.Map;

public interface DW02Svc {

	int searchAuditsCount(Map<String, String> paramMap);

	List<Map<String, String>> searchAuditsList(Map<String, String> paramMap);

}
