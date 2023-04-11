package com.dksys.biz.user.cr.cr02.service;

import java.util.List;
import java.util.Map;

public interface CR02Svc {

	int selectOrdrsCount(Map<String, String> param);

	List<Map<String, Object>> selectOrdrsList(Map<String, String> param);

	Map<String, Object> selectOrdrsInfo(Map<String, String> paramMap);


}