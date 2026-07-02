package com.dksys.biz.admin.cm.cm30.service;

import java.util.Map;

public interface CM30Svc {

	String selectGridConfig(Map<String, String> param);

	int saveGridConfig(Map<String, String> param);

	int deleteGridConfig(Map<String, String> param);

}
